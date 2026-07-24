package com.bankloan.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "emi_schedules")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class EmiSchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "loan_application_id", nullable = false)
    private LoanApplication loanApplication;

    @Column(nullable = false)
    private Integer installmentNumber;

    @Column(nullable = false)
    private LocalDate dueDate;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal emiAmount;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal principalComponent;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal interestComponent;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal outstandingBalance;

    @Column(nullable = false)
    @Builder.Default
    private Boolean isPaid = false;
}
