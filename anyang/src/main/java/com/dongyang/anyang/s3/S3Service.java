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
        if (base64Image == null || base64Image.isBlank()){
            throw new IllegalArgumentException("이미지가 없음");
        }

        if (base64Image.contains(",")) {
            base64Image = base64Image.substring(base64Image.indexOf(",") + 1);
        }
        byte[] imageBytes = Base64.getDecoder().decode(base64Image);

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

    public String uploadFile(byte[] imageBytes, String contentType){
        if(imageBytes == null || imageBytes.length == 0){
            throw new IllegalArgumentException("이미지 없음");
        }
        String extension = "jpg";
        if("image/png".equals(contentType)){
            extension = "png";
        }else if("image/webp".equals(contentType)){
            extension = "webp";
        }

        String fileName = UUID.randomUUID()+"."+extension;


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
