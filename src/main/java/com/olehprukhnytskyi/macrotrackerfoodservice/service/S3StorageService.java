package com.olehprukhnytskyi.macrotrackerfoodservice.service;

import com.olehprukhnytskyi.macrotrackerfoodservice.properties.S3Properties;
import java.io.InputStream;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.Delete;
import software.amazon.awssdk.services.s3.model.DeleteObjectsRequest;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.ObjectIdentifier;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Slf4j
@Service
@RequiredArgsConstructor
public class S3StorageService {
    private final S3Client s3Client;
    private final S3Properties s3Properties;

    public String uploadFile(InputStream inputStream, long contentLength,
                             String key, String contentType) {
        log.info("Uploading file to S3 bucket={} key={}", s3Properties.getS3Bucket(), key);
        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(s3Properties.getS3Bucket())
                .key(key)
                .contentType(contentType)
                .build();
        s3Client.putObject(request, RequestBody.fromInputStream(inputStream, contentLength));
        String url = s3Client.utilities().getUrl(builder -> builder
                .bucket(s3Properties.getS3Bucket()).key(key)).toString();
        log.debug("File uploaded successfully to S3: {}", url);
        return url;
    }

    public void deleteFolder(String prefix) {
        log.info("Deleting S3 folder with prefix={}", prefix);
        String continuationToken = null;
        do {
            ListObjectsV2Request listRequest = ListObjectsV2Request.builder()
                    .bucket(s3Properties.getS3Bucket())
                    .prefix(prefix)
                    .continuationToken(continuationToken)
                    .build();
            ListObjectsV2Response listResponse = s3Client.listObjectsV2(listRequest);
            List<ObjectIdentifier> toDelete = listResponse.contents().stream()
                    .map(o -> ObjectIdentifier.builder().key(o.key()).build())
                    .toList();
            if (!toDelete.isEmpty()) {
                DeleteObjectsRequest deleteRequest = DeleteObjectsRequest.builder()
                        .bucket(s3Properties.getS3Bucket())
                        .delete(Delete.builder().objects(toDelete).build())
                        .build();
                s3Client.deleteObjects(deleteRequest);
                log.debug("Deleted {} objects from S3 prefix={}", toDelete.size(), prefix);
            }
            continuationToken = listResponse.nextContinuationToken();
        } while (continuationToken != null);
    }
}
