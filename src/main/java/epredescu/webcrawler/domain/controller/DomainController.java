package epredescu.webcrawler.domain.controller;

import epredescu.webcrawler.domain.DomainService;
import epredescu.webcrawler.domain.elasticsearch.DomainDocument;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/domains")
public class DomainController {
    private final DomainService domainService;

    public DomainController(DomainService domainService) {
        this.domainService = domainService;
    }

    @GetMapping("/search")
    public ResponseEntity<DomainDocument> search(@RequestParam(required = false) String phoneNumber,
                                                 @RequestParam(required = false) String domain) {
        return domainService.get(domain, phoneNumber)
                .map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PatchMapping("/merge-csv")
    public ResponseEntity<String> mergeCSVData() {
        domainService.mergeDataFromCSV();

        return ResponseEntity.ok("CSV data merged successfully.");
    }

    @GetMapping("/all")
    public ResponseEntity<List<DomainDocument>> findAllDomains() {
        return ResponseEntity.ok(domainService.findAll());
    }
}
