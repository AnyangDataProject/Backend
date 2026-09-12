package com.dongyang.anyang.domain.report;

import com.dongyang.anyang.domain.user.User;
import jakarta.persistence.*;
import lombok.*;

import javax.print.attribute.standard.Severity;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder
@Entity
@Table(name = "reports")
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Report {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false) //지금 유저 로그인 구현 안해서 일단 널 허용해둠
    private User user;

    private String description;

    @Column(nullable = false)
    private BigDecimal latitude;

    @Column(nullable = false)
    private BigDecimal longitude;

    private String address;

    @Column(name = "damage_type", length = 50)
    private String damageType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Severity severity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReportStatus status;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public enum Severity {
        LOW,
        MEDIUM,
        HIGH
    }

    public enum ReportStatus {
        RECEIVED,
        AI_ANALYZED,
        CONFIRMED,
        IN_PROGRESS,
        COMPLETED,
        REJECTED
    }
}
