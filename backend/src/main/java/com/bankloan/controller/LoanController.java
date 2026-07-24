package com.bankloan.controller;

import com.bankloan.dto.LoanApplicationDTO;
import com.bankloan.dto.LoanApplicationRequest;
import com.bankloan.dto.RemarkRequest;
import com.bankloan.service.LoanApplicationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/loans")
@RequiredArgsConstructor
public class LoanController {

    private final LoanApplicationService loanService;

    @PostMapping
    public ResponseEntity<LoanApplicationDTO> createApplication(
            @Valid @RequestBody LoanApplicationRequest request,
            Authentication auth) {
        return ResponseEntity.ok(loanService.createApplication(request, auth.getName()));
    }

    @PostMapping("/{id}/submit")
    public ResponseEntity<LoanApplicationDTO> submitApplication(
            @PathVariable Long id, Authentication auth) {
        return ResponseEntity.ok(loanService.submitApplication(id, auth.getName()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<LoanApplicationDTO> getLoan(@PathVariable Long id) {
        return ResponseEntity.ok(loanService.getLoanById(id));
    }

    @GetMapping("/my")
    public ResponseEntity<List<LoanApplicationDTO>> getMyApplications(Authentication auth) {
        return ResponseEntity.ok(loanService.getMyApplications(auth.getName()));
    }

    @GetMapping("/branch/{branchId}/queue")
    public ResponseEntity<List<LoanApplicationDTO>> getBranchQueue(
            @PathVariable Long branchId, Authentication auth) {
        return ResponseEntity.ok(loanService.getBranchQueue(branchId, auth.getName()));
    }

    @PostMapping("/{id}/review")
    public ResponseEntity<LoanApplicationDTO> pickUpForReview(
            @PathVariable Long id, Authentication auth) {
        return ResponseEntity.ok(loanService.pickUpForReview(id, auth.getName()));
    }

    @PostMapping("/{id}/forward")
    public ResponseEntity<LoanApplicationDTO> forwardToManager(
            @PathVariable Long id,
            @RequestBody(required = false) RemarkRequest request,
            Authentication auth) {
        String remarks = request != null ? request.getRemarks() : null;
        return ResponseEntity.ok(loanService.forwardToManager(id, auth.getName(), remarks));
    }

    @PostMapping("/{id}/approve")
    public ResponseEntity<LoanApplicationDTO> approve(
            @PathVariable Long id,
            @RequestBody(required = false) RemarkRequest request,
            Authentication auth) {
        String remarks = request != null ? request.getRemarks() : null;
        return ResponseEntity.ok(loanService.approve(id, auth.getName(), remarks));
    }

    @PostMapping("/{id}/reject")
    public ResponseEntity<LoanApplicationDTO> reject(
            @PathVariable Long id,
            @RequestBody RemarkRequest request,
            Authentication auth) {
        return ResponseEntity.ok(loanService.reject(id, auth.getName(), request.getRemarks()));
    }
}
