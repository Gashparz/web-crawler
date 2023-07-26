package epredescu.webcrawler.crawler;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;
import edu.uci.ics.crawler4j.crawler.CrawlConfig;
import edu.uci.ics.crawler4j.crawler.CrawlController;
import edu.uci.ics.crawler4j.fetcher.PageFetcher;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtConfig;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtServer;
import epredescu.webcrawler.domain.DomainService;
import epredescu.webcrawler.domain.elasticsearch.DomainDocument;
import epredescu.webcrawler.domain.elasticsearch.ESDomainRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
public class CrawlerController {
    private final ElasticsearchOperations esOperations;
    private final ESDomainRepository es;
    private final DomainService domainService;

    private final static String QUEUE_NAME = "q.domains";
    private static final Logger logger = LoggerFactory.getLogger(CrawlerController.class);

    public CrawlerController(ElasticsearchOperations esOperations, ESDomainRepository es, DomainService domainService) {
        this.esOperations = esOperations;
        this.es = es;
        this.domainService = domainService;
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
        ConcurrentHashMap<String, CopyOnWriteArrayList<WebsiteData>> websiteDataMap = consumeDomains().stream()
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

        logger.info("Finished the crawling process");

        List<DomainDocument> domainDocuments = saveDocumentsInEs(websiteDataMap);
        domainService.mergeDataFromCSV(domainDocuments);
    }

    private List<DomainDocument> saveDocumentsInEs(ConcurrentHashMap<String, CopyOnWriteArrayList<WebsiteData>> websiteDataMap) {
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
        return domainDocuments;
    }

    public List<String> consumeDomains() {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("rabbitmq");
        try (Connection connection = factory.newConnection();
             Channel channel = connection.createChannel()) {

            channel.queueDeclare(QUEUE_NAME, false, false, false, null);

            List<String> domainUrls = new ArrayList<>();

            DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                String message = new String(delivery.getBody(), "UTF-8");
                System.out.println("Received URL: " + message);
                domainUrls.add(message);
            };

            channel.basicConsume(QUEUE_NAME, true, deliverCallback, consumerTag -> {});
            return domainUrls;
        } catch (IOException | TimeoutException e) {
            logger.error(e.getMessage());
        }
        throw new RuntimeException("Cannot consume the messages");
    }

}
