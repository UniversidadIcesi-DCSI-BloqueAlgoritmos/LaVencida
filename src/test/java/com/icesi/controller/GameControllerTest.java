package com.icesi.controller;

import com.icesi.model.CityMap;
import com.icesi.model.Direction;
import com.icesi.model.Incident;
import com.icesi.model.IncidentType;
import com.icesi.model.Operator;
import com.icesi.model.Vehicle;
import com.icesi.model.VehicleStatus;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class GameControllerTest {

    // Crea un controlador con el operador en la posicion indicada y una semilla fija para que sea repetible.
    private GameController createController(int row, int column) {
        return new GameController(new CityMap(), new Operator(row, column),
                GameController.createInitialVehicles(), new Random(42));
    }

    // RF2 - Caso 1: Mover al operador hacia una celda transitable lo mueve y retorna true.
    @Test
    void moveOperatorToWalkableCellMovesIt() {
        // Arrange
        GameController controller = createController(4, 4);

        // Act
        boolean moved = controller.moveOperator(Direction.UP);

        // Assert
        assertTrue(moved);
        assertEquals(3, controller.getOperator().getRow());
        assertEquals(4, controller.getOperator().getColumn());
    }

    // RF2 - Caso 2: Mover al operador hacia el borde del mapa (obstaculo) no lo mueve y retorna false.
    @Test
    void moveOperatorIntoBorderDoesNotMoveIt() {
        // Arrange
        GameController controller = createController(1, 4);

        // Act
        boolean moved = controller.moveOperator(Direction.UP);

        // Assert
        assertFalse(moved);
        assertEquals(1, controller.getOperator().getRow());
        assertEquals(0, controller.getOperator().getMovementCount());
    }

    // RF21 - Caso 3: Deshacer sin movimientos retorna false y, despues de moverse, deshacer devuelve al operador a su posicion.
    @Test
    void undoWithoutMovesReturnsFalseAndUndoAfterMoveRestoresPosition() {
        // Arrange
        GameController controller = createController(4, 4);

        // Act
        boolean emptyUndo = controller.undoOperatorMove();
        controller.moveOperator(Direction.RIGHT);
        boolean undo = controller.undoOperatorMove();

        // Assert
        assertFalse(emptyUndo);
        assertTrue(undo);
        assertEquals(4, controller.getOperator().getColumn());
    }

    // RF4 - Caso 4: Generar un incidente lo registra como activo y es un robo o un incendio.
    @Test
    void spawnRandomIncidentRegistersActiveTheftOrFire() {
        // Arrange
        GameController controller = createController(8, 8);

        // Act
        Incident incident = controller.spawnRandomIncident();

        // Assert
        assertEquals(1, controller.getActiveIncidentCount());
        assertTrue(incident.getType() == IncidentType.THEFT || incident.getType() == IncidentType.FIRE);
    }

    // RF7 y RF8 - Caso 5: Asignar un vehiculo compatible por ID y resolver el incidente suma puntos y libera el vehiculo.
    @Test
    void assignAndResolveIncidentAddsPointsAndFreesVehicle() {
        // Arrange
        GameController controller = createController(8, 8);
        Incident incident = controller.spawnRandomIncident();
        String vehicleId = controller.proposeVehicle(incident).getId();

        // Act
        controller.assignVehicle(vehicleId, incident.getId());
        int points = controller.resolveIncident(incident.getId());

        // Assert
        assertTrue(points > 0);
        assertEquals(points, controller.getScore());
        assertEquals(0, controller.getActiveIncidentCount());
        assertEquals(VehicleStatus.AVAILABLE, controller.findVehicleById(vehicleId).getStatus());
    }

    // RF5 - Caso 6: Sin incidentes activos, el incidente de mayor prioridad es null (la interfaz no se cierra).
    @Test
    void highestPriorityIncidentIsNullWhenThereAreNoIncidents() {
        // Arrange
        GameController controller = createController(8, 8);

        // Act
        Incident highest = controller.getHighestPriorityIncidentOrNull();

        // Assert
        assertNull(highest);
    }

    // RF6 - Caso 7: Buscar un vehiculo con un ID que no existe lanza una excepcion.
    @Test
    void findVehicleWithUnknownIdThrowsException() {
        // Arrange
        GameController controller = createController(8, 8);

        // Act & Assert
        assertThrows(IllegalArgumentException.class,
                () -> controller.findVehicleById("X-99"));
    }
}
