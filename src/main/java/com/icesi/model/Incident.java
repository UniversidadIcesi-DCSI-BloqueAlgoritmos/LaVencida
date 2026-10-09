package com.icesi.model;

import java.time.LocalDateTime;

public class Incident {

    private String id;
    private IncidentType type;
    private String location;
    private Severity severity;
    private IncidentStatus status;
    private LocalDateTime generatedAt;
    private LocalDateTime resolvedAt;
    private String description;
    private Vehicle assignedVehicle;

    public Incident(String id, IncidentType type, String location, Severity severity) {
        this(id, type, location, severity, LocalDateTime.now());
    }

    public Incident(String id, IncidentType type, String location, Severity severity, LocalDateTime generatedAt) {
        this.id = id;
        this.type = type;
        this.location = location;
        this.severity = severity;
        this.status = IncidentStatus.PENDING;
        this.generatedAt = generatedAt;
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

    public LocalDateTime getResolvedAt() {
        return resolvedAt;
    }

    public void setResolvedAt(LocalDateTime resolvedAt) {
        this.resolvedAt = resolvedAt;
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

    public Vehicle getAssignedVehicle() {
        return assignedVehicle;
    }

    public void setAssignedVehicle(Vehicle vehicle) {
        this.assignedVehicle = vehicle;
    }
}
