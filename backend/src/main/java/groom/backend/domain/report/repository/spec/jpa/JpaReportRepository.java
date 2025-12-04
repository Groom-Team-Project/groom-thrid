package groom.backend.domain.report.repository.spec.jpa;

import groom.backend.domain.report.entity.Report;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface JpaReportRepository extends JpaRepository<Report, Long> {
    List<Report> findByAuthor(String author);
    
    @Query("SELECT r FROM report r LEFT JOIN FETCH r.user WHERE r.user.id = :userId")
    List<Report> findByUserId(@Param("userId") UUID userId);
    
    List<Report> findByPlaceId(Long placeId);
    List<Report> findByAuthorAndPlaceId(String author, Long placeId);
    List<Report> findAll();
}


