package com.urisik.backend.global.external.s3;

import com.amazonaws.SdkClientException;
import com.amazonaws.services.s3.AmazonS3;
import com.urisik.backend.global.apiPayload.code.BaseErrorCode;
import com.urisik.backend.global.apiPayload.code.GeneralErrorCode;
import com.urisik.backend.global.apiPayload.exception.GeneralException;
import com.urisik.backend.global.external.s3.exception.S3ErrorCode;
import com.urisik.backend.global.external.s3.exception.S3Exception;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URI;

@Slf4j
@RequiredArgsConstructor
@Component
public class S3Remover {
    private final AmazonS3 amazonS3;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    /**
     * 기능 : S3에 저장된 파일을 삭제함
     * @param fileUrl : 삭제할 파일의 전체 URL
     */
    public void remove(String fileUrl) {
        // URL 유효성 검사
        if (fileUrl == null || fileUrl.isBlank()) {
            log.info("S3Remover: failed delete due to empty url.");
            return;
        }

        // 전체 URL에서 S3 객체 키 추출
        final String key;
        try {
            String rawPath = URI.create(fileUrl).getRawPath();
            if (rawPath == null || rawPath.length() <= 1) {
                log.warn("S3Remover: Invalid url path. url={}", fileUrl);
                return;
            }
            // 맨 앞의 '/' 제거
            key = (rawPath.charAt(0) == '/') ? rawPath.substring(1) : rawPath;
        } catch (Exception e) {
            log.error("S3Remover: URL parse failed. url={}", fileUrl, e);
            throw new S3Exception(S3ErrorCode.S3_REMOVE_FAIL.getMessage());
        }

        // 실제 S3 객체 삭제
        try {
            amazonS3.deleteObject(bucket, key);
            log.info("S3Remover: Delete successful. bucket={}, key={}", bucket, key);
        } catch (SdkClientException e) {
            log.error("S3Remover: Delete failed. bucket={}, key={}", bucket, key, e);
            throw new S3Exception(S3ErrorCode.S3_REMOVE_FAIL.getMessage());
        }
    }
}
