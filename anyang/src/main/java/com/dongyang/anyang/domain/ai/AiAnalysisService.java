package com.dongyang.anyang.domain.ai;

import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class AiAnalysisService {
    private final RestClient restClient;
    public AiResponseDto predict(MultipartFile image){
        try{
            ByteArrayResource resource = new ByteArrayResource(image.getBytes()){
                @Override
                public String getFilename(){
                    return image.getOriginalFilename();
                }
            };
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();

            body.add("file", resource);

            return restClient.post().uri("/predict").contentType(MediaType.MULTIPART_FORM_DATA).body(body).retrieve().body(AiResponseDto.class);

        }catch (IOException e){
            throw new RuntimeException("Ai 서버로 이미지 전송 중 에러", e);
        }
    }

}
