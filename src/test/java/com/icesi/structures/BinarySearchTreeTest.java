package com.icesi.structures;

import com.icesi.exceptions.EmptyStructureException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Comparator;

import static org.junit.jupiter.api.Assertions.*;

class BinarySearchTreeTest {

    private BinarySearchTree<Integer> bst;

    @BeforeEach
    void setUp() {
        bst = new BinarySearchTree<>();
    }

    // Caso 1: Insertar elementos incrementa el tamano y permite encontrarlos mediante search y contains.
    @Test
    void insertIncreasesSizeAndCanBeFound() {
        // Arrange & Act
        bst.insert(50);
        bst.insert(30);
        bst.insert(70);

        // Assert
        assertEquals(3, bst.size());
        assertFalse(bst.isEmpty());
        assertTrue(bst.contains(50));
        assertTrue(bst.contains(30));
        assertTrue(bst.contains(70));
        assertEquals(50, bst.search(50));
        assertEquals(30, bst.search(30));
    }

    // Caso 2: El recorrido in-orden retorna los elementos ordenados ascendentemente segun el orden natural.
    @Test
    void inOrderReturnsElementsInAscendingOrder() {
        // Arrange
        bst.insert(50);
        bst.insert(20);
        bst.insert(70);
        bst.insert(10);
        bst.insert(30);
        bst.insert(60);
        bst.insert(80);

        // Act
        LinkedList<Integer> inOrderList = bst.inOrder();

        // Assert
        assertEquals(7, inOrderList.size());
        int[] expected = {10, 20, 30, 50, 60, 70, 80};
        int index = 0;
        for (Integer val : inOrderList) {
            assertEquals(expected[index++], val);
        }
    }

    // Caso 3: Buscar un elemento inexistente retorna null y contains retorna false.
    @Test
    void searchNonExistentReturnsNullAndContainsFalse() {
        // Arrange
        bst.insert(10);
        bst.insert(20);

        // Act & Assert
        assertNull(bst.search(99));
        assertFalse(bst.contains(99));
    }

    // Caso 4: findMin y findMax retornan los extremos menor y mayor del arbol.
    @Test
    void findMinAndFindMaxReturnCorrectExtremes() {
        // Arrange
        bst.insert(40);
        bst.insert(15);
        bst.insert(65);
        bst.insert(5);
        bst.insert(90);

        // Act & Assert
        assertEquals(5, bst.findMin());
        assertEquals(90, bst.findMax());
    }

    // Caso 5: findMin y findMax en un arbol vacio lanzan EmptyStructureException.
    @Test
    void findMinAndMaxOnEmptyTreeThrowException() {
        // Act & Assert
        assertThrows(EmptyStructureException.class, () -> bst.findMin());
        assertThrows(EmptyStructureException.class, () -> bst.findMax());
    }

    // Caso 6: Eliminar un nodo hoja lo remueve sin alterar el resto del arbol.
    @Test
    void deleteLeafNodeRemovesItFromTree() {
        // Arrange
        bst.insert(50);
        bst.insert(30);
        bst.insert(70);

        // Act
        boolean deleted = bst.delete(30);

        // Assert
        assertTrue(deleted);
        assertEquals(2, bst.size());
        assertFalse(bst.contains(30));
        assertTrue(bst.contains(50));
        assertTrue(bst.contains(70));
    }

    // Caso 7: Eliminar un nodo con un solo hijo reemplaza el nodo por su hijo.
    @Test
    void deleteNodeWithOneChildReplacesWithChild() {
        // Arrange
        bst.insert(50);
        bst.insert(30);
        bst.insert(20); // 30 solo tiene hijo izquierdo (20)

        // Act
        boolean deleted = bst.delete(30);

        // Assert
        assertTrue(deleted);
        assertEquals(2, bst.size());
        assertFalse(bst.contains(30));
        assertTrue(bst.contains(20));
        assertEquals(20, bst.findMin());
    }

