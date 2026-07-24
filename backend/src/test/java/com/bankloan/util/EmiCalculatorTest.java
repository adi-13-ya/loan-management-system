package com.bankloan.util;

import com.bankloan.dto.EmiScheduleDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class EmiCalculatorTest {

    private EmiCalculator calculator;

    @BeforeEach
    void setUp() {
        calculator = new EmiCalculator();
    }

    @Test
    void testStandardEmiCalculation() {
        // ₹10,00,000 @ 9% for 60 months
        BigDecimal principal = new BigDecimal("1000000");
        BigDecimal rate = new BigDecimal("9.00");
        int tenure = 60;

        BigDecimal emi = calculator.calculateEmi(principal, rate, tenure);

        // Expected EMI ≈ ₹20,758.36
        assertEquals(new BigDecimal("20758.36"), emi);
    }

    @Test
    void testZeroInterestEmi() {
        BigDecimal principal = new BigDecimal("120000");
        BigDecimal rate = BigDecimal.ZERO;
        int tenure = 12;

        BigDecimal emi = calculator.calculateEmi(principal, rate, tenure);

        assertEquals(new BigDecimal("10000.00"), emi);
    }

    @Test
    void testSingleMonthTenure() {
        BigDecimal principal = new BigDecimal("100000");
        BigDecimal rate = new BigDecimal("12.00");
        int tenure = 1;

        BigDecimal emi = calculator.calculateEmi(principal, rate, tenure);

        // EMI = principal + one month's interest = 100000 + 1000 = 101000
        assertEquals(new BigDecimal("101000.00"), emi);
    }

    @Test
    void testTwoMonthTenure() {
        BigDecimal principal = new BigDecimal("100000");
        BigDecimal rate = new BigDecimal("12.00");
        int tenure = 2;

        BigDecimal emi = calculator.calculateEmi(principal, rate, tenure);
        assertNotNull(emi);
        assertTrue(emi.compareTo(BigDecimal.ZERO) > 0);
    }

    @Test
    void testScheduleSumsToExactPrincipal() {
        BigDecimal principal = new BigDecimal("1000000");
        BigDecimal rate = new BigDecimal("9.00");
        int tenure = 60;

        List<EmiScheduleDTO> schedule = calculator.generateSchedule(principal, rate, tenure, LocalDate.now());

        assertEquals(tenure, schedule.size());

        BigDecimal totalPrincipalPaid = schedule.stream()
                .map(EmiScheduleDTO::getPrincipalComponent)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        assertEquals(0, principal.compareTo(totalPrincipalPaid.setScale(2, RoundingMode.HALF_UP)),
                "Sum of principal components should equal original principal");
    }

    @Test
    void testScheduleLastMonthZerosOutBalance() {
        BigDecimal principal = new BigDecimal("500000");
        BigDecimal rate = new BigDecimal("11.50");
        int tenure = 36;

        List<EmiScheduleDTO> schedule = calculator.generateSchedule(principal, rate, tenure, LocalDate.now());

        EmiScheduleDTO lastInstallment = schedule.get(schedule.size() - 1);
        assertEquals(0, BigDecimal.ZERO.compareTo(lastInstallment.getOutstandingBalance()),
                "Outstanding balance after last EMI should be zero");
    }

    @Test
    void testZeroInterestSchedule() {
        BigDecimal principal = new BigDecimal("120000");
        BigDecimal rate = BigDecimal.ZERO;
        int tenure = 12;

        List<EmiScheduleDTO> schedule = calculator.generateSchedule(principal, rate, tenure, LocalDate.now());

        assertEquals(12, schedule.size());
        for (EmiScheduleDTO emi : schedule) {
            assertEquals(0, BigDecimal.ZERO.compareTo(emi.getInterestComponent()));
        }
    }

    @Test
    void testLargePrincipalLongTenure() {
        // 30-year home loan
        BigDecimal principal = new BigDecimal("50000000");
        BigDecimal rate = new BigDecimal("8.50");
        int tenure = 360;

        BigDecimal emi = calculator.calculateEmi(principal, rate, tenure);
        assertNotNull(emi);
        assertTrue(emi.compareTo(BigDecimal.ZERO) > 0);

        List<EmiScheduleDTO> schedule = calculator.generateSchedule(principal, rate, tenure, LocalDate.now());
        assertEquals(360, schedule.size());

        EmiScheduleDTO last = schedule.get(schedule.size() - 1);
        assertEquals(0, BigDecimal.ZERO.compareTo(last.getOutstandingBalance()));
    }

    @Test
    void testNegativePrincipalThrows() {
        assertThrows(IllegalArgumentException.class, () ->
                calculator.calculateEmi(new BigDecimal("-100000"), new BigDecimal("9"), 12));
    }

    @Test
    void testZeroPrincipalThrows() {
        assertThrows(IllegalArgumentException.class, () ->
                calculator.calculateEmi(BigDecimal.ZERO, new BigDecimal("9"), 12));
    }

    @Test
    void testZeroTenureThrows() {
        assertThrows(IllegalArgumentException.class, () ->
                calculator.calculateEmi(new BigDecimal("100000"), new BigDecimal("9"), 0));
    }

    @Test
    void testScheduleDueDatesAreMonthlyIncremental() {
        LocalDate start = LocalDate.of(2025, 1, 1);
        List<EmiScheduleDTO> schedule = calculator.generateSchedule(
                new BigDecimal("100000"), new BigDecimal("10"), 6, start);

        for (int i = 0; i < schedule.size(); i++) {
            assertEquals(start.plusMonths(i + 1), schedule.get(i).getDueDate());
        }
    }
}
