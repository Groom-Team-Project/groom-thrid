package groom.backend.domain.report.repository.spec;

import groom.backend.domain.report.entity.Report;

import java.util.List;
import java.util.Optional;

public interface ReportRepository {
    Report save(Report report);
    Optional<Report> findById(Long id);
    List<Report> findByAuthor(String author);
    List<Report> findByPlaceId(Long placeId);
    List<Report> findByAuthorAndPlaceId(String author, Long placeId);
    void deleteById(Long id);
    void deleteAllById(List<Long> ids);
    boolean existsById(Long id);
    boolean existsByIdAndAuthor(Long id, String author);
}
