package com.icesi.controller;

import com.icesi.model.CityMap;
import com.icesi.model.Incident;
import com.icesi.model.IncidentStatus;
import com.icesi.model.IncidentType;
import com.icesi.model.Operator;
import com.icesi.model.Severity;
import com.icesi.model.Vehicle;
import com.icesi.model.VehicleStatus;
import com.icesi.model.VehicleType;
import com.icesi.persistence.JsonPersistence;
import com.icesi.structures.LinkedList;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

// Pruebas de guardar y cargar la partida en JSON (F5 / F9).
class SaveGameTest {

    @TempDir
    Path folder;

    private GameController createController(int seed) {
        return new GameController(new CityMap(), new Operator(8, 8),
                GameController.createInitialVehicles(), new Random(seed));
    }

    // Caso 1: Guardar y cargar el operador conserva fila, columna y puntaje.
    @Test
    void operatorKeepsPositionAndScore() {
        // Arrange
        JsonPersistence json = new JsonPersistence();
        Operator operator = new Operator(8, 10);
        operator.addScore(340);
        String file = folder.resolve("operator.json").toString();

        // Act
        json.saveOperator(operator, file);
        Operator loaded = json.loadOperator(file);

        // Assert
        assertEquals(8, loaded.getRow());
        assertEquals(10, loaded.getColumn());
        assertEquals(340, loaded.getScore());
    }

    // Caso 2: Un incidente en proceso se carga unido a su vehiculo.
    @Test
    void inProgressIncidentIsLinkedToItsVehicle() {
        // Arrange
        JsonPersistence json = new JsonPersistence();
        Vehicle patrol = new Vehicle("P-01", VehicleType.PATROL, "Estacion Norte");
        Incident theft = new Incident("THEFT-001", IncidentType.THEFT, "Parque Bolivar", Severity.MEDIUM);
        theft.setStatus(IncidentStatus.IN_PROGRESS);
        theft.setAssignedVehicle(patrol);
        patrol.setStatus(VehicleStatus.EN_ROUTE);
        patrol.setAssignedIncident(theft);
        LinkedList<Vehicle> vehicles = new LinkedList<>();
        vehicles.addLast(patrol);
        LinkedList<Incident> incidents = new LinkedList<>();
        incidents.addLast(theft);
        json.saveVehicles(vehicles, folder.resolve("vehicles.json").toString());
        json.saveIncidents(incidents, folder.resolve("incidents.json").toString());

        // Act
        LinkedList<Vehicle> loadedVehicles = json.loadVehicles(folder.resolve("vehicles.json").toString());
        LinkedList<Incident> loadedIncidents = json.loadIncidents(folder.resolve("incidents.json").toString(), loadedVehicles);

        // Assert
        assertEquals("P-01", loadedIncidents.get(0).getAssignedVehicle().getId());
        assertSame(loadedIncidents.get(0), loadedVehicles.get(0).getAssignedIncident());
    }

    // Caso 3: Guardar y cargar la partida conserva puntaje, incidentes activos y estado de los vehiculos.
    @Test
    void saveAndLoadGameRestoresState() {
        // Arrange
        GameController controller = createController(11);
        Incident first = controller.spawnRandomIncident();
        controller.spawnRandomIncident();
        String assignedId = controller.proposeVehicle(first).getId();
        controller.assignVehicle(assignedId, first.getId());
        controller.getOperator().addScore(120);
        controller.saveGame(folder.toString());
        GameController other = createController(12);

        // Act
        other.loadGame(folder.toString());

        // Assert
        assertEquals(120, other.getScore());
        assertEquals(2, other.getActiveIncidentCount());
        assertEquals(VehicleStatus.EN_ROUTE, other.findVehicleById(assignedId).getStatus());
    }

    // Caso 4: Despues de cargar, un incidente nuevo no repite el id de los cargados.
    @Test
    void newIncidentAfterLoadDoesNotRepeatIds() {
        // Arrange
        GameController controller = createController(11);
        controller.spawnRandomIncident();
        controller.spawnRandomIncident();
        controller.saveGame(folder.toString());
        GameController other = createController(11);
        other.loadGame(folder.toString());

        // Act + Assert (con la misma semilla saldria el mismo id si no se tuviera en cuenta)
        assertDoesNotThrow(() -> other.spawnRandomIncident());
        assertEquals(3, other.getActiveIncidentCount());
    }
}
