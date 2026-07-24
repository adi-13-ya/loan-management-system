package com.bankloan.service;

import com.bankloan.dto.AuditLogDTO;
import com.bankloan.model.entity.ApprovalAuditLog;
import com.bankloan.model.entity.LoanApplication;
import com.bankloan.model.entity.User;
import com.bankloan.model.enums.LoanStatus;
import com.bankloan.repository.ApprovalAuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuditLogService {

    private final ApprovalAuditLogRepository auditLogRepository;

    public void logTransition(LoanApplication loan, User actor, LoanStatus from, LoanStatus to, String remarks) {
        ApprovalAuditLog log = ApprovalAuditLog.builder()
                .loanApplication(loan)
                .actor(actor)
                .fromStatus(from)
                .toStatus(to)
                .remarks(remarks)
                .build();
        auditLogRepository.save(log);
    }

    public List<AuditLogDTO> getLogsForLoan(Long loanId) {
        return auditLogRepository.findByLoanApplicationIdOrderByTimestampDesc(loanId)
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    public List<AuditLogDTO> getAllLogs() {
        return auditLogRepository.findAllByOrderByTimestampDesc()
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    private AuditLogDTO toDTO(ApprovalAuditLog log) {
        return AuditLogDTO.builder()
                .id(log.getId())
                .loanApplicationId(log.getLoanApplication().getId())
                .actorName(log.getActor().getName())
                .actorRole(log.getActor().getRole().name())
                .fromStatus(log.getFromStatus())
                .toStatus(log.getToStatus())
                .remarks(log.getRemarks())
                .timestamp(log.getTimestamp())
                .build();
    }
}
