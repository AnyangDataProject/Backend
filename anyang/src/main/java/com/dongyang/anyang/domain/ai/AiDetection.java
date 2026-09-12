package com.dongyang.anyang.domain.ai;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Builder
@Entity
@Table(name = "ai_detections")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AiDetection {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ai_analysis_id", nullable = false)
    private AiAnalysis aiAnalysis;

    @Column(name = "class_name", nullable = false, length = 100)
    private String className;

    @Column(nullable = false, precision = 5, scale = 4)
    private BigDecimal confidence;

    @Column(name = "bbox_x", precision = 10, scale = 4)
    private BigDecimal bboxX;
    @Column(name = "bbox_y", precision = 10, scale = 4)
    private BigDecimal bboxY;
    @Column(name = "bbox_width", precision = 10, scale = 4)
    private BigDecimal bboxWidth;
    @Column(name = "bbox_height", precision = 10, scale = 4)
    private BigDecimal bboxHeight;
}
