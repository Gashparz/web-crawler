package epredescu.webcrawler.crawler;

import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.crawler.WebCrawler;
import edu.uci.ics.crawler4j.parser.HtmlParseData;
import edu.uci.ics.crawler4j.url.WebURL;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Crawler extends WebCrawler {
    private static final Logger logger = LoggerFactory.getLogger(Crawler.class);
    private final ConcurrentHashMap<String, CopyOnWriteArrayList<WebsiteData>> websiteData;
    private final ConcurrentHashMap<String, AtomicInteger> processedPagesCounter;
    private final int maxProcessedPagesCount;
    private boolean foundPhoneNumber = false;
    private boolean foundSocialMedia = false;
    private static final Pattern FILTERS = Pattern
            .compile(".*(\\.(css|js|bmp|gif|jpe?g|png|tiff?|mid|mp2|mp3|mp4|wav|avi|mov|mpeg|ram|m4v|pdf" +
                    "|rm|smil|wmv|swf|wma|zip|rar|gz))$");

    private static final Pattern PHONE_REGEX = Pattern
            .compile("(\\+\\d{1,2}\\s?)?\\(?\\d{3}\\)?[\\s.-]?\\d{3}[\\s.-]?\\d{4}");

    private static final Pattern FACEBOOK_REGEX = Pattern
            .compile("/(?:https?:\\/\\/)?(?:www\\.)?(mbasic.facebook|m\\.facebook|facebook|fb)\\.(com|me)\\/(?:(?:\\w\\.)*#!\\/)?(?:pages\\/)?(?:[\\w\\-\\.]*\\/)*([\\w\\-\\.]*)/ig");

//    private static final Pattern INSTAGRAM_REGEX = Pattern
//            .compile("(?:https?:)?\\/\\/(?:www\\.)?(?:instagram\\.com|instagr\\.am)\\/(?P<username>[A-Za-z0-9_](?:(?:[A-Za-z0-9_]|(?:\\.(?!\\.))){0,28}(?:[A-Za-z0-9_]))?)")

    private static final Pattern COMBINED_REGEX = Pattern
            .compile("(?:https?:\\/\\/)?(?:www\\.)?(mbasic.facebook|m\\.facebook|facebook|fb)\\.(com|me)\\/(?:(?:\\w\\.)*#!\\/)?(?:pages\\/)?(?:[\\w\\-\\.]*\\/)*([\\w\\-\\.]*)|((\\+\\d{1,2}\\s?)?\\(?\\d{3}\\)?[\\s.-]?\\d{3}[\\s.-]?\\d{4})");

    public Crawler(ConcurrentHashMap<String, CopyOnWriteArrayList<WebsiteData>> websiteData,
                   ConcurrentHashMap<String, AtomicInteger> processedPagesCounter,
                   int maxProcessedPagesCount) {
        this.websiteData = websiteData;
        this.processedPagesCounter = processedPagesCounter;
        this.maxProcessedPagesCount = maxProcessedPagesCount;
    }

    //TODO: Add check to minimize phone number duplicates
    @Override
    public void visit(Page page) {
        WebURL webURL = page.getWebURL();
        logger.info("Visited: {}", webURL.getURL());

        if (page.getParseData() instanceof HtmlParseData) {
            String domain = webURL.getDomain();
            processedPagesCounter.get(domain).incrementAndGet();
            CopyOnWriteArrayList<WebsiteData> websiteDataList = websiteData.get(domain);
            if (Objects.nonNull(websiteDataList)) {
                WebsiteData newWebsiteData = new WebsiteData(domain);
                newWebsiteData.setSubDomains(webURL.getURL());
                newWebsiteData.setDomain(domain);

                HtmlParseData parseData = (HtmlParseData) page.getParseData();
                String html = parseData.getHtml();
                Document doc = Jsoup.parseBodyFragment(html);
                Elements phoneNumberElements = doc.getElementsMatchingOwnText(COMBINED_REGEX);
                phoneNumberElements.forEach(element -> {
                    Matcher phoneMatcher = PHONE_REGEX.matcher(element.text());
                    if (phoneMatcher.find()) {
                        this.foundPhoneNumber = true;
                        newWebsiteData.getPhoneNumbers().add(phoneMatcher.group());
                    }

                    Matcher socialMediaMatcher = FACEBOOK_REGEX.matcher(element.text());
                    if (socialMediaMatcher.find()) {
                        this.foundSocialMedia = true;
                        newWebsiteData.getPhoneNumbers().add(socialMediaMatcher.group());
                    }
                });

                websiteDataList.add(newWebsiteData);
            }
        }
    }

    @Override
    public boolean shouldVisit(Page referringPage, WebURL url) {
        String href = url.getURL().toLowerCase();
        String domain = url.getDomain();

        if (FILTERS.matcher(href).matches()) {
            return false;
        }

        if (websiteData.get(domain) == null) {
            return false;
        }

        AtomicInteger counter = processedPagesCounter.get(domain);
//        if (!foundPhoneNumber && (counter.incrementAndGet() > maxProcessedPagesCount + 10)) {
//                return false;
//        }

        if (counter.get() > maxProcessedPagesCount) {
            return false;
        }

        return true;
    }

    @Override
    public Object getMyLocalData() {
        return websiteData;
    }

}
