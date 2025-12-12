package groom.backend.domain.review.service.spec;

import groom.backend.common.security.AuthUser;
import groom.backend.domain.review.dto.request.CreateReviewRequest;
import groom.backend.domain.review.dto.request.UpdateReviewRequest;
import groom.backend.domain.review.dto.response.ReviewResponse;

import java.util.List;

public interface ReviewService {
    /**
     * 새로운 리뷰를 생성합니다
     */
    ReviewResponse createReview(Long placeId, CreateReviewRequest request);

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
     * USER, PROTECTOR: 자신이 생성한 리뷰만 수정 가능
     * ADMIN: 모든 리뷰 수정 가능
     */
    ReviewResponse updateReview(Long reviewId, UpdateReviewRequest request, AuthUser authUser);

    /**
     * 리뷰를 삭제합니다
     * USER, PROTECTOR: 자신이 생성한 리뷰만 삭제 가능
     * ADMIN: 모든 리뷰 삭제 가능
     */
    void deleteReview(Long reviewId, AuthUser authUser);
}


