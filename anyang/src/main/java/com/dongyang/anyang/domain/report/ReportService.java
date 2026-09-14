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
    public Long create(
            ReportDto dto,
            List<MultipartFile> images,
            Long userId
    ) {
        try {
            // AI가 탐지한 전체 파손 개수
            int detectedDamageCount = 0;

            // AI가 탐지한 포트홀 개수
            int detectedPotholeCount = 0;

            User user = userRepository.findById(userId)
                    .orElseThrow(() ->
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

            // 신고 위치와 가장 가까운 기존 클러스터 찾기
            InspectionCluster cluster =
                    inspectionClusterService.findClusterByLocation(
                            report.getLatitude(),
                            report.getLongitude()
                    );

            // 클러스터가 없으면 NULL로 저장
            report.setInspectionCluster(cluster);

            System.out.println(
                    "신고 위치: "
                            + report.getLatitude()
                            + ", "
                            + report.getLongitude()
            );

            System.out.println(
                    "찾은 클러스터: "
                            + (cluster != null
                            ? cluster.getId()
                            : "NULL")
            );

            if (cluster != null) {
                System.out.println(
                        "클러스터 번호: "
                                + cluster.getCluster()
                );
            }

            Report savedReport =
                    reportRepository.save(report);

            // 신고 이미지 처리
            for (MultipartFile image : images) {

                // 원본 이미지 S3 저장
                String imageBase64 =
                        Base64.getEncoder()
                                .encodeToString(image.getBytes());

                String imageUrl =
                        s3Service.uploadFile(imageBase64);

                ReportImage reportImage =
                        new ReportImage();

                reportImage.setReport(savedReport);
                reportImage.setImageUrl(imageUrl);
                reportImage.setImageType(
                        image.getContentType()
                );

                ReportImage savedImage =
                        reportImageRepository.save(reportImage);

                // AI 분석
                AiResponseDto aiResponse =
                        aiAnalysisService.predict(image);

                // AI 탐지 결과 누적
                if (aiResponse.getDetections() != null) {

                    // 전체 파손 개수
                    detectedDamageCount +=
                            aiResponse.getDetections().size();

                    // 포트홀 개수
                    for (AiResponseDto.Detections detection
                            : aiResponse.getDetections()) {

                        if ("pothole".equalsIgnoreCase(
                                detection.getClassName()
                        )) {
                            detectedPotholeCount++;
                        }
                    }
                }

                // AI 결과 이미지 S3 저장
                String base64ResultImage =
                        aiResponse.getResultImage();

                byte[] resultImageBytes =
                        Base64.getDecoder()
                                .decode(base64ResultImage);

                String resultImageUrl =
                        s3Service.uploadFile(
                                resultImageBytes,
                                "image/jpeg"
                        );

                // AI 분석 결과 저장
                AiAnalysis analysis =
                        AiAnalysis.builder()
                                .report(savedReport)
                                .modelName("yolov8s")
                                .modelVersion("rdd2022")
                                .resultImageUrl(resultImageUrl)
                                .reportImage(savedImage)
                                .build();

                AiAnalysis savedAnalysis =
                        aiAnalysisRepository.save(analysis);

                // AI detection 상세 저장
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

            /*
             * 기존 48개 클러스터 중 하나에 매칭된 경우에만
             * 해당 클러스터 정보를 갱신한다.
             *
             * 매칭되지 않은 경우:
             * inspection_cluster_id = NULL
             * → 미분류 신고
             */
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
    public List<ReportResponseDto> getMyReports(
            Long userId
    ) {

        List<Report> reports =
                reportRepository.findByUserId(userId);

        return reports.stream()
                .map(report -> {

                    List<AiAnalysis> aiResults =
                            aiAnalysisRepository
                                    .findByReportId(report.getId());

                    List<ReportImage> reportImages =
                            reportImageRepository
                                    .findByReportId(report.getId());

                    List<ReportImageResponseDto> resultImages =
                            reportImages.stream()
                                    .map(image -> {

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

                    Double aiConfidence = null;

                    if (!aiResults.isEmpty()) {

                        AiAnalysis analysis =
                                aiResults.get(0);

                        List<AiDetection> detections =
                                aiDetectionRepository
                                        .findByAiAnalysisId(
                                                analysis.getId()
                                        );

                        if (!detections.isEmpty()) {

                            aiConfidence =
                                    detections.stream()
                                            .map(AiDetection::getConfidence)
                                            .mapToDouble(
                                                    BigDecimal::doubleValue
                                            )
                                            .max()
                                            .orElse(0.0);
                        }
                    }

                    return ReportResponseDto.builder()
                            .id(report.getId())
                            .type(report.getDamageType())
                            .userName(report.getUser().getName())
                            .severity(
                                    report.getSeverity()
                                            .name()
                                            .toLowerCase()
                            )
                            .status(
                                    report.getStatus()
                                            .name()
                                            .toLowerCase()
                            )
                            .address(report.getAddress())
                            .latitude(report.getLatitude())
                            .longitude(report.getLongitude())
                            .reportedAt(report.getCreatedAt())
                            .description(report.getDescription())
                            .aiConfidence(aiConfidence)
                            .images(resultImages)

                            // 클러스터 ID
                            .inspectionClusterId(
                                    report.getInspectionCluster() != null
                                            ? report.getInspectionCluster().getId()
                                            : null
                            )

                            .build();
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ReportResponseDto> getAllReports() {

        List<Report> reports =
                reportRepository.findAll();

        return reports.stream()
                .map(report -> {

                    List<AiAnalysis> aiResults =
                            aiAnalysisRepository
                                    .findByReportId(report.getId());

                    List<ReportImage> reportImages =
                            reportImageRepository
                                    .findByReportId(report.getId());

                    List<ReportImageResponseDto> resultImages =
                            reportImages.stream()
                                    .map(image -> {

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

                    Double aiConfidence = null;

                    if (!aiResults.isEmpty()) {

                        AiAnalysis analysis =
                                aiResults.get(0);

                        List<AiDetection> detections =
                                aiDetectionRepository
                                        .findByAiAnalysisId(
                                                analysis.getId()
                                        );

                        if (!detections.isEmpty()) {

                            aiConfidence =
                                    detections.stream()
                                            .map(AiDetection::getConfidence)
                                            .mapToDouble(
                                                    BigDecimal::doubleValue
                                            )
                                            .max()
                                            .orElse(0.0);
                        }
                    }

                    return ReportResponseDto.builder()
                            .id(report.getId())
                            .type(report.getDamageType())
                            .userName(report.getUser().getName())
                            .severity(
                                    report.getSeverity()
                                            .name()
                                            .toLowerCase()
                            )
                            .status(
                                    report.getStatus()
                                            .name()
                                            .toLowerCase()
                            )
                            .address(report.getAddress())
                            .latitude(report.getLatitude())
                            .longitude(report.getLongitude())
                            .reportedAt(report.getCreatedAt())
                            .description(report.getDescription())
                            .aiConfidence(aiConfidence)
                            .images(resultImages)

                            // 클러스터 ID
                            .inspectionClusterId(
                                    report.getInspectionCluster() != null
                                            ? report.getInspectionCluster().getId()
                                            : null
                            )

                            .build();
                })
                .toList();
    }

    /**
     * 기존 48개 클러스터에 속하지 않는 미분류 신고
     */
    @Transactional(readOnly = true)
    public List<ReportResponseDto> getUnclassifiedReports() {

        return reportRepository
                .findByInspectionClusterIsNull()
                .stream()
                .map(report -> {

                    List<AiAnalysis> aiResults =
                            aiAnalysisRepository
                                    .findByReportId(report.getId());

                    List<ReportImage> reportImages =
                            reportImageRepository
                                    .findByReportId(report.getId());

                    List<ReportImageResponseDto> resultImages =
                            reportImages.stream()
                                    .map(image -> {

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

                    Double aiConfidence = null;

                    if (!aiResults.isEmpty()) {

                        AiAnalysis analysis =
                                aiResults.get(0);

                        List<AiDetection> detections =
                                aiDetectionRepository
                                        .findByAiAnalysisId(
                                                analysis.getId()
                                        );

                        if (!detections.isEmpty()) {

                            aiConfidence =
                                    detections.stream()
                                            .map(AiDetection::getConfidence)
                                            .mapToDouble(
                                                    BigDecimal::doubleValue
                                            )
                                            .max()
                                            .orElse(0.0);
                        }
                    }

                    return ReportResponseDto.builder()
                            .id(report.getId())
                            .type(report.getDamageType())
                            .userName(report.getUser().getName())
                            .severity(
                                    report.getSeverity()
                                            .name()
                                            .toLowerCase()
                            )
                            .status(
                                    report.getStatus()
                                            .name()
                                            .toLowerCase()
                            )
                            .address(report.getAddress())
                            .latitude(report.getLatitude())
                            .longitude(report.getLongitude())
                            .reportedAt(report.getCreatedAt())
                            .description(report.getDescription())
                            .aiConfidence(aiConfidence)
                            .images(resultImages)
                            .inspectionClusterId(null)
                            .build();
                })
                .toList();
    }
}