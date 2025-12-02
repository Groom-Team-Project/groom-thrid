package groom.backend.domain.review.service.impl;

import groom.backend.domain.review.dto.request.CreateReviewRequest;
import groom.backend.domain.review.dto.request.UpdateReviewRequest;
import groom.backend.domain.review.dto.response.ReviewResponse;
import groom.backend.domain.review.entity.Review;
import groom.backend.domain.review.mapper.ReviewMapper;
import groom.backend.domain.review.repository.spec.ReviewRepository;
import groom.backend.domain.review.service.spec.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;

    @Override
    @Transactional
    public ReviewResponse createReview(CreateReviewRequest request) {
        Review review = ReviewMapper.toEntity(request);
        Review savedReview = reviewRepository.save(review);
        return ReviewMapper.toResponse(savedReview);
    }

    @Override
    @Transactional(readOnly = true)
    public ReviewResponse getReview(Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("리뷰를 찾을 수 없습니다. ID: " + reviewId));
        return ReviewMapper.toResponse(review);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReviewResponse> getReviewsByPlaceId(Long placeId) {
        List<Review> reviews = reviewRepository.findByPlaceId(placeId);
        return ReviewMapper.toResponseList(reviews);
    }

    @Override
    @Transactional
    public ReviewResponse updateReview(Long reviewId, UpdateReviewRequest request) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("리뷰를 찾을 수 없습니다. ID: " + reviewId));
        
        // 엔티티의 update 메서드 호출 (BaseEntity의 @LastModifiedDate가 자동으로 updatedAt 업데이트)
        review.update(request.content(), request.rating(), request.imageUrl());
        
        Review savedReview = reviewRepository.save(review);
        return ReviewMapper.toResponse(savedReview);
    }

    @Override
    @Transactional
    public void deleteReview(Long reviewId) {
        if (!reviewRepository.existsById(reviewId)) {
            throw new IllegalArgumentException("리뷰를 찾을 수 없습니다. ID: " + reviewId);
        }
        reviewRepository.deleteById(reviewId);
    }
}
