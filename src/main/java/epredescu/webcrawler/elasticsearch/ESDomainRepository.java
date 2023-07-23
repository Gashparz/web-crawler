package epredescu.webcrawler.elasticsearch;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.List;

public interface ESDomainRepository extends ElasticsearchRepository<DomainDocument, Long> {
    List<DomainDocument> findAll();
}
