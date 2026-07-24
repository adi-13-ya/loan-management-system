package com.bankloan.dto;

import com.bankloan.model.enums.LoanStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoanApplicationDTO {
    private Long id;
    private Long customerId;
    private String customerName;
    private String customerEmail;
    private Long branchId;
    private String branchName;
    private String loanType;
    private BigDecimal principalAmount;
    private BigDecimal annualInterestRate;
    private Integer tenureMonths;
    private String purpose;
    private LoanStatus status;
    private String currentOfficerName;
    private String currentManagerName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
