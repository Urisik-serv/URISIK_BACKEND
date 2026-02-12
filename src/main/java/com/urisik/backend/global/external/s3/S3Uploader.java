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

import java.io.ByteArrayInputStream;
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
     * 1. MultipartFile 기반 이미지 업로드
     * 기능 : url을 통해 S3에 이미지를 업로드하고, 저장된 URL을 반환함
     * @param file : 업로드 할 파일
     * @param dirName : 사용하는 도메인 이름 (프로필이면 family_member_profile, 레시피면 recipe 등)
     *
     */
    public String uploadByFile(MultipartFile file, String dirName) {
        if (file == null || file.isEmpty()) {
            throw new S3Exception(S3ErrorCode.EMPTY_FILE);
        }

        String originalFilename = file.getOriginalFilename();
        String contentType = file.getContentType();
        long contentLength = file.getSize();

        try (InputStream inputStream = file.getInputStream()) {
            return putS3(inputStream, originalFilename, contentType, contentLength, dirName);
        } catch (IOException e) {
            throw new S3Exception(S3ErrorCode.S3_UPLOAD_FAIL);
        }
    }


    /**
     * 2.  URL 기반 이미지 업로드
     * 기능 : url을 통해 S3에 이미지를 업로드하고, 저장된 URL을 반환함
     * @param imageUrl
     * @param dirName : 사용하는 도메인 이름
     */
    public String uploadByUrl(String imageUrl, String dirName) {
        if (imageUrl == null || imageUrl.isEmpty()) {
            throw new S3Exception(S3ErrorCode.EMPTY_FILE);
        }

        try {
            java.net.URL url = new java.net.URL(imageUrl);
            java.net.URLConnection connection = url.openConnection();

            // URL 로부터 메타데이터 추출
            String contentType = connection.getContentType();
            long contentLength = connection.getContentLength();
            String originalFilename = extractCleanFileName(imageUrl, contentType);

            try (InputStream inputStream = connection.getInputStream()) {
                return putS3(inputStream, originalFilename, contentType, contentLength, dirName);
            }
        } catch (IOException e) {
            log.error("S3Uploader: URL upload failed", e);
            throw new S3Exception(S3ErrorCode.S3_UPLOAD_FAIL);
        }
    }


    //---------------------------
    /**
     S3 업로드 공통 로직
     */
    private String putS3(InputStream inputStream, String originalFilename, String contentType, long contentLength, String dirName) {
        // 파일명 생성
        String safeFileName = URLEncoder.encode(originalFilename, StandardCharsets.UTF_8)
                .replaceAll("\\+", "%20");
        String fileName = dirName + "/" + UUID.randomUUID() + "_" + safeFileName;

        // 메타데이터 설정
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType(contentType);
        metadata.setContentLength(contentLength);

        // 업로드
        amazonS3.putObject(new PutObjectRequest(bucket, fileName, inputStream, metadata));
        log.info("S3Uploader: Upload successful. Path={}", fileName);

        return amazonS3.getUrl(bucket, fileName).toString();
    }

    /**
     * URL에서 불필요한 파라미터 제거 후 확장자를 포함한 파일명 추출
     */
    private String extractCleanFileName(String imageUrl, String contentType) {
        // URL의 경로 부분만 추출 (쿼리 스트링 ? 뒤는 제거)
        String pathOnly = imageUrl.split("\\?")[0];
        String fileName = pathOnly.substring(pathOnly.lastIndexOf("/") + 1);

        // 만약 파일명이 너무 길거나(URL 기반 업로드의 특징), 확장자가 없다면 ContentType 활용
        if (fileName.length() > 50 || !fileName.contains(".")) {
            String extension = ".jpg"; // 기본값
            if (contentType != null && contentType.contains("/")) {
                extension = "." + contentType.split("/")[1].split(";")[0];
            }
            fileName = "image" + extension;
        }
        return fileName;
    }

    public String uploadBytes(
            byte[] data,
            String fileName,
            String contentType,
            String dirName
    ) {
        String safeContentType =
                (contentType == null || contentType.isBlank())
                        ? "image/png"
                        : contentType;

        try (ByteArrayInputStream is = new ByteArrayInputStream(data)) {
            return putS3(is, fileName, safeContentType, data.length, dirName);
        } catch (Exception e) {
            throw new S3Exception(S3ErrorCode.S3_UPLOAD_FAIL);
        }
    }
}
