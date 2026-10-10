package com.icesi.model;

import com.icesi.structures.Stack;

public class Operator {

    private int row;
    private int column;
    private Stack<Movement> movementHistory;
    private int score;

    public Operator(int row, int column) {
        this.row = row;
        this.column = column;
        this.movementHistory = new Stack<>();
        this.score = 0;
    }

    public int getScore() {
        return score;
    }

    public void addScore(int points) {
        this.score += points;
    }

    public int getRow() {
        return row;
    }

    public int getColumn() {
        return column;
    }

    public void move(Direction direction) {
        if (direction == null) {
            throw new IllegalArgumentException("La direccion no puede ser nula");
        }

        // guardamos donde estaba antes de moverse para poder deshacer
        movementHistory.push(new Movement(direction, row, column));

        if (direction == Direction.UP) {
            row--;
        } else if (direction == Direction.DOWN) {
            row++;
        } else if (direction == Direction.LEFT) {
            column--;
        } else if (direction == Direction.RIGHT) {
            column++;
        }
    }

    public Movement undoLastMovement() {
        // si no hay movimientos la pila lanza EmptyStructureException
        Movement last = movementHistory.pop();
        row = last.getPreviousRow();
        column = last.getPreviousColumn();
        return last;
    }

    public int getMovementCount() {
        return movementHistory.size();
    }
}
