package groom.backend.domain.report.repository.spec.jpa;

import groom.backend.domain.report.entity.Report;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface JpaReportRepository extends JpaRepository<Report, Long> {
    List<Report> findByAuthor(String author);
    
    @Query("SELECT r FROM report r LEFT JOIN FETCH r.user WHERE r.user.id = :userId")
    List<Report> findByUserId(@Param("userId") UUID userId);
    
    @Query("SELECT r FROM report r LEFT JOIN FETCH r.user WHERE r.id = :id")
    Optional<Report> findByIdWithUser(@Param("id") Long id);
    
    @Query("SELECT COUNT(r) > 0 FROM report r WHERE r.id = :id AND r.user.id = :userId")
    boolean existsByIdAndUserId(@Param("id") Long id, @Param("userId") UUID userId);
    
    List<Report> findByPlaceId(Long placeId);
    List<Report> findByAuthorAndPlaceId(String author, Long placeId);
    List<Report> findAll();
}


