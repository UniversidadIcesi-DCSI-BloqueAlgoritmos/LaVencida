package com.icesi.structures;

import com.icesi.exceptions.EmptyStructureException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StackTest {

    private Stack<String> stack;

    @BeforeEach
    void setUp() {
        stack = new Stack<>();
    }

    // Caso 1: Apilar elementos deja en el tope el último elemento apilado.
    @Test
    void pushLeavesLastElementOnTop() {
        // Act
        stack.push("A");
        stack.push("B");

        // Assert
        assertEquals("B", stack.peek());
        assertEquals(2, stack.size());
        assertFalse(stack.isEmpty());
    }

    // Caso 2: Desapilar retorna los elementos en orden inverso al que entraron (LIFO).
    @Test
    void popReturnsElementsInLifoOrder() {
        // Arrange
        stack.push("A");
        stack.push("B");
        stack.push("C");

        // Act
        String first = stack.pop();
        String second = stack.pop();

        // Assert
        assertEquals("C", first);
        assertEquals("B", second);
        assertEquals(1, stack.size());
        assertEquals("A", stack.peek());
    }

    // Caso 3: Consultar el tope con peek no saca el elemento de la pila.
    @Test
    void peekDoesNotRemoveTheElement() {
        // Arrange
        stack.push("A");

        // Act
        stack.peek();
        stack.peek();

        // Assert
        assertEquals("A", stack.peek());
        assertEquals(1, stack.size());
    }

    // Caso 4: Desapilar el único elemento deja la pila vacía.
    @Test
    void popOnlyElementLeavesStackEmpty() {
        // Arrange
        stack.push("A");

        // Act
        stack.pop();

        // Assert
        assertTrue(stack.isEmpty());
        assertEquals(0, stack.size());
    }

    // Caso 5: Desapilar una pila vacía lanza una excepción.
    @Test
    void popOnEmptyStackThrowsException() {
        // Act & Assert
        assertThrows(EmptyStructureException.class,
                () -> stack.pop());
    }

    // Caso 6: Consultar el tope de una pila vacía lanza una excepción.
    @Test
    void peekOnEmptyStackThrowsException() {
        // Act & Assert
        assertThrows(EmptyStructureException.class,
                () -> stack.peek());
    }
}
