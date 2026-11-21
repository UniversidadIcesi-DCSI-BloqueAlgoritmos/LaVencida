package com.icesi.persistence;

import com.icesi.exceptions.PersistenceException;
import com.icesi.model.Incident;
import com.icesi.model.IncidentStatus;
import com.icesi.model.IncidentType;
import com.icesi.model.Operator;
import com.icesi.model.Severity;
import com.icesi.model.Vehicle;
import com.icesi.model.VehicleStatus;
import com.icesi.model.VehicleType;
import com.icesi.structures.LinkedList;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;

public class JsonPersistence {

    public JsonPersistence() {
    }

    // Guarda la lista de vehiculos en un archivo JSON.
    public void saveVehicles(LinkedList<Vehicle> vehicles, String filePath) {
        if (vehicles == null || filePath == null || filePath.isBlank()) {
            throw new IllegalArgumentException("La lista de vehiculos y la ruta no pueden ser nulas");
        }
        try {
            JSONArray array = new JSONArray();
            for (Vehicle vehicle : vehicles) {
                JSONObject obj = new JSONObject();
                obj.put("id", vehicle.getId());
                obj.put("type", vehicle.getType().name());
                obj.put("location", vehicle.getLocation());
                obj.put("status", vehicle.getStatus().name());
                array.put(obj);
            }
            Path path = Path.of(filePath);
            if (path.getParent() != null) {
                Files.createDirectories(path.getParent());
            }
            Files.writeString(path, array.toString(2));
        } catch (IOException | JSONException e) {
            throw new PersistenceException("Error al guardar los vehiculos en JSON: " + e.getMessage(), e);
        }
    }

