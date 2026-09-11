package com.dongyang.anyang.domain.image;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ReportImageResponseDto {
    private Long id;
    private String imageUrl;
}
