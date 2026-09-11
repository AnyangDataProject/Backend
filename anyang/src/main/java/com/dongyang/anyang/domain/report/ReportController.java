package com.dongyang.anyang.domain.report;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @PostMapping("/api/report")
    public ResponseEntity<Long> createReport(@RequestPart("report") ReportDto dto, @RequestPart("images") List<MultipartFile> images){

        return ResponseEntity.ok( reportService.create(dto, images)
        );
    }
}