    // Carga y reconstruye la lista de vehiculos desde un archivo JSON.
    public LinkedList<Vehicle> loadVehicles(String filePath) {
        if (filePath == null || filePath.isBlank()) {
            throw new IllegalArgumentException("La ruta del archivo no puede ser nula");
        }
        try {
            String content = Files.readString(Path.of(filePath));
            JSONArray array = new JSONArray(content);
            LinkedList<Vehicle> vehicles = new LinkedList<>();

            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.getJSONObject(i);
                String id = obj.getString("id");
                VehicleType type = VehicleType.valueOf(obj.getString("type"));
                String location = obj.optString("location", "Base Central");
                VehicleStatus status = VehicleStatus.valueOf(obj.optString("status", VehicleStatus.AVAILABLE.name()));

                Vehicle vehicle = new Vehicle(id, type, location);
                vehicle.setStatus(status);
                vehicles.addLast(vehicle);
            }
            return vehicles;
        } catch (IOException | JSONException | IllegalArgumentException e) {
            throw new PersistenceException("Error al cargar los vehiculos desde JSON: " + e.getMessage(), e);
        }
    }

    // Guarda la lista de incidentes en un archivo JSON.
    public void saveIncidents(LinkedList<Incident> incidents, String filePath) {
        if (incidents == null || filePath == null || filePath.isBlank()) {
            throw new IllegalArgumentException("La lista de incidentes y la ruta no pueden ser nulas");
        }
        try {
            JSONArray array = new JSONArray();
            for (Incident incident : incidents) {
                JSONObject obj = new JSONObject();
                obj.put("id", incident.getId());
                obj.put("type", incident.getType().name());
                obj.put("location", incident.getLocation());
                obj.put("severity", incident.getSeverity().name());
                obj.put("status", incident.getStatus().name());
                if (incident.getGeneratedAt() != null) {
                    obj.put("generatedAt", incident.getGeneratedAt().toString());
                }
                if (incident.getDescription() != null) {
                    obj.put("description", incident.getDescription());
                }
                // se guarda el id del vehiculo para volver a unirlos al cargar
                if (incident.getAssignedVehicle() != null) {
                    obj.put("assignedVehicleId", incident.getAssignedVehicle().getId());
                }
                array.put(obj);
            }
            Path path = Path.of(filePath);
            if (path.getParent() != null) {
                Files.createDirectories(path.getParent());
            }
            Files.writeString(path, array.toString(2));
        } catch (IOException | JSONException e) {
            throw new PersistenceException("Error al guardar los incidentes en JSON: " + e.getMessage(), e);
        }
    }

    // Carga y reconstruye la lista de incidentes desde un archivo JSON.
    public LinkedList<Incident> loadIncidents(String filePath) {
        if (filePath == null || filePath.isBlank()) {
            throw new IllegalArgumentException("La ruta del archivo no puede ser nula");
        }
        try {
            String content = Files.readString(Path.of(filePath));
            JSONArray array = new JSONArray(content);
            LinkedList<Incident> incidents = new LinkedList<>();

            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.getJSONObject(i);
                String id = obj.getString("id");
                IncidentType type = IncidentType.valueOf(obj.getString("type"));
                String location = obj.getString("location");
                Severity severity = Severity.valueOf(obj.getString("severity"));
                IncidentStatus status = IncidentStatus.valueOf(obj.optString("status", IncidentStatus.PENDING.name()));

                LocalDateTime generatedAt = obj.has("generatedAt")
                        ? LocalDateTime.parse(obj.getString("generatedAt"))
                        : LocalDateTime.now();

                Incident incident = new Incident(id, type, location, severity, generatedAt);
                incident.setStatus(status);
                if (obj.has("description")) {
                    incident.setDescription(obj.getString("description"));
                }
                incidents.addLast(incident);
            }
            return incidents;
        } catch (IOException | JSONException | IllegalArgumentException e) {
            throw new PersistenceException("Error al cargar los incidentes desde JSON: " + e.getMessage(), e);
        }
    }

    // Guarda la posicion y el puntaje del operador en JSON.
    public void saveOperator(Operator operator, String filePath) {
        if (operator == null || filePath == null || filePath.isBlank()) {
            throw new IllegalArgumentException("El operador y la ruta no pueden ser nulos");
        }
        try {
            JSONObject obj = new JSONObject();
            obj.put("row", operator.getRow());
            obj.put("column", operator.getColumn());
            obj.put("score", operator.getScore());
            Path path = Path.of(filePath);
            if (path.getParent() != null) {
                Files.createDirectories(path.getParent());
            }
            Files.writeString(path, obj.toString(2));
        } catch (IOException | JSONException e) {
            throw new PersistenceException("Error al guardar el operador en JSON: " + e.getMessage(), e);
        }
    }

    // Carga el operador (posicion y puntaje) desde JSON.
    public Operator loadOperator(String filePath) {
        if (filePath == null || filePath.isBlank()) {
            throw new IllegalArgumentException("La ruta del archivo no puede ser nula");
        }
        try {
            JSONObject obj = new JSONObject(Files.readString(Path.of(filePath)));
            Operator operator = new Operator(obj.getInt("row"), obj.getInt("column"));
            operator.addScore(obj.optInt("score", 0));
            return operator;
        } catch (IOException | JSONException e) {
            throw new PersistenceException("Error al cargar el operador desde JSON: " + e.getMessage(), e);
        }
    }

    // Carga los incidentes y los vuelve a unir con su vehiculo asignado (buscandolo por id en la lista).
    public LinkedList<Incident> loadIncidents(String filePath, LinkedList<Vehicle> vehicles) {
        LinkedList<Incident> incidents = loadIncidents(filePath);
        try {
            JSONArray array = new JSONArray(Files.readString(Path.of(filePath)));
            for (int i = 0; i < array.length(); i++) {
                String vehicleId = array.getJSONObject(i).optString("assignedVehicleId", null);
                if (vehicleId == null) {
                    continue;
                }
                Incident incident = incidents.get(i);
                for (Vehicle vehicle : vehicles) {
                    if (vehicle.getId().equals(vehicleId)) {
                        incident.setAssignedVehicle(vehicle);
                        vehicle.setAssignedIncident(incident);
                    }
                }
            }
            return incidents;
        } catch (IOException | JSONException e) {
            throw new PersistenceException("Error al unir incidentes y vehiculos: " + e.getMessage(), e);
        }
    }
}
