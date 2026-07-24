package com.bankloan.service;

import com.bankloan.dto.LoanApplicationDTO;
import com.bankloan.dto.LoanApplicationRequest;
import com.bankloan.exception.AccessDeniedException;
import com.bankloan.exception.ResourceNotFoundException;
import com.bankloan.model.entity.Branch;
import com.bankloan.model.entity.LoanApplication;
import com.bankloan.model.entity.User;
import com.bankloan.model.enums.LoanStatus;
import com.bankloan.model.enums.Role;
import com.bankloan.repository.BranchRepository;
import com.bankloan.repository.LoanApplicationRepository;
import com.bankloan.repository.UserRepository;
import com.bankloan.util.LoanStatusTransitionValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LoanApplicationService {

    private final LoanApplicationRepository loanRepository;
    private final UserRepository userRepository;
    private final BranchRepository branchRepository;
    private final LoanStatusTransitionValidator transitionValidator;
    private final AuditLogService auditLogService;
    private final EmiCalculationService emiCalculationService;

    @Transactional
    public LoanApplicationDTO createApplication(LoanApplicationRequest request, String userEmail) {
        User customer = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Branch branch = null;
        if (request.getBranchId() != null) {
            branch = branchRepository.findById(request.getBranchId())
                    .orElseThrow(() -> new ResourceNotFoundException("Branch not found"));
        }

        LoanApplication loan = LoanApplication.builder()
                .customer(customer)
                .branch(branch)
                .loanType(request.getLoanType())
                .principalAmount(request.getPrincipalAmount())
                .annualInterestRate(request.getAnnualInterestRate())
                .tenureMonths(request.getTenureMonths())
                .purpose(request.getPurpose())
                .status(LoanStatus.DRAFT)
                .build();

        loan = loanRepository.save(loan);
        return toDTO(loan);
    }

    @Transactional
    public LoanApplicationDTO submitApplication(Long loanId, String userEmail) {
        LoanApplication loan = getLoanForCustomer(loanId, userEmail);
        transitionValidator.validate(loan.getStatus(), LoanStatus.SUBMITTED);

        User customer = loan.getCustomer();
        LoanStatus fromStatus = loan.getStatus();
        loan.setStatus(LoanStatus.SUBMITTED);
        loanRepository.save(loan);

        auditLogService.logTransition(loan, customer, fromStatus, LoanStatus.SUBMITTED, "Application submitted by customer");
        return toDTO(loan);
    }

    @Transactional
    public LoanApplicationDTO pickUpForReview(Long loanId, String officerEmail) {
        User officer = getUserByEmail(officerEmail);
        LoanApplication loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new ResourceNotFoundException("Loan not found"));

        verifyBranchAccess(officer, loan);
        transitionValidator.validate(loan.getStatus(), LoanStatus.UNDER_REVIEW);

        LoanStatus fromStatus = loan.getStatus();
        loan.setStatus(LoanStatus.UNDER_REVIEW);
        loan.setCurrentOfficer(officer);
        loanRepository.save(loan);

        auditLogService.logTransition(loan, officer, fromStatus, LoanStatus.UNDER_REVIEW, "Picked up for review by officer");
        return toDTO(loan);
    }

    @Transactional
    public LoanApplicationDTO forwardToManager(Long loanId, String officerEmail, String remarks) {
        User officer = getUserByEmail(officerEmail);
        LoanApplication loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new ResourceNotFoundException("Loan not found"));

        verifyBranchAccess(officer, loan);
        transitionValidator.validate(loan.getStatus(), LoanStatus.FORWARDED_TO_MANAGER);

        LoanStatus fromStatus = loan.getStatus();
        loan.setStatus(LoanStatus.FORWARDED_TO_MANAGER);

        if (loan.getBranch() != null && loan.getBranch().getManager() != null) {
            loan.setCurrentManager(loan.getBranch().getManager());
        }

        loanRepository.save(loan);
        auditLogService.logTransition(loan, officer, fromStatus, LoanStatus.FORWARDED_TO_MANAGER, remarks);
        return toDTO(loan);
    }

    @Transactional
    public LoanApplicationDTO approve(Long loanId, String managerEmail, String remarks) {
        User manager = getUserByEmail(managerEmail);
        LoanApplication loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new ResourceNotFoundException("Loan not found"));

        verifyBranchAccess(manager, loan);
        transitionValidator.validate(loan.getStatus(), LoanStatus.APPROVED);

        LoanStatus fromStatus = loan.getStatus();
        loan.setStatus(LoanStatus.APPROVED);
        loanRepository.save(loan);
        auditLogService.logTransition(loan, manager, fromStatus, LoanStatus.APPROVED, remarks);

        // Auto-disburse and generate EMI schedule
        transitionValidator.validate(loan.getStatus(), LoanStatus.DISBURSED);
        loan.setStatus(LoanStatus.DISBURSED);
        loanRepository.save(loan);
        auditLogService.logTransition(loan, manager, LoanStatus.APPROVED, LoanStatus.DISBURSED, "Auto-disbursed upon approval");

        emiCalculationService.generateAndSaveSchedule(loan);

        return toDTO(loan);
    }

    @Transactional
    public LoanApplicationDTO reject(Long loanId, String actorEmail, String remarks) {
        User actor = getUserByEmail(actorEmail);
        LoanApplication loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new ResourceNotFoundException("Loan not found"));

        if (actor.getRole() != Role.ADMIN) {
            verifyBranchAccess(actor, loan);
        }
        transitionValidator.validate(loan.getStatus(), LoanStatus.REJECTED);

        LoanStatus fromStatus = loan.getStatus();
        loan.setStatus(LoanStatus.REJECTED);
        loanRepository.save(loan);

        auditLogService.logTransition(loan, actor, fromStatus, LoanStatus.REJECTED, remarks);
        return toDTO(loan);
    }

    public LoanApplicationDTO getLoanById(Long loanId) {
        LoanApplication loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new ResourceNotFoundException("Loan not found"));
        return toDTO(loan);
    }

    public List<LoanApplicationDTO> getMyApplications(String userEmail) {
        User customer = getUserByEmail(userEmail);
        return loanRepository.findByCustomerIdOrderByCreatedAtDesc(customer.getId())
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    public List<LoanApplicationDTO> getBranchQueue(Long branchId, String userEmail) {
        User user = getUserByEmail(userEmail);
        if (user.getRole() != Role.ADMIN && (user.getBranch() == null || !user.getBranch().getId().equals(branchId))) {
            throw new AccessDeniedException("You don't have access to this branch's queue");
        }

        List<LoanStatus> queueStatuses;
        if (user.getRole() == Role.OFFICER) {
            queueStatuses = List.of(LoanStatus.SUBMITTED, LoanStatus.UNDER_REVIEW);
        } else if (user.getRole() == Role.MANAGER) {
            queueStatuses = List.of(LoanStatus.FORWARDED_TO_MANAGER);
        } else {
            return loanRepository.findByBranchIdOrderByCreatedAtDesc(branchId)
                    .stream().map(this::toDTO).collect(Collectors.toList());
        }

        return loanRepository.findByBranchIdAndStatusInOrderByCreatedAtDesc(branchId, queueStatuses)
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    public List<LoanApplicationDTO> getAllLoans(Long branchId, LoanStatus status) {
        return loanRepository.findAllFiltered(branchId, status)
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private LoanApplication getLoanForCustomer(Long loanId, String userEmail) {
        User customer = getUserByEmail(userEmail);
        LoanApplication loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new ResourceNotFoundException("Loan not found"));
        if (!loan.getCustomer().getId().equals(customer.getId())) {
            throw new AccessDeniedException("You don't have access to this loan application");
        }
        return loan;
    }

    private void verifyBranchAccess(User user, LoanApplication loan) {
        if (user.getBranch() == null || loan.getBranch() == null ||
                !user.getBranch().getId().equals(loan.getBranch().getId())) {
            throw new AccessDeniedException("You don't have access to this branch's applications");
        }
    }

    private LoanApplicationDTO toDTO(LoanApplication loan) {
        return LoanApplicationDTO.builder()
                .id(loan.getId())
                .customerId(loan.getCustomer().getId())
                .customerName(loan.getCustomer().getName())
                .customerEmail(loan.getCustomer().getEmail())
                .branchId(loan.getBranch() != null ? loan.getBranch().getId() : null)
                .branchName(loan.getBranch() != null ? loan.getBranch().getName() : null)
                .loanType(loan.getLoanType())
                .principalAmount(loan.getPrincipalAmount())
                .annualInterestRate(loan.getAnnualInterestRate())
                .tenureMonths(loan.getTenureMonths())
                .purpose(loan.getPurpose())
                .status(loan.getStatus())
                .currentOfficerName(loan.getCurrentOfficer() != null ? loan.getCurrentOfficer().getName() : null)
                .currentManagerName(loan.getCurrentManager() != null ? loan.getCurrentManager().getName() : null)
                .createdAt(loan.getCreatedAt())
                .updatedAt(loan.getUpdatedAt())
                .build();
    }
}
