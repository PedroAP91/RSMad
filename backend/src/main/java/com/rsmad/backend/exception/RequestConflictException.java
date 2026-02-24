package com.rsmad.backend.exception;

public class RequestConflictException extends RuntimeException {

    public RequestConflictException(Long id, String estado) {
        super("Cannot assign resource to request " + id + " because it is in state " + estado);
    }
}
