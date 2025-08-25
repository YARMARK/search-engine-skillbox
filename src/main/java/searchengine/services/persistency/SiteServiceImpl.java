package searchengine.services.persistency;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import searchengine.model.Site;
import searchengine.model.SiteStatus;
import searchengine.repository.SiteRepository;
import searchengine.services.SiteService;

import java.util.List;

/**
 * {@inheritDoc}
 * <p>
 * Обеспечивает операции сохранения, обновления, поиска и удаления сайтов,
 * а также взаимодействие с базой данных через {@link SiteRepository}.
 * </p>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SiteServiceImpl implements SiteService {

    private final SiteRepository siteRepository;

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public Site findSiteByUrl(String url) {
        return siteRepository.findByUrl(url);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public List<Site> findSiteByStatus(SiteStatus status) {
        return siteRepository.findByStatus(status);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public Site saveSite(Site site) {
        log.debug("Saving site: {}", site.getUrl());
        return siteRepository.save(site);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public List<Site> findAllSites() {
        return siteRepository.findAll();
    }
}
