package com.bankloan.repository;

import com.bankloan.model.entity.ApprovalAuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ApprovalAuditLogRepository extends JpaRepository<ApprovalAuditLog, Long> {
    List<ApprovalAuditLog> findByLoanApplicationIdOrderByTimestampDesc(Long loanApplicationId);
    List<ApprovalAuditLog> findAllByOrderByTimestampDesc();
}
