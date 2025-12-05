package groom.backend.domain.review.repository.spec;

import groom.backend.domain.review.entity.Review;

import java.util.List;
import java.util.Optional;

public interface ReviewRepository {
    Review save(Review review);
    Optional<Review> findById(Long id);
    List<Review> findByPlaceId(Long placeId);
    void deleteById(Long id);
    boolean existsById(Long id);
}


