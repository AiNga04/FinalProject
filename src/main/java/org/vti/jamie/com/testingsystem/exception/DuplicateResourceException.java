package org.vti.jamie.com.testingsystem.exception;

public class DuplicateResourceException
        extends ResourceConflictException {

    public DuplicateResourceException(String message) {
        super(message);
    }
}