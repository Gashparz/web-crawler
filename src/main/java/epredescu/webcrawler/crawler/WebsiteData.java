package epredescu.webcrawler.crawler;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Getter
@Setter
public class WebsiteData {
    private String domain;
    private String subDomains;
    private Set<String> phoneNumbers = new HashSet<>();
    private Set<String> socialMediaLinks = new HashSet<>();
    private String location;

    public WebsiteData(String domain) {
        this.domain = domain;
    }
}
