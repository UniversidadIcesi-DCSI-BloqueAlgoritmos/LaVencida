package com.icesi.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CityMapTest {

    private CityMap cityMap;

    @BeforeEach
    void setUp() {
        cityMap = new CityMap();
    }

    // RF2 - Caso 1: El mapa se inicializa con dimension 64x64 y bordes como obstaculos.
    @Test
    void cityMapInitializesWithCorrectSizeAndBorderObstacles() {
        // Act
        int size = cityMap.getSize();
        int topBorder = cityMap.getCell(0, 0);
        int bottomBorder = cityMap.getCell(63, 63);

        // Assert
        assertEquals(64, size);
        assertEquals(CityMap.OBSTACLE, topBorder);
        assertEquals(CityMap.OBSTACLE, bottomBorder);
    }

    // RF2 - Caso 2: Las vias principales y los caminos secundarios son transitables (isWalkable = true).
    @Test
    void pathsAndMainRoadsAreWalkable() {
        // Arrange
        int pathRow = 4;
        int pathCol = 4;
        int mainRoadRow = 8;
        int mainRoadCol = 8;

        // Act
        boolean pathWalkable = cityMap.isWalkable(pathRow, pathCol);
        boolean mainRoadWalkable = cityMap.isWalkable(mainRoadRow, mainRoadCol);

        // Assert
        assertEquals(CityMap.PATH, cityMap.getCell(pathRow, pathCol));
        assertEquals(CityMap.MAIN_ROAD, cityMap.getCell(mainRoadRow, mainRoadCol));
        assertTrue(pathWalkable);
        assertTrue(mainRoadWalkable);
    }

    // RF2 - Caso 3: Los obstaculos y las zonas residenciales o comerciales no son transitables.
    @Test
    void obstaclesAndZonesAreNotWalkable() {
        //Act & Assert
        assertFalse(cityMap.isWalkable(0, 0));     // borde obstaculo
        assertFalse(cityMap.isWalkable(20, 12));   // obstaculo en via
        assertFalse(cityMap.isWalkable(1, 1));     // zona dentro de manzana
    }

    // RF2 - Caso 4: Coordenadas fuera de los limites del mapa no son transitables y lanzan excepcion en getCell.
    @Test
    void outOfBoundsCoordinatesAreNotWalkableAndThrowExceptionInGetCell() {
        // Act & Assert
        assertFalse(cityMap.isInsideBounds(-1, 0));
        assertFalse(cityMap.isInsideBounds(0, 64));
        assertFalse(cityMap.isWalkable(-1, 5));
        assertFalse(cityMap.isWalkable(64, 5));
        assertThrows(IndexOutOfBoundsException.class,
                () -> cityMap.getCell(-1, 0));
    }
}
