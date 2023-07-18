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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Crawler extends WebCrawler {
    private static final Logger logger = LoggerFactory.getLogger(Crawler.class);
    private final ConcurrentHashMap<String, CopyOnWriteArrayList<WebsiteData>> websiteData;

    private int counter = 9;
    private static final Pattern FILTERS = Pattern
            .compile(".*(\\.(css|js|bmp|gif|jpe?g|png|tiff?|mid|mp2|mp3|mp4|wav|avi|mov|mpeg|ram|m4v|pdf" +
                    "|rm|smil|wmv|swf|wma|zip|rar|gz))$");

    private static final Pattern phoneRegex = Pattern
            .compile("(\\+\\d{1,2}\\s?)?\\(?\\d{3}\\)?[\\s.-]?\\d{3}[\\s.-]?\\d{4}");

    public Crawler(ConcurrentHashMap<String, CopyOnWriteArrayList<WebsiteData>> websiteData) {
        this.websiteData = websiteData;
    }

    @Override
    public void visit(Page page) {
        WebURL webURL = page.getWebURL();
        logger.info("Visited: {}", webURL.getURL());

        if (page.getParseData() instanceof HtmlParseData) {
            String domain = webURL.getDomain();
            CopyOnWriteArrayList<WebsiteData> websiteDataList = websiteData.get(domain);
            if (Objects.nonNull(websiteDataList)) {
                WebsiteData newWebsiteData = new WebsiteData(domain);
                newWebsiteData.setSubDomaina(webURL.getURL());
                newWebsiteData.setDomain(domain);

                HtmlParseData parseData = (HtmlParseData) page.getParseData();
                String html = parseData.getHtml();
                Document doc = Jsoup.parseBodyFragment(html);
                Elements phoneNumberElements = doc.getElementsMatchingOwnText(phoneRegex);
                phoneNumberElements.forEach(element -> {
                    Matcher matcher = phoneRegex.matcher(element.text());
                    if (matcher.find()) {
                        newWebsiteData.getPhoneNumbers().add(matcher.group());
                    }
                });
                websiteDataList.add(newWebsiteData);
            }

        }
    }

    @Override
    public boolean shouldVisit(Page referringPage, WebURL url) {
        String href = url.getURL().toLowerCase();
        if (FILTERS.matcher(href).matches()) {
            return false;
        }

        if (websiteData.get(url.getDomain()) == null) {
            return false;
        }

        return true;
    }

    @Override
    public Object getMyLocalData() {
        return websiteData;
    }


}
