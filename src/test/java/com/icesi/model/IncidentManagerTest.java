package com.icesi.model;

import com.icesi.structures.LinkedList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class IncidentManagerTest {

    private IncidentManager manager;

    @BeforeEach
    void setUp() {
        manager = new IncidentManager();
    }

    // RF3
    // Caso 1: Registrar un incidente válido lo agrega correctamente al sistema.
    @Test
    void registerValidIncidentAddsItToTheSystem() {
        // Arrange
        Incident incident1 = new Incident("INC001", IncidentType.ACCIDENT, "Zona Norte", Severity.HIGH);

        // Act
        manager.registerIncident(incident1);

        // Assert
        assertEquals(1, manager.getIncidentCount());
        assertEquals(incident1, manager.getIncidentAt(0));
    }

    // Caso 2: Registrar dos incidentes distintos conserva el orden de llegada.
    @Test
    void registerTwoDifferentIncidentsKeepsArrivalOrder() {
        // Arrange
        Incident incident1 = new Incident("INC001", IncidentType.ACCIDENT, "Zona Norte", Severity.HIGH);
        Incident incident2 = new Incident("INC002", IncidentType.THEFT, "Zona Sur", Severity.MEDIUM);

        // Act
        manager.registerIncident(incident1);
        manager.registerIncident(incident2);

        // Assert
        assertEquals(2, manager.getIncidentCount());
        assertEquals(incident1, manager.getIncidentAt(0));
        assertEquals(incident2, manager.getIncidentAt(1));
    }

    // Caso 3: Registrar un incidente con un ID ya existente lanza una excepción.
    @Test
    void registerIncidentWithExistingIdThrowsException() {
        // Arrange
        Incident incident1 = new Incident("INC001", IncidentType.ACCIDENT, "Zona Norte", Severity.HIGH);
        Incident incident1Duplicado = new Incident("INC001", IncidentType.THEFT, "Zona Sur", Severity.LOW);
        manager.registerIncident(incident1);

        // Act & Assert
        assertThrows(DuplicateIncidentException.class,
                () -> manager.registerIncident(incident1Duplicado));
        assertEquals(1, manager.getIncidentCount());
    }

    // Caso 4: Registrar un incidente nulo lanza una excepción.
    @Test
    void registerNullIncidentThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class,
                () -> manager.registerIncident(null));
        assertEquals(0, manager.getIncidentCount());
    }

    // RF5
    // Caso 1: Con un único incidente registrado, se retorna ese mismo incidente.
    @Test
    void getHighestPriorityIncidentWithSingleIncidentReturnsIt() {
        // Arrange
        Incident incident1 = new Incident("INC001", IncidentType.ACCIDENT, "Zona Norte", Severity.HIGH);
        manager.registerIncident(incident1);

        // Act
        Incident result = manager.getHighestPriorityIncident();

        // Assert
        assertEquals(incident1, result);
    }

    // Caso 2: Con incidentes de distinta gravedad, se retorna el de mayor gravedad.
    @Test
    void getHighestPriorityIncidentReturnsMostSevereOne() {
        // Arrange
        Incident incident1 = new Incident("INC001", IncidentType.THEFT, "Zona Norte", Severity.LOW);
        Incident incident2 = new Incident("INC002", IncidentType.ACCIDENT, "Zona Sur", Severity.HIGH);
        Incident incident3 = new Incident("INC003", IncidentType.FIRE, "Zona Este", Severity.MEDIUM);
        manager.registerIncident(incident1);
        manager.registerIncident(incident2);
        manager.registerIncident(incident3);

        // Act
        Incident result = manager.getHighestPriorityIncident();

        // Assert
        assertEquals(incident2, result);
    }

    // Caso 3: Con dos incidentes de la misma gravedad, se retorna el más antiguo (registrado primero).
    @Test
    void getHighestPriorityIncidentBreaksTiesByOldestFirst() {
        // Arrange
        Incident incident1 = new Incident("INC001", IncidentType.ACCIDENT, "Zona Norte", Severity.HIGH);
        Incident incident2 = new Incident("INC002", IncidentType.ACCIDENT, "Zona Sur", Severity.HIGH);
        manager.registerIncident(incident1);
        manager.registerIncident(incident2);

        // Act
        Incident result = manager.getHighestPriorityIncident();

        // Assert
        assertEquals(incident1, result);
    }

    // Caso 4: Consultar la prioridad sin incidentes registrados lanza una excepción.
    @Test
    void getHighestPriorityIncidentWithNoIncidentsThrowsException() {
        // Act & Assert
        assertThrows(NoActiveIncidentsException.class,
                () -> manager.getHighestPriorityIncident());
    }

    // RF7
    // Caso 1: Asignar un vehículo compatible y disponible a un incidente pendiente lo asigna correctamente.
    @Test
    void assignCompatibleAndAvailableVehicleToPendingIncidentAssignsIt() {
        // Arrange
        Incident incident1 = new Incident("INC001", IncidentType.THEFT, "Zona Norte", Severity.MEDIUM);
        Vehicle patrol1 = new Vehicle("VEH001", VehicleType.PATROL);

        // Act
        manager.assignVehicle(patrol1, incident1);

        // Assert
        assertEquals(IncidentStatus.IN_PROGRESS, incident1.getStatus());
        assertEquals(VehicleStatus.EN_ROUTE, patrol1.getStatus());
        assertEquals(patrol1, incident1.getAssignedVehicle());
    }

    // Caso 2: Asignar un vehículo incompatible con el tipo de incidente lanza una excepción.
    @Test
    void assignIncompatibleVehicleThrowsException() {
        // Arrange
        Incident incident1 = new Incident("INC001", IncidentType.FIRE, "Zona Norte", Severity.HIGH);
        Vehicle ambulance1 = new Vehicle("VEH001", VehicleType.AMBULANCE);

        // Act & Assert
        assertThrows(VehicleAssignmentException.class,
                () -> manager.assignVehicle(ambulance1, incident1));
        assertEquals(IncidentStatus.PENDING, incident1.getStatus());
    }

    // Caso 3: Asignar un vehículo no disponible lanza una excepción.
    @Test
    void assignUnavailableVehicleThrowsException() {
        // Arrange
        Incident incident1 = new Incident("INC001", IncidentType.ACCIDENT, "Zona Norte", Severity.HIGH);
        Vehicle ambulance1 = new Vehicle("VEH001", VehicleType.AMBULANCE);
        ambulance1.setStatus(VehicleStatus.OUT_OF_SERVICE);

        // Act & Assert
        assertThrows(VehicleAssignmentException.class,
                () -> manager.assignVehicle(ambulance1, incident1));
        assertEquals(IncidentStatus.PENDING, incident1.getStatus());
    }

    // Caso 4: Asignar un vehículo a un incidente que ya no está pendiente lanza una excepción.
    @Test
    void assignVehicleToNonPendingIncidentThrowsException() {
        // Arrange
        Incident incident1 = new Incident("INC001", IncidentType.THEFT, "Zona Norte", Severity.MEDIUM);
        incident1.setStatus(IncidentStatus.IN_PROGRESS);
        Vehicle patrol1 = new Vehicle("VEH001", VehicleType.PATROL);

        // Act & Assert
        assertThrows(VehicleAssignmentException.class,
                () -> manager.assignVehicle(patrol1, incident1));
        assertEquals(VehicleStatus.AVAILABLE, patrol1.getStatus());
    }
    //RF8
    // Caso 1: Finalizar la atención de un incidente "En proceso" lo marca como resuelto y libera el vehículo asignado.
    @Test
    void finishAttentionOfInProgressIncidentResolvesItAndFreesVehicle() {
        // Arrange
        Incident incident1 = new Incident("INC001", IncidentType.THEFT, "Zona Norte", Severity.MEDIUM);
        Vehicle patrol1 = new Vehicle("VEH001", VehicleType.PATROL);
        manager.assignVehicle(patrol1, incident1);

        // Act
        manager.finishAttention(incident1);

        // Assert
        assertEquals(IncidentStatus.RESOLVED, incident1.getStatus());
        assertEquals(VehicleStatus.AVAILABLE, patrol1.getStatus());
    }

    // Caso 2: Finalizar un incidente que aún no ha sido atendido ("Pendiente") lanza una excepción.
    @Test
    void finishAttentionOfPendingIncidentThrowsException() {
        // Arrange
        Incident incident1 = new Incident("INC001", IncidentType.THEFT, "Zona Norte", Severity.MEDIUM);

        // Act & Assert
        assertThrows(IncidentStateException.class,
                () -> manager.finishAttention(incident1));
        assertEquals(IncidentStatus.PENDING, incident1.getStatus());
    }

    // Caso 3: Finalizar un incidente que ya se encuentra "Resuelto" lanza una excepción y no vuelve a modificar el vehículo.
    @Test
    void finishAttentionOfResolvedIncidentThrowsExceptionAndDoesNotChangeVehicle() {
        // Arrange
        Incident incident1 = new Incident("INC001", IncidentType.ACCIDENT, "Zona Norte", Severity.HIGH);
        Vehicle ambulance1 = new Vehicle("VEH001", VehicleType.AMBULANCE);
        manager.assignVehicle(ambulance1, incident1);
        manager.finishAttention(incident1);

        // Act & Assert
        assertThrows(IncidentStateException.class,
                () -> manager.finishAttention(incident1));
        assertEquals(VehicleStatus.AVAILABLE, ambulance1.getStatus());
    }

    // RF6
    // Caso 1: Liberar un vehículo que atiende un incidente "En proceso" lo deja disponible y el incidente vuelve a quedar pendiente.
    @Test
    void releaseVehicleOfInProgressIncidentMakesItAvailableAndIncidentPending() {
        // Arrange
        Incident incident1 = new Incident("INC001", IncidentType.THEFT, "Zona Norte", Severity.MEDIUM);
        Vehicle patrol1 = new Vehicle("VEH001", VehicleType.PATROL);
        manager.registerIncident(incident1);
        manager.assignVehicle(patrol1, incident1);

        // Act
        manager.releaseVehicle(patrol1);

        // Assert
        assertEquals(VehicleStatus.AVAILABLE, patrol1.getStatus());
        assertEquals(IncidentStatus.PENDING, incident1.getStatus());
        assertNull(incident1.getAssignedVehicle());
    }

    // Caso 2: Después de liberar un vehículo, el incidente puede recibir otro vehículo compatible.
    @Test
    void releasedIncidentCanReceiveAnotherVehicle() {
        // Arrange
        Incident incident1 = new Incident("INC001", IncidentType.ACCIDENT, "Zona Norte", Severity.HIGH);
        Vehicle patrol1 = new Vehicle("VEH001", VehicleType.PATROL);
        Vehicle ambulance1 = new Vehicle("VEH002", VehicleType.AMBULANCE);
        manager.registerIncident(incident1);
        manager.assignVehicle(patrol1, incident1);
        manager.releaseVehicle(patrol1);

        // Act
        manager.assignVehicle(ambulance1, incident1);

        // Assert
        assertEquals(IncidentStatus.IN_PROGRESS, incident1.getStatus());
        assertEquals(ambulance1, incident1.getAssignedVehicle());
        assertEquals(VehicleStatus.AVAILABLE, patrol1.getStatus());
    }

    // Caso 3: Liberar un vehículo que ya está "Disponible" lanza una excepción.
    @Test
    void releaseAvailableVehicleThrowsException() {
        // Arrange
        Vehicle ambulance1 = new Vehicle("VEH001", VehicleType.AMBULANCE);

        // Act & Assert
        assertThrows(VehicleReleaseException.class,
                () -> manager.releaseVehicle(ambulance1));
        assertEquals(VehicleStatus.AVAILABLE, ambulance1.getStatus());
    }

    // Caso 4: Liberar un vehículo que no está atendiendo ningún incidente lanza una excepción y no cambia su estado.
    @Test
    void releaseVehicleWithoutIncidentThrowsExceptionAndKeepsStatus() {
        // Arrange
        Vehicle fireTruck1 = new Vehicle("VEH001", VehicleType.FIRE_TRUCK);
        fireTruck1.setStatus(VehicleStatus.OUT_OF_SERVICE);

        // Act & Assert
        assertThrows(VehicleReleaseException.class,
                () -> manager.releaseVehicle(fireTruck1));
        assertEquals(VehicleStatus.OUT_OF_SERVICE, fireTruck1.getStatus());
    }

    // RF5 - ordenamiento
    // Caso 1: Los incidentes se retornan ordenados de mayor a menor gravedad.
    @Test
    void getIncidentsSortedByPriorityOrdersFromHighToLow() {
        // Arrange
        Incident incident1 = new Incident("INC001", IncidentType.THEFT, "Zona Norte", Severity.LOW);
        Incident incident2 = new Incident("INC002", IncidentType.ACCIDENT, "Zona Sur", Severity.HIGH);
        Incident incident3 = new Incident("INC003", IncidentType.FIRE, "Zona Este", Severity.MEDIUM);
        manager.registerIncident(incident1);
        manager.registerIncident(incident2);
        manager.registerIncident(incident3);

        // Act
        LinkedList<Incident> sorted = manager.getIncidentsSortedByPriority();

        // Assert
        assertEquals(incident2, sorted.get(0));
        assertEquals(incident3, sorted.get(1));
        assertEquals(incident1, sorted.get(2));
    }

    // Caso 2: Con la misma gravedad, el más antiguo queda primero y el orden original no cambia.
    @Test
    void getIncidentsSortedByPriorityBreaksTiesAndKeepsOriginalOrder() {
        // Arrange
        Incident incident1 = new Incident("INC001", IncidentType.THEFT, "Zona Norte", Severity.MEDIUM);
        Incident incident2 = new Incident("INC002", IncidentType.ACCIDENT, "Zona Sur", Severity.HIGH);
        Incident incident3 = new Incident("INC003", IncidentType.FIRE, "Zona Este", Severity.HIGH);
        manager.registerIncident(incident1);
        manager.registerIncident(incident2);
        manager.registerIncident(incident3);

        // Act
        LinkedList<Incident> sorted = manager.getIncidentsSortedByPriority();

        // Assert
        assertEquals(incident2, sorted.get(0));
        assertEquals(incident3, sorted.get(1));
        assertEquals(incident1, manager.getIncidentAt(0));
    }

    // RF3 - búsqueda binaria
    // Caso 3: Buscar por ID un incidente registrado lo retorna.
    @Test
    void findIncidentByIdReturnsTheRightIncident() {
        // Arrange
        Incident incident1 = new Incident("INC003", IncidentType.THEFT, "Zona Norte", Severity.LOW);
        Incident incident2 = new Incident("INC001", IncidentType.ACCIDENT, "Zona Sur", Severity.HIGH);
        Incident incident3 = new Incident("INC002", IncidentType.FIRE, "Zona Este", Severity.MEDIUM);
        manager.registerIncident(incident1);
        manager.registerIncident(incident2);
        manager.registerIncident(incident3);

        // Act
        Incident result = manager.findIncidentById("INC002");

        // Assert
        assertEquals(incident3, result);
    }

    // Caso 4: Buscar por ID un incidente que no existe lanza una excepción.
    @Test
    void findIncidentByIdThatDoesNotExistThrowsException() {
        // Arrange
        Incident incident1 = new Incident("INC001", IncidentType.THEFT, "Zona Norte", Severity.LOW);
        manager.registerIncident(incident1);

        // Act & Assert
        assertThrows(IncidentNotFoundException.class,
                () -> manager.findIncidentById("INC999"));
    }
}
