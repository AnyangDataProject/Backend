package com.dongyang.anyang.domain.report;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor
public class ReportDto {

    private String description;
    private BigDecimal latitude;
    private BigDecimal longtitude;
    private String address;
    private String damage_type;
    private String severity;


}
