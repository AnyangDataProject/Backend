package com.dongyang.anyang.domain.inquiry;

import com.dongyang.anyang.domain.inquiry.file.InquiryFile;
import com.dongyang.anyang.domain.inquiry.file.InquiryFileRepository;
import com.dongyang.anyang.domain.user.Role;
import com.dongyang.anyang.domain.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class InquiryService {
    private final InquiryRepository inquiryRepository;
    private final InquiryFileRepository inquiryFileRepository;

    public Long create(User user, InquiryDto.CreateRequest request) {
        Inquiry inquiry = Inquiry.builder()
                .user(user)
                .inquiryType(request.inquiryType())
                .title(request.title())
                .content(request.content())
                .email(request.email())
                .build();

        return inquiryRepository.save(inquiry).getId();
    }

    @Transactional(readOnly = true)
    public Page<InquiryDto.ListResponse> getMyInquiries(User user, Pageable pageable) {
        return inquiryRepository.findByUserId(user.getId(), pageable)
                .map(this::toListResponse);
    }

    @Transactional(readOnly = true)
    public Page<InquiryDto.ListResponse> getAllInquiries(Pageable pageable) {
        return inquiryRepository.findAll(pageable)
                .map(this::toListResponse);
    }

    @Transactional(readOnly = true)
    public InquiryDto.DetailResponse getDetail(Long id, User currentUser) {
        Inquiry inquiry = findByIdOrThrow(id);
        validateAccess(inquiry, currentUser);
        return toDetailResponse(inquiry);
    }

    public void answer(Long id, User admin, InquiryDto.AnswerRequest request) {
        Inquiry inquiry = findByIdOrThrow(id);
        inquiry.answer(admin, request.answer());
    }

    public void update(Long id, User currentUSer, InquiryDto.UpdateRequest request) {
        Inquiry inquiry = findByIdOrThrow(id);
        validateAccess(inquiry, currentUSer);

        if(inquiry.getStatus() == InquiryStatus.ANSWERED) {
            throw new IllegalStateException("이미 답변이 완료된 문의는 수정할 수 없습니다.");
        }

        inquiry.update(request.title(), request.content());
    }

    public void delete(Long id, User currentUser) {
        Inquiry inquiry = findByIdOrThrow(id);
        validateAccess(inquiry, currentUser);
        inquiryRepository.delete(inquiry);
    }

    public Inquiry findByIdOrThrow(Long id) {
        return inquiryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 문의입니다."));
    }

    private void validateAccess(Inquiry inquiry, User currentUser) {
        boolean isOwner = inquiry.getUser().getId().equals(currentUser.getId());
        boolean isAdmin = currentUser.getRole() == Role.ADMIN;

        if(!isOwner && !isAdmin) {
            throw new AccessDeniedException("본인 문의만 접근할 수 있습니다.");
        }
    }

    private InquiryDto.ListResponse toListResponse(Inquiry inquiry) {
        return new InquiryDto.ListResponse(
                inquiry.getId(),
                inquiry.getInquiryType(),
                inquiry.getTitle(),
                inquiry.getStatus(),
                inquiry.getCreatedAt()
        );
    }

    private InquiryDto.DetailResponse toDetailResponse(Inquiry inquiry) {
        List<String> fileUrls = inquiryFileRepository.findByInquiryId(inquiry.getId())
                .stream()
                .map(InquiryFile::getFileUrl)
                .collect(Collectors.toList());

        return new InquiryDto.DetailResponse(
                inquiry.getId(),
                inquiry.getInquiryType(),
                inquiry.getTitle(),
                inquiry.getContent(),
                inquiry.getEmail(),
                inquiry.getStatus(),
                inquiry.getAnswer(),
                inquiry.getAnsweredBy() != null ? inquiry.getAnsweredBy().getName() : null,
                inquiry.getAnsweredAt(),
                inquiry.getCreatedAt(),
                inquiry.getUpdatedAt(),
                fileUrls
        );
    }
}
