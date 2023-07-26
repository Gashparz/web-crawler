package epredescu.webcrawler.crawler;


import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class CrawlerUtils {
    public static final String PHONE_REGEX = "(\\+\\d{1,2}\\s?)?\\(?\\d{3}\\)?[\\s.-]?\\d{3}[\\s.-]?\\d{4}";
    public static final String INSTAGRAM_REGEX = "((?:https?:\\/\\/)?(?:www\\.)?(instagram|instagr))\\.(com|am)\\/(?:[\\w\\-\\.]*\\/)*([\\w\\-\\.]*)";
    public static final String FACEBOOK_REGEX = "(?:https?:\\/\\/)?(?:www\\.)?(mbasic.facebook|m\\.facebook|facebook|fb)\\.(com|me)\\/(?:(?:\\w\\.)*#!\\/)?(?:pages\\/)?(?:[\\w\\-\\.]*\\/)*([\\w\\-\\.]*)";
    public static final String TWITTER_REGEX = "((?:https?:\\/\\/)?(?:www\\.)?(twitter))\\.(com)\\/@?\\/*([\\w\\-\\.]*)";
    public static final String LINKEDIN_REGEX = "((?:https?:\\/\\/)?(?:www\\.)?(linkedin))\\.(com)\\/(?:company|in|school\\/)?\\/*([\\w\\-\\.]*)\\/*([\\w\\-\\.]*)";
    public static final String SOCIAL_REGEX = String.format("%s|%s|%s|%s", FACEBOOK_REGEX, INSTAGRAM_REGEX, TWITTER_REGEX, LINKEDIN_REGEX);
    public static final String COMBINED_REGEX = String.format("%s|%s", PHONE_REGEX, SOCIAL_REGEX);

    public List<String> readUrlCsv() throws IOException {
        Resource resource = new ClassPathResource("sample-websites.csv");
        InputStream inputStream = resource.getInputStream();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            reader.readLine();
            List<String> domainUrls = new ArrayList<>();
            String line;

            while ((line = reader.readLine()) != null) {
                String[] fields = line.split(",");
                domainUrls.add(fields[0].trim());
            }
            return domainUrls;
        } catch (IOException e) {
//            logger.error(e.getMessage());
        }
        throw new RuntimeException("Cannot read csv");
    }
}
