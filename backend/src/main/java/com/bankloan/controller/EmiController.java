package com.bankloan.controller;

import com.bankloan.dto.EmiScheduleDTO;
import com.bankloan.service.EmiCalculationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class EmiController {

    private final EmiCalculationService emiCalculationService;

    @GetMapping("/api/loans/{loanId}/emi-schedule")
    public ResponseEntity<List<EmiScheduleDTO>> getEmiSchedule(@PathVariable Long loanId) {
        return ResponseEntity.ok(emiCalculationService.getScheduleForLoan(loanId));
    }

    @PostMapping("/api/emi/{emiId}/mark-paid")
    public ResponseEntity<EmiScheduleDTO> markPaid(@PathVariable Long emiId) {
        return ResponseEntity.ok(emiCalculationService.markAsPaid(emiId));
    }
}
