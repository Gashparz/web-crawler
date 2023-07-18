package epredescu.webcrawler.crawler;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;

@Getter
@Setter
@Table(name = "Domain")
@Entity
public class Domain implements Serializable {
    @Id
    @Column(name = "Id")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "domainSeqGen")
    @SequenceGenerator(name = "domainSeqGen", sequenceName = "SEQ_DOMAIN_ID", allocationSize = 1)
    private Long id;

    @Column(name = "Url")
    private String url;

    @Column(name = "Visited", columnDefinition = "BIT default 0")
    private Boolean visited = Boolean.FALSE;
}
