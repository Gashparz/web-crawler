package epredescu.webcrawler.domain.elasticsearch;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.elasticsearch.client.IndicesClient;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;


@Service
public class ElasticsearchService {
    private static final Logger logger = LoggerFactory.getLogger(ElasticsearchService.class);

    private final RestHighLevelClient restHighLevelClient;
    private final ElasticsearchRestTemplate esRestTemplate;
    private final ElasticsearchOperations esOperations;
    private final ObjectMapper objectMapper;

    public ElasticsearchService(RestHighLevelClient restHighLevelClient,
                                ElasticsearchRestTemplate esRestTemplate,
                                ElasticsearchOperations esOperations,
                                ObjectMapper objectMapper) {
        this.restHighLevelClient = restHighLevelClient;
        this.esRestTemplate = esRestTemplate;
        this.esOperations = esOperations;
        this.objectMapper = objectMapper;
    }

    public <T> Optional<T> searchSingle(BoolQueryBuilder queryBuilder, Class<T> className) {
        final NativeSearchQueryBuilder searchQueryBuilder = new NativeSearchQueryBuilder().withQuery(queryBuilder);

        final NativeSearchQuery searchQuery = searchQueryBuilder.build();

        SearchHits<T> result = esOperations.search(searchQuery, className);

        SearchHit<T> firstHit = result.stream().findFirst().orElse(null);

        return Optional.ofNullable(firstHit != null ? firstHit.getContent() : null);
    }

    public void createIndex() throws Exception {
        IndicesClient indicesClient = restHighLevelClient.indices();
        GetIndexRequest indexRequest = new GetIndexRequest("domains");
        try {
            boolean indexExists = indicesClient.exists(indexRequest, RequestOptions.DEFAULT);
            if (!indexExists) {
                createIndex("domains", indicesClient);
            }
        }
        catch (IOException ex) {
            throw new Exception(String.format("Index creation failed with error %s", ex.getMessage()));
        }
    }

    private Settings.Builder buildAnalyzerSettings(String indexName) throws Exception {
        try {
            Settings.Builder builder = Settings.builder();

            final String resourceName = "/elasticsearch/settings.json";
            final ClassPathResource classPathResource = new ClassPathResource(resourceName);

            return builder.loadFromStream(resourceName, classPathResource.getInputStream(), false);
        } catch (IOException e) {
            final String logMessage = String.format("Analyzer settings for %s index could not be created " +
                    "due to error: %s", indexName, e.getMessage());
            throw new Exception(logMessage);
        }
    }

    private void createIndex(String indexName, IndicesClient indicesClient) throws Exception {
        Map<String, Object> properties = getIndexPropertiesFile(indexName);
        CreateIndexRequest createIndexRequest = new CreateIndexRequest(indexName)
                .mapping(Collections.singletonMap("properties", properties))
                .settings(buildAnalyzerSettings(indexName).build());

        indicesClient.create(createIndexRequest, RequestOptions.DEFAULT);
    }

    private Map<String, Object> getIndexPropertiesFile(String indexName) throws IOException {
        String path = String.format("elasticsearch/%s.json", indexName);
        ClassPathResource classPathResource = new ClassPathResource(path);

        return objectMapper.readValue(classPathResource.getInputStream(), new TypeReference<>() {});
    }
}
