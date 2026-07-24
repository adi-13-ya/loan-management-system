package com.bankloan.repository;

import com.bankloan.model.entity.EmiSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmiScheduleRepository extends JpaRepository<EmiSchedule, Long> {
    List<EmiSchedule> findByLoanApplicationIdOrderByInstallmentNumber(Long loanApplicationId);
}
