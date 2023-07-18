package epredescu.webcrawler.crawler;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DomainRepository extends JpaRepository<Domain, Long> {
    List<Domain> findAllByVisitedFalse();
}

