package com.dongyang.anyang.domain.inquiry.file;

import com.dongyang.anyang.domain.inquiry.Inquiry;
import com.dongyang.anyang.domain.inquiry.InquiryRepository;
import com.dongyang.anyang.domain.user.User;
import com.dongyang.anyang.s3.S3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class InquiryFileService {
    private final S3Service s3Service;
    private final InquiryRepository inquiryRepository;
    private final InquiryFileRepository inquiryFileRepository;

    public List<String> uploadFiles(Long inquiryId, User currentUser, List<MultipartFile> files) {
        Inquiry inquiry = inquiryRepository.findById(inquiryId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 문의입니다."));

        if(!inquiry.getUser().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("본인 문의에만 파일을 첨부할 수 있습니다.");
        }

        return files.stream()
                .map(file -> uploadOne(inquiry, file))
                .collect(Collectors.toList());
    }

    private String uploadOne(Inquiry inquiry, MultipartFile file) {
        try {
            String fileUrl = s3Service.uploadFile(file.getBytes(), file.getContentType());

            InquiryFile inquiryFile = InquiryFile.builder()
                    .inquiry(inquiry)
                    .fileName(file.getOriginalFilename())
                    .fileUrl(fileUrl)
                    .build();

            inquiryFileRepository.save(inquiryFile);
            return fileUrl;
        } catch (IOException e) {
            throw new RuntimeException("파일 업로드 실패", e);
        }
    }
}
