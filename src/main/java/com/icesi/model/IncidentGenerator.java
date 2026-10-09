package com.icesi.model;

import java.util.Random;

public class IncidentGenerator {

    public static final int CRITICAL_CONGESTION_THRESHOLD = 5;
    public static final int STOPPED_CYCLES_THRESHOLD = 3;

    private static final String[] THEFT_LOCATIONS = {
            "Zona Comercial Centro",
            "Sector La Estacion",
            "Centro Comercial Unicentro",
            "Parque Bolivar"
    };

    private static final String[] FIRE_LOCATIONS = {
            "Zona Residencial Las Palmas",
            "Zona Industrial Cencar",
            "Barrio El Prado",
            "Barrio Zamorano"
    };

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
        theftCounter++;
        String id = String.format("THEFT-%03d", theftCounter);
        String location = THEFT_LOCATIONS[random.nextInt(THEFT_LOCATIONS.length)];
        Severity severity = random.nextBoolean() ? Severity.HIGH : Severity.MEDIUM;

        Incident incident = new Incident(id, IncidentType.THEFT, location, severity);
        incident.setDescription("Robo reportado en " + location);
        return incident;
    }

    // Genera un incidente aleatorio de tipo FIRE en una zona de la ciudad.
    public Incident generateRandomFire() {
        fireCounter++;
        String id = String.format("FIRE-%03d", fireCounter);
        String location = FIRE_LOCATIONS[random.nextInt(FIRE_LOCATIONS.length)];
        Severity severity = random.nextBoolean() ? Severity.HIGH : Severity.MEDIUM;

        Incident incident = new Incident(id, IncidentType.FIRE, location, severity);
        incident.setDescription("Incendio reportado en " + location);
        return incident;
    }

    // Criterio 1 de accidente: Dos vehiculos intentan ocupar la misma posicion (colision).
    public Incident checkCollisionAccident(String vehicle1Id, String vehicle2Id, String location) {
        if (vehicle1Id == null || vehicle2Id == null || vehicle1Id.equals(vehicle2Id)) {
            return null;
        }

        accidentCounter++;
        String id = String.format("ACC-%03d", accidentCounter);
        String desc = "Accidente por colision entre vehiculo " + vehicle1Id + " y vehiculo " + vehicle2Id + " en " + location;

        Incident incident = new Incident(id, IncidentType.ACCIDENT, location, Severity.HIGH);
        incident.setDescription(desc);
        return incident;
    }

    // Criterio 2 de accidente: Una zona supera el limite maximo de vehiculos y genera congestion critica.
    public Incident checkCongestionAccident(String zone, int vehicleCount) {
        if (zone == null || vehicleCount <= CRITICAL_CONGESTION_THRESHOLD) {
            return null;
        }

        accidentCounter++;
        String id = String.format("ACC-%03d", accidentCounter);
        String desc = "Accidente por congestion critica en " + zone + " (" + vehicleCount + " vehiculos acumulados)";

        Incident incident = new Incident(id, IncidentType.ACCIDENT, zone, Severity.MEDIUM);
        incident.setDescription(desc);
        return incident;
    }

    // Criterio 3 de accidente: Un vehiculo permanece detenido durante varios ciclos sobre una via.
    public Incident checkStoppedVehicleAccident(String vehicleId, String location, int stoppedCycles) {
        if (vehicleId == null || location == null || stoppedCycles < STOPPED_CYCLES_THRESHOLD) {
            return null;
        }

        accidentCounter++;
        String id = String.format("ACC-%03d", accidentCounter);
        String desc = "Accidente generado por vehiculo " + vehicleId + " detenido durante " + stoppedCycles + " ciclos en " + location;

        Incident incident = new Incident(id, IncidentType.ACCIDENT, location, Severity.MEDIUM);
        incident.setDescription(desc);
        return incident;
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
