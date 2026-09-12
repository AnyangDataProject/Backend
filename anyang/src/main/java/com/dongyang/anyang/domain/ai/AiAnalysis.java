package com.dongyang.anyang.domain.ai;

import com.dongyang.anyang.domain.image.ReportImage;
import com.dongyang.anyang.domain.report.Report;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.cglib.core.Local;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name="ai_analyses")
public class AiAnalysis {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "report_id", nullable = false)
    private Report report;

    @Column(name = "model_name", nullable = false, length = 100)
    private String modelName;

    @Column(name = "model_version", length = 50)
    private String modelVersion;

    @Column(name = "result_image_url", length = 1000)
    private String resultImageUrl;

    @Column(name = "analyzed_at", nullable = false)
    private LocalDateTime analyzedAt;

    @OneToMany(
            mappedBy = "aiAnalysis",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @Builder.Default
    private List<AiDetection> detections = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        if(analyzedAt == null){
            analyzedAt = LocalDateTime.now();
        }
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "report_image_id")
    private ReportImage reportImage;

}
