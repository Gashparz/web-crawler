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
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class Crawler extends WebCrawler {
    private static final Logger logger = LoggerFactory.getLogger(Crawler.class);
    private final ConcurrentHashMap<String, CopyOnWriteArrayList<WebsiteData>> websiteData;

    private static final Pattern FILTERS = Pattern.compile(
            ".*(\\.(css|bmp|gif|jpe?g|png|tiff?|js|mid|mp2|mp3|mp4|wav|avi|mov|mpeg|ram|m4v|pdf" +
                    "|rm|smil|wmv|swf|wma|zip|rar|gz))$");

    private static final Pattern PHONE_REGEX = Pattern.compile(CrawlerUtils.PHONE_REGEX);

    private static final Pattern SOCIAL_MEDIA_REGEX = Pattern.compile(CrawlerUtils.SOCIAL_REGEX);

    public Crawler(ConcurrentHashMap<String, CopyOnWriteArrayList<WebsiteData>> websiteData) {
        this.websiteData = websiteData;
    }

    @Override
    public void visit(Page page) {
        WebURL webURL = page.getWebURL();
        if (page.getParseData() instanceof HtmlParseData) {
            String domain = webURL.getDomain();
            logger.info("Visited: {}, {}", webURL.getURL());
            CopyOnWriteArrayList<WebsiteData> websiteDataList = websiteData.get(domain);
            if (Objects.nonNull(websiteDataList)) {
                WebsiteData newWebsiteData = new WebsiteData(domain);
                newWebsiteData.setSubDomains(webURL.getURL());
                newWebsiteData.setDomain(domain);
                HtmlParseData parseData = (HtmlParseData) page.getParseData();
                String html = parseData.getHtml();
                Document doc = Jsoup.parseBodyFragment(html);
                Elements links = doc.select("a[href]");
                links.forEach(link -> {
                    String linkHref = link.attr("href");
                    Matcher socialMediaMatcher = SOCIAL_MEDIA_REGEX.matcher(linkHref);
                    if (linkHref.contains("www.facebook.com") || linkHref.contains("instagram.com") ||
                            linkHref.contains("twitter.com") || linkHref.contains("linkedin.com")) {
                        if (!linkHref.contains("facebook.com/share") || !linkHref.contains("twitter.com/intent/") ||
                                !linkHref.contains("linkedin.com/share")) {
                            newWebsiteData.getSocialMediaLinks().add(linkHref);
                        }
                    } else if (socialMediaMatcher.find()) {
                        String socialMediaLink = socialMediaMatcher.group();
                        if (!socialMediaLink.contains("www.instagram.com/p/") ||
                                socialMediaLink.contains("www.instagram.com/stories/")) {
                            newWebsiteData.getSocialMediaLinks().add(socialMediaLink);
                        }
                    }
                });
                Elements phoneNumberElements = doc.getElementsMatchingOwnText(PHONE_REGEX);
                phoneNumberElements.forEach(element -> {
                    Matcher phoneMatcher = PHONE_REGEX.matcher(element.text());
                    if (phoneMatcher.find()) {
                        newWebsiteData.getPhoneNumbers().add(phoneMatcher.group());
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

        return true;
    }

    @Override
    public Object getMyLocalData() {
        return websiteData;
    }

}
