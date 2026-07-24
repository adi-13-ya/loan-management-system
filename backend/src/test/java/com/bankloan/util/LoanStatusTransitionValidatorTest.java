package com.bankloan.util;

import com.bankloan.exception.IllegalStateTransitionException;
import com.bankloan.model.enums.LoanStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

class LoanStatusTransitionValidatorTest {

    private LoanStatusTransitionValidator validator;

    @BeforeEach
    void setUp() {
        validator = new LoanStatusTransitionValidator();
    }

    @ParameterizedTest
    @CsvSource({
            "DRAFT, SUBMITTED",
            "SUBMITTED, UNDER_REVIEW",
            "UNDER_REVIEW, FORWARDED_TO_MANAGER",
            "UNDER_REVIEW, REJECTED",
            "FORWARDED_TO_MANAGER, APPROVED",
            "FORWARDED_TO_MANAGER, REJECTED",
            "APPROVED, DISBURSED"
    })
    void testValidTransitions(LoanStatus from, LoanStatus to) {
        assertDoesNotThrow(() -> validator.validate(from, to));
    }

    @ParameterizedTest
    @CsvSource({
            "DRAFT, APPROVED",
            "DRAFT, UNDER_REVIEW",
            "DRAFT, DISBURSED",
            "DRAFT, REJECTED",
            "SUBMITTED, FORWARDED_TO_MANAGER",
            "SUBMITTED, APPROVED",
            "SUBMITTED, DISBURSED",
            "UNDER_REVIEW, APPROVED",
            "UNDER_REVIEW, DISBURSED",
            "FORWARDED_TO_MANAGER, UNDER_REVIEW",
            "FORWARDED_TO_MANAGER, DISBURSED",
            "APPROVED, REJECTED",
            "APPROVED, UNDER_REVIEW"
    })
    void testInvalidTransitionsThrow(LoanStatus from, LoanStatus to) {
        assertThrows(IllegalStateTransitionException.class, () -> validator.validate(from, to));
    }

    @Test
    void testTerminalStateRejectedCannotTransition() {
        for (LoanStatus target : LoanStatus.values()) {
            assertThrows(IllegalStateTransitionException.class,
                    () -> validator.validate(LoanStatus.REJECTED, target));
        }
    }

    @Test
    void testTerminalStateDisbursedCannotTransition() {
        for (LoanStatus target : LoanStatus.values()) {
            assertThrows(IllegalStateTransitionException.class,
                    () -> validator.validate(LoanStatus.DISBURSED, target));
        }
    }

    @Test
    void testIsTerminal() {
        assertTrue(validator.isTerminal(LoanStatus.REJECTED));
        assertTrue(validator.isTerminal(LoanStatus.DISBURSED));
        assertFalse(validator.isTerminal(LoanStatus.DRAFT));
        assertFalse(validator.isTerminal(LoanStatus.SUBMITTED));
        assertFalse(validator.isTerminal(LoanStatus.UNDER_REVIEW));
        assertFalse(validator.isTerminal(LoanStatus.FORWARDED_TO_MANAGER));
        assertFalse(validator.isTerminal(LoanStatus.APPROVED));
    }

    @Test
    void testGetAllowedTransitionsFromDraft() {
        var allowed = validator.getAllowedTransitions(LoanStatus.DRAFT);
        assertEquals(1, allowed.size());
        assertTrue(allowed.contains(LoanStatus.SUBMITTED));
    }

    @Test
    void testGetAllowedTransitionsFromTerminal() {
        assertTrue(validator.getAllowedTransitions(LoanStatus.REJECTED).isEmpty());
        assertTrue(validator.getAllowedTransitions(LoanStatus.DISBURSED).isEmpty());
    }
}
