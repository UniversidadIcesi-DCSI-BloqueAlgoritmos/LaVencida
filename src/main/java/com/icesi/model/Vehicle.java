package com.icesi.model;

public class Vehicle {

    private String id;
    private VehicleType type;
    private VehicleStatus status;

    public Vehicle(String id, VehicleType type) {
        this.id = id;
        this.type = type;
        this.status = VehicleStatus.AVAILABLE;
    }

    public String getId() {
        return id;
    }

    public VehicleType getType() {
        return type;
    }

    public VehicleStatus getStatus() {
        return status;
    }

    public void setStatus(VehicleStatus status) {
        this.status = status;
    }
}
