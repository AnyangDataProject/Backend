package com.dongyang.anyang.domain.inspection;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class InspectionClusterResponseDto {

    private Long id;
    private Integer cluster;
    private String roadAddress;
    private Integer linkCount;
    private Integer damageCount;
    private Double potholeRatio;
    private Double avgSpeed;
    private Double avgTravelTime;
    private Double congestionRatio;
    private Double delayCongestionRatio;
    private Double trafficDataCoverage;
    private Double damageScore;
    private Double trafficScore;
    private Double priorityScore;
    private Integer priorityRank;
    private String priorityGrade;
    private Double latitude;
    private Double longitude;
    private Integer reportCount;
    private Double reportScore;
    private Double currentPriorityScore;

    public static InspectionClusterResponseDto from(
            InspectionCluster cluster
    ) {
        return InspectionClusterResponseDto.builder()
                .id(cluster.getId())
                .cluster(cluster.getCluster())
                .roadAddress(cluster.getRoadAddress())
                .linkCount(cluster.getLinkCount())
                .damageCount(cluster.getDamageCount())
                .potholeRatio(cluster.getPotholeRatio())
                .avgSpeed(cluster.getAvgSpeed())
                .avgTravelTime(cluster.getAvgTravelTime())
                .congestionRatio(cluster.getCongestionRatio())
                .delayCongestionRatio(cluster.getDelayCongestionRatio())
                .trafficDataCoverage(cluster.getTrafficDataCoverage())
                .damageScore(cluster.getDamageScore())
                .trafficScore(cluster.getTrafficScore())
                .priorityScore(cluster.getPriorityScore())
                .priorityRank(cluster.getPriorityRank())
                .priorityGrade(cluster.getPriorityGrade())
                .latitude(cluster.getLatitude())
                .longitude(cluster.getLongitude())
                .reportCount(cluster.getReportCount())
                .reportScore(cluster.getReportScore())
                .currentPriorityScore(cluster.getCurrentPriorityScore())
                .build();
    }
}