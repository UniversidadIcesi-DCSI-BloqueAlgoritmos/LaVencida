package com.icesi.controller;

import com.icesi.model.CityMap;
import com.icesi.structures.LinkedList;

// Busca el camino mas corto por las calles entre dos celdas del mapa (busqueda en anchura, BFS).
// Lo usa la interfaz para que los vehiculos viajen por las calles hasta el incidente.
public class RouteFinder {

    private static final int[] ROW_STEPS = {-1, 1, 0, 0};
    private static final int[] COLUMN_STEPS = {0, 0, -1, 1};

    private CityMap cityMap;

    public RouteFinder(CityMap cityMap) {
        this.cityMap = cityMap;
    }

    // Devuelve las celdas del camino desde from hasta to (las dos incluidas).
    // Si no hay camino devuelve una lista que solo tiene el destino, asi el vehiculo "aparece" alla.
    public LinkedList<int[]> findRoute(int[] from, int[] to) {
        int size = cityMap.getSize();
        boolean[][] visited = new boolean[size][size];
        int[][] previous = new int[size * size][]; // de que celda venimos a cada celda

        // la lista enlazada propia funciona como cola: se agrega al final y se saca del inicio
        LinkedList<int[]> queue = new LinkedList<>();
        queue.addLast(from);
        visited[from[0]][from[1]] = true;

        boolean found = false;
        while (!queue.isEmpty() && !found) {
            int[] current = queue.removeFirst();
            if (current[0] == to[0] && current[1] == to[1]) {
                found = true;
            } else {
                for (int i = 0; i < ROW_STEPS.length; i++) {
                    int row = current[0] + ROW_STEPS[i];
                    int column = current[1] + COLUMN_STEPS[i];
                    if (cityMap.isWalkable(row, column) && !visited[row][column]) {
                        visited[row][column] = true;
                        previous[row * size + column] = current;
                        queue.addLast(new int[]{row, column});
                    }
                }
            }
        }

        LinkedList<int[]> route = new LinkedList<>();
        if (!found) {
            route.addFirst(to);
            return route;
        }
        // se reconstruye el camino de atras hacia adelante siguiendo "previous"
        int[] step = to;
        while (step != null) {
            route.addFirst(step);
            step = (step[0] == from[0] && step[1] == from[1]) ? null : previous[step[0] * size + step[1]];
        }
        return route;
    }
}
