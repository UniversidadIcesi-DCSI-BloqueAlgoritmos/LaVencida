package com.icesi.persistence;

import com.icesi.exceptions.PersistenceException;
import com.icesi.model.Incident;
import com.icesi.model.IncidentStatus;
import com.icesi.model.IncidentType;
import com.icesi.model.Severity;
import com.icesi.model.Vehicle;
import com.icesi.model.VehicleStatus;
import com.icesi.model.VehicleType;
import com.icesi.structures.LinkedList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class JsonPersistenceTest {

    private JsonPersistence jsonPersistence;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        jsonPersistence = new JsonPersistence();
    }

    // RF15 - Caso 1: Guardar y cargar vehiculos en formato JSON conserva todos sus datos (id, tipo, ubicacion y estado).
    @Test
    void saveAndLoadVehiclesPreservesAllVehicleData() {
        // Arrange
        Path file = tempDir.resolve("vehicles.json");
        LinkedList<Vehicle> original = new LinkedList<>();
        Vehicle patrol = new Vehicle("P-01", VehicleType.PATROL, "Estacion Norte");
        Vehicle ambulance = new Vehicle("A-01", VehicleType.AMBULANCE, "Hospital Sur");
        ambulance.setStatus(VehicleStatus.EN_ROUTE);
        original.addLast(patrol);
        original.addLast(ambulance);

        // Act
        jsonPersistence.saveVehicles(original, file.toString());
        LinkedList<Vehicle> loaded = jsonPersistence.loadVehicles(file.toString());

        // Assert
        assertNotNull(loaded);
        assertEquals(2, loaded.size());
        assertEquals("P-01", loaded.get(0).getId());
        assertEquals(VehicleType.PATROL, loaded.get(0).getType());
        assertEquals("Estacion Norte", loaded.get(0).getLocation());
        assertEquals(VehicleStatus.AVAILABLE, loaded.get(0).getStatus());

        assertEquals("A-01", loaded.get(1).getId());
        assertEquals(VehicleType.AMBULANCE, loaded.get(1).getType());
        assertEquals("Hospital Sur", loaded.get(1).getLocation());
        assertEquals(VehicleStatus.EN_ROUTE, loaded.get(1).getStatus());
    }

    // RF15 - Caso 2: Guardar y cargar incidentes en formato JSON conserva sus datos (id, tipo, ubicacion, gravedad, estado y descripcion).
    @Test
    void saveAndLoadIncidentsPreservesAllIncidentData() {
        // Arrange
        Path file = tempDir.resolve("incidents.json");
        LinkedList<Incident> original = new LinkedList<>();
        Incident inc1 = new Incident("INC-01", IncidentType.ACCIDENT, "Calle 5", Severity.HIGH);
        inc1.setDescription("Colision en interseccion");
        Incident inc2 = new Incident("INC-02", IncidentType.FIRE, "Zona Industrial", Severity.MEDIUM);
        inc2.setStatus(IncidentStatus.IN_PROGRESS);
        original.addLast(inc1);
        original.addLast(inc2);

        // Act
        jsonPersistence.saveIncidents(original, file.toString());
        LinkedList<Incident> loaded = jsonPersistence.loadIncidents(file.toString());

        // Assert
        assertNotNull(loaded);
        assertEquals(2, loaded.size());
        assertEquals("INC-01", loaded.get(0).getId());
        assertEquals(IncidentType.ACCIDENT, loaded.get(0).getType());
        assertEquals("Calle 5", loaded.get(0).getLocation());
        assertEquals(Severity.HIGH, loaded.get(0).getSeverity());
        assertEquals(IncidentStatus.PENDING, loaded.get(0).getStatus());
        assertEquals("Colision en interseccion", loaded.get(0).getDescription());

        assertEquals("INC-02", loaded.get(1).getId());
        assertEquals(IncidentType.FIRE, loaded.get(1).getType());
        assertEquals(IncidentStatus.IN_PROGRESS, loaded.get(1).getStatus());
    }

    // RF18 - Caso 3: Intentar cargar un archivo JSON que no existe lanza PersistenceException.
    @Test
    void loadVehiclesFromNonExistentFileThrowsPersistenceException() {
        // Arrange
        Path nonExistent = tempDir.resolve("no_existe.json");

        // Act & Assert
        assertThrows(PersistenceException.class,
                () -> jsonPersistence.loadVehicles(nonExistent.toString()));
    }

    // RF18 - Caso 4: Intentar cargar un archivo con formato JSON invalido o corrupto lanza PersistenceException.
    @Test
    void loadIncidentsFromInvalidJsonThrowsPersistenceException() throws IOException {
        // Arrange
        Path invalidFile = tempDir.resolve("corrupto.json");
        Files.writeString(invalidFile, "{ json_invalido_sin_cerrar ");

        // Act & Assert
        assertThrows(PersistenceException.class,
                () -> jsonPersistence.loadIncidents(invalidFile.toString()));
    }
}
