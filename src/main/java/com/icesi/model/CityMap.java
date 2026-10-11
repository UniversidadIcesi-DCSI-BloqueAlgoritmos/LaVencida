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
        buildMap();
    }

    // Construye el mapa: obstaculos en el borde, vias principales cada 16 celdas, caminos cada 4, zonas residenciales y comerciales en las manzanas, y algunos obstaculos.
    private void buildMap() {
        for (int i = 0; i < MAP_SIZE; i++) {
            for (int j = 0; j < MAP_SIZE; j++) {
                if (i == 0 || j == 0 || i == MAP_SIZE - 1 || j == MAP_SIZE - 1) {
                    grid[i][j] = OBSTACLE;
                } else if (i % 16 == 8 || j % 16 == 8) {
                    grid[i][j] = MAIN_ROAD;
                } else if (i % 4 == 0 || j % 4 == 0) {
                    grid[i][j] = PATH;
                } else if ((i / 4 + j / 4) % 3 == 0) {
                    grid[i][j] = COMMERCIAL;
                } else {
                    grid[i][j] = RESIDENTIAL;
                }
            }
        }

        // obstaculos dentro de manzanas (3x3 celdas)
        fillObstacle(17, 19, 17, 19);
        fillObstacle(45, 47, 29, 31);

        // obstaculos sobre caminos: obligan a buscar otra ruta
        grid[20][12] = OBSTACLE;
        grid[12][28] = OBSTACLE;
    }

    // Marca como obstaculo todas las celdas de un rectangulo (limites incluidos).
    private void fillObstacle(int fromRow, int toRow, int fromColumn, int toColumn) {
        for (int i = fromRow; i <= toRow; i++) {
            for (int j = fromColumn; j <= toColumn; j++) {
                grid[i][j] = OBSTACLE;
            }
        }
    }

    public int getSize() {
        return MAP_SIZE;
    }

    public boolean isInsideBounds(int row, int column) {
        return row >= 0 && row < MAP_SIZE && column >= 0 && column < MAP_SIZE;
    }

    public int getCell(int row, int column) {
        if (!isInsideBounds(row, column)) {
            throw new IndexOutOfBoundsException("Coordenada fuera de los limites del mapa: (" + row + ", " + column + ")");
        }
        return grid[row][column];
    }

    // Indica si una celda esta dentro del mapa y se puede transitar: solo los caminos (0) y las vias principales (4).
    public boolean isWalkable(int row, int column) {
        if (!isInsideBounds(row, column)) {
            return false;
        }
        return grid[row][column] == PATH || grid[row][column] == MAIN_ROAD;
    }
}
