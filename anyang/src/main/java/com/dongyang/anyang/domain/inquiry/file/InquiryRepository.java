package com.dongyang.anyang.domain.inquiry.file;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InquiryRepository extends JpaRepository<InquiryFile, Long> {
    List<InquiryFile> findByInquiryId(Long inquiryId);
}
