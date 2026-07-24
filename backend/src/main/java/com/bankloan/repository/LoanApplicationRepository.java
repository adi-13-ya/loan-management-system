package com.bankloan.repository;

import com.bankloan.model.entity.LoanApplication;
import com.bankloan.model.enums.LoanStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LoanApplicationRepository extends JpaRepository<LoanApplication, Long> {
    List<LoanApplication> findByCustomerIdOrderByCreatedAtDesc(Long customerId);
    List<LoanApplication> findByBranchIdAndStatusInOrderByCreatedAtDesc(Long branchId, List<LoanStatus> statuses);
    List<LoanApplication> findByBranchIdOrderByCreatedAtDesc(Long branchId);
    List<LoanApplication> findByStatusOrderByCreatedAtDesc(LoanStatus status);

    @Query("SELECT l FROM LoanApplication l WHERE " +
           "(:branchId IS NULL OR l.branch.id = :branchId) AND " +
           "(:status IS NULL OR l.status = :status) " +
           "ORDER BY l.createdAt DESC")
    List<LoanApplication> findAllFiltered(@Param("branchId") Long branchId,
                                          @Param("status") LoanStatus status);

    @Query("SELECT COUNT(l) FROM LoanApplication l WHERE l.branch.id = :branchId AND l.status = :status")
    long countByBranchIdAndStatus(@Param("branchId") Long branchId, @Param("status") LoanStatus status);

    @Query("SELECT COALESCE(SUM(l.principalAmount), 0) FROM LoanApplication l WHERE l.branch.id = :branchId AND l.status = :status")
    java.math.BigDecimal sumPrincipalByBranchAndStatus(@Param("branchId") Long branchId, @Param("status") LoanStatus status);
}
