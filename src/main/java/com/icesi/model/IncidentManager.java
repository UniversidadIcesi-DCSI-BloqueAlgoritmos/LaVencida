package com.icesi.model;

import com.icesi.exceptions.DuplicateIncidentException;
import com.icesi.exceptions.IncidentNotFoundException;
import com.icesi.exceptions.IncidentStateException;
import com.icesi.exceptions.NoActiveIncidentsException;
import com.icesi.exceptions.VehicleAssignmentException;
import com.icesi.exceptions.VehicleReleaseException;
import com.icesi.structures.LinkedList;

import java.time.Duration;
import java.time.LocalDateTime;

public class IncidentManager {

    private LinkedList<Incident> incidents;

    // Crea el gestor con la lista de incidentes vacia.
    public IncidentManager() {
        this.incidents = new LinkedList<>();
    }

    // Registra un incidente al final de la lista; lanza IllegalArgumentException si es nulo y DuplicateIncidentException si ya existe uno con el mismo id.
    public void registerIncident(Incident incident) {
        if (incident == null) {
            throw new IllegalArgumentException("El incidente no puede ser nulo");
        }
        if (existsIncidentWithId(incident.getId())) {
            throw new DuplicateIncidentException("Ya existe un incidente con id " + incident.getId());
        }
        incidents.addLast(incident);
    }

    // Indica si ya hay un incidente registrado con ese id.
    private boolean existsIncidentWithId(String id) {
        for (Incident current : incidents) {
            if (current.getId().equals(id)) {
                return true;
            }
        }
        return false;
    }

    // Devuelve la cantidad de incidentes registrados.
    public int getIncidentCount() {
        return incidents.size();
    }

    // Devuelve el incidente que esta en la posicion indicada (en orden de registro).
    public Incident getIncidentAt(int index) {
        return incidents.get(index);
    }

    // Devuelve el incidente de mayor prioridad (gravedad HIGH, MEDIUM, LOW; si empatan, el mas antiguo); lanza NoActiveIncidentsException si no hay incidentes.
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

    // Indica si el candidato tiene mas prioridad que el actual: primero por gravedad y, si es igual, por antiguedad.
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

    // Asigna un vehiculo a un incidente pendiente; lanza VehicleAssignmentException si el incidente no esta pendiente, el vehiculo no esta disponible o no es compatible. El incidente pasa a IN_PROGRESS y el vehiculo a EN_ROUTE.
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

    // Indica si el tipo de vehiculo puede atender el tipo de incidente (patrulla: robo y accidente; ambulancia: accidente; camion de bomberos: incendio).
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

    public static final int HIGH_SEVERITY_POINTS = 100;
    public static final int MEDIUM_SEVERITY_POINTS = 70;
    public static final int LOW_SEVERITY_POINTS = 40;
    public static final int TIME_BONUS_POINTS = 20;

    public static final long HIGH_MAX_DURATION_SECONDS = 300;   // 5 min
    public static final long MEDIUM_MAX_DURATION_SECONDS = 600;  // 10 min
    public static final long LOW_MAX_DURATION_SECONDS = 900;     // 15 min

    // Finaliza la atencion de un incidente en proceso: queda RESOLVED y su vehiculo vuelve a estar AVAILABLE; lanza IncidentStateException si no esta IN_PROGRESS.
    public void finishAttention(Incident incident) {
        if (incident.getStatus() != IncidentStatus.IN_PROGRESS) {
            throw new IncidentStateException("El incidente no esta en proceso de atencion");
        }
        incident.setStatus(IncidentStatus.RESOLVED);
        Vehicle vehicle = incident.getAssignedVehicle();
        vehicle.setStatus(VehicleStatus.AVAILABLE);
    }

