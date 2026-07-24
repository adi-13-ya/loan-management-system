package com.bankloan.dto;

import com.bankloan.model.enums.LoanStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLogDTO {
    private Long id;
    private Long loanApplicationId;
    private String actorName;
    private String actorRole;
    private LoanStatus fromStatus;
    private LoanStatus toStatus;
    private String remarks;
    private LocalDateTime timestamp;
}
