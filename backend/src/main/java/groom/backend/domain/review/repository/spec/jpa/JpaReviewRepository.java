package groom.backend.domain.review.repository.spec.jpa;

import groom.backend.domain.review.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JpaReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByPlaceId(Long placeId);
}
