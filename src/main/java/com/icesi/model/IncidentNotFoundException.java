package com.icesi.model;

public class IncidentNotFoundException extends RuntimeException {

    public IncidentNotFoundException(String message) {
        super(message);
    }
}
