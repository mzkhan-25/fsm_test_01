package com.fsm.task.application.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when a task status transition is invalid.
 * Indicates that the requested status change violates domain invariants.
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidStatusTransitionException extends RuntimeException {
    
    public InvalidStatusTransitionException(String message) {
        super(message);
    }
}
