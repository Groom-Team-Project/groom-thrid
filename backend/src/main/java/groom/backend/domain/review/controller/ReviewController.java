package groom.backend.domain.review.controller;

import groom.backend.domain.review.dto.request.CreateReviewRequest;
import groom.backend.domain.review.dto.request.UpdateReviewRequest;
import groom.backend.domain.review.dto.response.ReviewResponse;
import groom.backend.domain.review.service.spec.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/reviews")
@Tag(name = "Review", description = "리뷰 관리 API")
public class ReviewController {

    private final ReviewService reviewService;

    @Autowired
    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @PostMapping("/place/{placeId}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'PROTECTOR')")
    @Operation(
            summary = "리뷰 생성",
            description = "장소별 새로운 리뷰를 생성합니다",
            security = {@SecurityRequirement(name = "bearerAuth")}
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "생성 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청")
    })
    public ReviewResponse createReview(
            @PathVariable Long placeId,
            @Valid @RequestBody CreateReviewRequest request) {
        return reviewService.createReview(placeId, request);
    }

    @GetMapping("/{reviewId}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'PROTECTOR')")
    @Operation(
            summary = "리뷰 조회",
            description = "ID로 리뷰를 조회합니다",
            security = {@SecurityRequirement(name = "bearerAuth")}
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "리뷰를 찾을 수 없음")
    })
    public ReviewResponse getReview(@PathVariable Long reviewId) {
        return reviewService.getReview(reviewId);
    }

    @GetMapping("/place/{placeId}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'PROTECTOR')")
    @Operation(
            summary = "장소별 리뷰 목록 조회",
            description = "장소 ID로 해당 장소의 모든 리뷰를 조회합니다",
            security = {@SecurityRequirement(name = "bearerAuth")}
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공")
    })
    public List<ReviewResponse> getReviewsByPlaceId(@PathVariable Long placeId) {
        return reviewService.getReviewsByPlaceId(placeId);
    }

    @PutMapping("/{reviewId}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'PROTECTOR')")
    @Operation(
            summary = "리뷰 수정",
            description = "리뷰를 수정합니다",
            security = {@SecurityRequirement(name = "bearerAuth")}
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "수정 성공"),
            @ApiResponse(responseCode = "404", description = "리뷰를 찾을 수 없음"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청")
    })
    public ReviewResponse updateReview(
            @PathVariable Long reviewId,
            @Valid @RequestBody UpdateReviewRequest request) {
        return reviewService.updateReview(reviewId, request);
    }

    @DeleteMapping("/{reviewId}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'PROTECTOR')")
    @Operation(
            summary = "리뷰 삭제",
            description = "리뷰를 삭제합니다",
            security = {@SecurityRequirement(name = "bearerAuth")}
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "삭제 성공"),
            @ApiResponse(responseCode = "404", description = "리뷰를 찾을 수 없음")
    })
    public void deleteReview(@PathVariable Long reviewId) {
        reviewService.deleteReview(reviewId);
    }
}

