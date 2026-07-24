package com.bankloan.repository;

import com.bankloan.model.entity.LoanType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LoanTypeRepository extends JpaRepository<LoanType, Long> {
    Optional<LoanType> findByName(String name);
}
