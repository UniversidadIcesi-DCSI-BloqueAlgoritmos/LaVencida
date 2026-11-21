package com.icesi.ui;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

// Pruebas del registro de eventos del Centro de Monitoreo.
class EventLogTest {

    // Caso 1: El evento mas nuevo queda de primero y avisa el cambio.
    @Test
    void newestEventGoesFirstAndNotifies() {
        // Arrange
        EventLog log = new EventLog();
        int[] changes = new int[1];
        log.setOnChange(() -> changes[0]++);

        // Act
        log.add("primero", EventLog.SYSTEM);
        log.add("segundo", EventLog.ALERT);

        // Assert
        assertEquals("segundo", log.getEntries().get(0).getText());
        assertEquals(EventLog.ALERT, log.getEntries().get(0).getColor());
        assertEquals(2, changes[0]);
    }

    // Caso 2: El registro guarda maximo 30 eventos y borra el mas viejo.
    @Test
    void logKeepsOnlyThirtyEvents() {
        // Arrange
        EventLog log = new EventLog();

        // Act
        for (int i = 1; i <= 35; i++) {
            log.add("evento " + i, EventLog.SYSTEM);
        }

        // Assert
        assertEquals(30, log.getEntries().size());
        assertEquals("evento 35", log.getEntries().get(0).getText());
        assertEquals("evento 6", log.getEntries().get(29).getText());
    }
}
