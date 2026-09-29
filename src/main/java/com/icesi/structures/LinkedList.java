package com.icesi.structures;

public class LinkedList<T> {

    private Node<T> head;
    private Node<T> tail;
    private int size;

    private static class Node<T> {
        T value;
        Node<T> next;

        Node(T value) {
            this.value = value;
        }
    }

    public LinkedList() {
    }

    public void addLast(T value) {
    }

    public T get(int index) {
        return null;
    }

    public int size() {
        return 0;
    }

    public boolean isEmpty() {
        return true;
    }
}
