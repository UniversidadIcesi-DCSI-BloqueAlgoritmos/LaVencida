package com.icesi.model;

import java.util.Comparator;

// Ordena los incidentes por ID alfabeticamente, se usa para la busqueda binaria
public class IncidentIdComparator implements Comparator<Incident> {

    @Override
    public int compare(Incident first, Incident second) {
        return first.getId().compareTo(second.getId());
    }
}
