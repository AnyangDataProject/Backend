package com.dongyang.anyang.s3;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.util.Base64;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class S3Service {
    private final S3Client s3Client;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    public String uploadFile(String base64Image){
        if(base64Image.isBlank() || base64Image == null){
            throw new IllegalArgumentException("이미지가 없음");
        }

        byte[] imageBytes = Base64.getDecoder().decode(base64Image);


        if (base64Image.contains(",")) {
            base64Image = base64Image.substring(base64Image.indexOf(",") + 1);
        }

        String fileName = UUID.randomUUID()+".png";

        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(bucket)
                .key(fileName)
                .build();
        s3Client.putObject(
                request,
                RequestBody.fromBytes(imageBytes)
        );


        return String.format("https://%s.s3.amazonaws.com/%s", bucket, fileName);
    }
}
