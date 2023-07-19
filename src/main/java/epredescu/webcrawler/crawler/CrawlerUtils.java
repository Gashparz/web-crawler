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
