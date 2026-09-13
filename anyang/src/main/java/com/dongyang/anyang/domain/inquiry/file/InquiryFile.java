package com.dongyang.anyang.domain.inquiry.file;

import com.dongyang.anyang.domain.inquiry.Inquiry;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "inquiry_files")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class InquiryFile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inquiry_id", nullable = false)
    private Inquiry inquiry;

    @Column(nullable = false)
    private String fileName;

    @Column(nullable = false, length = 500)
    private String fileUrl;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Builder
    public InquiryFile(Inquiry inquiry, String fileName, String fileUrl) {
        this.inquiry = inquiry;
        this.fileName = fileName;
        this.fileUrl = fileUrl;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
