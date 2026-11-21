package com.icesi.controller;

import com.icesi.model.CityMap;
import com.icesi.model.Incident;
import com.icesi.model.Operator;
import org.junit.jupiter.api.Test;

import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class IncidentSpawnerTest {

    // Crea un controlador con una semilla fija para que sea repetible.
    private GameController createController() {
        return new GameController(new CityMap(), new Operator(8, 8),
                GameController.createInitialVehicles(), new Random(7));
    }

    // Caso 1: Con 0 puntos el intervalo es el inicial de 12 segundos.
    @Test
    void intervalWithZeroScoreIsTwelveSeconds() {
        // Act
        long interval = IncidentSpawner.intervalForScore(0);

        // Assert
        assertEquals(12000, interval);
    }

    // Caso 2: Cada 300 puntos el intervalo baja 1 segundo.
    @Test
    void intervalDropsOneSecondEveryThreeHundredPoints() {
        // Act
        long interval = IncidentSpawner.intervalForScore(650);

        // Assert
        assertEquals(10000, interval);
    }

    // Caso 3: El intervalo nunca baja de 5 segundos aunque el puntaje sea muy alto.
    @Test
    void intervalNeverGoesBelowFiveSeconds() {
        // Act
        long interval = IncidentSpawner.intervalForScore(100000);

        // Assert
        assertEquals(5000, interval);
    }

    // Caso 4: spawnOnce registra un incidente activo y avisa a quien escucha.
    @Test
    void spawnOnceRegistersIncidentAndNotifies() {
        // Arrange
        GameController controller = createController();
        Incident[] received = new Incident[1];
        IncidentSpawner spawner = new IncidentSpawner(controller, incident -> received[0] = incident);

        // Act
        Incident incident = spawner.spawnOnce();

        // Assert
        assertNotNull(incident);
        assertEquals(1, controller.getActiveIncidentCount());
        assertSame(incident, received[0]);
    }

    // Caso 5: En pausa spawnOnce no genera nada.
    @Test
    void spawnOnceWhilePausedDoesNothing() {
        // Arrange
        GameController controller = createController();
        IncidentSpawner spawner = new IncidentSpawner(controller, incident -> { });
        spawner.setPaused(true);

        // Act
        Incident incident = spawner.spawnOnce();

        // Assert
        assertNull(incident);
        assertEquals(0, controller.getActiveIncidentCount());
    }

    // Caso 6: El hilo genera incidentes por si solo y se detiene con stop.
    @Test
    void threadSpawnsIncidentsByItselfAndStops() throws InterruptedException {
        // Arrange
        GameController controller = createController();
        CountDownLatch latch = new CountDownLatch(2);
        IncidentSpawner spawner = new IncidentSpawner(controller, incident -> latch.countDown(), 50);

        // Act
        spawner.start();
        boolean generated = latch.await(2, TimeUnit.SECONDS);
        spawner.stop();

        // Assert
        assertTrue(generated);
        assertTrue(controller.getActiveIncidentCount() >= 2);
        assertFalse(spawner.isRunning());
    }

    // Caso 7: En velocidad RAPIDA se espera 40% menos.
    @Test
    void fastModeWaitsFortyPercentLess() {
        // Act
        long interval = IncidentSpawner.intervalFor(0, true);

        // Assert
        assertEquals(7200, interval);
    }

    // Caso 8: En velocidad RAPIDA el intervalo nunca baja de 3 segundos.
    @Test
    void fastModeNeverGoesBelowThreeSeconds() {
        // Act
        long interval = IncidentSpawner.intervalFor(100000, true);

        // Assert
        assertEquals(3000, interval);
    }
}
