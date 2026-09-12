package com.dongyang.anyang.domain.ai;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AiDetectionRepository extends JpaRepository<AiDetection, Long> {
    List<AiDetection> findByAiAnalysisId(Long aiAnalysisId);
}
