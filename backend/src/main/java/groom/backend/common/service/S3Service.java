package groom.backend.common.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.io.ByteArrayInputStream;
import java.util.Base64;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
public class S3Service {

    private final S3Client s3Client;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    @Value("${cloud.aws.region.static}")
    private String region;

    private static final Pattern BASE64_PATTERN = Pattern.compile("^data:image/([a-zA-Z]+);base64,(.+)$");
    private static final String S3_BASE_URL = "https://%s.s3.%s.amazonaws.com/%s";

    /**
     * Base64 이미지 문자열을 S3에 업로드하고 URL을 반환합니다.
     * Base64가 아닌 경우(이미 URL인 경우) 그대로 반환합니다.
     *
     * @param base64Image Base64 이미지 문자열 (data:image/... 형식 또는 일반 base64)
     * @param folder      S3 폴더 경로 (예: "reviews", "reports")
     * @return S3 URL 또는 원본 URL
     */
    public String uploadImageIfBase64(String base64Image, String folder) {
        if (base64Image == null || base64Image.isBlank()) {
            log.debug("이미지가 null이거나 비어있어 업로드를 건너뜁니다.");
            return null;
        }

        // 이미 URL인 경우 (http:// 또는 https://로 시작)
        if (base64Image.startsWith("http://") || base64Image.startsWith("https://")) {
            log.debug("이미 URL 형식입니다. 업로드 없이 반환: {}", base64Image);
            return base64Image;
        }

        log.info("S3 이미지 업로드 시작 - 폴더: {}, Base64 길이: {}", folder, base64Image.length());

        try {
            // Base64 데이터 파싱
            String imageData;
            String contentType = "image/jpeg"; // 기본값

            Matcher matcher = BASE64_PATTERN.matcher(base64Image);
            if (matcher.matches()) {
                // data:image/jpeg;base64,... 형식
                String imageType = matcher.group(1);
                imageData = matcher.group(2);
                contentType = "image/" + imageType;
                log.debug("Base64 형식 감지: {}", contentType);
            } else {
                // 순수 base64 문자열
                imageData = base64Image;
                log.debug("순수 Base64 문자열로 처리");
            }

            // Base64 디코딩
            byte[] imageBytes = Base64.getDecoder().decode(imageData);
            log.info("Base64 디코딩 완료 - 이미지 크기: {} bytes", imageBytes.length);

            // 파일명 생성 (UUID 사용)
            String fileName = folder + "/" + UUID.randomUUID() + "." + getFileExtension(contentType);
            log.info("S3 업로드 대상 - 버킷: {}, 파일명: {}, Content-Type: {}", bucket, fileName, contentType);

            // S3에 업로드
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(fileName)
                    .contentType(contentType)
                    .build();

            s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(
                    new ByteArrayInputStream(imageBytes), imageBytes.length));

            // S3 URL 생성
            String s3Url = String.format(S3_BASE_URL, bucket, region.toLowerCase(), fileName);

            log.info("✅ S3 이미지 업로드 성공!");
            log.info("   - 버킷: {}", bucket);
            log.info("   - 파일명: {}", fileName);
            log.info("   - 파일 크기: {} bytes", imageBytes.length);
            log.info("   - Content-Type: {}", contentType);
            log.info("   - S3 URL: {}", s3Url);
            return s3Url;

        } catch (S3Exception e) {
            log.error("S3 업로드 실패: {}", e.getMessage(), e);
            throw new RuntimeException("이미지 업로드에 실패했습니다: " + e.getMessage(), e);
        } catch (IllegalArgumentException e) {
            log.error("Base64 디코딩 실패: {}", e.getMessage(), e);
            throw new RuntimeException("이미지 형식이 올바르지 않습니다: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("이미지 업로드 중 오류 발생: {}", e.getMessage(), e);
            throw new RuntimeException("이미지 업로드 중 오류가 발생했습니다: " + e.getMessage(), e);
        }
    }

    /**
     * Content-Type에서 파일 확장자 추출
     */
    private String getFileExtension(String contentType) {
        if (contentType == null) {
            return "jpg";
        }
        return contentType.substring(contentType.lastIndexOf("/") + 1);
    }

    /**
     * S3에서 이미지 삭제
     *
     * @param imageUrl 삭제할 이미지의 S3 URL
     */
    public void deleteImage(String imageUrl) {
        if (imageUrl == null || imageUrl.isBlank()) {
            return;
        }

        // S3 URL이 아닌 경우 삭제하지 않음
        if (!imageUrl.contains("s3") && !imageUrl.contains("amazonaws.com")) {
            return;
        }

        try {
            // URL에서 키 추출
            String key = extractKeyFromUrl(imageUrl);
            if (key == null) {
                log.warn("S3 URL에서 키를 추출할 수 없습니다: {}", imageUrl);
                return;
            }

            s3Client.deleteObject(builder -> builder
                    .bucket(bucket)
                    .key(key)
                    .build());

            log.info("이미지 삭제 성공: {}", imageUrl);
        } catch (Exception e) {
            log.error("이미지 삭제 실패: {}", e.getMessage(), e);
            // 삭제 실패해도 예외를 던지지 않음 (이미 삭제된 경우 등)
        }
    }

    /**
     * S3 URL에서 키 추출
     */
    private String extractKeyFromUrl(String url) {
        try {
            // https://bucket-name.s3.region.amazonaws.com/folder/file.jpg 형식
            String pattern = "https://[^/]+\\.s3\\.[^/]+/(.+)";
            Pattern r = Pattern.compile(pattern);
            Matcher m = r.matcher(url);
            if (m.find()) {
                return m.group(1);
            }
        } catch (Exception e) {
            log.error("URL에서 키 추출 실패: {}", e.getMessage());
        }
        return null;
    }
}

