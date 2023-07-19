package epredescu.webcrawler.crawler;

import org.springframework.stereotype.Service;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

@Service
public class CrawlerUtils {
    private final DomainRepository domainRepository;

    public CrawlerUtils(DomainRepository domainRepository) {
        this.domainRepository = domainRepository;
    }
    public static final String PHONE_REGEX = "(\\+\\d{1,2}\\s?)?\\(?\\d{3}\\)?[\\s.-]?\\d{3}[\\s.-]?\\d{4}";
    public static final String INSTAGRAM_REGEX = "((?:https?:\\/\\/)?(?:www\\.)?(instagram|instagr))\\.(com|am)\\/(?:[\\w\\-\\.]*\\/)*([\\w\\-\\.]*)";
    public static final String FACEBOOK_REGEX = "(?:https?:\\/\\/)?(?:www\\.)?(mbasic.facebook|m\\.facebook|facebook|fb)\\.(com|me)\\/(?:(?:\\w\\.)*#!\\/)?(?:pages\\/)?(?:[\\w\\-\\.]*\\/)*([\\w\\-\\.]*)";
    public static final String TWITTER_REGEX = "((?:https?:\\/\\/)?(?:www\\.)?(twitter))\\.(com)\\/@?\\/*([\\w\\-\\.]*)";
    public static final String LINKEDIN_REGEX = "((?:https?:\\/\\/)?(?:www\\.)?(linkedin))\\.(com)\\/(?:company|in|school\\/)?\\/*([\\w\\-\\.]*)\\/*([\\w\\-\\.]*)";
    public static final String SOCIAL_REGEX = String.format("%s|%s|%s|%s", FACEBOOK_REGEX, INSTAGRAM_REGEX, TWITTER_REGEX, LINKEDIN_REGEX);
    public static final String COMBINED_REGEX = String.format("%s|%s", PHONE_REGEX, SOCIAL_REGEX);


    public List<Domain> readUrlCsv() {
        List<Domain> urls = new ArrayList<>();
        try {
            Scanner sc = new Scanner(new File("C:\\Users\\Predescu Eduard\\IdeaProjects\\web-crawler\\src\\main\\resources\\sample-websites.csv"));
            sc.useDelimiter("\r\n");
            sc.next();
            while (sc.hasNext()) {
                Domain domain = new Domain();
                domain.setUrl(sc.next());
                urls.add(domain);
            }
            sc.close();
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }

        domainRepository.saveAll(urls);

        return urls;
    }
}
