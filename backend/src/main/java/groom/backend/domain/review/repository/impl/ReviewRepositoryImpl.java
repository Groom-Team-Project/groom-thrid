package groom.backend.domain.review.repository.impl;

import groom.backend.domain.review.entity.Review;
import groom.backend.domain.review.repository.spec.ReviewRepository;
import groom.backend.domain.review.repository.spec.jpa.JpaReviewRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class ReviewRepositoryImpl implements ReviewRepository {

    private final JpaReviewRepository jpaReviewRepository;

    public ReviewRepositoryImpl(JpaReviewRepository jpaReviewRepository) {
        this.jpaReviewRepository = jpaReviewRepository;
    }

    @Override
    public Review save(Review review) {
        return jpaReviewRepository.save(review);
    }

    @Override
    public Optional<Review> findById(Long id) {
        return jpaReviewRepository.findById(id);
    }

    @Override
    public List<Review> findByPlaceId(Long placeId) {
        return jpaReviewRepository.findByPlaceId(placeId);
    }

    @Override
    public void deleteById(Long id) {
        jpaReviewRepository.deleteById(id);
    }

    @Override
    public boolean existsById(Long id) {
        return jpaReviewRepository.existsById(id);
    }
}


