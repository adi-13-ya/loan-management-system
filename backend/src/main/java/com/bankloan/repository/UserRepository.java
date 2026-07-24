package com.bankloan.repository;

import com.bankloan.model.entity.User;
import com.bankloan.model.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    List<User> findByRole(Role role);
    List<User> findByBranchId(Long branchId);
    List<User> findByRoleAndBranchId(Role role, Long branchId);
}
