package com.icesi.persistence;

import com.icesi.exceptions.PersistenceException;
import com.icesi.model.Incident;
import com.icesi.model.IncidentManager;
import com.icesi.model.IncidentStatus;
import com.icesi.model.IncidentType;
import com.icesi.model.Severity;
import com.icesi.model.Vehicle;
import com.icesi.model.VehicleStatus;
import com.icesi.model.VehicleType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class SerializationPersistenceTest {

    private SerializationPersistence serializationPersistence;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        serializationPersistence = new SerializationPersistence();
    }

    // RF16 - Caso 1: Serializar y deserializar el estado de IncidentManager conserva los incidentes y sus vehiculos asignados.
    @Test
    void saveAndLoadManagerStatePreservesIncidentsAndAssignedVehicles() {
        // Arrange
        Path file = tempDir.resolve("simulation_state.dat");
        IncidentManager originalManager = new IncidentManager();
        Incident inc1 = new Incident("INC-01", IncidentType.ACCIDENT, "Avenida 6", Severity.HIGH);
        Incident inc2 = new Incident("INC-02", IncidentType.THEFT, "Centro", Severity.MEDIUM);
        Vehicle ambulance = new Vehicle("A-01", VehicleType.AMBULANCE, "Clinica Norte");

        originalManager.registerIncident(inc1);
        originalManager.registerIncident(inc2);
        originalManager.assignVehicle(ambulance, inc1);

        // Act
        serializationPersistence.saveManagerState(originalManager, file.toString());
        IncidentManager loadedManager = serializationPersistence.loadManagerState(file.toString());

        // Assert
        assertNotNull(loadedManager);
        assertEquals(2, loadedManager.getIncidentCount());

        Incident loadedInc1 = loadedManager.findIncidentById("INC-01");
        assertEquals(IncidentStatus.IN_PROGRESS, loadedInc1.getStatus());
        assertNotNull(loadedInc1.getAssignedVehicle());
        assertEquals("A-01", loadedInc1.getAssignedVehicle().getId());
        assertEquals(VehicleStatus.EN_ROUTE, loadedInc1.getAssignedVehicle().getStatus());

        Incident loadedInc2 = loadedManager.findIncidentById("INC-02");
        assertEquals(IncidentStatus.PENDING, loadedInc2.getStatus());
    }

    // RF16 - Caso 2: Serializar y deserializar un IncidentManager sin incidentes conserva el estado vacio.
    @Test
    void saveAndLoadManagerStatePreservesEmptyManager() {
        // Arrange
        Path file = tempDir.resolve("empty_state.dat");
        IncidentManager emptyManager = new IncidentManager();

        // Act
        serializationPersistence.saveManagerState(emptyManager, file.toString());
        IncidentManager loaded = serializationPersistence.loadManagerState(file.toString());

        // Assert
        assertNotNull(loaded);
        assertEquals(0, loaded.getIncidentCount());
        assertEquals(0, loaded.getActiveIncidentCount());
    }

    // RF18 - Caso 3: Intentar deserializar desde una ruta que no existe lanza PersistenceException.
    @Test
    void loadManagerStateFromNonExistentFileThrowsPersistenceException() {
        // Arrange
        Path nonExistent = tempDir.resolve("no_existe.dat");

        // Act & Assert
        assertThrows(PersistenceException.class,
                () -> serializationPersistence.loadManagerState(nonExistent.toString()));
    }

    // RF18 - Caso 4: Intentar deserializar un archivo corrupto o no binario lanza PersistenceException.
    @Test
    void loadManagerStateFromCorruptedFileThrowsPersistenceException() throws IOException {
        // Arrange
        Path corrupted = tempDir.resolve("corrupto.dat");
        Files.writeString(corrupted, "contenido texto no serializado");

        // Act & Assert
        assertThrows(PersistenceException.class,
                () -> serializationPersistence.loadManagerState(corrupted.toString()));
    }
}
