package epredescu.webcrawler.elasticsearch;

import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.Setting;

import javax.persistence.Id;
import java.util.List;

@Document(indexName = "domains", createIndex = false)
@Setting(settingPath = "/elasticsearch/settings.json")
public class DomainDocument {
    @Id
    @Field(type = FieldType.Long)
    public Long id;

    @Field(type = FieldType.Text, analyzer = "ascii_folding")
    public String domain;

    @Field(type = FieldType.Text, analyzer = "ascii_folding")
    public String companyCommercialName;

    @Field(type = FieldType.Text, analyzer = "ascii_folding")
    public String companyLegalName;

    @Field(type = FieldType.Text, analyzer = "ascii_folding")
    public String companyAllAvailableNames;

    @Field(type = FieldType.Keyword)
    public List<String> phoneNumbers;

    @Field(type = FieldType.Text)
    public List<String> socialMedia;

    @Field(type = FieldType.Text)
    public List<String> locations;
}
