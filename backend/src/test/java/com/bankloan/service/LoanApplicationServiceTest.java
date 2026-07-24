package com.bankloan.service;

import com.bankloan.dto.LoanApplicationDTO;
import com.bankloan.dto.LoanApplicationRequest;
import com.bankloan.exception.AccessDeniedException;
import com.bankloan.exception.IllegalStateTransitionException;
import com.bankloan.model.entity.Branch;
import com.bankloan.model.entity.LoanApplication;
import com.bankloan.model.entity.User;
import com.bankloan.model.enums.LoanStatus;
import com.bankloan.model.enums.Role;
import com.bankloan.repository.BranchRepository;
import com.bankloan.repository.LoanApplicationRepository;
import com.bankloan.repository.UserRepository;
import com.bankloan.util.LoanStatusTransitionValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoanApplicationServiceTest {

    @Mock private LoanApplicationRepository loanRepository;
    @Mock private UserRepository userRepository;
    @Mock private BranchRepository branchRepository;
    @Mock private LoanStatusTransitionValidator transitionValidator;
    @Mock private AuditLogService auditLogService;
    @Mock private EmiCalculationService emiCalculationService;

    @InjectMocks
    private LoanApplicationService loanService;

    private User customer;
    private User officer;
    private User manager;
    private Branch branch;
    private LoanApplication draftLoan;

    @BeforeEach
    void setUp() {
        branch = Branch.builder().id(1L).name("Test Branch").code("TST").city("Test City").build();

        customer = User.builder().id(1L).name("Customer").email("customer@test.com")
                .role(Role.CUSTOMER).active(true).build();

        officer = User.builder().id(2L).name("Officer").email("officer@test.com")
                .role(Role.OFFICER).branch(branch).active(true).build();

        manager = User.builder().id(3L).name("Manager").email("manager@test.com")
                .role(Role.MANAGER).branch(branch).active(true).build();

        branch.setManager(manager);

        draftLoan = LoanApplication.builder()
                .id(1L).customer(customer).branch(branch)
                .loanType("Home Loan").principalAmount(new BigDecimal("1000000"))
                .annualInterestRate(new BigDecimal("9.0")).tenureMonths(60)
                .purpose("Test").status(LoanStatus.DRAFT).build();
    }

    @Test
    void testCreateApplication() {
        LoanApplicationRequest request = new LoanApplicationRequest();
        request.setLoanType("Home Loan");
        request.setPrincipalAmount(new BigDecimal("1000000"));
        request.setAnnualInterestRate(new BigDecimal("9.0"));
        request.setTenureMonths(60);
        request.setPurpose("Test");
        request.setBranchId(1L);

        when(userRepository.findByEmail("customer@test.com")).thenReturn(Optional.of(customer));
        when(branchRepository.findById(1L)).thenReturn(Optional.of(branch));
        when(loanRepository.save(any())).thenReturn(draftLoan);

        LoanApplicationDTO result = loanService.createApplication(request, "customer@test.com");

        assertNotNull(result);
        assertEquals(LoanStatus.DRAFT, result.getStatus());
        verify(loanRepository).save(any());
    }

    @Test
    void testSubmitApplication() {
        when(userRepository.findByEmail("customer@test.com")).thenReturn(Optional.of(customer));
        when(loanRepository.findById(1L)).thenReturn(Optional.of(draftLoan));
        when(loanRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        LoanApplicationDTO result = loanService.submitApplication(1L, "customer@test.com");

        assertEquals(LoanStatus.SUBMITTED, result.getStatus());
        verify(transitionValidator).validate(LoanStatus.DRAFT, LoanStatus.SUBMITTED);
        verify(auditLogService).logTransition(any(), any(), eq(LoanStatus.DRAFT), eq(LoanStatus.SUBMITTED), any());
    }

    @Test
    void testOfficerForwardToManager() {
        LoanApplication underReview = LoanApplication.builder()
                .id(1L).customer(customer).branch(branch)
                .loanType("Home Loan").principalAmount(new BigDecimal("1000000"))
                .annualInterestRate(new BigDecimal("9.0")).tenureMonths(60)
                .status(LoanStatus.UNDER_REVIEW).currentOfficer(officer).build();

        when(userRepository.findByEmail("officer@test.com")).thenReturn(Optional.of(officer));
        when(loanRepository.findById(1L)).thenReturn(Optional.of(underReview));
        when(loanRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        LoanApplicationDTO result = loanService.forwardToManager(1L, "officer@test.com", "Docs verified");

        assertEquals(LoanStatus.FORWARDED_TO_MANAGER, result.getStatus());
        verify(transitionValidator).validate(LoanStatus.UNDER_REVIEW, LoanStatus.FORWARDED_TO_MANAGER);
    }

    @Test
    void testManagerApproveTriggersEmiGeneration() {
        LoanApplication forwarded = LoanApplication.builder()
                .id(1L).customer(customer).branch(branch)
                .loanType("Home Loan").principalAmount(new BigDecimal("1000000"))
                .annualInterestRate(new BigDecimal("9.0")).tenureMonths(60)
                .status(LoanStatus.FORWARDED_TO_MANAGER).currentManager(manager).build();

        when(userRepository.findByEmail("manager@test.com")).thenReturn(Optional.of(manager));
        when(loanRepository.findById(1L)).thenReturn(Optional.of(forwarded));
        when(loanRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        loanService.approve(1L, "manager@test.com", "Approved");

        verify(emiCalculationService).generateAndSaveSchedule(any());
    }

    @Test
    void testRejectRecordsReason() {
        LoanApplication underReview = LoanApplication.builder()
                .id(1L).customer(customer).branch(branch)
                .loanType("Home Loan").principalAmount(new BigDecimal("1000000"))
                .annualInterestRate(new BigDecimal("9.0")).tenureMonths(60)
                .status(LoanStatus.UNDER_REVIEW).build();

        when(userRepository.findByEmail("officer@test.com")).thenReturn(Optional.of(officer));
        when(loanRepository.findById(1L)).thenReturn(Optional.of(underReview));
        when(loanRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        loanService.reject(1L, "officer@test.com", "Insufficient docs");

        verify(auditLogService).logTransition(any(), any(), eq(LoanStatus.UNDER_REVIEW),
                eq(LoanStatus.REJECTED), eq("Insufficient docs"));
    }

    @Test
    void testBranchAccessDeniedForWrongBranch() {
        Branch otherBranch = Branch.builder().id(2L).name("Other Branch").build();
        User otherOfficer = User.builder().id(10L).name("Other").email("other@test.com")
                .role(Role.OFFICER).branch(otherBranch).build();

        LoanApplication loan = LoanApplication.builder()
                .id(1L).customer(customer).branch(branch)
                .status(LoanStatus.SUBMITTED).build();

        when(userRepository.findByEmail("other@test.com")).thenReturn(Optional.of(otherOfficer));
        when(loanRepository.findById(1L)).thenReturn(Optional.of(loan));

        assertThrows(AccessDeniedException.class,
                () -> loanService.pickUpForReview(1L, "other@test.com"));
    }
}
