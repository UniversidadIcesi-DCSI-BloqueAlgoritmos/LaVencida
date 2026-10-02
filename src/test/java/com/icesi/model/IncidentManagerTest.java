package com.icesi.model;

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
}
