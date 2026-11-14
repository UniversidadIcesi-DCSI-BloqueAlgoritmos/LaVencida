package com.icesi.controller;

import com.icesi.model.CityMap;

// Traduce el nombre de una ubicacion (por ejemplo "Parque Bolivar") a una celda del mapa para poder dibujarla en la interfaz.
public class MapPositionResolver {

    private CityMap cityMap;

    // Crea el traductor de ubicaciones sobre el mapa de la ciudad.
    public MapPositionResolver(CityMap cityMap) {
        this.cityMap = cityMap;
    }

    // Devuelve la celda {fila, columna} transitable que le corresponde a la ubicacion; lanza IllegalArgumentException si es nula o vacia.
    public int[] resolve(String location) {
        if (location == null || location.isBlank()) {
            throw new IllegalArgumentException("La ubicacion no puede ser nula ni vacia");
        }

        int size = cityMap.getSize();
        int totalCells = size * size;

        // el hash del nombre siempre da el mismo numero, asi la misma ubicacion cae en la misma celda
        int start = Math.floorMod(location.trim().hashCode() * 31 + 7, totalCells);

        // desde esa celda avanzamos hasta encontrar una por la que se pueda transitar
        for (int i = 0; i < totalCells; i++) {
            int index = (start + i) % totalCells;
            int row = index / size;
            int column = index % size;
            if (cityMap.isWalkable(row, column)) {
                return new int[]{row, column};
            }
        }
        throw new IllegalStateException("El mapa no tiene celdas transitables");
    }
}
