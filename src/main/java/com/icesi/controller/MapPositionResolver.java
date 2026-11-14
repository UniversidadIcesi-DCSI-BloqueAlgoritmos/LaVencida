package com.icesi.controller;

import com.icesi.model.CityMap;

// Traduce el nombre de una ubicacion (por ejemplo "Parque Bolivar") a una celda del mapa para poder dibujarla en la interfaz.
public class MapPositionResolver {

    private CityMap cityMap;

    // Crea el traductor de ubicaciones sobre el mapa de la ciudad.
    public MapPositionResolver(CityMap cityMap) {
        this.cityMap = cityMap;
    }

    // Devuelve la celda {fila, columna} transitable que le corresponde a la ubicacion.
    public int[] resolve(String location) {
        return null;
    }
}
