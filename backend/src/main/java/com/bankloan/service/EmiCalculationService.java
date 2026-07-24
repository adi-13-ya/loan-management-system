package com.bankloan.service;

import com.bankloan.dto.EmiScheduleDTO;
import com.bankloan.exception.ResourceNotFoundException;
import com.bankloan.model.entity.EmiSchedule;
import com.bankloan.model.entity.LoanApplication;
import com.bankloan.repository.EmiScheduleRepository;
import com.bankloan.repository.LoanApplicationRepository;
import com.bankloan.util.EmiCalculator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EmiCalculationService {

    private final EmiCalculator emiCalculator;
    private final EmiScheduleRepository emiScheduleRepository;
    private final LoanApplicationRepository loanApplicationRepository;

    @Transactional
    public List<EmiScheduleDTO> generateAndSaveSchedule(LoanApplication loan) {
        List<EmiScheduleDTO> schedule = emiCalculator.generateSchedule(
                loan.getPrincipalAmount(),
                loan.getAnnualInterestRate(),
                loan.getTenureMonths(),
                LocalDate.now()
        );

        List<EmiSchedule> entities = schedule.stream().map(dto ->
                EmiSchedule.builder()
                        .loanApplication(loan)
                        .installmentNumber(dto.getInstallmentNumber())
                        .dueDate(dto.getDueDate())
                        .emiAmount(dto.getEmiAmount())
                        .principalComponent(dto.getPrincipalComponent())
                        .interestComponent(dto.getInterestComponent())
                        .outstandingBalance(dto.getOutstandingBalance())
                        .isPaid(false)
                        .build()
        ).collect(Collectors.toList());

        emiScheduleRepository.saveAll(entities);
        return schedule;
    }

    public List<EmiScheduleDTO> getScheduleForLoan(Long loanId) {
        return emiScheduleRepository.findByLoanApplicationIdOrderByInstallmentNumber(loanId)
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Transactional
    public EmiScheduleDTO markAsPaid(Long emiId) {
        EmiSchedule emi = emiScheduleRepository.findById(emiId)
                .orElseThrow(() -> new ResourceNotFoundException("EMI installment not found"));
        emi.setIsPaid(true);
        emiScheduleRepository.save(emi);
        return toDTO(emi);
    }

    private EmiScheduleDTO toDTO(EmiSchedule emi) {
        return EmiScheduleDTO.builder()
                .id(emi.getId())
                .installmentNumber(emi.getInstallmentNumber())
                .dueDate(emi.getDueDate())
                .emiAmount(emi.getEmiAmount())
                .principalComponent(emi.getPrincipalComponent())
                .interestComponent(emi.getInterestComponent())
                .outstandingBalance(emi.getOutstandingBalance())
                .isPaid(emi.getIsPaid())
                .build();
    }
}
