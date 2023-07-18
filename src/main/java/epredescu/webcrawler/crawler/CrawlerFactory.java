package epredescu.webcrawler.crawler;

import edu.uci.ics.crawler4j.crawler.CrawlController;

import java.util.List;
import java.util.Map;

public class CrawlerFactory implements CrawlController.WebCrawlerFactory<Crawler> {
    Map<String, List<WebsiteData>> websiteDataMap;

    public CrawlerFactory(Map<String, List<WebsiteData>> websiteDataMap) {
        this.websiteDataMap = websiteDataMap;
    }

    @Override
    public Crawler newInstance() {
        return new Crawler(this.websiteDataMap);
    }
}
