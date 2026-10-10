package com.icesi.model;

import com.icesi.exceptions.EmptyStructureException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OperatorTest {

    // RF21
    // Caso 1: Deshacer después de un movimiento devuelve al operador a su posición anterior.
    @Test
    void undoAfterOneMovementReturnsToPreviousPosition() {
        // Arrange
        Operator operator1 = new Operator(5, 5);
        operator1.move(Direction.UP);

        // Act
        operator1.undoLastMovement();

        // Assert
        assertEquals(5, operator1.getRow());
        assertEquals(5, operator1.getColumn());
        assertEquals(0, operator1.getMovementCount());
    }

    // Caso 2: Con varios movimientos, deshacer revierte solo el último (orden LIFO).
    @Test
    void undoWithSeveralMovementsRevertsOnlyTheLastOne() {
        // Arrange
        Operator operator1 = new Operator(5, 5);
        operator1.move(Direction.RIGHT);
        operator1.move(Direction.DOWN);

        // Act
        Movement undone = operator1.undoLastMovement();

        // Assert
        assertEquals(Direction.DOWN, undone.getDirection());
        assertEquals(5, operator1.getRow());
        assertEquals(6, operator1.getColumn());
        assertEquals(1, operator1.getMovementCount());
    }

    // Caso 3: Deshacer todos los movimientos devuelve al operador a la posición inicial.
    @Test
    void undoAllMovementsReturnsToInitialPosition() {
        // Arrange
        Operator operator1 = new Operator(2, 3);
        operator1.move(Direction.LEFT);
        operator1.move(Direction.UP);
        operator1.move(Direction.LEFT);

        // Act
        operator1.undoLastMovement();
        operator1.undoLastMovement();
        operator1.undoLastMovement();

        // Assert
        assertEquals(2, operator1.getRow());
        assertEquals(3, operator1.getColumn());
        assertEquals(0, operator1.getMovementCount());
    }

    // Caso 4: Deshacer con el historial vacío lanza una excepción y no cambia la posición.
    @Test
    void undoWithEmptyHistoryThrowsExceptionAndKeepsPosition() {
        // Arrange
        Operator operator1 = new Operator(5, 5);

        // Act & Assert
        assertThrows(EmptyStructureException.class,
                () -> operator1.undoLastMovement());
        assertEquals(5, operator1.getRow());
        assertEquals(5, operator1.getColumn());
    }

    // RF14
    // Caso 5: El puntaje inicial del operador debe ser cero.
    @Test
    void operatorInitialScoreIsZero() {
        Operator operator1 = new Operator(0, 0);
        assertEquals(0, operator1.getScore());
    }

    // RF14
    // Caso 6: Agregar puntos incrementa el puntaje acumulado del operador.
    @Test
    void addScoreIncreasesOperatorScore() {
        Operator operator1 = new Operator(0, 0);
        operator1.addScore(100);
        operator1.addScore(20);
        assertEquals(120, operator1.getScore());
    }
}
