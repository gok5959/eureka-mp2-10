package com.mycom.myapp.common.config.storage;

import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GcpStorageService {

    private final Storage storage;

    @Value("${gcp.bucket}")
    private String bucketName;

    public String uploadFile(String fileName, byte[] content, String contentType) {
        BlobId blobId = BlobId.of(bucketName, fileName);
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
                .setContentType(contentType)
                .build();

        storage.create(blobInfo, content);
        return String.format("gs://%s/%s", bucketName, fileName);
    }

}
