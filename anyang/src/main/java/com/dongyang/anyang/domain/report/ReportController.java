package com.dongyang.anyang.domain.report;

import com.dongyang.anyang.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Controller
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
}
