package com.icesi.structures;

public class LinkedList<T> {

    private Node<T> head;
    private Node<T> tail;
    private int size;

    public LinkedList() {
        this.head = null;
        this.tail = null;
        this.size = 0;
    }

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

    public T getFirst() {
        if (isEmpty()) {
            throw new EmptyStructureException("La lista esta vacia");
        }
        return head.getValue();
    }

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

    public T get(int index) {
        Node<T> current = head;
        for (int i = 0; i < index; i++) {
            current = current.getNext();
        }
        return current.getValue();
    }

    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return size == 0;
    }
}