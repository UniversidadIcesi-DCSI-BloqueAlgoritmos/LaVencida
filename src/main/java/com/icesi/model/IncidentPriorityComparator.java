package com.icesi.model;

import java.io.Serializable;
import java.util.Comparator;

// Ordena primero los mas graves (HIGH > MEDIUM > LOW) y si empatan, el mas antiguo
public class IncidentPriorityComparator implements Comparator<Incident>, Serializable {

    private static final long serialVersionUID = 1L;

    @Override
    public int compare(Incident first, Incident second) {
        // en el enum HIGH esta primero, entonces ordinal menor = mas grave
        int severityComparison = first.getSeverity().ordinal() - second.getSeverity().ordinal();
        if (severityComparison != 0) {
            return severityComparison;
        }
        return first.getGeneratedAt().compareTo(second.getGeneratedAt());
    }
}