    // Calcula el puntaje obtenido por resolver un incidente considerando su gravedad y tiempo de atencion.
    public int calculateScore(Incident incident, LocalDateTime resolvedAt) {
        if (incident == null) {
            throw new IllegalArgumentException("El incidente no puede ser nulo");
        }
        int basePoints;
        long maxDuration;
        if (incident.getSeverity() == Severity.HIGH) {
            basePoints = HIGH_SEVERITY_POINTS;
            maxDuration = HIGH_MAX_DURATION_SECONDS;
        } else if (incident.getSeverity() == Severity.MEDIUM) {
            basePoints = MEDIUM_SEVERITY_POINTS;
            maxDuration = MEDIUM_MAX_DURATION_SECONDS;
        } else {
            basePoints = LOW_SEVERITY_POINTS;
            maxDuration = LOW_MAX_DURATION_SECONDS;
        }

        int bonus = 0;
        if (incident.getGeneratedAt() != null && resolvedAt != null) {
            long elapsedSeconds = Duration.between(incident.getGeneratedAt(), resolvedAt).getSeconds();
            if (elapsedSeconds >= 0 && elapsedSeconds <= maxDuration) {
                bonus = TIME_BONUS_POINTS;
            }
        }
        return basePoints + bonus;
    }

    // Finaliza la atencion de un incidente, registra la hora de resolucion y actualiza el puntaje del operador.
    public int finishAttention(Incident incident, Operator operator, LocalDateTime resolvedAt) {
        finishAttention(incident);
        incident.setResolvedAt(resolvedAt);
        int points = calculateScore(incident, resolvedAt);
        if (operator != null) {
            operator.addScore(points);
        }
        return points;
    }

    // Finaliza la atencion de un incidente en la hora actual y actualiza el puntaje del operador.
    public int finishAttention(Incident incident, Operator operator) {
        return finishAttention(incident, operator, LocalDateTime.now());
    }

    // Libera un vehiculo que esta atendiendo un incidente: el vehiculo queda AVAILABLE y el incidente vuelve a PENDING; lanza IllegalArgumentException si es nulo y VehicleReleaseException si ya esta disponible o no atiende ningun incidente.
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

    // Busca el incidente en proceso que tiene asignado el vehiculo; devuelve null si no hay ninguno.
    private Incident findInProgressIncidentOf(Vehicle vehicle) {
        for (Incident current : incidents) {
            if (current.getStatus() == IncidentStatus.IN_PROGRESS && current.getAssignedVehicle() == vehicle) {
                return current;
            }
        }
        return null;
    }

    // Devuelve una copia de los incidentes ordenada por prioridad, sin cambiar el orden de registro original.
    public LinkedList<Incident> getIncidentsSortedByPriority() {
        // ordenamos una copia para no cambiar el orden de registro original
        LinkedList<Incident> sorted = copyIncidents();
        sorted.sort(new IncidentPriorityComparator());
        return sorted;
    }

    // Busca un incidente por id con busqueda binaria sobre una copia ordenada por id; lanza IncidentNotFoundException si no existe.
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

    // Devuelve una copia de la lista de incidentes (los mismos incidentes en una lista nueva).
    private LinkedList<Incident> copyIncidents() {
        LinkedList<Incident> copy = new LinkedList<>();
        for (Incident current : incidents) {
            copy.addLast(current);
        }
        return copy;
    }

    // RF13: Retorna la cantidad total de incidentes activos (PENDING o IN_PROGRESS).
    public int getActiveIncidentCount() {
        return 0;
    }

    // RF13: Retorna la cantidad de accidentes activos.
    public int getActiveAccidentCount() {
        return 0;
    }

    // RF13: Retorna la cantidad de robos activos.
    public int getActiveTheftCount() {
        return 0;
    }

    // RF13: Retorna la cantidad de incendios activos.
    public int getActiveFireCount() {
        return 0;
    }

    // RF13: Retorna la cantidad de vehiculos en estado AVAILABLE dentro de la lista proporcionada.
    public int countAvailableVehicles(LinkedList<Vehicle> vehicles) {
        return 0;
    }
}