package epredescu.webcrawler.domain.elasticsearch;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.List;

public interface ESDomainRepository extends ElasticsearchRepository<DomainDocument, String> {
    List<DomainDocument> findAll();
}
