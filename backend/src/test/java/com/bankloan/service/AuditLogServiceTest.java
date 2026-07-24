package com.bankloan.service;

import com.bankloan.model.entity.ApprovalAuditLog;
import com.bankloan.model.entity.LoanApplication;
import com.bankloan.model.entity.User;
import com.bankloan.model.enums.LoanStatus;
import com.bankloan.model.enums.Role;
import com.bankloan.repository.ApprovalAuditLogRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AuditLogServiceTest {

    @Mock
    private ApprovalAuditLogRepository auditLogRepository;

    @InjectMocks
    private AuditLogService auditLogService;

    @Test
    void testLogTransitionCreatesExactlyOneRecord() {
        User actor = User.builder().id(1L).name("Officer").role(Role.OFFICER).build();
        LoanApplication loan = LoanApplication.builder().id(1L).build();

        auditLogService.logTransition(loan, actor, LoanStatus.SUBMITTED, LoanStatus.UNDER_REVIEW, "Review started");

        ArgumentCaptor<ApprovalAuditLog> captor = ArgumentCaptor.forClass(ApprovalAuditLog.class);
        verify(auditLogRepository).save(captor.capture());

        ApprovalAuditLog saved = captor.getValue();
        assertEquals(loan, saved.getLoanApplication());
        assertEquals(actor, saved.getActor());
        assertEquals(LoanStatus.SUBMITTED, saved.getFromStatus());
        assertEquals(LoanStatus.UNDER_REVIEW, saved.getToStatus());
        assertEquals("Review started", saved.getRemarks());
        assertNotNull(saved.getTimestamp());
    }

    @Test
    void testLogTransitionWithNullRemarks() {
        User actor = User.builder().id(1L).name("Manager").role(Role.MANAGER).build();
        LoanApplication loan = LoanApplication.builder().id(2L).build();

        auditLogService.logTransition(loan, actor, LoanStatus.FORWARDED_TO_MANAGER, LoanStatus.APPROVED, null);

        ArgumentCaptor<ApprovalAuditLog> captor = ArgumentCaptor.forClass(ApprovalAuditLog.class);
        verify(auditLogRepository).save(captor.capture());

        ApprovalAuditLog saved = captor.getValue();
        assertNull(saved.getRemarks());
    }
}
