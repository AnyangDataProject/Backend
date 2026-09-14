package com.dongyang.anyang.domain.inspection;

import com.dongyang.anyang.domain.report.Report;
import com.dongyang.anyang.domain.report.ReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InspectionClusterService {

    private final InspectionClusterRepository inspectionClusterRepository;
    private final ReportRepository reportRepository;

    public List<InspectionClusterResponseDto> getAllClusters() {

        return inspectionClusterRepository.findAll()
                .stream()
                .map(InspectionClusterResponseDto::from)
                .toList();
    }

    public InspectionClusterResponseDto getCluster(Integer cluster) {

        InspectionCluster entity =
                inspectionClusterRepository.findByCluster(cluster)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "해당 클러스터가 존재하지 않습니다. cluster=" + cluster
                                )
                        );

        return InspectionClusterResponseDto.from(entity);
    }
    public List<MonthlyDamageResponseDto> getMonthlyDamage(
            Integer cluster,
            int year
    ) {

        inspectionClusterRepository.findByCluster(cluster)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "해당 클러스터가 존재하지 않습니다. cluster=" + cluster
                        )
                );

        LocalDateTime start =
                LocalDateTime.of(year, 1, 1, 0, 0);

        LocalDateTime end =
                LocalDateTime.of(year + 1, 1, 1, 0, 0);

        List<Report> reports =
                reportRepository.findByInspectionClusterClusterAndCreatedAtBetween(
                        cluster,
                        start,
                        end
                );

        int[] monthlyCounts = new int[12];

        for (Report report : reports) {

            int month =
                    report.getCreatedAt().getMonthValue();

            monthlyCounts[month - 1]++;
        }

        List<MonthlyDamageResponseDto> result =
                new ArrayList<>();

        for (int month = 1; month <= 12; month++) {

            result.add(
                    MonthlyDamageResponseDto.of(
                            month,
                            monthlyCounts[month - 1]
                    )
            );
        }

        return result;
    }

    public InspectionCluster findClusterByLocation(
            BigDecimal latitude,
            BigDecimal longitude
    ) {
        if (latitude == null || longitude == null) {
            return null;
        }

        double reportLat = latitude.doubleValue();
        double reportLng = longitude.doubleValue();

        List<InspectionCluster> clusters =
                inspectionClusterRepository.findAll();

        InspectionCluster nearestCluster = null;
        double nearestDistance = Double.MAX_VALUE;

        for (InspectionCluster cluster : clusters) {

            if (cluster.getLatitude() == null ||
                    cluster.getLongitude() == null) {
                continue;
            }

            double distance = calculateDistance(
                    reportLat,
                    reportLng,
                    cluster.getLatitude(),
                    cluster.getLongitude()
            );

            if (distance < nearestDistance) {
                nearestDistance = distance;
                nearestCluster = cluster;
            }
        }

        // DBSCAN 기준 반경 150m
        if (nearestCluster != null && nearestDistance <= 150) {
            return nearestCluster;
        }

        return null;
    }

    private double calculateDistance(
            double lat1,
            double lon1,
            double lat2,
            double lon2
    ) {

        final int EARTH_RADIUS = 6371000;

        double lat1Rad = Math.toRadians(lat1);
        double lat2Rad = Math.toRadians(lat2);

        double deltaLat =
                Math.toRadians(lat2 - lat1);

        double deltaLng =
                Math.toRadians(lon2 - lon1);

        double a =
                Math.sin(deltaLat / 2) * Math.sin(deltaLat / 2)
                        +
                        Math.cos(lat1Rad)
                                * Math.cos(lat2Rad)
                                * Math.sin(deltaLng / 2)
                                * Math.sin(deltaLng / 2);

        double c =
                2 * Math.atan2(
                        Math.sqrt(a),
                        Math.sqrt(1 - a)
                );

        return EARTH_RADIUS * c;
    }

    @Transactional
    public void updateClusterAfterReport(
            InspectionCluster cluster,
            int detectedDamageCount,
            int detectedPotholeCount
    ) {
        cluster.updateAfterReport(
                detectedDamageCount,
                detectedPotholeCount
        );

        inspectionClusterRepository.save(cluster);
    }
}