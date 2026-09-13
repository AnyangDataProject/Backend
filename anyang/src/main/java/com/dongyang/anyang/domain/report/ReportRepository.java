package com.dongyang.anyang.domain.report;


import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReportRepository extends JpaRepository<Report, Long> {
    List<Report> findByUserId(Long userId);
    long countByUserId(Long userId);
}
