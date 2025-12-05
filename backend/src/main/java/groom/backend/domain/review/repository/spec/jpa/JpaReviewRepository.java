package groom.backend.domain.review.repository.spec.jpa;

import groom.backend.domain.review.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface JpaReviewRepository extends JpaRepository<Review, Long> {
    @Query("SELECT r FROM review r LEFT JOIN FETCH r.user WHERE r.placeId = :placeId")
    List<Review> findByPlaceId(@Param("placeId") Long placeId);
}


