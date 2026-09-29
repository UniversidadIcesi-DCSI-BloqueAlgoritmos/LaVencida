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
}
