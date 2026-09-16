package com.dongyang.anyang.domain.report;

import com.dongyang.anyang.domain.ai.*;
import com.dongyang.anyang.domain.image.ReportImage;
import com.dongyang.anyang.domain.image.ReportImageRepository;
import com.dongyang.anyang.domain.image.ReportImageResponseDto;
import com.dongyang.anyang.domain.inspection.InspectionCluster;
import com.dongyang.anyang.domain.inspection.InspectionClusterService;
import com.dongyang.anyang.domain.user.User;
import com.dongyang.anyang.domain.user.UserRepository;
import com.dongyang.anyang.s3.S3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Base64;
import java.util.List;
import java.util.OptionalDouble;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final ReportRepository reportRepository;
    private final ReportImageRepository reportImageRepository;
    private final S3Service s3Service;
    private final AiAnalysisService aiAnalysisService;
    private final AiAnalysisRepository aiAnalysisRepository;
    private final AiDetectionRepository aiDetectionRepository;
    private final UserRepository userRepository;
    private final InspectionClusterService inspectionClusterService;

    @Transactional
    public Long create(ReportDto dto, List<MultipartFile> images, Long userId) {
        try {
            int detectedDamageCount = 0;
            int detectedPotholeCount = 0;

            User user = userRepository.findById(userId).orElseThrow(() ->
                            new RuntimeException("사용자를 찾을 수 없음")
                    );

            Report report = Report.builder()
                    .user(user)
                    .description(dto.getDetail())
                    .latitude(dto.getLatitude())
                    .longitude(dto.getLongitude())
                    .address(dto.getAddress())
                    .damageType(dto.getDamageType())
                    .severity(
                            Report.Severity.valueOf(
                                    dto.getSeverity().toUpperCase()
                            )
                    )
                    .status(Report.ReportStatus.RECEIVED)
                    .build();

            InspectionCluster cluster =
                    inspectionClusterService.findClusterByLocation(
                            report.getLatitude(),
                            report.getLongitude()
                    );

            report.setInspectionCluster(cluster);

            System.out.println( "신고 위치: "+ report.getLatitude() + ", " + report.getLongitude());
            System.out.println("찾은 클러스터: "+ (cluster != null ? cluster.getId() : "NULL"));
            if (cluster != null) {System.out.println( "클러스터 번호: " + cluster.getCluster());}

            Report savedReport = reportRepository.save(report);

            for (MultipartFile image : images) {
                String imageBase64 =
                        Base64.getEncoder()
                                .encodeToString(image.getBytes());

                String imageUrl = s3Service.uploadFile(imageBase64);

                ReportImage reportImage = new ReportImage();

                reportImage.setReport(savedReport);
                reportImage.setImageUrl(imageUrl);
                reportImage.setImageType(
                        image.getContentType()
                );

                ReportImage savedImage = reportImageRepository.save(reportImage);
                AiResponseDto aiResponse = aiAnalysisService.predict(image);


                if (aiResponse.getDetections() != null) {
                    detectedDamageCount += aiResponse.getDetections().size();
                    for (AiResponseDto.Detections detection : aiResponse.getDetections()) {
                        if ("pothole".equalsIgnoreCase(detection.getClassName())) {
                            detectedPotholeCount++;
                        }
                    }
                }

                String base64ResultImage = aiResponse.getResultImage();
                byte[] resultImageBytes = Base64.getDecoder().decode(base64ResultImage);
                String resultImageUrl =s3Service.uploadFile(
                                resultImageBytes,
                                "image/jpeg"
                        );

                AiAnalysis analysis = AiAnalysis.builder()
                                .report(savedReport)
                                .modelName("yolov8s")
                                .modelVersion("rdd2022")
                                .resultImageUrl(resultImageUrl)
                                .reportImage(savedImage)
                                .build();

                AiAnalysis savedAnalysis = aiAnalysisRepository.save(analysis);


                if (aiResponse.getDetections() != null) {

                    for (AiResponseDto.Detections detections
                            : aiResponse.getDetections()) {

                        AiDetection aiDetection =
                                AiDetection.builder()
                                        .aiAnalysis(savedAnalysis)
                                        .className(
                                                detections.getClassName()
                                        )
                                        .confidence(
                                                BigDecimal.valueOf(
                                                        detections.getConfidence()
                                                )
                                        )
                                        .bboxX(
                                                BigDecimal.valueOf(
                                                        detections.getBboxX()
                                                )
                                        )
                                        .bboxY(
                                                BigDecimal.valueOf(
                                                        detections.getBboxY()
                                                )
                                        )
                                        .bboxWidth(
                                                BigDecimal.valueOf(
                                                        detections.getBboxWidth()
                                                )
                                        )
                                        .bboxHeight(
                                                BigDecimal.valueOf(
                                                        detections.getBboxHeight()
                                                )
                                        )
                                        .build();

                        aiDetectionRepository.save(
                                aiDetection
                        );
                    }
                }
            }

            if (cluster != null) {
                inspectionClusterService
                        .updateClusterAfterReport(
                                cluster,
                                detectedDamageCount,
                                detectedPotholeCount
                        );
            }

            return savedReport.getId();

        } catch (IOException e) {

            throw new RuntimeException(
                    "이미지 처리 중 오류 발생",
                    e
            );
        }
    }

    @Transactional(readOnly = true)
    public List<ReportResponseDto> getMyReports(Long userId) {

        List<Report> reports = reportRepository.findByUserId(userId);
        return reports.stream().map(this::toReportResponseDto).toList();
    }

    @Transactional(readOnly = true)
    public List<ReportResponseDto> getAllReports() {
        List<Report> reports = reportRepository.findAll();

        return reports.stream().map(this::toReportResponseDto).toList();
    }


    @Transactional(readOnly = true)
    public List<ReportResponseDto> getUnclassifiedReports() {

        List<Report> reports = reportRepository.findByInspectionClusterIsNull();
        return reports.stream().map(this::toReportResponseDto).toList();
    }

    @Transactional
    public void updateStatus(Long reportId, Report.ReportStatus status) {

        Report report = reportRepository.findById(reportId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "해당 신고를 찾을 수 없습니다. reportId=" + reportId
                        )
                );

        report.setStatus(status);
    }

    private ReportResponseDto toReportResponseDto(Report report){

        List<AiAnalysis> aiResults = aiAnalysisRepository.findByReportId(report.getId());
        List<ReportImage> reportImages = reportImageRepository.findByReportId(report.getId());
        List<ReportImageResponseDto> resultImages = reportImages.stream().map(image -> {
                    AiAnalysis aiAnalysis =
                            aiAnalysisRepository
                                    .findByReportImageId(
                                            image.getId()
                                    )
                                    .orElse(null);

                    return ReportImageResponseDto
                            .builder()
                            .id(image.getId())
                            .imageUrl(
                                    image.getImageUrl()
                            )
                            .resultImageUrl(
                                    aiAnalysis != null
                                            ? aiAnalysis
                                            .getResultImageUrl()
                                            : null
                            )
                            .build();
                })
                .toList();

        List<AiDetection> detections = aiResults.stream().flatMap(analysis -> aiDetectionRepository.findByAiAnalysisId(analysis.getId())
                .stream())
                .toList();
//        Double aiConfidence = detections.stream().map(AiDetection::getConfidence)
//                .mapToDouble(BigDecimal::doubleValue).max().orElse(null);
        OptionalDouble maxConfidence = detections.stream().map(AiDetection::getConfidence)
                .mapToDouble(BigDecimal::doubleValue).max();
        Double aiConfidence = maxConfidence.isPresent()? maxConfidence.getAsDouble() : null;
        List<AiDetectionSummaryDto> aiDetections = detections.stream().collect(
                Collectors.groupingBy(AiDetection::getClassName, Collectors.counting())
        ).entrySet().stream().map(entry -> AiDetectionSummaryDto.builder().className(entry.getKey())
                .detectionsCount(entry.getValue()).build()).toList();

        return ReportResponseDto.builder()
                .id(report.getId())
                .type(report.getDamageType())
                .userName(report.getUser().getName())
                .severity(
                        report.getSeverity()
                                .name()
                )
                .status(
                        report.getStatus()
                                .name()
                )
                .address(report.getAddress())
                .latitude(report.getLatitude())
                .longitude(report.getLongitude())
                .reportedAt(report.getCreatedAt())
                .description(report.getDescription())
                .aiConfidence(aiConfidence)
                .aiDetections(aiDetections)
                .images(resultImages)

                // 클러스터 ID
                .inspectionClusterId(
                        report.getInspectionCluster() != null
                                ? report.getInspectionCluster().getId()
                                : null
                )

                .build();

    }
}

