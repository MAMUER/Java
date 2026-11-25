package com.example.userservice.exception;

import java.util.List;

public class ValidationException extends RuntimeException {

    private final List<String> violations;

    public ValidationException(List<String> violations) {
        super(String.join("; ", violations));
        this.violations = List.copyOf(violations);
    }

    public ValidationException(List<String> violations, Throwable cause) {
        super(String.join("; ", violations), cause);
        this.violations = List.copyOf(violations);
    }

    public List<String> getViolations() {
        return violations;
    }
}
