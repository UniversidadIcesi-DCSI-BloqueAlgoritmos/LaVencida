package com.icesi.controller;

import com.icesi.model.CityMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MapPositionResolverTest {

    private CityMap cityMap;
    private MapPositionResolver resolver;

    @BeforeEach
    void setUp() {
        cityMap = new CityMap();
        resolver = new MapPositionResolver(cityMap);
    }

    // RF2 - Caso 1: Resolver dos veces la misma ubicacion devuelve la misma celda.
    @Test
    void resolveSameLocationTwiceReturnsSameCell() {
        // Act
        int[] first = resolver.resolve("Parque Bolivar");
        int[] second = resolver.resolve("Parque Bolivar");

        // Assert
        assertArrayEquals(first, second);
    }

    // RF2 - Caso 2: La celda resuelta siempre es transitable y esta dentro del mapa.
    @Test
    void resolvedCellIsAlwaysWalkable() {
        // Arrange
        String[] locations = {"Zona Comercial Centro", "Barrio El Prado", "Hospital Central", "Estacion Norte"};

        for (String location : locations) {
            // Act
            int[] cell = resolver.resolve(location);

            // Assert
            assertTrue(cityMap.isWalkable(cell[0], cell[1]));
        }
    }

    // RF2 - Caso 3: Ubicaciones distintas quedan en celdas distintas.
    @Test
    void differentLocationsGetDifferentCells() {
        // Act
        int[] park = resolver.resolve("Parque Bolivar");
        int[] prado = resolver.resolve("Barrio El Prado");

        // Assert
        assertFalse(park[0] == prado[0] && park[1] == prado[1]);
    }

    // RF2 - Caso 4: Resolver una ubicacion nula o vacia lanza una excepcion.
    @Test
    void resolveNullOrBlankLocationThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class,
                () -> resolver.resolve(null));
        assertThrows(IllegalArgumentException.class,
                () -> resolver.resolve("   "));
    }
}
