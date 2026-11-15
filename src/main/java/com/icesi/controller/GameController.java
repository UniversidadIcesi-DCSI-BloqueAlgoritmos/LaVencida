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
        if (direction == null) {
            return false;
        }

        // calculamos a donde quedaria antes de moverlo de verdad
        int newRow = operator.getRow();
        int newColumn = operator.getColumn();
        if (direction == Direction.UP) {
            newRow--;
        } else if (direction == Direction.DOWN) {
            newRow++;
        } else if (direction == Direction.LEFT) {
            newColumn--;
        } else if (direction == Direction.RIGHT) {
            newColumn++;
        }

        // no se puede salir del mapa ni pasar por obstaculos o manzanas
        if (!cityMap.isWalkable(newRow, newColumn)) {
            return false;
        }
        operator.move(direction);
        return true;
    }

    // Deshace el ultimo movimiento del operador; retorna false si no habia movimientos.
    public boolean undoOperatorMove() {
        // si la pila esta vacia no hay nada que deshacer, pero el juego sigue
        if (operator.getMovementCount() == 0) {
            return false;
        }
        operator.undoLastMovement();
        return true;
    }

    // Genera un robo o un incendio aleatorio y lo registra como activo.
    public Incident spawnRandomIncident() {
        Incident incident;
        if (random.nextBoolean()) {
            incident = generator.generateRandomTheft();
        } else {
            incident = generator.generateRandomFire();
        }
        manager.registerIncident(incident);
        return incident;
    }

    // Propone el vehiculo disponible mas adecuado para el incidente.
    public Vehicle proposeVehicle(Incident incident) {
        return manager.proposeCandidateVehicle(incident, vehicles);
    }

    // Asigna el vehiculo al incidente usando sus IDs.
    public void assignVehicle(String vehicleId, String incidentId) {
        Vehicle vehicle = findVehicleById(vehicleId);
        Incident incident = manager.findIncidentById(incidentId);
        manager.assignVehicle(vehicle, incident);
    }

    // Finaliza la atencion del incidente y retorna los puntos ganados.
    public int resolveIncident(String incidentId) {
        Incident incident = manager.findIncidentById(incidentId);
        return manager.finishAttention(incident, operator);
    }

    // Devuelve el incidente activo de mayor prioridad o null si no hay ninguno.
    public Incident getHighestPriorityIncidentOrNull() {
        if (manager.getActiveIncidentCount() == 0) {
            return null;
        }
        return manager.getHighestPriorityIncident();
    }

    // Busca un vehiculo por su ID.
    public Vehicle findVehicleById(String vehicleId) {
        for (Vehicle current : vehicles) {
            if (current.getId().equals(vehicleId)) {
                return current;
            }
        }
        throw new IllegalArgumentException("No existe un vehiculo con id " + vehicleId);
    }

    // Devuelve la celda donde se dibuja el incidente en el mapa.
    public int[] getCellOf(Incident incident) {
        return resolver.resolve(incident.getLocation());
    }

    // Devuelve la celda donde se dibuja el vehiculo: en su incidente si esta atendiendo uno, si no en su estacion.
    public int[] getCellOf(Vehicle vehicle) {
        if (vehicle.getAssignedIncident() != null) {
            return getCellOf(vehicle.getAssignedIncident());
        }
        return resolver.resolve(vehicle.getLocation());
    }

    // Devuelve los incidentes activos ordenados por prioridad.
    public LinkedList<Incident> getActiveIncidentsByPriority() {
        return manager.getActiveIncidentsByPriority();
    }

    public int getActiveIncidentCount() {
        return manager.getActiveIncidentCount();
    }

    public int getActiveAccidentCount() {
        return manager.getActiveAccidentCount();
    }

    public int getActiveTheftCount() {
        return manager.getActiveTheftCount();
    }

    public int getActiveFireCount() {
        return manager.getActiveFireCount();
    }

    public int getAvailableVehicleCount() {
        return manager.countAvailableVehicles(vehicles);
    }

    public int getScore() {
        return operator.getScore();
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
