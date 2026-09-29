package com.icesi.model;

import java.time.LocalDateTime;

public class Incident {

    private String id;
    private IncidentType type;
    private String location;
    private Severity severity;
    private IncidentStatus status;
    private LocalDateTime generatedAt;
    private String description;

    public Incident(String id, IncidentType type, String location, Severity severity) {
    }

    public String getId() {
        return null;
    }

    public IncidentType getType() {
        return null;
    }

    public String getLocation() {
        return null;
    }

    public Severity getSeverity() {
        return null;
    }

    public IncidentStatus getStatus() {
        return null;
    }

    public LocalDateTime getGeneratedAt() {
        return null;
    }

    public String getDescription() {
        return null;
    }

    public void setDescription(String description) {
    }

    public void setStatus(IncidentStatus status) {
    }
}
