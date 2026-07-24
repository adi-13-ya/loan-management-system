package com.bankloan.model.entity;

import com.bankloan.model.enums.LoanStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "approval_audit_logs")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class ApprovalAuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "loan_application_id", nullable = false)
    private LoanApplication loanApplication;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "actor_id", nullable = false)
    private User actor;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LoanStatus fromStatus;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LoanStatus toStatus;

    private String remarks;

    @Column(nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();
}
