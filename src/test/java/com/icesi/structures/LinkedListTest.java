package com.icesi.structures;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Comparator;
import java.util.Iterator;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;

class LinkedListTest {

    private LinkedList<Integer> list;

    @BeforeEach
    void setUp() {
        list = new LinkedList<>();
    }

    // Caso 1: addFirst y addLast agregan al inicio y al final respectivamente.
    @Test
    void addFirstAndAddLastKeepTheRightOrder() {
        // Act
        list.addLast(2);
        list.addFirst(1);
        list.addLast(3);

        // Assert
        assertEquals(1, list.get(0));
        assertEquals(2, list.get(1));
        assertEquals(3, list.get(2));
        assertEquals(3, list.size());
    }

    // Caso 2: Eliminar un elemento del medio conserva el orden de los demás.
    @Test
    void removeMiddleElementKeepsOrder() {
        // Arrange
        list.addLast(10);
        list.addLast(20);
        list.addLast(30);

        // Act
        int removed = list.remove(1);

        // Assert
        assertEquals(20, removed);
        assertEquals(2, list.size());
        assertEquals(10, list.get(0));
        assertEquals(30, list.get(1));
    }

    // Caso 3: Eliminar el último elemento actualiza la cola y se puede seguir agregando al final.
    @Test
    void removeLastElementUpdatesTail() {
        // Arrange
        list.addLast(10);
        list.addLast(20);
        list.addLast(30);

        // Act
        list.remove(2);
        list.addLast(40);

        // Assert
        assertEquals(3, list.size());
        assertEquals(20, list.get(1));
        assertEquals(40, list.get(2));
    }

    // Caso 4: Eliminar con un índice fuera de rango lanza una excepción.
    @Test
    void removeWithInvalidIndexThrowsException() {
        // Arrange
        list.addLast(10);

        // Act & Assert
        assertThrows(IndexOutOfBoundsException.class,
                () -> list.remove(5));
        assertEquals(1, list.size());
    }

    // Caso 5: contains retorna true si el elemento está y false si no.
    @Test
    void containsFindsOnlyExistingElements() {
        // Arrange
        list.addLast(10);
        list.addLast(20);

        // Act & Assert
        assertTrue(list.contains(20));
        assertFalse(list.contains(99));
    }

    // Caso 6: Ordenar una lista desordenada la deja en orden ascendente.
    @Test
    void sortLeavesListInAscendingOrder() {
        // Arrange
        list.addLast(5);
        list.addLast(3);
        list.addLast(8);
        list.addLast(1);
        list.addLast(4);

        // Act
        list.sort(Comparator.naturalOrder());

        // Assert
        assertEquals(1, list.get(0));
        assertEquals(3, list.get(1));
        assertEquals(4, list.get(2));
        assertEquals(5, list.get(3));
        assertEquals(8, list.get(4));
    }

    // Caso 7: Ordenar una lista vacía o de un solo elemento no lanza error.
    @Test
    void sortEmptyOrSingleElementListDoesNotFail() {
        // Arrange
        LinkedList<Integer> emptyList = new LinkedList<>();
        LinkedList<Integer> singleList = new LinkedList<>();
        singleList.addLast(7);

        // Act
        emptyList.sort(Comparator.naturalOrder());
        singleList.sort(Comparator.naturalOrder());

        // Assert
        assertEquals(0, emptyList.size());
        assertEquals(7, singleList.get(0));
    }

    // Caso 8: La búsqueda binaria encuentra la posición de un elemento en una lista ordenada.
    @Test
    void binarySearchFindsElementPosition() {
        // Arrange
        list.addLast(1);
        list.addLast(3);
        list.addLast(5);
        list.addLast(7);
        list.addLast(9);

        // Act
        int index = list.binarySearch(7, Comparator.naturalOrder());

        // Assert
        assertEquals(3, index);
    }

    // Caso 9: La búsqueda binaria retorna -1 si el elemento no está.
    @Test
    void binarySearchReturnsMinusOneWhenElementIsMissing() {
        // Arrange
        list.addLast(1);
        list.addLast(3);
        list.addLast(5);
        list.addLast(7);
        list.addLast(9);

        // Act
        int index = list.binarySearch(4, Comparator.naturalOrder());

        // Assert
        assertEquals(-1, index);
    }

    // Iterator - Caso 1: Recorrer una lista con varios elementos los entrega en el orden en que fueron agregados.
    @Test
    void iteratorReturnsElementsInInsertionOrder() {
        // Arrange
        LinkedList<String> letters = new LinkedList<>();
        letters.addLast("A");
        letters.addLast("B");
        letters.addLast("C");

        // Act
        Iterator<String> iterator = letters.iterator();

        // Assert
        assertTrue(iterator.hasNext());
        assertEquals("A", iterator.next());
        assertEquals("B", iterator.next());
        assertEquals("C", iterator.next());
        assertFalse(iterator.hasNext());
    }

    // Iterator - Caso 2: Iterar una lista vacía indica que no hay elementos.
    @Test
    void iteratorOfEmptyListHasNoElements() {
        // Arrange
        LinkedList<String> emptyList = new LinkedList<>();

        // Act
        Iterator<String> iterator = emptyList.iterator();

        // Assert
        assertFalse(iterator.hasNext());
    }

    // Iterator - Caso 3: Pedir el siguiente elemento cuando ya no quedan más lanza una excepción.
    @Test
    void iteratorNextWithoutMoreElementsThrowsException() {
        // Arrange
        LinkedList<String> letters = new LinkedList<>();
        letters.addLast("A");
        Iterator<String> iterator = letters.iterator();
        iterator.next();

        // Act & Assert
        assertThrows(NoSuchElementException.class,
                () -> iterator.next());
    }

    // Iterator - Caso 4: Recorrer la lista con un ciclo for-each visita todos los elementos.
    @Test
    void forEachVisitsAllElements() {
        // Arrange
        list.addLast(1);
        list.addLast(2);
        list.addLast(3);
        int sum = 0;

        // Act
        for (int value : list) {
            sum += value;
        }

        // Assert
        assertEquals(6, sum);
    }
}
