package epredescu.webcrawler.crawler;

import edu.uci.ics.crawler4j.crawler.CrawlConfig;
import edu.uci.ics.crawler4j.crawler.CrawlController;
import edu.uci.ics.crawler4j.fetcher.PageFetcher;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtConfig;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtServer;
import epredescu.webcrawler.domain.elasticsearch.DomainDocument;
import epredescu.webcrawler.domain.elasticsearch.ESDomainRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
public class CrawlerController {
    private final ElasticsearchOperations esOperations;
    private final ESDomainRepository es;

    private static final Logger logger = LoggerFactory.getLogger(CrawlerController.class);

    public CrawlerController(ElasticsearchOperations esOperations, ESDomainRepository es) {
        this.esOperations = esOperations;
        this.es = es;
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

        ConcurrentHashMap<String, AtomicInteger> processedPagesCounter = new ConcurrentHashMap<>();
        ConcurrentHashMap<String, CopyOnWriteArrayList<WebsiteData>> websiteDataMap = readUrlCsv().stream()
                .collect(Collectors.toConcurrentMap(
                        domain -> domain,
                        domain -> {
                            try {
                                controller.addSeed("http://" + domain);
                            } catch (Exception e) {
                                controller.addSeed("https://" + domain);
                            }
                            CopyOnWriteArrayList<WebsiteData> websiteDataList = new CopyOnWriteArrayList<>();
                            websiteDataList.add(new WebsiteData(domain));

                            processedPagesCounter.put(domain, new AtomicInteger(1));
                            return websiteDataList;
                        },
                        (list1, list2) -> {
                            list1.addAll(list2);
                            return list1;
                        },
                        ConcurrentHashMap::new
                ));

        CrawlerFactory factory = new CrawlerFactory(websiteDataMap, processedPagesCounter, 10);
        controller.startNonBlocking(factory, 7);
        controller.waitUntilFinish();

        logger.info("Done");

        List<DomainDocument> domainDocuments = websiteDataMap.entrySet().stream()
                .map(entrySet -> {
                    DomainDocument newDoc = new DomainDocument();
                    newDoc.id = entrySet.getKey();
                    entrySet.getValue().forEach(value -> {
                        newDoc.phoneNumbers.addAll(value.getPhoneNumbers());
                        newDoc.socialMedia.addAll(value.getSocialMediaLinks());
                    });
                    return newDoc;
                }).collect(Collectors.toList());

        esOperations.save(domainDocuments);
    }

    public List<String> readUrlCsv() {
        Path csvFilePath = Paths.get("C:\\Users\\Predescu Eduard\\IdeaProjects\\web-crawler\\src\\main\\resources\\sample-websites.csv");

        try {
            return Files.lines(csvFilePath)
                    .skip(1)
                    .limit(5)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new RuntimeException("Failed to read the CSV file.", e);
        }
    }
}
