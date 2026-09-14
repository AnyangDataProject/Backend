package com.dongyang.anyang.domain.report;


import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface ReportRepository extends JpaRepository<Report, Long> {
    List<Report> findByUserId(Long userId);
    long countByUserId(Long userId);
    List<Report> findByCreatedAtBetween(
            LocalDateTime start,
            LocalDateTime end
    );
    List<Report> findByInspectionClusterClusterAndCreatedAtBetween(
            Integer cluster,
            LocalDateTime start,
            LocalDateTime end
    );
    long countByInspectionClusterCluster(Integer cluster);
    List<Report> findByInspectionClusterIsNull();
}
