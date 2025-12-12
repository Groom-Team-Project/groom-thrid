package groom.backend.domain.review.service.impl;

import groom.backend.common.security.AuthUser;
import groom.backend.domain.review.dto.request.CreateReviewRequest;
import groom.backend.domain.review.dto.request.UpdateReviewRequest;
import groom.backend.domain.review.dto.response.ReviewResponse;
import groom.backend.domain.review.entity.Review;
import groom.backend.domain.review.mapper.ReviewMapper;
import groom.backend.domain.review.repository.spec.ReviewRepository;
import groom.backend.domain.review.service.spec.ReviewService;
import groom.backend.domain.users.entity.Role;
import groom.backend.domain.users.entity.User;
import groom.backend.domain.users.repository.spec.UserRepository;
import groom.backend.common.service.S3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final S3Service s3Service;

    @Override
    @Transactional
    public ReviewResponse createReview(Long placeId, CreateReviewRequest request) {
        // 현재 로그인한 사용자 정보 가져오기
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        AuthUser authUser = (AuthUser) authentication.getPrincipal();
        UUID userId = authUser.userId();

        // User 엔티티 조회
        User user = userRepository.findUserById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다. ID: " + userId));

        // Base64 이미지를 S3에 업로드하고 URL로 변환
        String imageUrl = s3Service.uploadImageIfBase64(request.imageUrl(), "reviews");

        // imageUrl이 업데이트된 CreateReviewRequest 생성
        CreateReviewRequest updatedRequest = new CreateReviewRequest(
                request.content(),
                request.rating(),
                imageUrl
        );

        Review review = ReviewMapper.toEntity(placeId, updatedRequest, user);
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
    public ReviewResponse updateReview(Long reviewId, UpdateReviewRequest request, AuthUser authUser) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("리뷰를 찾을 수 없습니다. ID: " + reviewId));
        
        // ADMIN: 모든 리뷰 수정 가능
        // USER, PROTECTOR: 자신이 생성한 리뷰만 수정 가능
        if (authUser.role() != Role.ADMIN) {
            if (review.getUser() == null || !review.getUser().getId().equals(authUser.userId())) {
                throw new IllegalArgumentException("본인의 리뷰만 수정할 수 있습니다.");
            }
        }
        
        // 기존 이미지가 S3에 있다면 삭제
        if (review.getImageUrl() != null && !review.getImageUrl().isBlank()) {
            s3Service.deleteImage(review.getImageUrl());
        }
        
        // Base64 이미지를 S3에 업로드하고 URL로 변환
        String imageUrl = s3Service.uploadImageIfBase64(request.imageUrl(), "reviews");
        
        // 엔티티의 update 메서드 호출 (BaseEntity의 @LastModifiedDate가 자동으로 updatedAt 업데이트)
        review.update(request.content(), request.rating(), imageUrl);
        
        Review savedReview = reviewRepository.save(review);
        return ReviewMapper.toResponse(savedReview);
    }

    @Override
    @Transactional
    public void deleteReview(Long reviewId, AuthUser authUser) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("리뷰를 찾을 수 없습니다. ID: " + reviewId));
        
        // ADMIN: 모든 리뷰 삭제 가능
        // USER, PROTECTOR: 자신이 생성한 리뷰만 삭제 가능
        if (authUser.role() != Role.ADMIN) {
            if (review.getUser() == null || !review.getUser().getId().equals(authUser.userId())) {
                throw new IllegalArgumentException("본인의 리뷰만 삭제할 수 있습니다.");
            }
        }
        
        // S3에서 이미지 삭제
        if (review.getImageUrl() != null && !review.getImageUrl().isBlank()) {
            s3Service.deleteImage(review.getImageUrl());
        }
        
        reviewRepository.deleteById(reviewId);
    }
}


