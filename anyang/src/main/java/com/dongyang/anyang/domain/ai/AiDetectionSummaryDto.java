package com.dongyang.anyang.domain.ai;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class AiDetectionSummaryDto {
    private String className;
    private Long detectionsCount;
}
