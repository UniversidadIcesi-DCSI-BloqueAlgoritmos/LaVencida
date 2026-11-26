package com.icesi.controller;

import com.icesi.model.CityMap;
import com.icesi.model.Incident;
import com.icesi.model.IncidentManager;
import com.icesi.model.IncidentType;
import com.icesi.model.Operator;
import com.icesi.model.Severity;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

// Pruebas del bonus por rapidez y de la penalizacion por espera.
class ScoreRulesTest {

    private static final LocalDateTime START = LocalDateTime.of(2026, 11, 21, 10, 0, 0);

    private GameController createController(int initialScore) {
        GameController controller = new GameController(new CityMap(), new Operator(8, 8),
                GameController.createInitialVehicles(), new Random(3));
        controller.getOperator().addScore(initialScore);
        return controller;
    }

    // Registra un incendio con la gravedad y hora indicadas.
    private Incident registerFire(GameController controller, String id, Severity severity) {
        Incident incident = new Incident(id, IncidentType.FIRE, "Zona Industrial Cencar", severity, START);
        controller.registerIncident(incident);
        return incident;
    }

    // Caso 1: Resolver 10 segundos despues de la alerta suma el bonus de 50.
    @Test
    void resolvingFastAddsBonus() {
        // Arrange
        GameController controller = createController(0);
        Incident incident = registerFire(controller, "FIRE-900", Severity.MEDIUM);
        controller.assignVehicle(controller.proposeVehicle(incident).getId(), incident.getId());
        int normal = new IncidentManager().calculateScore(incident, START.plusSeconds(10));

        // Act
        int points = controller.resolveIncident(incident.getId(), START.plusSeconds(10));

        // Assert
        assertEquals(normal + 50, points);
        assertTrue(controller.wasLastResolveFast());
        assertEquals(points, controller.getScore());
    }

    // Caso 2: Resolver 45 segundos despues no da bonus.
    @Test
    void resolvingSlowGivesNoBonus() {
        // Arrange
        GameController controller = createController(0);
        Incident incident = registerFire(controller, "FIRE-901", Severity.MEDIUM);
        controller.assignVehicle(controller.proposeVehicle(incident).getId(), incident.getId());
        int normal = new IncidentManager().calculateScore(incident, START.plusSeconds(45));

        // Act
        int points = controller.resolveIncident(incident.getId(), START.plusSeconds(45));

        // Assert
        assertEquals(normal, points);
        assertFalse(controller.wasLastResolveFast());
    }

    // Caso 3: Un incidente ALTO pendiente 65 segundos cobra dos bloques de 25.
    @Test
    void highPendingIncidentIsPenalizedPerBlock() {
        // Arrange
        GameController controller = createController(200);
        registerFire(controller, "FIRE-902", Severity.HIGH);

        // Act
        int penalty = controller.applyWaitPenalties(START.plusSeconds(65));

        // Assert
        assertEquals(50, penalty);
        assertEquals(150, controller.getScore());
    }

    // Caso 4: El mismo bloque de espera no se cobra dos veces.
    @Test
    void sameBlockIsNotChargedTwice() {
        // Arrange
        GameController controller = createController(200);
        registerFire(controller, "FIRE-903", Severity.HIGH);
        controller.applyWaitPenalties(START.plusSeconds(65));

        // Act
        int penalty = controller.applyWaitPenalties(START.plusSeconds(80));

        // Assert
        assertEquals(0, penalty);
        assertEquals(150, controller.getScore());
    }

    // Caso 5: Los incidentes que no son ALTOS o ya tienen unidad no se penalizan.
    @Test
    void mediumOrAssignedIncidentsAreNotPenalized() {
        // Arrange
        GameController controller = createController(200);
        registerFire(controller, "FIRE-904", Severity.MEDIUM);
        Incident high = registerFire(controller, "FIRE-905", Severity.HIGH);
        controller.assignVehicle(controller.proposeVehicle(high).getId(), high.getId());

        // Act
        int penalty = controller.applyWaitPenalties(START.plusSeconds(120));

        // Assert
        assertEquals(0, penalty);
        assertEquals(200, controller.getScore());
    }

    // Caso 6: La penalizacion nunca deja el puntaje negativo.
    @Test
    void penaltyNeverMakesScoreNegative() {
        // Arrange
        GameController controller = createController(10);
        registerFire(controller, "FIRE-906", Severity.HIGH);

        // Act
        int penalty = controller.applyWaitPenalties(START.plusSeconds(95));

        // Assert
        assertEquals(10, penalty);
        assertEquals(0, controller.getScore());
    }
}
