package epredescu.webcrawler.config;

import epredescu.webcrawler.crawler.Crawler;
import org.elasticsearch.client.RestHighLevelClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.client.ClientConfiguration;
import org.springframework.data.elasticsearch.client.RestClients;
import org.springframework.data.elasticsearch.config.AbstractElasticsearchConfiguration;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

@Configuration
@EnableElasticsearchRepositories(basePackages = "epredescu.webcrawler.domain.elasticsearch")
public class ElasticsearchClientConfig extends AbstractElasticsearchConfiguration {
    private static final Logger logger = LoggerFactory.getLogger(ElasticsearchClientConfig.class);

    @Override
    @Bean
    public RestHighLevelClient elasticsearchClient() {
        final ClientConfiguration clientConfiguration =
//                ClientConfiguration
//                        .builder()
//                        .connectedTo("localhost:9200")
//                        .build();
        ClientConfiguration
                        .builder()
                        .connectedTo("elasticsearch:9200")
                        .build();
        try {
            return RestClients.create(clientConfiguration).rest();

        }catch (Exception e) {
            logger.error(e.getMessage());
        }
        return null;
    }
}
