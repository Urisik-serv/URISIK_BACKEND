package com.urisik.backend.global.external.s3;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.urisik.backend.global.external.s3.exception.S3ErrorCode;
import com.urisik.backend.global.external.s3.exception.S3Exception;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Component
public class S3Uploader {

    private final AmazonS3 amazonS3;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    /**
     * 기능 : S3에 파일을 업로드하고, 저장된 URL을 반환함
     * @param file : 실제 파일 이름
     * @param dirName : 사용하는 도메인 이름 (프로필이면,,, familyMember,  음식 사진이면 아직 안 정해진 것 같긴 한데 food)
     */
    public String upload(MultipartFile file, String dirName) {
        // 입력 받는 이미지 파일이 빈 파일인지 검증
        if (file == null || file.isEmpty()) {
            log.error("S3Uploader : File is null or empty");
            throw new S3Exception(S3ErrorCode.EMPTY_FILE);
        }

        // 파일 이름 생성 및 인코딩
        String originalFilename = file.getOriginalFilename(); // 원본 파일 이름
        String safeFileName = URLEncoder.encode(originalFilename, StandardCharsets.UTF_8)
                .replaceAll("\\+", "%20");

        // 2. S3에 저장될 최종 경로 (디렉토리/UUID_파일명)
        String fileName = dirName + "/" + UUID.randomUUID() + "_" + safeFileName;


        // 메타 데이터 설정 (파일 타입, 크기)
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType(file.getContentType());
        metadata.setContentLength(file.getSize());

        // 이미지 업로드 로직
        try (InputStream inputStream = file.getInputStream()) {
            amazonS3.putObject(new PutObjectRequest(bucket, fileName, inputStream, metadata));
            log.info("S3Uploader: Upload successful. URL={}, bucket={}", fileName, bucket);
        } catch (IOException e) {
            log.error("S3Uploader: Upload failed due to IO error  ", e);
            throw new S3Exception(S3ErrorCode.S3_UPLOAD_FAIL);
        }

        // 업로드 된 URL 반환
        return amazonS3.getUrl(bucket, fileName).toString();


    }
}
