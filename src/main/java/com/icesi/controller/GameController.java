package com.icesi.controller;

import com.icesi.model.CityMap;
import com.icesi.model.Direction;
import com.icesi.model.Incident;
import com.icesi.model.IncidentGenerator;
import com.icesi.model.IncidentManager;
import com.icesi.model.Operator;
import com.icesi.model.Vehicle;
import com.icesi.model.VehicleType;
import com.icesi.structures.LinkedList;

import java.util.Random;

// Controlador de la interfaz grafica (MVC): recibe las acciones del jugador y las traduce en operaciones del modelo.
public class GameController {

    private CityMap cityMap;
    private Operator operator;
    private LinkedList<Vehicle> vehicles;
    private IncidentManager manager;
    private IncidentGenerator generator;
    private MapPositionResolver resolver;
    private Random random;

    // Crea el juego con el mapa de Palmira, el operador en una via principal y los vehiculos iniciales.
    public GameController() {
        this(new CityMap(), new Operator(8, 8), createInitialVehicles(), new Random());
    }

    // Crea el juego con las piezas que se le pasan (sirve para las pruebas).
    public GameController(CityMap cityMap, Operator operator, LinkedList<Vehicle> vehicles, Random random) {
        this.cityMap = cityMap;
        this.operator = operator;
        this.vehicles = vehicles;
        this.random = random;
        this.manager = new IncidentManager();
        this.generator = new IncidentGenerator(random);
        this.resolver = new MapPositionResolver(cityMap);
    }

    // Crea dos vehiculos de cada tipo en sus estaciones.
    public static LinkedList<Vehicle> createInitialVehicles() {
        LinkedList<Vehicle> initial = new LinkedList<>();
        initial.addLast(new Vehicle("P-01", VehicleType.PATROL, "Estacion Norte"));
        initial.addLast(new Vehicle("P-02", VehicleType.PATROL, "Estacion Sur"));
        initial.addLast(new Vehicle("A-01", VehicleType.AMBULANCE, "Hospital Central"));
        initial.addLast(new Vehicle("A-02", VehicleType.AMBULANCE, "Clinica Palmira"));
        initial.addLast(new Vehicle("F-01", VehicleType.FIRE_TRUCK, "Bomberos Centro"));
        initial.addLast(new Vehicle("F-02", VehicleType.FIRE_TRUCK, "Bomberos Sur"));
        return initial;
    }

    // Mueve al operador si la celda destino es transitable; retorna true si se movio.
    public boolean moveOperator(Direction direction) {
        return false;
    }

    // Deshace el ultimo movimiento del operador; retorna false si no habia movimientos.
    public boolean undoOperatorMove() {
        return false;
    }

    // Genera un robo o un incendio aleatorio y lo registra como activo.
    public Incident spawnRandomIncident() {
        return null;
    }

    // Propone el vehiculo disponible mas adecuado para el incidente.
    public Vehicle proposeVehicle(Incident incident) {
        return null;
    }

    // Asigna el vehiculo al incidente usando sus IDs.
    public void assignVehicle(String vehicleId, String incidentId) {

    }

    // Finaliza la atencion del incidente y retorna los puntos ganados.
    public int resolveIncident(String incidentId) {
        return 0;
    }

    // Devuelve el incidente activo de mayor prioridad o null si no hay ninguno.
    public Incident getHighestPriorityIncidentOrNull() {
        return null;
    }

    // Busca un vehiculo por su ID.
    public Vehicle findVehicleById(String vehicleId) {
        return null;
    }

    // Devuelve los incidentes activos ordenados por prioridad.
    public LinkedList<Incident> getActiveIncidentsByPriority() {
        return null;
    }

    public int getActiveIncidentCount() {
        return 0;
    }

    public int getActiveAccidentCount() {
        return 0;
    }

    public int getActiveTheftCount() {
        return 0;
    }

    public int getActiveFireCount() {
        return 0;
    }

    public int getAvailableVehicleCount() {
        return 0;
    }

    public int getScore() {
        return 0;
    }

    public CityMap getCityMap() {
        return cityMap;
    }

    public Operator getOperator() {
        return operator;
    }

    public LinkedList<Vehicle> getVehicles() {
        return vehicles;
    }

    public MapPositionResolver getResolver() {
        return resolver;
    }
}
