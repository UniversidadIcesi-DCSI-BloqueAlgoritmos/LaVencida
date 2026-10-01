package com.icesi.model;

import com.icesi.structures.LinkedList;

public class IncidentManager {

    private LinkedList<Incident> incidents;

    public IncidentManager() {
        this.incidents = new LinkedList<>();
    }

    public void registerIncident(Incident incident) {
        if (incident == null) {
            throw new IllegalArgumentException("El incidente no puede ser nulo");
        }
        if (existsIncidentWithId(incident.getId())) {
            throw new DuplicateIncidentException("Ya existe un incidente con id " + incident.getId());
        }
        incidents.addLast(incident);
    }

    private boolean existsIncidentWithId(String id) {
        for (int i = 0; i < incidents.size(); i++) {
            if (incidents.get(i).getId().equals(id)) {
                return true;
            }
        }
        return false;
    }

    public int getIncidentCount() {
        return incidents.size();
    }

    public Incident getIncidentAt(int index) {
        return incidents.get(index);
    }

    public Incident getHighestPriorityIncident() {
        if (incidents.isEmpty()) {
            throw new NoActiveIncidentsException("No hay incidentes activos registrados");
        }

        Incident best = incidents.get(0);
        for (int i = 1; i < incidents.size(); i++) {
            Incident current = incidents.get(i);
            if (isHigherPriority(current, best)) {
                best = current;
            }
        }
        return best;
    }

    private boolean isHigherPriority(Incident candidate, Incident current) {
        int severityComparison = candidate.getSeverity().ordinal() - current.getSeverity().ordinal();
        if (severityComparison < 0) {
            return true; // candidate tiene mayor gravedad (ordinal menor)
        }
        if (severityComparison > 0) {
            return false; // current tiene mayor gravedad
        }
        // misma gravedad: gana el más antiguo (generatedAt menor)
        return candidate.getGeneratedAt().isBefore(current.getGeneratedAt());
    }

    public void assignVehicle(Vehicle vehicle, Incident incident) {

    }
}