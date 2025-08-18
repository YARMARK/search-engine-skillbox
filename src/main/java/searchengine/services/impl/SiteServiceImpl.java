package searchengine.services.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import searchengine.model.Site;
import searchengine.model.SiteStatus;
import searchengine.repository.SiteRepository;
import searchengine.services.SiteService;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SiteServiceImpl implements SiteService {

    private final SiteRepository siteRepository;

    @Override
    @Transactional(readOnly = true)
    public Site findSiteByUrl(String url) {
        return siteRepository.findByUrl(url);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Site> findSiteByStatus(SiteStatus status) {
        return siteRepository.findByStatus(status);
    }

    @Override
    @Transactional
    public void deleteSiteByUrl(String url) {
        siteRepository.deleteByUrl(url);
    }

    @Override
    @Transactional
    public Site saveSite(Site site) {
        return siteRepository.save(site);
    }

    @Override
    @Transactional
    public void deleteSite(Site site) {
        siteRepository.delete(site);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Site> findAllSites() {
        return siteRepository.findAll();
    }


}
