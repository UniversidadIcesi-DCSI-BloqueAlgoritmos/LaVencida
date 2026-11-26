package com.icesi.controller;

import com.icesi.model.CityMap;
import com.icesi.model.Direction;
import com.icesi.model.Incident;
import com.icesi.model.IncidentStatus;
import com.icesi.model.IncidentType;
import com.icesi.model.IncidentGenerator;
import com.icesi.model.IncidentManager;
import com.icesi.model.Operator;
import com.icesi.model.Severity;
import com.icesi.model.Vehicle;
import com.icesi.model.VehicleType;
import com.icesi.persistence.JsonPersistence;
import com.icesi.persistence.SerializationPersistence;
import com.icesi.structures.BinarySearchTree;
import com.icesi.structures.LinkedList;

import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

// Controlador de la interfaz grafica (MVC): recibe las acciones del jugador y las traduce en operaciones del modelo.
// Los metodos que tocan los incidentes son synchronized porque el generador de incidentes corre en otro hilo:
// asi nunca se lee la lista mientras el otro hilo la esta cambiando.
public class GameController {

    private CityMap cityMap;
    private Operator operator;
    private LinkedList<Vehicle> vehicles;
    private IncidentManager manager;
    private IncidentGenerator generator;
    private MapPositionResolver resolver;
    private Random random;

