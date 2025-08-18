package searchengine.services;

import searchengine.model.Site;
import searchengine.model.SiteStatus;

import java.util.List;

public interface SiteService {

    Site findSiteByUrl(String url);

    List<Site> findSiteByStatus(SiteStatus status);

    void deleteSiteByUrl(String url);

    Site saveSite(Site site);

    void deleteSite(Site site);

    List<Site> findAllSites();
}
