package com.dongyang.anyang.domain.report;

import com.dongyang.anyang.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @PostMapping("/api/report")
    public ResponseEntity<Long> createReport(@RequestPart("report") ReportDto dto,
                                             @RequestPart("images") List<MultipartFile> images,
                                             @AuthenticationPrincipal CustomUserDetails userDetails){
        Long userId = userDetails.getId();
        return ResponseEntity.ok( reportService.create(dto, images, userId
                )
        );
    }

    @GetMapping("/api/report/my")
    public ResponseEntity<List<ReportResponseDto>> getMyReports(@AuthenticationPrincipal CustomUserDetails userDetails){
        Long userId = userDetails.getId();
        return ResponseEntity.ok(reportService.getMyReports(userId));
    }

    @GetMapping("/api/admin/report")
    public ResponseEntity<List<ReportResponseDto>> getAllReports(){
        return ResponseEntity.ok(reportService.getAllReports());
    }

    @GetMapping("/api/admin/report/unclassified")
    public ResponseEntity<List<ReportResponseDto>> getUnclassifiedReports() {
        return ResponseEntity.ok(reportService.getUnclassifiedReports());
    }

    @PatchMapping("/api/admin/report/{reportId}/status")
    public ResponseEntity<Void> updateReportStatus(
            @PathVariable Long reportId,
            @RequestParam Report.ReportStatus status
    ) {
        reportService.updateStatus(reportId, status);
        return ResponseEntity.ok().build();
    }
}
