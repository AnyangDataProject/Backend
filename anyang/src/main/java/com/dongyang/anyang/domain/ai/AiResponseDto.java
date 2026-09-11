package com.dongyang.anyang.domain.ai;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
public class AiResponseDto {

    private List<Detections> detections;
    private String resultImage;

    @Getter
    @NoArgsConstructor
    public static class Detections{
        private String className;
        private Double confidence;
        private int bboxX;
        private int bboxY;
        private int bboxWidth;
        private int bboxHeight;
    }


}
