package com.dongyang.anyang.domain.ai;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AiAnalysisRepository extends JpaRepository<AiAnalysis,Long> {
    List<AiAnalysis> findByReportId(Long reportId);
    Optional<AiAnalysis> findByReportImageId(Long reportImageId);
}
