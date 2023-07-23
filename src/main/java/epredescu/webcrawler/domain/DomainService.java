package epredescu.webcrawler.domain;

import epredescu.webcrawler.domain.elasticsearch.DomainDocument;
import epredescu.webcrawler.domain.elasticsearch.ESDomainRepository;
import epredescu.webcrawler.domain.elasticsearch.ElasticsearchService;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


@Service
public class DomainService {
    private final ElasticsearchService esService;
    private final ESDomainRepository esDomainRepository;
    public DomainService(ElasticsearchService esService,
                         ESDomainRepository esDomainRepository) {
        this.esService = esService;
        this.esDomainRepository = esDomainRepository;
    }

    public void mergeDataFromCSV() {
        try (BufferedReader reader = new BufferedReader(new FileReader("C:\\Users\\Predescu Eduard\\IdeaProjects\\web-crawler\\src\\main\\resources\\sample-websites-company-names.csv"))) {
            reader.readLine();

            Map<String,DomainDocument> existingDomains = esDomainRepository.findAll()
                    .stream()
                    .collect(Collectors.toMap(document -> document.id, domainDocument -> domainDocument));
            List<DomainDocument> updatedDomains = new ArrayList<>();
            String line;
            while ((line = reader.readLine()) != null) {

                String[] fields = line.split(",");
                String domain = fields[0].trim();
                String companyCommercialName = fields[1].trim();
                String companyLegalName = fields[2].trim();
                String companyAllAvailableNames = fields[3].trim();

                DomainDocument domainDocument = existingDomains.get(domain);

                if (domainDocument != null) {
                    domainDocument.companyCommercialName = companyCommercialName;
                    domainDocument.companyLegalName = companyLegalName;
                    domainDocument.companyAllAvailableNames = companyAllAvailableNames;
                    updatedDomains.add(domainDocument);
                }

                if (domainDocument == null) {
//                    throw new Exception("There was a problem regarding the merge of data!");
                    esDomainRepository.saveAll(updatedDomains);
                    return;
                }
            }

            esDomainRepository.saveAll(updatedDomains);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public List<DomainDocument> findAll() {
        return esDomainRepository.findAll();
    }

    public Optional<DomainDocument> get(String domain, String phoneNumber) {
        DomainSearchParams domainSearchParams = new DomainSearchParams(domain, phoneNumber);

        BoolQueryBuilder query = doQuery(domainSearchParams);

        return esService.searchSingle(query, DomainDocument.class);
    }

    private BoolQueryBuilder doQuery(DomainSearchParams searchParams) {
        BoolQueryBuilder query = QueryBuilders.boolQuery();

        final String domain = searchParams.getDomain();
        if (StringUtils.isNotEmpty(domain)) {
            query.must(QueryBuilders.matchQuery("id", trimDomain(domain)));
        }

        final String phoneNumber = searchParams.getPhoneNumber();
        if (StringUtils.isNotEmpty(phoneNumber)) {
            query.must(QueryBuilders.matchQuery("phoneNumbers", phoneNumber));
        }

        return query;
    }

    private String trimDomain(String domain) {
        domain = domain.trim();
        domain = domain.toLowerCase();

        Pattern pattern = Pattern.compile("^(?:.*://)?(?:www\\.)?(.*?)(?:/.*)?$");
        Matcher matcher = pattern.matcher(domain);
        if (matcher.find()) {
            domain = matcher.group(1);
        }

        if (domain.startsWith("http://")) {
            domain = domain.substring(7);
        } else if (domain.startsWith("https://")) {
            domain = domain.substring(8);
        }

        if (domain.endsWith("/")) {
            domain = domain.substring(0, domain.length() - 1);
        }

        return domain;
    }
}
