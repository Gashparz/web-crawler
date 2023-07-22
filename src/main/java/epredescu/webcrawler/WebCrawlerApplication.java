package epredescu.webcrawler;

import epredescu.webcrawler.crawler.*;
import epredescu.webcrawler.elasticsearch.ElasticsearchService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;


@SpringBootApplication
@EnableElasticsearchRepositories(basePackages = "epredescu.webcrawler.elasticsearch")
public class WebCrawlerApplication {
    private static CrawlerUtils crawlerUtils = null;
    private static  CrawlerController crawlerController = null;
    private static ElasticsearchService elasticsearchService = null;
    private static final Logger logger =
            LoggerFactory.getLogger(WebCrawlerApplication.class);

    public WebCrawlerApplication(CrawlerUtils crawlerUtils, CrawlerController crawlerController, ElasticsearchService elasticsearchService) {
        this.crawlerUtils = crawlerUtils;
        this.crawlerController = crawlerController;
        this.elasticsearchService = elasticsearchService;
    }

    public static void main(String[] args) throws Exception {
        SpringApplication.run(WebCrawlerApplication.class, args);
        elasticsearchService.createIndex();
        crawlerController.run();
    }

}
