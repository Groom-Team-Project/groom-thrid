package groom.backend.domain.review.service.spec;

import groom.backend.domain.review.dto.request.CreateReviewRequest;
import groom.backend.domain.review.dto.request.UpdateReviewRequest;
import groom.backend.domain.review.dto.response.ReviewResponse;

import java.util.List;

public interface ReviewService {
    /**
     * 새로운 리뷰를 생성합니다
     */
    ReviewResponse createReview(CreateReviewRequest request);

    /**
     * ID로 리뷰를 조회합니다
     */
    ReviewResponse getReview(Long reviewId);

    /**
     * 장소별 리뷰 목록을 조회합니다
     */
    List<ReviewResponse> getReviewsByPlaceId(Long placeId);

    /**
     * 리뷰를 수정합니다
     */
    ReviewResponse updateReview(Long reviewId, UpdateReviewRequest request);

    /**
     * 리뷰를 삭제합니다
     */
    void deleteReview(Long reviewId);
}
