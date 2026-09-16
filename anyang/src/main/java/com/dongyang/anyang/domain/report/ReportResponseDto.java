package com.dongyang.anyang.domain.report;

import com.dongyang.anyang.domain.ai.AiDetectionSummaryDto;
import com.dongyang.anyang.domain.ai.AiResponseDto;
import com.dongyang.anyang.domain.image.ReportImageResponseDto;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class ReportResponseDto {
    private Long id;
    private String userName;
    private String type;
    private String severity;
    private String status;
    private String address;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private LocalDateTime reportedAt;
    private String description;
    private Double aiConfidence;
    private List<ReportImageResponseDto> images;
    private Long inspectionClusterId;
    private List<AiDetectionSummaryDto> aiDetections;
}
