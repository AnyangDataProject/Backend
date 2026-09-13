package com.dongyang.anyang.domain.inquiry;

import com.dongyang.anyang.domain.inquiry.file.InquiryFileService;
import com.dongyang.anyang.domain.user.Role;
import com.dongyang.anyang.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/inquiries")
@RequiredArgsConstructor
public class InquiryController {
    private final InquiryService inquiryService;
    private final InquiryFileService inquiryFileService;

    @PostMapping
    public ResponseEntity<Long> create(@RequestBody InquiryDto.CreateRequest request,
                                       @AuthenticationPrincipal CustomUserDetails userDetails) {
        Long id = inquiryService.create(userDetails.getUser(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(id);
    }

    @GetMapping
    public ResponseEntity<Page<InquiryDto.ListResponse>> getList(Pageable pageable,
                                                                 @AuthenticationPrincipal CustomUserDetails userDetails) {
        boolean isAdmin = userDetails.getUser().getRole() == Role.ADMIN;
        Page<InquiryDto.ListResponse> result = isAdmin
                ? inquiryService.getAllInquiries(pageable)
                : inquiryService.getMyInquiries(userDetails.getUser(), pageable);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    public ResponseEntity<InquiryDto.DetailResponse> getDetail(@PathVariable Long id,
                                                             @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(inquiryService.getDetail(id, userDetails.getUser()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> update(@PathVariable Long id,
                                       @RequestBody InquiryDto.UpdateRequest request,
                                       @AuthenticationPrincipal CustomUserDetails userDetails) {
        inquiryService.update(id, userDetails.getUser(), request);
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}/answer")
    public ResponseEntity<Void> answer(@PathVariable Long id,
                                       @RequestBody InquiryDto.AnswerRequest request,
                                       @AuthenticationPrincipal CustomUserDetails userDetails) {
        inquiryService.answer(id, userDetails.getUser(), request);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id,
                                       @AuthenticationPrincipal CustomUserDetails userDetails) {
        inquiryService.delete(id, userDetails.getUser());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/files")
    public ResponseEntity<List<String>> uploadFiles(@PathVariable Long id,
                                                    @RequestParam("files")List<MultipartFile> files,
                                                    @AuthenticationPrincipal CustomUserDetails userDetails) {
        List<String> fileUrls = inquiryFileService.uploadFiles(id, userDetails.getUser(), files);
        return ResponseEntity.ok(fileUrls);
    }
}
