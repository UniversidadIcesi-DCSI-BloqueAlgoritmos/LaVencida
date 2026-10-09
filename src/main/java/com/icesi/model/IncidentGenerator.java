package com.icesi.model;

import java.util.Random;

public class IncidentGenerator {

    public static final int CRITICAL_CONGESTION_THRESHOLD = 5;
    public static final int STOPPED_CYCLES_THRESHOLD = 3;

    private int accidentCounter;
    private int theftCounter;
    private int fireCounter;
    private Random random;

    public IncidentGenerator() {
        this(new Random());
    }

    public IncidentGenerator(Random random) {
        this.random = random;
        this.accidentCounter = 0;
        this.theftCounter = 0;
        this.fireCounter = 0;
    }

    // Genera un incidente aleatorio de tipo THEFT en una zona de la ciudad.
    public Incident generateRandomTheft() {
        return null;
    }

    // Genera un incidente aleatorio de tipo FIRE en una zona de la ciudad.
    public Incident generateRandomFire() {
        return null;
    }

    // Criterio 1 de accidente: Dos vehiculos intentan ocupar la misma posicion (colision).
    public Incident checkCollisionAccident(String vehicle1Id, String vehicle2Id, String location) {
        return null;
    }

    // Criterio 2 de accidente: Una zona supera el limite maximo de vehiculos y genera congestion critica.
    public Incident checkCongestionAccident(String zone, int vehicleCount) {
        return null;
    }

    // Criterio 3 de accidente: Un vehiculo permanece detenido durante varios ciclos sobre una via.
    public Incident checkStoppedVehicleAccident(String vehicleId, String location, int stoppedCycles) {
        return null;
    }

    public int getAccidentCounter() {
        return accidentCounter;
    }

    public int getTheftCounter() {
        return theftCounter;
    }

    public int getFireCounter() {
        return fireCounter;
    }
}
