package com.icesi.model;

public class CityMap {

    public static final int MAP_SIZE = 64;
    public static final int PATH = 0;
    public static final int OBSTACLE = 1;
    public static final int RESIDENTIAL = 2;
    public static final int COMMERCIAL = 3;
    public static final int MAIN_ROAD = 4;

    private int[][] grid;

    public CityMap() {
        this.grid = new int[MAP_SIZE][MAP_SIZE];
    }

    public int getSize() {
        return 0;
    }

    public boolean isInsideBounds(int row, int column) {
        return false;
    }

    public int getCell(int row, int column) {
        return -1;
    }

    public boolean isWalkable(int row, int column) {
        return false;
    }
}
