package com.icesi.model;

public class Vehicle {

    private String id;
    private VehicleType type;
    private String location;
    private VehicleStatus status;
    private Incident assignedIncident;

    public Vehicle(String id, VehicleType type) {
        this(id, type, "Base Central");
    }

    public Vehicle(String id, VehicleType type, String location) {
        this.id = id;
        this.type = type;
        this.location = location;
        this.status = VehicleStatus.AVAILABLE;
        this.assignedIncident = null;
    }

    public String getId() {
        return id;
    }

    public VehicleType getType() {
        return type;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public VehicleStatus getStatus() {
        return status;
    }

    public void setStatus(VehicleStatus status) {
        this.status = status;
    }

    public Incident getAssignedIncident() {
        return assignedIncident;
    }

    public void setAssignedIncident(Incident assignedIncident) {
        this.assignedIncident = assignedIncident;
    }
}
