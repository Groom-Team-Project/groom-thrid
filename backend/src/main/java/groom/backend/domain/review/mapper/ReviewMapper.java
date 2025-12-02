package groom.backend.domain.review.mapper;

import groom.backend.domain.review.dto.request.CreateReviewRequest;
import groom.backend.domain.review.dto.response.ReviewResponse;
import groom.backend.domain.review.entity.Review;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ReviewMapper {

    public static Review toEntity(Long placeId, CreateReviewRequest request) {
        return Review.builder()
                .placeId(placeId)
                .content(request.content())
                .rating(request.rating())
                .author(request.author())
                .imageUrl(request.imageUrl())
                .isActive(true) // 기본값은 활성화
                .build();
    }

    public static ReviewResponse toResponse(Review review) {
        return new ReviewResponse(
                review.getId(),
                review.getPlaceId(),
                review.getContent(),
                review.getRating(),
                review.getAuthor(),
                review.getImageUrl(),
                review.getIsActive(),
                review.getCreatedAt(),
                review.getUpdatedAt()
        );
    }

    public static List<ReviewResponse> toResponseList(List<Review> reviews) {
        return reviews.stream()
                .map(ReviewMapper::toResponse)
                .toList();
    }
}
