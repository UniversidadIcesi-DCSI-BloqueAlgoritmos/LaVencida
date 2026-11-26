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

        // Si la ubicacion especifica coordenadas explicitas como "Sector (row, col)"
        if (location.contains("(") && location.contains(")") && location.contains(",")) {
            try {
                int open = location.lastIndexOf('(');
                int close = location.lastIndexOf(')');
                String inside = location.substring(open + 1, close);
                String[] parts = inside.split(",");
                if (parts.length == 2) {
                    int r = Integer.parseInt(parts[0].trim());
                    int c = Integer.parseInt(parts[1].trim());
                    if (r >= 0 && r < cityMap.getSize() && c >= 0 && c < cityMap.getSize() && cityMap.isWalkable(r, c)) {
                        return new int[]{r, c};
                    }
                }
            } catch (NumberFormatException ignored) {
            }
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
