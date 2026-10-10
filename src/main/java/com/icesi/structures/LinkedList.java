package com.icesi.structures;

import com.icesi.exceptions.EmptyStructureException;

import java.util.Comparator;
import java.util.Iterator;
import java.util.NoSuchElementException;

public class LinkedList<T> implements Iterable<T> {

    private Node<T> head;
    private Node<T> tail;
    private int size;

    // Crea una lista vacia: sin nodos y con tamano 0.
    public LinkedList() {
        this.head = null;
        this.tail = null;
        this.size = 0;
    }

    // Agrega un elemento al final de la lista.
    public void addLast(T value) {
        Node<T> newNode = new Node<>(value);
        if (isEmpty()) {
            head = newNode;
        } else {
            tail.setNext(newNode);
        }
        tail = newNode;
        size++;
    }

    // Agrega un elemento al inicio de la lista.
    public void addFirst(T value) {
        Node<T> newNode = new Node<>(value);
        if (isEmpty()) {
            tail = newNode;
        } else {
            newNode.setNext(head);
        }
        head = newNode;
        size++;
    }

    // Devuelve el primer elemento sin quitarlo; lanza EmptyStructureException si la lista esta vacia.
    public T getFirst() {
        if (isEmpty()) {
            throw new EmptyStructureException("La lista esta vacia");
        }
        return head.getValue();
    }

    // Quita y devuelve el primer elemento; lanza EmptyStructureException si la lista esta vacia.
    public T removeFirst() {
        if (isEmpty()) {
            throw new EmptyStructureException("La lista esta vacia");
        }
        T value = head.getValue();
        head = head.getNext();
        size--;
        // si quedo vacia la cola tampoco apunta a nada
        if (isEmpty()) {
            tail = null;
        }
        return value;
    }

    // Devuelve el elemento que esta en la posicion indicada, recorriendo desde la cabeza.
    public T get(int index) {
        Node<T> current = head;
        for (int i = 0; i < index; i++) {
            current = current.getNext();
        }
        return current.getValue();
    }

    // Devuelve un iterador para recorrer la lista de la cabeza a la cola.
    @Override
    public Iterator<T> iterator() {
        return new LinkedListIterator();
    }

    // Devuelve la cantidad de elementos de la lista.
    public int size() {
        return size;
    }

    // Indica si la lista no tiene elementos.
    public boolean isEmpty() {
        return size == 0;
    }

    // Quita y devuelve el elemento de la posicion indicada; lanza IndexOutOfBoundsException si el indice no es valido.
    public T remove(int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("Indice fuera de rango: " + index);
        }
        if (index == 0) {
            return removeFirst();
        }

        // nos paramos en el nodo anterior al que queremos quitar
        Node<T> previous = head;
        for (int i = 0; i < index - 1; i++) {
            previous = previous.getNext();
        }
        Node<T> removed = previous.getNext();
        previous.setNext(removed.getNext());

        if (removed == tail) {
            tail = previous;
        }
        size--;
        return removed.getValue();
    }

    // Indica si el valor esta en la lista, comparandolo con equals.
    public boolean contains(T value) {
        Node<T> current = head;
        while (current != null) {
            if (current.getValue().equals(value)) {
                return true;
            }
            current = current.getNext();
        }
        return false;
    }

    /**
     * Ordena la lista de menor a mayor segun el comparador usando ordenamiento burbuja.
     * Solo se intercambian los valores de los nodos, los enlaces no cambian.
     * Es estable: los elementos iguales conservan el orden en que estaban.
     *
     * Complejidad temporal: mejor caso O(n) (lista ya ordenada, se detiene en la primera pasada),
     * caso promedio O(n^2), peor caso O(n^2) (lista en orden inverso).
     * Complejidad espacial: O(1) en todos los casos, no se crean listas auxiliares.
     *
     * @param comparator criterio para comparar los elementos
     */
    public void sort(Comparator<T> comparator) {
        for (int i = 0; i < size - 1; i++) {
            boolean swapped = false;
            Node<T> current = head;

            // en cada pasada el mayor que falta queda al final
            for (int j = 0; j < size - 1 - i; j++) {
                Node<T> next = current.getNext();
                if (comparator.compare(current.getValue(), next.getValue()) > 0) {
                    T temp = current.getValue();
                    current.setValue(next.getValue());
                    next.setValue(temp);
                    swapped = true;
                }
                current = next;
            }

            // si no hubo cambios la lista ya esta ordenada
            if (!swapped) {
                break;
            }
        }
    }

    /**
     * Busca un elemento con busqueda binaria. La lista debe estar ordenada con el mismo comparador.
     *
     * Complejidad temporal: se hacen O(log n) comparaciones, pero como get(i) en una lista enlazada
     * recorre desde la cabeza, cada acceso cuesta O(n). Mejor caso O(n) (el elemento esta en la mitad),
     * caso promedio O(n log n), peor caso O(n log n) (el elemento no esta).
     * Complejidad espacial: O(1) en todos los casos.
     *
     * @param target elemento a buscar
     * @param comparator criterio con el que esta ordenada la lista
     * @return la posicion del elemento o -1 si no esta
     */
    public int binarySearch(T target, Comparator<T> comparator) {
        int low = 0;
        int high = size - 1;

        while (low <= high) {
            int middle = (low + high) / 2;
            int result = comparator.compare(get(middle), target);

            if (result == 0) {
                return middle;
            } else if (result < 0) {
                low = middle + 1; // el buscado esta a la derecha
            } else {
                high = middle - 1; // el buscado esta a la izquierda
            }
        }
        return -1;
    }

    // Iterador interno: guarda el nodo por el que va y avanza un nodo cada vez que se pide el siguiente elemento.
    private class LinkedListIterator implements Iterator<T> {

        private Node<T> current = head;

        // Indica si todavia quedan elementos por recorrer.
        @Override
        public boolean hasNext() {
            return current != null;
        }

        // Devuelve el elemento actual y avanza al siguiente; lanza NoSuchElementException si ya no quedan elementos.
        @Override
        public T next() {
            if (!hasNext()) {
                throw new NoSuchElementException("No hay mas elementos");
            }
            T value = current.getValue();
            current = current.getNext();
            return value;
        }
    }
}