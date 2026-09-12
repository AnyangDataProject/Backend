package com.dongyang.anyang.domain.inquiry;

import java.time.LocalDateTime;
import java.util.List;

public class InquiryDto {
    public record CreateRequest(
            InquiryType inquiryType,
            String title,
            String content,
            String email
    ) {}

    public record AnswerRequest(
            String answer
    ) {}

    public record ListResponse(
            Long id,
            InquiryType inquiryType,
            String title,
            InquiryStatus status,
            LocalDateTime createdAt
    ) {}

    public record DetailResponse(
            Long id,
            InquiryType inquiryType,
            String title,
            String content,
            InquiryStatus status,
            String answer,
            String answeredByName,
            LocalDateTime answeredAt,
            LocalDateTime createdAt,
            LocalDateTime updatedAt,
            List<String> fileUrls
    ) {

    }
}
