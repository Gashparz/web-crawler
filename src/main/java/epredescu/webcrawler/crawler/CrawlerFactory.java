package epredescu.webcrawler.crawler;

import edu.uci.ics.crawler4j.crawler.CrawlController;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

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
