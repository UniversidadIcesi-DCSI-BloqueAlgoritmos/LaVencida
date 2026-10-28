package com.icesi.structures;

import com.icesi.exceptions.EmptyStructureException;

import java.io.Serializable;
import java.util.Comparator;

/**
 * Arbol Binario de Busqueda (BST) generico.
 * Permite almacenar elementos organizados segun su orden natural (Comparable)
 * o mediante un Comparator personalizado.
 *
 * @param <T> Tipo de elemento almacenado en el arbol.
 */
public class BinarySearchTree<T> implements Serializable {

    private BSTNode<T> root;
    private Comparator<T> comparator;
    private int size;

    // Constructor que utiliza el orden natural de los elementos (deben ser Comparable).
    public BinarySearchTree() {
        this.root = null;
        this.comparator = null;
        this.size = 0;
    }

    // Constructor que utiliza un Comparator explicito para definir el orden.
    public BinarySearchTree(Comparator<T> comparator) {
        this.root = null;
        this.comparator = comparator;
        this.size = 0;
    }

    // Compara dos elementos usando el comparator configurado o el orden natural.
    @SuppressWarnings("unchecked")
    private int compare(T first, T second) {
        if (comparator != null) {
            return comparator.compare(first, second);
        }
        if (first instanceof Comparable) {
            return ((Comparable<T>) first).compareTo(second);
        }
        throw new IllegalArgumentException("El tipo no implementa Comparable y no se configuro un Comparator.");
    }

    // Inserta un nuevo elemento en el arbol manteniendo la propiedad de BST.
    public void insert(T value) {
        if (value == null) {
            throw new IllegalArgumentException("No se pueden insertar valores nulos en el arbol.");
        }
        root = insertRecursive(root, value);
    }

    private BSTNode<T> insertRecursive(BSTNode<T> current, T value) {
        if (current == null) {
            size++;
            return new BSTNode<>(value);
        }

        int cmp = compare(value, current.getValue());
        if (cmp < 0) {
            current.setLeft(insertRecursive(current.getLeft(), value));
        } else if (cmp > 0) {
            current.setRight(insertRecursive(current.getRight(), value));
        } else {
            // Si el valor ya existe con la misma clave, se actualiza el valor almacenado
            current.setValue(value);
        }
        return current;
    }

    // Verifica si un elemento existe dentro del arbol.
    public boolean contains(T value) {
        return search(value) != null;
    }

    // Busca un elemento en el arbol y lo retorna si existe; de lo contrario retorna null.
    public T search(T value) {
        if (value == null || root == null) {
            return null;
        }
        return searchRecursive(root, value);
    }

    private T searchRecursive(BSTNode<T> current, T value) {
        if (current == null) {
            return null;
        }
        int cmp = compare(value, current.getValue());
        if (cmp == 0) {
            return current.getValue();
        } else if (cmp < 0) {
            return searchRecursive(current.getLeft(), value);
        } else {
            return searchRecursive(current.getRight(), value);
        }
    }

    // Elimina un elemento del arbol si existe. Retorna true si fue eliminado.
    public boolean delete(T value) {
        if (value == null || root == null) {
            return false;
        }
        int initialSize = size;
        root = deleteRecursive(root, value);
        return size < initialSize;
    }

    private BSTNode<T> deleteRecursive(BSTNode<T> current, T value) {
        if (current == null) {
            return null;
        }

        int cmp = compare(value, current.getValue());
        if (cmp < 0) {
            current.setLeft(deleteRecursive(current.getLeft(), value));
        } else if (cmp > 0) {
            current.setRight(deleteRecursive(current.getRight(), value));
        } else {
            // Nodo encontrado, procedemos con los 3 casos de eliminacion:
            size--;

            // Caso 1 y 2: Sin hijos (hoja) o con un solo hijo
            if (current.getLeft() == null) {
                return current.getRight();
            } else if (current.getRight() == null) {
                return current.getLeft();
            }

            // Caso 3: Dos hijos. Buscamos el sucesor in-orden (el menor del subarbol derecho)
            T successorValue = findMinNode(current.getRight()).getValue();
            current.setValue(successorValue);
            // Restauramos el contador temporalmente para no decrementar doble
            size++;
            current.setRight(deleteRecursive(current.getRight(), successorValue));
        }
        return current;
    }

    private BSTNode<T> findMinNode(BSTNode<T> node) {
        BSTNode<T> current = node;
        while (current.getLeft() != null) {
            current = current.getLeft();
        }
        return current;
    }

    // Retorna el elemento menor del arbol (nodo mas a la izquierda).
    public T findMin() {
        if (isEmpty()) {
            throw new EmptyStructureException("El arbol esta vacio.");
        }
        return findMinNode(root).getValue();
    }

    // Retorna el elemento mayor del arbol (nodo mas a la derecha).
    public T findMax() {
        if (isEmpty()) {
            throw new EmptyStructureException("El arbol esta vacio.");
        }
        BSTNode<T> current = root;
        while (current.getRight() != null) {
            current = current.getRight();
        }
        return current.getValue();
    }

    // Retorna una lista con los elementos del arbol ordenados ascendentemente (in-orden).
    public LinkedList<T> inOrder() {
        LinkedList<T> result = new LinkedList<>();
        inOrderRecursive(root, result);
        return result;
    }

    private void inOrderRecursive(BSTNode<T> current, LinkedList<T> result) {
        if (current != null) {
            inOrderRecursive(current.getLeft(), result);
            result.addLast(current.getValue());
            inOrderRecursive(current.getRight(), result);
        }
    }

    // Retorna una lista con el recorrido pre-orden (raiz, izquierda, derecha).
    public LinkedList<T> preOrder() {
        LinkedList<T> result = new LinkedList<>();
        preOrderRecursive(root, result);
        return result;
    }

    private void preOrderRecursive(BSTNode<T> current, LinkedList<T> result) {
        if (current != null) {
            result.addLast(current.getValue());
            preOrderRecursive(current.getLeft(), result);
            preOrderRecursive(current.getRight(), result);
        }
    }

    // Retorna una lista con el recorrido post-orden (izquierda, derecha, raiz).
    public LinkedList<T> postOrder() {
        LinkedList<T> result = new LinkedList<>();
        postOrderRecursive(root, result);
        return result;
    }

    private void postOrderRecursive(BSTNode<T> current, LinkedList<T> result) {
        if (current != null) {
            postOrderRecursive(current.getLeft(), result);
            postOrderRecursive(current.getRight(), result);
            result.addLast(current.getValue());
        }
    }

    // Calcula la altura del arbol. Un arbol vacio tiene altura 0; con solo la raiz tiene altura 1.
    public int height() {
        return heightRecursive(root);
    }

    private int heightRecursive(BSTNode<T> current) {
        if (current == null) {
            return 0;
        }
        int leftHeight = heightRecursive(current.getLeft());
        int rightHeight = heightRecursive(current.getRight());
        return Math.max(leftHeight, rightHeight) + 1;
    }

    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return root == null || size == 0;
    }

    public void clear() {
        this.root = null;
        this.size = 0;
    }

    public BSTNode<T> getRoot() {
        return root;
    }
}
