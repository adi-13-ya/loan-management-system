package com.bankloan.controller;

import com.bankloan.dto.*;
import com.bankloan.exception.ResourceNotFoundException;
import com.bankloan.model.entity.Branch;
import com.bankloan.model.entity.User;
import com.bankloan.model.enums.LoanStatus;
import com.bankloan.repository.BranchRepository;
import com.bankloan.repository.LoanApplicationRepository;
import com.bankloan.repository.UserRepository;
import com.bankloan.service.AuditLogService;
import com.bankloan.service.LoanApplicationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final BranchRepository branchRepository;
    private final UserRepository userRepository;
    private final LoanApplicationService loanService;
    private final AuditLogService auditLogService;
    private final LoanApplicationRepository loanApplicationRepository;

    @GetMapping("/branches")
    public ResponseEntity<List<BranchDTO>> getAllBranches() {
        List<BranchDTO> branches = branchRepository.findAll().stream()
                .map(this::toBranchDTO).collect(Collectors.toList());
        return ResponseEntity.ok(branches);
    }

    @PostMapping("/branches")
    public ResponseEntity<BranchDTO> createBranch(@Valid @RequestBody BranchRequest request) {
        User manager = null;
        if (request.getManagerId() != null) {
            manager = userRepository.findById(request.getManagerId())
                    .orElseThrow(() -> new ResourceNotFoundException("Manager not found"));
        }

        Branch branch = Branch.builder()
                .name(request.getName())
                .code(request.getCode())
                .city(request.getCity())
                .manager(manager)
                .build();

        branch = branchRepository.save(branch);
        return ResponseEntity.ok(toBranchDTO(branch));
    }

    @GetMapping("/users")
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        List<UserDTO> users = userRepository.findAll().stream()
                .map(this::toUserDTO).collect(Collectors.toList());
        return ResponseEntity.ok(users);
    }

    @PutMapping("/users/{id}/toggle-active")
    public ResponseEntity<UserDTO> toggleUserActive(@PathVariable Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        user.setActive(!user.getActive());
        userRepository.save(user);
        return ResponseEntity.ok(toUserDTO(user));
    }

    @GetMapping("/loans")
    public ResponseEntity<List<LoanApplicationDTO>> getAllLoans(
            @RequestParam(required = false) Long branchId,
            @RequestParam(required = false) LoanStatus status) {
        return ResponseEntity.ok(loanService.getAllLoans(branchId, status));
    }

    @GetMapping("/audit-log")
    public ResponseEntity<List<AuditLogDTO>> getAuditLog() {
        return ResponseEntity.ok(auditLogService.getAllLogs());
    }

    @GetMapping("/analytics")
    public ResponseEntity<Map<String, Object>> getAnalytics() {
        Map<String, Object> analytics = new HashMap<>();

        List<Branch> branches = branchRepository.findAll();
        List<Map<String, Object>> branchStats = branches.stream().map(branch -> {
            Map<String, Object> stat = new HashMap<>();
            stat.put("branchId", branch.getId());
            stat.put("branchName", branch.getName());
            stat.put("totalDisbursed", loanApplicationRepository.sumPrincipalByBranchAndStatus(branch.getId(), LoanStatus.DISBURSED));
            stat.put("activeLoans", loanApplicationRepository.countByBranchIdAndStatus(branch.getId(), LoanStatus.DISBURSED));
            stat.put("pendingLoans", loanApplicationRepository.countByBranchIdAndStatus(branch.getId(), LoanStatus.SUBMITTED)
                    + loanApplicationRepository.countByBranchIdAndStatus(branch.getId(), LoanStatus.UNDER_REVIEW)
                    + loanApplicationRepository.countByBranchIdAndStatus(branch.getId(), LoanStatus.FORWARDED_TO_MANAGER));
            stat.put("rejectedLoans", loanApplicationRepository.countByBranchIdAndStatus(branch.getId(), LoanStatus.REJECTED));
            return stat;
        }).collect(Collectors.toList());

        analytics.put("branchStats", branchStats);
        analytics.put("totalBranches", branches.size());
        analytics.put("totalUsers", userRepository.count());
        analytics.put("totalLoans", loanApplicationRepository.count());

        return ResponseEntity.ok(analytics);
    }

    @GetMapping("/branch-portfolio/{branchId}")
    public ResponseEntity<Map<String, Object>> getBranchPortfolio(@PathVariable Long branchId) {
        Map<String, Object> portfolio = new HashMap<>();
        portfolio.put("totalDisbursed", loanApplicationRepository.sumPrincipalByBranchAndStatus(branchId, LoanStatus.DISBURSED));
        portfolio.put("activeLoans", loanApplicationRepository.countByBranchIdAndStatus(branchId, LoanStatus.DISBURSED));
        portfolio.put("approvedLoans", loanApplicationRepository.countByBranchIdAndStatus(branchId, LoanStatus.APPROVED));
        portfolio.put("rejectedLoans", loanApplicationRepository.countByBranchIdAndStatus(branchId, LoanStatus.REJECTED));
        portfolio.put("pendingLoans", loanApplicationRepository.countByBranchIdAndStatus(branchId, LoanStatus.SUBMITTED)
                + loanApplicationRepository.countByBranchIdAndStatus(branchId, LoanStatus.UNDER_REVIEW)
                + loanApplicationRepository.countByBranchIdAndStatus(branchId, LoanStatus.FORWARDED_TO_MANAGER));
        return ResponseEntity.ok(portfolio);
    }

    private BranchDTO toBranchDTO(Branch branch) {
        return BranchDTO.builder()
                .id(branch.getId())
                .name(branch.getName())
                .code(branch.getCode())
                .city(branch.getCity())
                .managerId(branch.getManager() != null ? branch.getManager().getId() : null)
                .managerName(branch.getManager() != null ? branch.getManager().getName() : null)
                .build();
    }

    private UserDTO toUserDTO(User user) {
        return UserDTO.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole())
                .branchId(user.getBranch() != null ? user.getBranch().getId() : null)
                .branchName(user.getBranch() != null ? user.getBranch().getName() : null)
                .active(user.getActive())
                .build();
    }
}
