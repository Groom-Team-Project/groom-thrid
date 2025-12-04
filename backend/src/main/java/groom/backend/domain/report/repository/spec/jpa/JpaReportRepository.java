package groom.backend.domain.report.repository.spec.jpa;

import groom.backend.domain.report.entity.Report;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JpaReportRepository extends JpaRepository<Report, Long> {
    List<Report> findByAuthor(String author);
    List<Report> findByPlaceId(Long placeId);
    List<Report> findByAuthorAndPlaceId(String author, Long placeId);
    List<Report> findAll();
}

