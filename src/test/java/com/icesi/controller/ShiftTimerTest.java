package com.icesi.controller;

import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class ShiftTimerTest {

    // Caso 1: Un turno nuevo empieza con 300 segundos y se muestra como 05:00.
    @Test
    void newShiftStartsWithFiveMinutes() {
        // Arrange
        ShiftTimer timer = new ShiftTimer(() -> { });

        // Act
        int remaining = timer.getRemainingSeconds();
        String text = timer.formatRemaining();

        // Assert
        assertEquals(300, remaining);
        assertEquals("05:00", text);
    }

    // Caso 2: Al pasar 61 segundos quedan 239 y se muestra 03:59.
    @Test
    void tickDiscountsSecondsAndFormats() {
        // Arrange
        ShiftTimer timer = new ShiftTimer(() -> { });

        // Act
        timer.tick(61);

        // Assert
        assertEquals(239, timer.getRemainingSeconds());
        assertEquals("03:59", timer.formatRemaining());
    }

    // Caso 3: En pausa el reloj no avanza.
    @Test
    void pausedTimerDoesNotAdvance() {
        // Arrange
        ShiftTimer timer = new ShiftTimer(() -> { });
        timer.setPaused(true);

        // Act
        timer.tick(30);

        // Assert
        assertEquals(300, timer.getRemainingSeconds());
    }

    // Caso 4: El tiempo restante nunca baja de 0.
    @Test
    void remainingNeverGoesBelowZero() {
        // Arrange
        ShiftTimer timer = new ShiftTimer(() -> { });

        // Act
        timer.tick(1000);

        // Assert
        assertEquals(0, timer.getRemainingSeconds());
    }

    // Caso 5: Con tiempo y menos de 10 incidentes el turno sigue.
    @Test
    void shiftKeepsPlayingWithTimeAndFewIncidents() {
        // Arrange
        ShiftTimer timer = new ShiftTimer(() -> { });

        // Act
        ShiftTimer.Result result = timer.evaluate(9);

        // Assert
        assertEquals(ShiftTimer.Result.PLAYING, result);
    }

    // Caso 6: Con 10 incidentes activos se pierde por saturacion aunque quede tiempo.
    @Test
    void tenActiveIncidentsOverloadTheCity() {
        // Arrange
        ShiftTimer timer = new ShiftTimer(() -> { });

        // Act
        ShiftTimer.Result result = timer.evaluate(10);

        // Assert
        assertEquals(ShiftTimer.Result.OVERLOADED, result);
    }

    // Caso 7: Sin tiempo y sin saturacion el turno termina por tiempo.
    @Test
    void shiftEndsWhenTimeIsUp() {
        // Arrange
        ShiftTimer timer = new ShiftTimer(() -> { });
        timer.tick(300);

        // Act
        ShiftTimer.Result result = timer.evaluate(3);

        // Assert
        assertEquals(ShiftTimer.Result.TIME_UP, result);
    }

    // Caso 8: El hilo del reloj avanza solo y se detiene con stop.
    @Test
    void timerThreadTicksByItselfAndStops() throws InterruptedException {
        // Arrange
        CountDownLatch latch = new CountDownLatch(3);
        ShiftTimer timer = new ShiftTimer(latch::countDown, 20);

        // Act
        timer.start();
        boolean ticked = latch.await(2, TimeUnit.SECONDS);
        timer.stop();

        // Assert
        assertTrue(ticked);
        assertTrue(timer.getRemainingSeconds() <= 297);
        assertFalse(timer.isRunning());
    }
}
