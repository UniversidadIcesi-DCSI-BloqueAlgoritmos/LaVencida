package com.icesi.controller;

import com.icesi.model.CityMap;
import com.icesi.structures.LinkedList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RouteFinderTest {

    private CityMap cityMap;
    private RouteFinder finder;

    @BeforeEach
    void setUp() {
        cityMap = new CityMap();
        finder = new RouteFinder(cityMap);
    }

    // Caso 1: en una calle recta el camino tiene todas las celdas entre el inicio y el destino.
    @Test
    void findRouteOnStraightStreetReturnsAllCells() {
        // Act
        LinkedList<int[]> route = finder.findRoute(new int[]{4, 4}, new int[]{4, 8});

        // Assert
        assertEquals(5, route.size());
        assertArrayEquals(new int[]{4, 4}, route.getFirst());
        assertArrayEquals(new int[]{4, 8}, route.get(4));
    }

    // Caso 2: cada paso del camino es una celda transitable vecina de la anterior.
    @Test
    void findRouteOnlyUsesWalkableNeighbourCells() {
        // Act
        LinkedList<int[]> route = finder.findRoute(new int[]{4, 4}, new int[]{40, 52});

        // Assert
        int[] before = null;
        for (int[] cell : route) {
            assertTrue(cityMap.isWalkable(cell[0], cell[1]));
            if (before != null) {
                assertEquals(1, Math.abs(cell[0] - before[0]) + Math.abs(cell[1] - before[1]));
            }
            before = cell;
        }
        assertArrayEquals(new int[]{40, 52}, before);
    }

    // Caso 3: si el inicio y el destino son la misma celda el camino tiene una sola celda.
    @Test
    void findRouteToSameCellReturnsOneCell() {
        // Act
        LinkedList<int[]> route = finder.findRoute(new int[]{8, 8}, new int[]{8, 8});

        // Assert
        assertEquals(1, route.size());
    }
}