    // Caso 8: Eliminar un nodo con dos hijos lo reemplaza por el sucesor in-orden (menor del subarbol derecho).
    @Test
    void deleteNodeWithTwoChildrenReplacesWithSuccessor() {
        // Arrange
        bst.insert(50);
        bst.insert(30);
        bst.insert(70);
        bst.insert(60);
        bst.insert(80);

        // Act: 70 tiene dos hijos (60 y 80), el sucesor in-orden es 80 o el menor de su derecha.
        boolean deleted = bst.delete(70);

        // Assert
        assertTrue(deleted);
        assertEquals(4, bst.size());
        assertFalse(bst.contains(70));
        assertTrue(bst.contains(60));
        assertTrue(bst.contains(80));

        LinkedList<Integer> inOrder = bst.inOrder();
        int[] expected = {30, 50, 60, 80};
        int index = 0;
        for (Integer val : inOrder) {
            assertEquals(expected[index++], val);
        }
    }

    // Caso 9: Intentar eliminar un valor inexistente retorna false y no modifica el tamano.
    @Test
    void deleteNonExistentReturnsFalse() {
        // Arrange
        bst.insert(10);
        bst.insert(20);

        // Act
        boolean deleted = bst.delete(99);

        // Assert
        assertFalse(deleted);
        assertEquals(2, bst.size());
    }

    // Caso 10: Recorridos pre-orden y post-orden visitan los nodos en el orden esperado.
    @Test
    void preOrderAndPostOrderTraversals() {
        // Arrange
        bst.insert(50);
        bst.insert(30);
        bst.insert(70);

        // Act
        LinkedList<Integer> pre = bst.preOrder();
        LinkedList<Integer> post = bst.postOrder();

        // Assert Pre-order: Raiz, Izquierda, Derecha -> 50, 30, 70
        int[] expectedPre = {50, 30, 70};
        int iPre = 0;
        for (Integer val : pre) {
            assertEquals(expectedPre[iPre++], val);
        }

        // Assert Post-order: Izquierda, Derecha, Raiz -> 30, 70, 50
        int[] expectedPost = {30, 70, 50};
        int iPost = 0;
        for (Integer val : post) {
            assertEquals(expectedPost[iPost++], val);
        }
    }

    // Caso 11: Altura del arbol se calcula correctamente.
    @Test
    void heightCalculatesCorrectly() {
        // Arrange & Assert vacio
        assertEquals(0, bst.height());

        // Un solo nodo
        bst.insert(50);
        assertEquals(1, bst.height());

        // Con hijos
        bst.insert(30);
        bst.insert(70);
        assertEquals(2, bst.height());

        bst.insert(20);
        assertEquals(3, bst.height());
    }

    // Caso 12: Arbol con Comparator personalizado (orden descendente).
    @Test
    void treeWithCustomComparatorOrdersCorrectly() {
        // Arrange: Comparator que ordena de mayor a menor
        Comparator<Integer> descending = (a, b) -> b.compareTo(a);
        BinarySearchTree<Integer> descBst = new BinarySearchTree<>(descending);

        // Act
        descBst.insert(10);
        descBst.insert(30);
        descBst.insert(20);

        // Assert: In-orden debe ser 30, 20, 10
        LinkedList<Integer> inOrder = descBst.inOrder();
        int[] expected = {30, 20, 10};
        int idx = 0;
        for (Integer val : inOrder) {
            assertEquals(expected[idx++], val);
        }

        // El findMin bajo este comparador es 30 y findMax es 10
        assertEquals(30, descBst.findMin());
        assertEquals(10, descBst.findMax());
    }

    // Caso 13: Insertar nulo lanza IllegalArgumentException.
    @Test
    void insertNullThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> bst.insert(null));
    }

    // Caso 14: clear resetea el arbol dejandolo vacio.
    @Test
    void clearResetsTree() {
        // Arrange
        bst.insert(10);
        bst.insert(20);

        // Act
        bst.clear();

        // Assert
        assertTrue(bst.isEmpty());
        assertEquals(0, bst.size());
        assertEquals(0, bst.height());
        assertNull(bst.getRoot());
    }
}
