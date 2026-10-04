package com.icesi.model;

import com.icesi.structures.Stack;

public class Operator {

    private int row;
    private int column;
    private Stack<Movement> movementHistory;

    public Operator(int row, int column) {
        this.row = row;
        this.column = column;
        this.movementHistory = new Stack<>();
    }

    public int getRow() {
        return row;
    }

    public int getColumn() {
        return column;
    }

    public void move(Direction direction) {

    }

    public Movement undoLastMovement() {
        return null;
    }

    public int getMovementCount() {
        return 0;
    }
}
