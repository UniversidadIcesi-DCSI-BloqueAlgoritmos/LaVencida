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
        this.id = id;
        this.type = type;
        this.location = location;
        this.severity = severity;
        this.status = IncidentStatus.PENDING;
        this.generatedAt = LocalDateTime.now();
    }

    public String getId() {
        return id;
    }

    public IncidentType getType() {
        return type;
    }

    public String getLocation() {
        return location;
    }

    public Severity getSeverity() {
        return severity;
    }

    public IncidentStatus getStatus() {
        return status;
    }

    public LocalDateTime getGeneratedAt() {
        return generatedAt;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setStatus(IncidentStatus status) {
        this.status = status;
    }
}
