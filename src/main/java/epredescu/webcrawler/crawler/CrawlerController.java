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
import java.util.stream.Collectors;

@Service
public class CrawlerController {
    private final ElasticsearchOperations esOperations;
    private final DomainService domainService;

    private static final String QUEUE_NAME = "q.domains";
    private static final Logger logger = LoggerFactory.getLogger(CrawlerController.class);

    public CrawlerController(ElasticsearchOperations esOperations, DomainService domainService) {
        this.esOperations = esOperations;
        this.domainService = domainService;
    }

    public void run() throws Exception {
        String crawlStorageFolder = "/data/crawl/root";
        CrawlConfig config = new CrawlConfig();
        config.setCrawlStorageFolder(crawlStorageFolder);
        config.setMaxDepthOfCrawling(1);
        config.setMaxPagesToFetch(2000);
        config.setPolitenessDelay(1);
        config.setIncludeHttpsPages(true);

        PageFetcher pageFetcher = new PageFetcher(config);
        RobotstxtConfig robotstxtConfig = new RobotstxtConfig();
        robotstxtConfig.setEnabled(false);
        RobotstxtServer robotstxtServer = new RobotstxtServer(robotstxtConfig, pageFetcher);
        CrawlController controller = new CrawlController(config, pageFetcher, robotstxtServer);

        List<String> strings = consumeDomains();
        ConcurrentHashMap<String, CopyOnWriteArrayList<WebsiteData>> websiteDataMap = strings.stream()
                .collect(Collectors.toConcurrentMap(
                        domain -> domain,
                        domain -> {
                            try {
                                logger.info("seeding " + "http://" + domain);
                                controller.addSeed("http://" + domain);
                            } catch (Exception e) {
                                logger.info("seeding " + "https://" + domain);
                                controller.addSeed("https://" + domain);
                            }
                            CopyOnWriteArrayList<WebsiteData> websiteDataList = new CopyOnWriteArrayList<>();
                            websiteDataList.add(new WebsiteData(domain));

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
        factory.setHost("rabbit");
        try (Connection connection = factory.newConnection();
             Channel channel = connection.createChannel()) {
            String queueName = QUEUE_NAME + getReplicaNumber();
            channel.queueDeclare(queueName, false, false, false, null);

            List<String> domainUrls = new ArrayList<>();

            DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                String message = new String(delivery.getBody(), "UTF-8");
                System.out.println("Received URL: " + message);
                domainUrls.add(message);
            };

            channel.basicConsume(queueName, true, deliverCallback, consumerTag -> {
            });
            Thread.sleep(5000);
            return domainUrls;
        } catch (IOException | TimeoutException | InterruptedException e) {
            logger.error(e.getMessage());
        }
        throw new RuntimeException("Cannot consume the messages");
    }

    public int getReplicaNumber() {
        String hostname = System.getenv("HOSTNAME");
        if (hostname != null && hostname.matches(".*-([0-9]+)$")) {
            String replicaNumberStr = hostname.replaceFirst(".*-([0-9]+)$", "$1");
            return Integer.parseInt(replicaNumberStr);
        } else {
            return 0;
        }
    }
}
