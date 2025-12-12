package groom.backend.domain.review.mapper;

import groom.backend.domain.review.dto.request.CreateReviewRequest;
import groom.backend.domain.review.dto.response.ReviewResponse;
import groom.backend.domain.review.entity.Review;
import groom.backend.domain.users.entity.User;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ReviewMapper {

    public static Review toEntity(Long placeId, CreateReviewRequest request, User user) {
        return Review.builder()
                .placeId(placeId)
                .content(request.content())
                .rating(request.rating())
                .user(user)
                .author(user.getName()) // User의 name을 author로 설정
                .imageUrl(request.imageUrl())
                .isActive(true) // 기본값은 활성화
                .build();
    }

    public static ReviewResponse toResponse(Review review) {
        // User의 name을 author로 사용 (User가 로드되지 않은 경우를 대비해 author 필드 사용)
        String authorName = review.getUser() != null ? review.getUser().getName() : review.getAuthor();
        
        return new ReviewResponse(
                review.getId(),
                review.getPlaceId(),
                review.getContent(),
                review.getRating(),
                authorName,
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


