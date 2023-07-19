package epredescu.webcrawler.crawler;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class WebsiteData {
    private String domain;
    private String subDomains;
    private List<String> phoneNumbers = new ArrayList<>();
    private List<String> socialMediaLinks = new ArrayList<>();
    private String location;

    public WebsiteData(String domain) {
        this.domain = domain;
    }

    //TODO: EQUALS
}
