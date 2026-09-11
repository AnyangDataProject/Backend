package com.dongyang.anyang.domain.report;

import com.dongyang.anyang.domain.ai.*;
import com.dongyang.anyang.domain.image.ReportImage;
import com.dongyang.anyang.domain.image.ReportImageRepository;
import com.dongyang.anyang.domain.image.ReportImageResponseDto;
import com.dongyang.anyang.s3.S3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Base64;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReportService {
    private final ReportRepository reportRepository;
    private final ReportImageRepository reportImageRepository;
    private final S3Service s3Service;
    private final AiAnalysisService aiAnalysisService;
    private final AiAnalysisRepository aiAnalysisRepository;
    private final AiDetectionRepository aiDetectionRepository;

    public Long create(ReportDto dto, List<MultipartFile> images) {
        try {
            Report report = Report.builder()
                    .description(dto.getDetail())
                    .latitude(dto.getLatitude())
                    .longitude(dto.getLongitude())
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

                ReportImage savedImage = reportImageRepository.save(reportImage);

                AiResponseDto aiResponse = aiAnalysisService.predict(image);
                String base64ResultImage = aiResponse.getResultImage();
                byte[] resultImageBytes = Base64.getDecoder().decode(base64ResultImage);
                String resultImageUrl = s3Service.uploadFile(resultImageBytes, "image/jpeg");

                AiAnalysis analysis = AiAnalysis.builder().report(savedReport)
                        .modelName("yolov8s").modelVersion("rdd2022").resultImageUrl(resultImageUrl)
                        .reportImage(savedImage)
                        .build();

                AiAnalysis savedAnalysis = aiAnalysisRepository.save(analysis);

                for(AiResponseDto.Detections detections: aiResponse.getDetections()){
                    AiDetection aiDetection = AiDetection.builder()
                            .aiAnalysis(savedAnalysis).className(detections.getClassName())
                            .confidence(BigDecimal.valueOf(detections.getConfidence()))
                            .bboxX(BigDecimal.valueOf(detections.getBboxX()))
                            .bboxY(BigDecimal.valueOf(detections.getBboxY()))
                            .bboxWidth(BigDecimal.valueOf(detections.getBboxWidth()))
                            .bboxHeight(BigDecimal.valueOf(detections.getBboxHeight()))
                            .build();

                    aiDetectionRepository.save(aiDetection);

                }

            }
            return savedReport.getId();
        }
        catch(IOException e){
            throw new RuntimeException("이미지 처리 중 오류 발생", e);
        }
    }

    public List<ReportResponseDto> getMyReports(){
        List<Report> reports = reportRepository.findAll();


        return reports.stream().map(
                report -> {
                    List<AiAnalysis> aiResults = aiAnalysisRepository.findByReportId(report.getId());
                    List<ReportImage> reportImages = reportImageRepository.findByReportId(report.getId());
                    List<ReportImageResponseDto> resultImages = reportImages.stream().map(image -> {
                        AiAnalysis aiAnalysis = aiAnalysisRepository.findByReportImageId(image.getId()).orElse(null);
                        return ReportImageResponseDto.builder().id(image.getId())
                                .imageUrl(image.getImageUrl())
                                .resultImageUrl(
                                        aiAnalysis != null ? aiAnalysis.getResultImageUrl() : null)
                                .build();

                    })
                            .toList();


                    Double aiConfidence = null;
                    if(!aiResults.isEmpty()){
                        AiAnalysis analysis = aiResults.get(0);
                        List<AiDetection> detections = aiDetectionRepository.findByAiAnalysisId(analysis.getId());
                        if(!detections.isEmpty()){
                            aiConfidence = detections.stream()
                                    .map(detection -> detection.getConfidence())
                                    .mapToDouble(confidence -> confidence.doubleValue())
                                    .max()
                                    .orElse(0.0);

                        }
                    }

//                    List<ReportImageResponseDto> images = reportImages.stream()
//                            .map(image -> ReportImageResponseDto.builder()
//                                    .id(image.getId())
//                                    .imageUrl(image.getImageUrl())
//                                    .build())
//                            .toList();


                return ReportResponseDto.builder()
                        .id(report.getId()).type(report.getDamageType())
                        .severity(report.getSeverity().name().toLowerCase())
                        .status(report.getStatus().name().toLowerCase())
                        .address(report.getAddress())
                        .latitude(report.getLatitude())
                        .longitude(report.getLongitude())
                        .reportedAt(report.getCreatedAt())
                        .description(report.getDescription())
                        .aiConfidence(aiConfidence)
                        .images(resultImages)
                        .build();

                })
                .toList();


    }
}
