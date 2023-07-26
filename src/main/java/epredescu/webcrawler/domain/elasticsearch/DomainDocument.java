package epredescu.webcrawler.domain.elasticsearch;

import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.Setting;

import javax.persistence.Id;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Document(indexName = "domains", createIndex = false)
@Setting(settingPath = "/elasticsearch/settings.json")
public class DomainDocument {
    @Id
    @Field(type = FieldType.Text, analyzer = "ascii_folding")
    public String id;

    @Field(type = FieldType.Text, analyzer = "ascii_folding")
    public String companyCommercialName;

    @Field(type = FieldType.Text, analyzer = "ascii_folding")
    public String companyLegalName;

    @Field(type = FieldType.Text, analyzer = "ascii_folding")
    public String companyAllAvailableNames;

    @Field(type = FieldType.Keyword, name = "phoneNumbers", analyzer = "phone_tokenizer")
    public Set<String> phoneNumbers = new HashSet<>();

    @Field(type = FieldType.Keyword, name = "socialMedia")
    public Set<String> socialMedia = new HashSet<>();

    @Field(type = FieldType.Text)
    public List<String> locations;
}
