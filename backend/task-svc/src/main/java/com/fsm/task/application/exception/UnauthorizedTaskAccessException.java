package com.fsm.task.application.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when a technician tries to access or modify a task they are not authorized for.
 * Indicates that the technician is not assigned to the task.
 */
@ResponseStatus(HttpStatus.FORBIDDEN)
public class UnauthorizedTaskAccessException extends RuntimeException {
    
    public UnauthorizedTaskAccessException(String message) {
        super(message);
    }
}
