package epredescu.webcrawler.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class DomainSearchParams {
    private String domain;
    private String phoneNumber;
}
