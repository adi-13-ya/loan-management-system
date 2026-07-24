package com.bankloan.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "loan_types")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class LoanType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal minInterestRate;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal maxInterestRate;

    @Column(nullable = false)
    private Integer maxTenureMonths;
}
