package com.dongyang.anyang.domain.ai;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AiAnalysisRepository extends JpaRepository<AiAnalysis,Long> {
    List<AiAnalysis> findByReportId(Long reportId);
}
