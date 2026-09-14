package com.dongyang.anyang.domain.inspection;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MonthlyDamageResponseDto {

    private int month;
    private int count;

    public static MonthlyDamageResponseDto of(int month, int count) {
        return MonthlyDamageResponseDto.builder()
                .month(month)
                .count(count)
                .build();
    }
}