package com.icesi.model;

import java.io.Serializable;

public class Movement implements Serializable {

    private Direction direction;
    private int previousRow;
    private int previousColumn;

    public Movement(Direction direction, int previousRow, int previousColumn) {
        this.direction = direction;
        this.previousRow = previousRow;
        this.previousColumn = previousColumn;
    }

    public Direction getDirection() {
        return direction;
    }

    public int getPreviousRow() {
        return previousRow;
    }

    public int getPreviousColumn() {
        return previousColumn;
    }
}
