package com.icesi.controller;

import com.icesi.exceptions.DuplicateIncidentException;
import com.icesi.exceptions.IncidentNotFoundException;
import com.icesi.exceptions.IncidentStateException;
import com.icesi.model.CityMap;
import com.icesi.model.Direction;
import com.icesi.model.Incident;
import com.icesi.model.IncidentType;
import com.icesi.model.Operator;
import com.icesi.model.Severity;
import com.icesi.model.Vehicle;
import com.icesi.model.VehicleStatus;
import com.icesi.model.VehicleType;
import com.icesi.structures.BinarySearchTree;
import com.icesi.structures.LinkedList;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
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

    // RF3 / HU-06 - Caso 8: Buscar un incidente existente por su ID con busqueda binaria lo retorna exitosamente.
    @Test
    void findIncidentByIdReturnsMatchingIncident() {
        // Arrange
        GameController controller = createController(8, 8);
        Incident manual = new Incident("INC-777", IncidentType.THEFT, "Parque Bolivar", Severity.HIGH);
        controller.registerIncident(manual);

        // Act
        Incident found = controller.findIncidentById("INC-777");

        // Assert
        assertNotNull(found);
        assertEquals("INC-777", found.getId());
        assertEquals(IncidentType.THEFT, found.getType());
    }

    // RF3 / HU-06 - Caso 9: Buscar un incidente con ID inexistente lanza IncidentNotFoundException.
    @Test
    void findIncidentByIdThrowsWhenNotFound() {
        // Arrange
        GameController controller = createController(8, 8);

        // Act & Assert
        assertThrows(IncidentNotFoundException.class,
                () -> controller.findIncidentById("NO-EXISTE"));
    }

    // RF3 / HU-05 - Caso 10: Registrar un incidente con ID duplicado lanza DuplicateIncidentException.
    @Test
    void registerIncidentWithDuplicateIdThrowsException() {
        // Arrange
        GameController controller = createController(8, 8);
        Incident first = new Incident("INC-DUPLICADO", IncidentType.THEFT, "Parque Bolivar", Severity.LOW);
        Incident second = new Incident("INC-DUPLICADO", IncidentType.FIRE, "Barrio El Prado", Severity.HIGH);
        controller.registerIncident(first);

        // Act & Assert
        assertThrows(DuplicateIncidentException.class,
                () -> controller.registerIncident(second));
    }

    // RF4 / HU-08 - Caso 11: Detonar accidente por colision entre dos vehiculos.
    @Test
    void triggerCollisionAccidentRegistersActiveAccident() {
        // Arrange
        GameController controller = createController(8, 8);

        // Act
        Incident accident = controller.triggerCollisionAccident("P-01", "A-01", "Cruce Calle 30 Cra 19");

        // Assert
        assertNotNull(accident);
        assertEquals(IncidentType.ACCIDENT, accident.getType());
        assertEquals(Severity.HIGH, accident.getSeverity());
        assertEquals(1, controller.getActiveAccidentCount());
        assertEquals(1, controller.getActiveIncidentCount());
    }

    // RF4 / HU-09 - Caso 12: Detonar accidente por congestion critica cuando supera el umbral.
    @Test
    void triggerCongestionAccidentRegistersActiveAccident() {
        // Arrange
        GameController controller = createController(8, 8);

        // Act
        Incident accident = controller.triggerCongestionAccident("Sector La Estacion", 7);

        // Assert
        assertNotNull(accident);
        assertEquals(IncidentType.ACCIDENT, accident.getType());
        assertEquals(Severity.MEDIUM, accident.getSeverity());
        assertEquals(1, controller.getActiveAccidentCount());
    }

    // RF4 / HU-10 - Caso 13: Detonar accidente por vehiculo detenido por varios ciclos.
    @Test
    void triggerStoppedVehicleAccidentRegistersActiveAccident() {
        // Arrange
        GameController controller = createController(8, 8);

        // Act
        Incident accident = controller.triggerStoppedVehicleAccident("F-01", "Zona Industrial Cencar", 4);

        // Assert
        assertNotNull(accident);
        assertEquals(IncidentType.ACCIDENT, accident.getType());
        assertEquals(Severity.MEDIUM, accident.getSeverity());
        assertEquals(1, controller.getActiveAccidentCount());
    }

    // Canvas click - Caso 14: Generar accidente en celda especifica del mapa.
    @Test
    void spawnAccidentAtCellRegistersActiveAccidentInExactPosition() {
        // Arrange
        GameController controller = createController(8, 8);

        // Act
        Incident accident = controller.spawnAccidentAt(4, 4);

        // Assert
        assertNotNull(accident);
        assertEquals(1, controller.getActiveAccidentCount());
        int[] cell = controller.getCellOf(accident);
        assertEquals(4, cell[0]);
        assertEquals(4, cell[1]);
    }

    // RF6 / HU-16 - Caso 15: Liberar vehiculo cancela el despacho y devuelve la unidad a disponible.
    @Test
    void cancelIncidentDispatchFreesVehicleAndRestoresPending() {
        // Arrange
        GameController controller = createController(8, 8);
        Incident incident = controller.spawnRandomIncident();
        String vehicleId = controller.proposeVehicle(incident).getId();
        controller.assignVehicle(vehicleId, incident.getId());

        // Act
        controller.cancelIncidentDispatch(incident.getId());

        // Assert
        Vehicle vehicle = controller.findVehicleById(vehicleId);
        assertEquals(VehicleStatus.AVAILABLE, vehicle.getStatus());
        assertNull(vehicle.getAssignedIncident());
        assertEquals(com.icesi.model.IncidentStatus.PENDING, incident.getStatus());
        assertNull(incident.getAssignedVehicle());
    }

    // Registro de vehiculos - Caso 16: Registrar nuevo vehiculo lo agrega a la flota y valida duplicados.
    @Test
    void registerVehicleAddsUnitAndRejectsDuplicate() {
        // Arrange
        GameController controller = createController(8, 8);
        Vehicle newAmbulance = new Vehicle("A-99", VehicleType.AMBULANCE, "Hospital Sur");

        // Act
        controller.registerVehicle(newAmbulance);

        // Assert
        Vehicle found = controller.findVehicleById("A-99");
        assertNotNull(found);
        assertEquals(VehicleType.AMBULANCE, found.getType());
        assertThrows(IllegalArgumentException.class,
                () -> controller.registerVehicle(new Vehicle("A-99", VehicleType.PATROL, "Estacion Norte")));
    }

    // RF16 / HU-22 - Caso 17: Guardado y carga binaria (.dat) persiste los incidentes serializados.
    @Test
    void saveAndLoadBinaryStateRestoresIncidents(@TempDir Path tempDir) {
        // Arrange
        GameController controller = createController(8, 8);
        Incident inc = controller.spawnRandomIncident();
        Path datFile = tempDir.resolve("test_partida.dat");

        // Act
        controller.saveGameBinary(datFile.toString());
        GameController restoredController = createController(8, 8);
        restoredController.loadGameBinary(datFile.toString());

        // Assert
        assertEquals(1, restoredController.getActiveIncidentCount());
        assertNotNull(restoredController.findIncidentById(inc.getId()));
    }

    // BST de incidentes - Caso 18: getActiveIncidentsTree retorna el arbol binario de busqueda con altura y tamano consistentes.
    @Test
    void getActiveIncidentsTreeReturnsValidBST() {
        // Arrange
        GameController controller = createController(8, 8);
        BinarySearchTree<Incident> treeBefore = controller.getActiveIncidentsTree();
        assertNotNull(treeBefore);
        assertEquals(0, treeBefore.size());

        // Act
        Incident inc1 = new Incident("INC-A", IncidentType.THEFT, "Calle 10", Severity.LOW);
        Incident inc2 = new Incident("INC-B", IncidentType.FIRE, "Calle 12", Severity.HIGH);
        controller.registerIncident(inc1);
        controller.registerIncident(inc2);

        // Assert
        BinarySearchTree<Incident> treeAfter = controller.getActiveIncidentsTree();
        assertEquals(2, treeAfter.size());
        assertTrue(treeAfter.height() >= 1);
        assertNotNull(treeAfter.getRoot());
        assertTrue(treeAfter.contains(inc1));
        assertTrue(treeAfter.contains(inc2));
    }

    // Corredores de emergencia - Caso 19: isCorridorCell identifica correctamente vias MAIN_ROAD y countCorridorCells es consistente.
    @Test
    void isCorridorCellAndCountCorridorCellsAccuratelyDetectMainRoads() {
        // Arrange
        GameController controller = createController(8, 8);

        // Assert: Segun CityMap, fila 8 o columna 8 son MAIN_ROAD
        assertTrue(controller.isCorridorCell(8, 4));
        assertTrue(controller.isCorridorCell(4, 8));
        assertTrue(controller.isCorridorCell(8, 8)); // Cruce arterial
        assertFalse(controller.isCorridorCell(0, 0)); // Borde / Obstaculo
        assertFalse(controller.isCorridorCell(99, 99)); // Fuera de limites

        int totalCorridorCells = controller.countCorridorCells();
        assertTrue(totalCorridorCells > 0, "Debe haber celdas asignadas a corredores arteriales");
    }

    // Recorrido BST - Caso 20: El recorrido in-order del arbol organiza los incidentes por prioridad.
    @Test
    void bstInOrderTraversalMaintainsPrioritySequence() {
        // Arrange
        GameController controller = createController(8, 8);
        Incident lowInc = new Incident("LOW-01", IncidentType.THEFT, "Zona A", Severity.LOW);
        Incident medInc = new Incident("MED-01", IncidentType.ACCIDENT, "Zona B", Severity.MEDIUM);
        Incident highInc = new Incident("HIGH-01", IncidentType.FIRE, "Zona C", Severity.HIGH);

        controller.registerIncident(lowInc);
        controller.registerIncident(medInc);
        controller.registerIncident(highInc);

        // Act
        LinkedList<Incident> inOrder = controller.getActiveIncidentsTree().inOrder();

        // Assert
        assertEquals(3, inOrder.size());
        // En in-order segun IncidentPriorityComparator (mayor a menor urgencia): HIGH -> MEDIUM -> LOW
        assertEquals(Severity.HIGH, inOrder.get(0).getSeverity());
        assertEquals(Severity.MEDIUM, inOrder.get(1).getSeverity());
        assertEquals(Severity.LOW, inOrder.get(2).getSeverity());
    }

    // Actualizacion de incidente - Caso 21: updateIncident actualiza los datos y reordena el incidente en el BST sin duplicar nodos.
    @Test
    void updateIncidentModifiesFieldsAndRebalancesBST() {
        // Arrange
        GameController controller = createController(8, 8);
        Incident first = new Incident("INC-1", IncidentType.THEFT, "Zona A", Severity.MEDIUM);
        Incident second = new Incident("INC-2", IncidentType.FIRE, "Zona B", Severity.LOW);
        controller.registerIncident(first);
        controller.registerIncident(second);

        // Act: promovemos INC-2 de LOW a HIGH y actualizamos su ubicacion
        controller.updateIncident("INC-2", IncidentType.ACCIDENT, Severity.HIGH, "Calle 30", "Choque multiple");

        // Assert
        Incident updated = controller.findIncidentById("INC-2");
        assertEquals(IncidentType.ACCIDENT, updated.getType());
        assertEquals(Severity.HIGH, updated.getSeverity());
        assertEquals("Calle 30", updated.getLocation());
        assertEquals(2, controller.getActiveIncidentsTree().size());
        assertEquals("INC-2", controller.getHighestPriorityIncidentOrNull().getId());

        // Si se resuelve, ya no debe permitir editarse
        controller.assignVehicle(controller.proposeVehicle(updated).getId(), "INC-2");
        controller.resolveIncident("INC-2");
        assertThrows(IncidentStateException.class,
                () -> controller.updateIncident("INC-2", IncidentType.THEFT, Severity.LOW, "Zona C", "Desc"));
    }
}
