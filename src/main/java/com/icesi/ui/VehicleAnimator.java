package com.icesi.ui;

import com.icesi.controller.GameController;
import com.icesi.controller.RouteFinder;
import com.icesi.model.Vehicle;
import com.icesi.structures.LinkedList;

import java.util.HashMap;
import java.util.Map;

// Lleva la cuenta de donde se esta dibujando cada vehiculo para que viaje por las calles
// (celda por celda) cuando lo asignan a un incidente o cuando vuelve a su estacion.
public class VehicleAnimator {

    private static final double CELLS_PER_SECOND = 9;

    private GameController controller;
    private RouteFinder routeFinder;
    private Map<String, Trip> trips = new HashMap<>();

    public VehicleAnimator(GameController controller) {
        this.controller = controller;
        this.routeFinder = new RouteFinder(controller.getCityMap());
    }

    // Avanza todos los viajes; seconds es el tiempo que paso desde el cuadro anterior.
    public void update(double seconds) {
        for (Vehicle vehicle : controller.getVehicles()) {
            int[] target = controller.getCellOf(vehicle);
            Trip trip = trips.get(vehicle.getId());
            if (trip == null) {
                // la primera vez el vehiculo aparece directo en su lugar
                trip = new Trip(routeFinder.findRoute(target, target), target);
                trips.put(vehicle.getId(), trip);
            } else if (trip.target[0] != target[0] || trip.target[1] != target[1]) {
                // cambio el destino: se arma una ruta nueva desde donde va ahora
                int[] here = trip.currentCell();
                trip.route = routeFinder.findRoute(here, target);
                trip.target = target;
                trip.progress = 0;
            }
            trip.progress = Math.min(trip.route.size() - 1, trip.progress + seconds * CELLS_PER_SECOND);
        }
    }

    // Devuelve la posicion del vehiculo en celdas (fila, columna) con decimales, para dibujarlo entre dos celdas.
    public double[] positionOf(Vehicle vehicle) {
        Trip trip = trips.get(vehicle.getId());
        if (trip == null) {
            int[] cell = controller.getCellOf(vehicle);
            return new double[]{cell[0], cell[1]};
        }
        int index = (int) Math.floor(trip.progress);
        double fraction = trip.progress - index;
        int[] from = trip.route.get(index);
        int[] to = trip.route.get(Math.min(index + 1, trip.route.size() - 1));
        return new double[]{from[0] + (to[0] - from[0]) * fraction, from[1] + (to[1] - from[1]) * fraction};
    }

    // Indica si el vehiculo todavia va en camino (no ha llegado a su destino).
    public boolean isMoving(Vehicle vehicle) {
        Trip trip = trips.get(vehicle.getId());
        return trip != null && trip.progress < trip.route.size() - 1;
    }

    // Indica si el vehiculo ya llego al lugar donde lo mandaron (se considera cuando ya termino su ruta).
    public boolean hasArrived(Vehicle vehicle) {
        Trip trip = trips.get(vehicle.getId());
        if (trip == null) {
            return false;
        }
        // si lo acaban de asignar y el viaje aun no se actualizo, el destino todavia es el viejo
        int[] target = controller.getCellOf(vehicle);
        boolean sameTarget = trip.target[0] == target[0] && trip.target[1] == target[1];
        return sameTarget && !isMoving(vehicle);
    }

    // Un viaje: la ruta por las calles, el destino y cuantas celdas lleva recorridas.
    private static class Trip {
        private LinkedList<int[]> route;
        private int[] target;
        private double progress;

        private Trip(LinkedList<int[]> route, int[] target) {
            this.route = route;
            this.target = target;
        }

        private int[] currentCell() {
            return route.get((int) Math.round(progress));
        }
    }
}
