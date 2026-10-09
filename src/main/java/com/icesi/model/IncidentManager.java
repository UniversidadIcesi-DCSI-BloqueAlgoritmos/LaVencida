package com.icesi.model;

import com.icesi.exceptions.DuplicateIncidentException;
import com.icesi.exceptions.IncidentNotFoundException;
import com.icesi.exceptions.IncidentStateException;
import com.icesi.exceptions.NoActiveIncidentsException;
import com.icesi.exceptions.VehicleAssignmentException;
import com.icesi.exceptions.VehicleReleaseException;
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
        if (incident.getStatus() != IncidentStatus.PENDING) {
            throw new VehicleAssignmentException("El incidente no esta pendiente de asignacion");
        }
        if (vehicle.getStatus() != VehicleStatus.AVAILABLE) {
            throw new VehicleAssignmentException("El vehiculo no esta disponible");
        }
        if (!isCompatible(vehicle.getType(), incident.getType())) {
            throw new VehicleAssignmentException("El vehiculo no es compatible con el tipo de incidente");
        }

        incident.setStatus(IncidentStatus.IN_PROGRESS);
        incident.setAssignedVehicle(vehicle);
        vehicle.setStatus(VehicleStatus.EN_ROUTE);
    }

    private boolean isCompatible(VehicleType vehicleType, IncidentType incidentType) {
        if (vehicleType == VehicleType.PATROL) {
            return incidentType == IncidentType.THEFT || incidentType == IncidentType.ACCIDENT;
        }
        if (vehicleType == VehicleType.AMBULANCE) {
            return incidentType == IncidentType.ACCIDENT;
        }
        if (vehicleType == VehicleType.FIRE_TRUCK) {
            return incidentType == IncidentType.FIRE;
        }
        return false;
    }

    public void finishAttention(Incident incident) {
        if (incident.getStatus() != IncidentStatus.IN_PROGRESS) {
            throw new IncidentStateException("El incidente no esta en proceso de atencion");
        }
        incident.setStatus(IncidentStatus.RESOLVED);
        Vehicle vehicle = incident.getAssignedVehicle();
        vehicle.setStatus(VehicleStatus.AVAILABLE);
    }

    public void releaseVehicle(Vehicle vehicle) {
        if (vehicle == null) {
            throw new IllegalArgumentException("El vehiculo no puede ser nulo");
        }
        if (vehicle.getStatus() == VehicleStatus.AVAILABLE) {
            throw new VehicleReleaseException("El vehiculo ya esta disponible");
        }

        // buscamos el incidente en proceso que tiene este vehiculo asignado
        Incident incident = findInProgressIncidentOf(vehicle);
        if (incident == null) {
            throw new VehicleReleaseException("El vehiculo no esta atendiendo ningun incidente");
        }

        // el incidente vuelve a quedar pendiente para poder asignarle otro vehiculo
        incident.setAssignedVehicle(null);
        incident.setStatus(IncidentStatus.PENDING);
        vehicle.setStatus(VehicleStatus.AVAILABLE);
    }

    private Incident findInProgressIncidentOf(Vehicle vehicle) {
        for (int i = 0; i < incidents.size(); i++) {
            Incident current = incidents.get(i);
            if (current.getStatus() == IncidentStatus.IN_PROGRESS && current.getAssignedVehicle() == vehicle) {
                return current;
            }
        }
        return null;
    }

    public LinkedList<Incident> getIncidentsSortedByPriority() {
        // ordenamos una copia para no cambiar el orden de registro original
        LinkedList<Incident> sorted = copyIncidents();
        sorted.sort(new IncidentPriorityComparator());
        return sorted;
    }

    public Incident findIncidentById(String id) {
        IncidentIdComparator comparator = new IncidentIdComparator();
        LinkedList<Incident> sortedById = copyIncidents();
        sortedById.sort(comparator);

        // incidente "falso" que solo sirve para comparar el ID en la busqueda binaria
        Incident target = new Incident(id, null, null, null);
        int index = sortedById.binarySearch(target, comparator);
        if (index == -1) {
            throw new IncidentNotFoundException("No existe un incidente con id " + id);
        }
        return sortedById.get(index);
    }

    private LinkedList<Incident> copyIncidents() {
        LinkedList<Incident> copy = new LinkedList<>();
        for (int i = 0; i < incidents.size(); i++) {
            copy.addLast(incidents.get(i));
        }
        return copy;
    }
}