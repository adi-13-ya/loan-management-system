package com.bankloan.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoanApplicationRequest {
    @NotBlank
    private String loanType;

    @NotNull @DecimalMin("10000")
    private BigDecimal principalAmount;

    @NotNull @DecimalMin("0.0")
    private BigDecimal annualInterestRate;

    @NotNull @Min(1) @Max(360)
    private Integer tenureMonths;

    private String purpose;

    private Long branchId;
}
