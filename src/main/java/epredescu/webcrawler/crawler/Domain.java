package epredescu.webcrawler.crawler;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;


@Getter
@Setter
public class Domain implements Serializable {
    private String url;
}
