package epredescu.webcrawler;

import epredescu.webcrawler.crawler.*;
import epredescu.webcrawler.domain.elasticsearch.ElasticsearchService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;


@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class, HibernateJpaAutoConfiguration.class})
public class WebCrawlerApplication {
    private static  CrawlerController crawlerController = null;
    private static ElasticsearchService elasticsearchService = null;
    private static final Logger logger =
            LoggerFactory.getLogger(WebCrawlerApplication.class);

    public WebCrawlerApplication(CrawlerController crawlerController, ElasticsearchService elasticsearchService) {
        this.crawlerController = crawlerController;
        this.elasticsearchService = elasticsearchService;
    }

    public static void main(String[] args) throws Exception {
        SpringApplication.run(WebCrawlerApplication.class, args);
        elasticsearchService.createIndex();
        crawlerController.run();
    }

}
