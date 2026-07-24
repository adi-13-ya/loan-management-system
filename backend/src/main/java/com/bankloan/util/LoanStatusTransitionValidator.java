package com.bankloan.util;

import com.bankloan.exception.IllegalStateTransitionException;
import com.bankloan.model.enums.LoanStatus;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;

@Component
public class LoanStatusTransitionValidator {

    private static final Map<LoanStatus, Set<LoanStatus>> ALLOWED_TRANSITIONS = Map.of(
            LoanStatus.DRAFT, Set.of(LoanStatus.SUBMITTED),
            LoanStatus.SUBMITTED, Set.of(LoanStatus.UNDER_REVIEW),
            LoanStatus.UNDER_REVIEW, Set.of(LoanStatus.FORWARDED_TO_MANAGER, LoanStatus.REJECTED),
            LoanStatus.FORWARDED_TO_MANAGER, Set.of(LoanStatus.APPROVED, LoanStatus.REJECTED),
            LoanStatus.APPROVED, Set.of(LoanStatus.DISBURSED),
            LoanStatus.REJECTED, Set.of(),
            LoanStatus.DISBURSED, Set.of()
    );

    /**
     * Validate that a transition from `from` to `to` is allowed.
     * @throws IllegalStateTransitionException if the transition is not allowed
     */
    public void validate(LoanStatus from, LoanStatus to) {
        Set<LoanStatus> allowed = ALLOWED_TRANSITIONS.get(from);
        if (allowed == null || !allowed.contains(to)) {
            throw new IllegalStateTransitionException(from, to);
        }
    }

    public boolean isTerminal(LoanStatus status) {
        return status == LoanStatus.REJECTED || status == LoanStatus.DISBURSED;
    }

    public Set<LoanStatus> getAllowedTransitions(LoanStatus from) {
        return ALLOWED_TRANSITIONS.getOrDefault(from, Set.of());
    }
}
