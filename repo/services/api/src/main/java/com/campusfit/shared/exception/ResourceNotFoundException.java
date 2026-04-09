package com.campusfit.shared.exception;

public class ResourceNotFoundException extends BusinessException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException(String entityType, Object id) {
        super(String.format("%s not found with id: %s", entityType, id));
    }
}
