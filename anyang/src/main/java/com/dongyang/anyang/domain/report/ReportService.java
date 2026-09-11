package com.dongyang.anyang.domain.report;

import com.dongyang.anyang.domain.image.ReportImage;
import com.dongyang.anyang.domain.image.ReportImageRepository;
import com.dongyang.anyang.s3.S3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Base64;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReportService {
    private final ReportRepository reportRepository;
    private final ReportImageRepository reportImageRepository;
    private final S3Service s3Service;

    public Long create(ReportDto dto, List<MultipartFile> images) {
        try {
            Report report = Report.builder()
                    .description(dto.getDetail())
                    .latitude(dto.getLatitude())
                    .longtitude(dto.getLongtitude())
                    .address(dto.getAddress())
                    .damageType(dto.getDamageType())
                    .severity(Report.Severity.valueOf(dto.getSeverity().toUpperCase()))
                    .status(Report.ReportStatus.RECEIVED)
                    .build();
            // report.setUser(user);

            Report savedReport = reportRepository.save(report);

            for (MultipartFile image : images) {

                String imageBase64 = Base64.getEncoder().encodeToString(image.getBytes());

                String imageUrl = s3Service.uploadFile(imageBase64);

                ReportImage reportImage = new ReportImage();
                reportImage.setReport(savedReport);
                reportImage.setImageUrl(imageUrl);
                reportImage.setImageType(image.getContentType());

                reportImageRepository.save(reportImage);
            }
            return savedReport.getId();
        }
        catch(IOException e){
            throw new RuntimeException("이미지 처리 중 오류 발생", e);
        }
    }
}
