package groom.backend.domain.review.entity;

import groom.backend.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity(name = "review")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
public class Review extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ChargerLocation의 placeId 참조
    @Column(nullable = false)
    private Long placeId;

    // 리뷰 내용
    @Column(nullable = false, length = 1000)
    private String content;

    // 평점 (0-5점, 0.5 단위)
    @Column(nullable = false)
    private Double rating;

    // 작성자 (추후 User 엔티티와 연결 가능)
    @Column(nullable = false, length = 100)
    private String author;

    // 이미지 URL
    @Column(name = "image_url", length = 500)
    private String imageUrl;

    // 활성화 여부
    @Column(name = "is_active")
    private Boolean isActive;

    // 리뷰 수정 메서드
    public void update(String content, Double rating, String imageUrl) {
        this.content = content;
        this.rating = rating;
        this.imageUrl = imageUrl;
    }
}
