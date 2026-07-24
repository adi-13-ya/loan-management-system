package com.bankloan.exception;

import com.bankloan.model.enums.LoanStatus;

public class IllegalStateTransitionException extends RuntimeException {
    public IllegalStateTransitionException(LoanStatus from, LoanStatus to) {
        super(String.format("Illegal state transition from %s to %s", from, to));
    }
}
