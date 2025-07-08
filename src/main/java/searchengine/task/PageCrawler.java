package searchengine.task;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import searchengine.model.Page;
import searchengine.model.Site;
import searchengine.repository.PageRepository;
import searchengine.repository.SiteRepository;
import searchengine.services.LemmaService;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.RecursiveAction;
import java.util.regex.Pattern;


@Slf4j
@AllArgsConstructor
public class PageCrawler extends RecursiveAction {

    private static final int MIN_DELAY_MS = 100;
    private static final int MAX_DELAY_MS = 150;

    private static final Set<String> SKIPPED_FILE_EXTENSIONS = Set.of(
            ".jpg", ".jpeg", ".png", ".gif", ".webp", ".pdf",
            ".eps", ".xlsx", ".doc", ".pptx", ".docx"
    );

    private static final Pattern SKIP_PATTERNS = Pattern.compile("\\?_ga|#");

    private static final String[] URL_PREFIXES = {
            "http://www.", "https://www.", "http://", "https://", "www."
    };

    private final String url;
    private final String userAgent;
    private final String referrer;
    private final Site site;
    private final PageRepository pageRepository;
    private final SiteRepository siteRepository;
    private final LemmaService lemmaService;
    private final CopyOnWriteArraySet<String> visitedLinks;

    public PageCrawler(String url, String userAgent, String referrer, Site site,
                       PageRepository pageRepository, SiteRepository siteRepository, LemmaService lemmaService) {
        this(url, userAgent, referrer, site, pageRepository, siteRepository, lemmaService, new CopyOnWriteArraySet<>());
    }

    @Override
    protected void compute() {
        if (isInterrupted()) {
            handleInterruption("before execution");
            return;
        }

        if (!visitedLinks.add(url)) {
            log.info("Link is already visited: {}", url);
            return;
        }

        try {
            processPage();
        } catch (InterruptedException e) {
            handleInterruption("during processing");
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            log.error("Error processing URL: {}", url, e);
        }
    }

    private void processPage() throws InterruptedException, IOException {
        Thread.sleep(generateRandomDelay());

        Connection.Response response = fetchPage();

        if (isInterrupted()) {
            handleInterruption("after getting connection response");
            return;
        }

        Document document = response.parse();
        if (isValidLink() && isValidStatusCode(response)) {
            savePage(response, document);
        }

        if (response.statusCode() >= 400 && response.statusCode() < 600) {
            return;
        }

        processChildLinks(document);
    }

    private Connection.Response fetchPage() throws IOException {
        return Jsoup.connect(url)
                .userAgent(userAgent)
                .referrer(referrer)
                .execute();
    }

    private void savePage(Connection.Response response, Document document) {
        log.info("Page parsing process is running: {}", url);

        Page page = createPage(response, document);
        updateSiteTimestamp();

        pageRepository.save(page);
        siteRepository.save(site);
        lemmaService.saveAllLemmas(page);
    }

    private Page createPage(Connection.Response response, Document document) {
        Page page = new Page();
        page.setCode(response.statusCode());
        page.setPath(url);
        page.setContent(document.html());
        page.setSite(site);
        return page;
    }

    private void updateSiteTimestamp() {
        site.setStatusTime(LocalDateTime.now());
    }

    private void processChildLinks(Document document) throws InterruptedException {
        List<PageCrawler> childTasks = new ArrayList<>();

        for (Element element : document.select("a[href]")) {
            String nextUrl = element.absUrl("href");

            if (isValidChildUrl(nextUrl)) {
                if (isInterrupted()) {
                    handleInterruption("before creating sub-task");
                    return;
                }

                PageCrawler childTask = createChildTask(nextUrl);
                childTask.fork();
                childTasks.add(childTask);
            }
        }

        waitForChildTasks(childTasks);
    }

    private boolean isValidChildUrl(String nextUrl) {
        return !nextUrl.isEmpty() && isLink(nextUrl);
    }

    private PageCrawler createChildTask(String nextUrl) {
        return new PageCrawler(nextUrl, userAgent, referrer, site, pageRepository, siteRepository, lemmaService, visitedLinks);
    }

    private void waitForChildTasks(List<PageCrawler> childTasks) {
        for (PageCrawler task : childTasks) {
            task.join();
        }
    }


    private boolean isValidLink() {
        return isLink(url) && !isFile(url);
    }

    private boolean isLink(String link) {
        String cleanedBaseUrl = getCleanedBaseUrl();
        return link.contains(cleanedBaseUrl) && !containsSkipPatterns(link);
    }

    private boolean containsSkipPatterns(String link) {
        return SKIP_PATTERNS.matcher(link).find();
    }

    private String getCleanedBaseUrl() {
        for (String prefix : URL_PREFIXES) {
            if (url.startsWith(prefix)) {
                return url.substring(prefix.length());
            }
        }
        return url;
    }

    private boolean isFile(String link) {
        String lowerCaseLink = link.toLowerCase();
        return SKIPPED_FILE_EXTENSIONS.stream()
                .anyMatch(lowerCaseLink::endsWith);
    }

    private boolean isValidStatusCode(Connection.Response response) {
        return response.statusCode() < 400;
    }


    private int generateRandomDelay() {
        return MIN_DELAY_MS + (int) (Math.random() * (MAX_DELAY_MS - MIN_DELAY_MS));
    }

    private boolean isInterrupted() {
        return Thread.currentThread().isInterrupted();
    }

    private void handleInterruption(String context) {
        log.info("Task was interrupted {}: {}", context, url);
        visitedLinks.clear();
    }
}

