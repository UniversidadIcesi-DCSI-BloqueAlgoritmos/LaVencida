package com.icesi.structures;

import com.icesi.exceptions.EmptyStructureException;

public class Stack<T> {

    // el tope de la pila es la cabeza de la lista, asi push y pop son O(1)
    private LinkedList<T> elements;

    public Stack() {
        this.elements = new LinkedList<>();
    }

    public void push(T value) {
        elements.addFirst(value);
    }

    public T pop() {
        if (isEmpty()) {
            throw new EmptyStructureException("La pila esta vacia");
        }
        return elements.removeFirst();
    }

    public T peek() {
        if (isEmpty()) {
            throw new EmptyStructureException("La pila esta vacia");
        }
        return elements.getFirst();
    }

    public boolean isEmpty() {
        return elements.isEmpty();
    }

    public int size() {
        return elements.size();
    }
}
