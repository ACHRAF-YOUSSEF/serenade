package com.serenade.backend.domain.track;

import com.serenade.backend.config.AppProperties;
import io.minio.*;
import io.minio.http.Method;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.concurrent.TimeUnit;

@Service
public class MinioService {

    private final MinioClient minio;
    private final AppProperties props;

    public MinioService(MinioClient minio, AppProperties props) {
        this.minio = minio;
        this.props = props;
    }

    public String uploadRaw(String objectKey, MultipartFile file) {
        try {
            String bucket = props.minio().bucket();
            String contentType = file.getContentType() != null
                    ? file.getContentType()
                    : "application/octet-stream";
            ensureBucket(bucket);
            minio.putObject(PutObjectArgs.builder()
                    .bucket(bucket)
                    .object(objectKey)
                    .stream(file.getInputStream(), file.getSize(), -1L)
                    .contentType(contentType)
                    .build());
            return objectKey;
        } catch (Exception e) {
            throw new RuntimeException("MinIO upload failed: " + e.getMessage(), e);
        }
    }

    public String presignedGetUrl(String objectKey) {
        try {
            int expiryMinutes = props.minio().presignedUrlExpiryMinutes();
            int clampedExpiry = Math.clamp(expiryMinutes, 1, 15);
            return minio.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder()
                    .method(Method.GET)
                    .bucket(props.minio().bucket())
                    .object(objectKey)
                    .expiry(clampedExpiry, TimeUnit.MINUTES)
                    .build());
        } catch (Exception e) {
            throw new RuntimeException("MinIO presign failed: " + e.getMessage(), e);
        }
    }

    public record ObjectData(InputStream stream, long size) {}

    public ObjectData getObject(String objectKey) {
        try {
            var response = minio.getObject(GetObjectArgs.builder()
                    .bucket(props.minio().bucket())
                    .object(objectKey)
                    .build());
            String cl = response.headers().get("Content-Length");
            long size = cl != null ? Long.parseLong(cl) : -1L;
            return new ObjectData(response, size);
        } catch (Exception e) {
            throw new RuntimeException("MinIO object read failed: " + e.getMessage(), e);
        }
    }

    private void ensureBucket(String bucket) throws Exception {
        boolean exists = minio.bucketExists(BucketExistsArgs.builder().bucket(bucket).build());
        if (!exists) {
            minio.makeBucket(MakeBucketArgs.builder().bucket(bucket).build());
        }
    }
}
