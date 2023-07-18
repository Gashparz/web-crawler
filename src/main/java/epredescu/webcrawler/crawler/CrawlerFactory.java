package epredescu.webcrawler.crawler;

import edu.uci.ics.crawler4j.crawler.CrawlController;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class CrawlerFactory implements CrawlController.WebCrawlerFactory<Crawler> {
    ConcurrentHashMap<String, CopyOnWriteArrayList<WebsiteData>>  websiteDataMap;

    public CrawlerFactory(ConcurrentHashMap<String, CopyOnWriteArrayList<WebsiteData>> websiteDataMap) {
        this.websiteDataMap = websiteDataMap;
    }

    @Override
    public Crawler newInstance() {
        return new Crawler(this.websiteDataMap);
    }
}
