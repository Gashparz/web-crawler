package epredescu.webcrawler.elasticsearch;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface ESDomainRepository extends ElasticsearchRepository<DomainDocument, Long> {

}
