package com.dongyang.anyang.domain.inspection;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/inspection-clusters")
@RequiredArgsConstructor
public class InspectionClusterController {

    private final InspectionClusterService inspectionClusterService;

    @GetMapping
    public ResponseEntity<List<InspectionClusterResponseDto>> getAllClusters() {
        return ResponseEntity.ok(
                inspectionClusterService.getAllClusters()
        );
    }

    @GetMapping("/{cluster}")
    public ResponseEntity<InspectionClusterResponseDto> getCluster(
            @PathVariable Integer cluster
    ) {
        return ResponseEntity.ok(
                inspectionClusterService.getCluster(cluster)
        );
    }

    // 특정 클러스터의 월별 신고/파손 이력
    @GetMapping("/{cluster}/monthly-damage")
    public ResponseEntity<List<MonthlyDamageResponseDto>> getMonthlyDamage(
            @PathVariable Integer cluster,
            @RequestParam(defaultValue = "2026") int year
    ) {
        return ResponseEntity.ok(
                inspectionClusterService.getMonthlyDamage(cluster, year)
        );
    }
}