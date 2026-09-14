package com.dongyang.anyang.domain.inspection;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "inspection_clusters")
@Getter
@NoArgsConstructor
public class InspectionCluster {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer cluster;

    @Column(name = "road_address")
    private String roadAddress;

    @Column(name = "link_count")
    private Integer linkCount;

    @Column(name = "damage_count")
    private Integer damageCount;

    @Column(name = "pothole_ratio")
    private Double potholeRatio;

    @Column(name = "avg_speed")
    private Double avgSpeed;

    @Column(name = "avg_travel_time")
    private Double avgTravelTime;

    @Column(name = "congestion_ratio")
    private Double congestionRatio;

    @Column(name = "delay_congestion_ratio")
    private Double delayCongestionRatio;

    @Column(name = "traffic_data_coverage")
    private Double trafficDataCoverage;

    @Column(name = "damage_score")
    private Double damageScore;

    @Column(name = "traffic_score")
    private Double trafficScore;

    @Column(name = "priority_score")
    private Double priorityScore;

    @Column(name = "priority_rank")
    private Integer priorityRank;

    @Column(name = "priority_grade")
    private String priorityGrade;

    private Double latitude;

    private Double longitude;


    @Column(name = "report_count")
    private Integer reportCount = 0;

    @Column(name = "report_score")
    private Double reportScore = 0.0;

    @Column(name = "current_priority_score")
    private Double currentPriorityScore;

    public void updateReportInfo(
            int reportCount,
            double reportScore,
            double currentPriorityScore
    ) {
        this.reportCount = reportCount;
        this.reportScore = reportScore;
        this.currentPriorityScore = currentPriorityScore;
    }

    public void updateDamageCount(int damageCount) {
        this.damageCount = damageCount;
    }

    public void updateAfterReport(
            int detectedDamageCount,
            int detectedPotholeCount
    ) {
        int currentReportCount =
                this.reportCount != null ? this.reportCount : 0;

        int currentDamageCount =
                this.damageCount != null ? this.damageCount : 0;

        double currentPotholeRatio =
                this.potholeRatio != null ? this.potholeRatio : 0.0;

        // 시민 신고 수
        this.reportCount = currentReportCount + 1;

        // AI가 탐지한 전체 파손 수
        this.damageCount =
                currentDamageCount + detectedDamageCount;

        // 기존 포트홀 개수 추정
        double currentPotholeCount = 0.0;

        if (currentDamageCount > 0) {
            currentPotholeCount =
                    currentDamageCount * currentPotholeRatio / 100.0;
        }

        // 새 포트홀 추가
        double newPotholeCount =
                currentPotholeCount + detectedPotholeCount;

        // 포트홀 비율 재계산
        if (this.damageCount > 0) {
            this.potholeRatio =
                    (newPotholeCount / this.damageCount) * 100.0;
        } else {
            this.potholeRatio = 0.0;
        }

        // 신고 점수
        this.reportScore =
                (double) Math.min(this.reportCount, 10);

        // 현재 우선순위
        double basePriorityScore =
                this.priorityScore != null
                        ? this.priorityScore
                        : 0.0;

        this.currentPriorityScore =
                basePriorityScore * 0.9
                        + this.reportScore;
    }
}