    // reglas de puntaje extra
    public static final int FAST_BONUS_POINTS = 50;
    public static final int FAST_LIMIT_SECONDS = 30;
    public static final int WAIT_PENALTY_POINTS = 25;
    public static final int WAIT_PERIOD_SECONDS = 30;
    private boolean lastResolveFast;
    // cuantos bloques de 30 s ya se le cobraron a cada incidente
    private Map<String, Integer> chargedBlocks = new HashMap<>();

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
    public synchronized boolean moveOperator(Direction direction) {
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
    public synchronized boolean undoOperatorMove() {
        // si la pila esta vacia no hay nada que deshacer, pero el juego sigue
        if (operator.getMovementCount() == 0) {
            return false;
        }
        operator.undoLastMovement();
        return true;
    }

    // Genera un robo o un incendio aleatorio y lo registra como activo.
    public synchronized Incident spawnRandomIncident() {
        Incident incident;
        if (random.nextBoolean()) {
            incident = generator.generateRandomTheft();
        } else {
            incident = generator.generateRandomFire();
        }
        manager.registerIncident(incident);
        return incident;
    }

    // RF4 / HU-08: Detona un accidente por colision entre dos vehiculos.
    public synchronized Incident triggerCollisionAccident(String vehicle1Id, String vehicle2Id, String location) {
        Incident accident = generator.checkCollisionAccident(vehicle1Id, vehicle2Id, location);
        if (accident != null) {
            manager.registerIncident(accident);
        }
        return accident;
    }

    // RF4 / HU-09: Detona un accidente por congestion critica en una zona.
    public synchronized Incident triggerCongestionAccident(String zone, int vehicleCount) {
        Incident accident = generator.checkCongestionAccident(zone, vehicleCount);
        if (accident != null) {
            manager.registerIncident(accident);
        }
        return accident;
    }

    // RF4 / HU-10: Detona un accidente por vehiculo detenido durante varios ciclos.
    public synchronized Incident triggerStoppedVehicleAccident(String vehicleId, String location, int stoppedCycles) {
        Incident accident = generator.checkStoppedVehicleAccident(vehicleId, location, stoppedCycles);
        if (accident != null) {
            manager.registerIncident(accident);
        }
        return accident;
    }

    // Genera un accidente directamente en una celda del mapa (al hacer clic en el Canvas).
    public synchronized Incident spawnAccidentAt(int row, int column) {
        int counter = generator.getAccidentCounter() + 1;
        String id = String.format("ACC-%03d", counter);
        String location = "Calle Palmira (" + row + ", " + column + ")";
        Incident accident = new Incident(id, com.icesi.model.IncidentType.ACCIDENT, location, Severity.HIGH);
        accident.setDescription("Accidente reportado en coordenada (" + row + ", " + column + ")");
        manager.registerIncident(accident);
        generator.registerExistingId(id);
        return accident;
    }

    // Propone el vehiculo disponible mas adecuado para el incidente.
    public synchronized Vehicle proposeVehicle(Incident incident) {
        return manager.proposeCandidateVehicle(incident, vehicles);
    }

    // Asigna el vehiculo al incidente usando sus IDs.
    public synchronized void assignVehicle(String vehicleId, String incidentId) {
        Vehicle vehicle = findVehicleById(vehicleId);
        Incident incident = manager.findIncidentById(incidentId);
        manager.assignVehicle(vehicle, incident);
    }

    // RF6 / HU-16: Libera un vehiculo asignado a un incidente; el vehiculo vuelve a AVAILABLE y el incidente a PENDING.
    public synchronized void releaseVehicle(String vehicleId) {
        Vehicle vehicle = findVehicleById(vehicleId);
        manager.releaseVehicle(vehicle);
    }

    // RF6 / HU-16: Cancela el despacho de un incidente en proceso, liberando su vehiculo asignado.
    public synchronized void cancelIncidentDispatch(String incidentId) {
        Incident incident = manager.findIncidentById(incidentId);
        if (incident.getAssignedVehicle() != null) {
            manager.releaseVehicle(incident.getAssignedVehicle());
        }
    }

    // Registra un nuevo vehiculo en la flota del centro de monitoreo.
    public synchronized void registerVehicle(Vehicle vehicle) {
        if (vehicle == null) {
            throw new IllegalArgumentException("El vehiculo no puede ser nulo");
        }
        for (Vehicle v : vehicles) {
            if (v.getId().equalsIgnoreCase(vehicle.getId())) {
                throw new IllegalArgumentException("Ya existe un vehiculo con id " + vehicle.getId());
            }
        }
        vehicles.addLast(vehicle);
    }

    // RF16 / HU-22: Guarda el estado del sistema en archivo binario serializable (.dat).
    public synchronized void saveGameBinary(String filePath) {
        SerializationPersistence binary = new SerializationPersistence();
        binary.saveManagerState(manager, filePath);
    }

    // RF16 / HU-22: Carga el estado del sistema desde archivo binario serializable (.dat).
    public synchronized void loadGameBinary(String filePath) {
        SerializationPersistence binary = new SerializationPersistence();
        IncidentManager loaded = binary.loadManagerState(filePath);
        this.manager = loaded;
    }

    // Finaliza la atencion del incidente y retorna los puntos ganados.
    public synchronized int resolveIncident(String incidentId) {
        Incident incident = manager.findIncidentById(incidentId);
        return manager.finishAttention(incident, operator);
    }

    // Finaliza la atencion en el momento indicado; suma el bonus si fue rapido.
    public synchronized int resolveIncident(String incidentId, LocalDateTime now) {
        Incident incident = manager.findIncidentById(incidentId);
        int points = manager.finishAttention(incident, operator, now);
        long seconds = Duration.between(incident.getGeneratedAt(), now).getSeconds();
        lastResolveFast = seconds >= 0 && seconds < FAST_LIMIT_SECONDS;
        if (lastResolveFast) {
            operator.addScore(FAST_BONUS_POINTS);
            points += FAST_BONUS_POINTS;
        }
        return points;
    }

    // Indica si la ultima resolucion gano el bonus de rapidez.
    public synchronized boolean wasLastResolveFast() {
        return lastResolveFast;
    }

    // Cobra la penalizacion a los incidentes ALTOS que siguen esperando unidad; retorna los puntos quitados.
    public synchronized int applyWaitPenalties(LocalDateTime now) {
        int penalty = 0;
        for (Incident incident : manager.getActiveIncidentsByPriority()) {
            if (incident.getSeverity() != Severity.HIGH || incident.getStatus() != IncidentStatus.PENDING) {
                continue;
            }
            long blocks = Duration.between(incident.getGeneratedAt(), now).getSeconds() / WAIT_PERIOD_SECONDS;
            int charged = chargedBlocks.getOrDefault(incident.getId(), 0);
            if (blocks > charged) {
                penalty += (int) (blocks - charged) * WAIT_PENALTY_POINTS;
                chargedBlocks.put(incident.getId(), (int) blocks);
            }
        }
        // el puntaje nunca queda negativo
        penalty = Math.min(penalty, operator.getScore());
        operator.addScore(-penalty);
        return penalty;
    }

    // Guarda la partida (operador, vehiculos e incidentes activos) en archivos JSON dentro de la carpeta.
    public synchronized void saveGame(String folder) {
        JsonPersistence json = new JsonPersistence();
        json.saveOperator(operator, Path.of(folder, "operator.json").toString());
        json.saveVehicles(vehicles, Path.of(folder, "vehicles.json").toString());
        json.saveIncidents(manager.getActiveIncidentsByPriority(), Path.of(folder, "incidents.json").toString());
    }

    // Carga la partida guardada en la carpeta y reemplaza la actual.
    public synchronized void loadGame(String folder) {
        JsonPersistence json = new JsonPersistence();
        // primero se lee todo; si algo falla la partida actual queda igual
        LinkedList<Vehicle> loadedVehicles = json.loadVehicles(Path.of(folder, "vehicles.json").toString());
        LinkedList<Incident> loadedIncidents = json.loadIncidents(Path.of(folder, "incidents.json").toString(), loadedVehicles);
        Operator loadedOperator = json.loadOperator(Path.of(folder, "operator.json").toString());

        vehicles = loadedVehicles;
        operator = loadedOperator;
        manager = new IncidentManager();
        generator = new IncidentGenerator(random);
        chargedBlocks.clear();
        for (Incident incident : loadedIncidents) {
            manager.registerIncident(incident);
            generator.registerExistingId(incident.getId());
        }
    }

    // Registra un incidente manual o existente (sirve para las pruebas, registro en UI y cargar partidas).
    public synchronized void registerIncident(Incident incident) {
        manager.registerIncident(incident);
        if (incident != null && incident.getId() != null) {
            generator.registerExistingId(incident.getId());
        }
    }

    // Actualiza un incidente activo y recalcula su prioridad dentro del BST.
    public synchronized void updateIncident(String id, IncidentType type, Severity severity, String location, String description) {
        manager.updateIncident(id, type, severity, location, description);
    }

    // RF3 / HU-06: Busca un incidente por id mediante busqueda binaria; lanza IncidentNotFoundException si no existe.
    public synchronized Incident findIncidentById(String incidentId) {
        if (incidentId == null || incidentId.isBlank()) {
            throw new IllegalArgumentException("El id del incidente no puede ser nulo ni vacio");
        }
        return manager.findIncidentById(incidentId.trim());
    }

    // Devuelve el incidente activo de mayor prioridad o null si no hay ninguno.
    public synchronized Incident getHighestPriorityIncidentOrNull() {
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
    public synchronized LinkedList<Incident> getActiveIncidentsByPriority() {
        return manager.getActiveIncidentsByPriority();
    }

    // Devuelve el arbol binario de busqueda (BST) de incidentes activos por prioridad.
    public synchronized BinarySearchTree<Incident> getActiveIncidentsTree() {
        return manager.getActiveIncidentsTree();
    }

    // Indica si una celda corresponde a un corredor arterial de emergencia (MAIN_ROAD).
    public boolean isCorridorCell(int row, int column) {
        if (!cityMap.isInsideBounds(row, column)) {
            return false;
        }
        return cityMap.getCell(row, column) == CityMap.MAIN_ROAD;
    }

    // Cuenta el total de celdas que conforman los corredores de emergencia en la red vial.
    public int countCorridorCells() {
        int count = 0;
        int size = cityMap.getSize();
        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                if (cityMap.getCell(r, c) == CityMap.MAIN_ROAD) {
                    count++;
                }
            }
        }
        return count;
    }

    public synchronized int getActiveIncidentCount() {
        return manager.getActiveIncidentCount();
    }

    public synchronized int getResolvedIncidentCount() {
        return manager.getResolvedIncidentCount();
    }

    public synchronized int getTotalIncidentCount() {
        return manager.getTotalIncidentCount();
    }

    public synchronized int getActiveAccidentCount() {
        return manager.getActiveAccidentCount();
    }

    public synchronized int getActiveTheftCount() {
        return manager.getActiveTheftCount();
    }

    public synchronized int getActiveFireCount() {
        return manager.getActiveFireCount();
    }

    public synchronized int getAvailableVehicleCount() {
        return manager.countAvailableVehicles(vehicles);
    }

    public synchronized int getScore() {
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
