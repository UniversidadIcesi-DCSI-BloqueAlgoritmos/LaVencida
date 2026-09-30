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
        return null;
    }
}