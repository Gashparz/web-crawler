package epredescu.webcrawler.crawler;

import edu.uci.ics.crawler4j.crawler.CrawlController;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

public class CrawlerFactory implements CrawlController.WebCrawlerFactory<Crawler> {
    ConcurrentHashMap<String, CopyOnWriteArrayList<WebsiteData>>  websiteDataMap;
    ConcurrentHashMap<String, AtomicInteger> processedPagesCounter; // Counter for processed subdomains
    int maxProcessedPagesCount;

    public CrawlerFactory(ConcurrentHashMap<String, CopyOnWriteArrayList<WebsiteData>> websiteDataMap,
                          ConcurrentHashMap<String, AtomicInteger> processedPagesCounter,
                          int maxProcessedPagesCount) {
        this.websiteDataMap = websiteDataMap;
        this.processedPagesCounter = processedPagesCounter;
        this.maxProcessedPagesCount = maxProcessedPagesCount;
    }

    @Override
    public Crawler newInstance() {
        return new Crawler(this.websiteDataMap, this.processedPagesCounter, this.maxProcessedPagesCount);
    }
}
