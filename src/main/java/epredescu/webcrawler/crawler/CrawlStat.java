package epredescu.webcrawler.crawler;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CrawlStat {
    private int totalProcessedPages;
    private long totalLinks;
    private long totalTextSize;

    public void incTotalLinks(int count) {
        this.totalLinks += count;
    }

    public void incTotalTextSize(int count) {
        this.totalTextSize += count;
    }

    public void incProcessedPages() {
        this.totalProcessedPages++;
    }
}
