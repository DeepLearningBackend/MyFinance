package com.myfinance.exception;

/**
 * Custom runtime exception thrown when entity or input validation rules fail.
 * Holds optional field name indicating which specific attribute caused the validation error.
 */
public class ValidationException extends RuntimeException {

    private final String fieldName;

    public ValidationException(String message) {
        super(message);
        this.fieldName = null;
    }

    public ValidationException(String fieldName, String message) {
        super(message);
        this.fieldName = fieldName;
    }

    public String getFieldName() {
        return fieldName;
    }
}
