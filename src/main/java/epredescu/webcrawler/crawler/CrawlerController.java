package epredescu.webcrawler.crawler;

import edu.uci.ics.crawler4j.crawler.CrawlConfig;
import edu.uci.ics.crawler4j.crawler.CrawlController;
import edu.uci.ics.crawler4j.fetcher.PageFetcher;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtConfig;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

@Service
public class CrawlerController {
    private final DomainRepository domainRepository;

    private static final Logger logger = LoggerFactory.getLogger(CrawlerController.class);

    public CrawlerController(DomainRepository domainRepository) {
        this.domainRepository = domainRepository;
    }

    public void run() throws Exception {
        String crawlStorageFolder = "/data/crawl/root";
        CrawlConfig config = new CrawlConfig();
        config.setCrawlStorageFolder(crawlStorageFolder);
        config.setMaxDepthOfCrawling(10);
        config.setMaxPagesToFetch(100);
        config.setPolitenessDelay(1);
        config.setIncludeHttpsPages(true);

        PageFetcher pageFetcher = new PageFetcher(config);
        RobotstxtConfig robotstxtConfig = new RobotstxtConfig();
        RobotstxtServer robotstxtServer = new RobotstxtServer(robotstxtConfig, pageFetcher);
        CrawlController controller = new CrawlController(config, pageFetcher, robotstxtServer);

        List<Domain> domainList = domainRepository.findAllByVisitedFalse().stream().limit(3L)
                .collect(Collectors.toList());

        ConcurrentHashMap<String, CopyOnWriteArrayList<WebsiteData>> websiteDataMap = domainList.stream()
                .collect(Collectors.toConcurrentMap(
                        Domain::getUrl,
                        domain -> {
                            try {
                                controller.addSeed("http://" + domain.getUrl());
                            } catch (Exception e) {
                                controller.addSeed("https://" + domain.getUrl());
                            }
                            CopyOnWriteArrayList<WebsiteData> websiteDataList = new CopyOnWriteArrayList<>();
                            websiteDataList.add(new WebsiteData(domain.getUrl()));
                            return websiteDataList;
                        },
                        (list1, list2) -> {
                            list1.addAll(list2);
                            return list1;
                        },
                        ConcurrentHashMap::new
                ));

        CrawlerFactory factory = new CrawlerFactory(websiteDataMap);
        controller.startNonBlocking(factory, 7);
        controller.waitUntilFinish();

        logger.info("Done");
    }
}
