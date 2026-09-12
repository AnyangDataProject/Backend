package com.dongyang.anyang.domain.report;

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

    //분석 기능(위험도 분석이나 우선순위 할거면 여기 추가해야할듯)
}
