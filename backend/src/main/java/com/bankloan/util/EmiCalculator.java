package com.bankloan.util;

import com.bankloan.dto.EmiScheduleDTO;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Component
public class EmiCalculator {

    private static final MathContext MC = MathContext.DECIMAL128;
    private static final int SCALE = 2;

    /**
     * Calculate EMI using the reducing balance formula:
     * EMI = [P × R × (1+R)^N] / [(1+R)^N − 1]
     * If R = 0, EMI = P / N
     */
    public BigDecimal calculateEmi(BigDecimal principal, BigDecimal annualRate, int tenureMonths) {
        if (principal.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Principal must be positive");
        }
        if (tenureMonths <= 0) {
            throw new IllegalArgumentException("Tenure must be positive");
        }

        if (annualRate.compareTo(BigDecimal.ZERO) == 0) {
            return principal.divide(BigDecimal.valueOf(tenureMonths), SCALE, RoundingMode.HALF_UP);
        }

        BigDecimal monthlyRate = annualRate.divide(BigDecimal.valueOf(12 * 100), 10, RoundingMode.HALF_UP);
        BigDecimal onePlusR = BigDecimal.ONE.add(monthlyRate);
        BigDecimal onePlusRPowN = onePlusR.pow(tenureMonths, MC);

        BigDecimal numerator = principal.multiply(monthlyRate, MC).multiply(onePlusRPowN, MC);
        BigDecimal denominator = onePlusRPowN.subtract(BigDecimal.ONE);

        return numerator.divide(denominator, SCALE, RoundingMode.HALF_UP);
    }

    /**
     * Generate full EMI schedule with month-by-month breakdown.
     * The last installment is adjusted to zero out the outstanding balance exactly.
     */
    public List<EmiScheduleDTO> generateSchedule(BigDecimal principal, BigDecimal annualRate,
                                                   int tenureMonths, LocalDate startDate) {
        BigDecimal emi = calculateEmi(principal, annualRate, tenureMonths);
        BigDecimal monthlyRate = annualRate.compareTo(BigDecimal.ZERO) == 0
                ? BigDecimal.ZERO
                : annualRate.divide(BigDecimal.valueOf(12 * 100), 10, RoundingMode.HALF_UP);

        List<EmiScheduleDTO> schedule = new ArrayList<>();
        BigDecimal outstandingBalance = principal;

        for (int i = 1; i <= tenureMonths; i++) {
            BigDecimal interestComponent = outstandingBalance.multiply(monthlyRate, MC)
                    .setScale(SCALE, RoundingMode.HALF_UP);

            BigDecimal principalComponent;
            BigDecimal currentEmi;

            if (i == tenureMonths) {
                // Last month: adjust to zero out balance
                principalComponent = outstandingBalance;
                currentEmi = principalComponent.add(interestComponent);
            } else {
                principalComponent = emi.subtract(interestComponent);
                currentEmi = emi;
            }

            outstandingBalance = outstandingBalance.subtract(principalComponent);

            schedule.add(EmiScheduleDTO.builder()
                    .installmentNumber(i)
                    .dueDate(startDate.plusMonths(i))
                    .emiAmount(currentEmi)
                    .principalComponent(principalComponent)
                    .interestComponent(interestComponent)
                    .outstandingBalance(outstandingBalance.max(BigDecimal.ZERO).setScale(SCALE, RoundingMode.HALF_UP))
                    .isPaid(false)
                    .build());
        }

        return schedule;
    }
}
