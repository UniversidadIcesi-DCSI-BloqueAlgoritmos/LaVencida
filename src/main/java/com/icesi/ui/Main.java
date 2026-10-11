package com.icesi.ui;

import com.icesi.model.CityMap;
import com.icesi.model.Direction;
import com.icesi.model.Incident;
import com.icesi.model.IncidentGenerator;
import com.icesi.model.IncidentManager;
import com.icesi.model.IncidentType;
import com.icesi.model.Movement;
import com.icesi.model.Operator;
import com.icesi.model.Severity;
import com.icesi.model.Vehicle;
import com.icesi.model.VehicleType;
import com.icesi.persistence.JsonPersistence;
import com.icesi.persistence.SerializationPersistence;
import com.icesi.structures.LinkedList;

import java.util.Scanner;

public class Main {

    private static final Scanner scanner = new Scanner(System.in);
    private static IncidentManager manager = new IncidentManager();
    private static LinkedList<Vehicle> vehicles = new LinkedList<>();
    private static final Operator operator = new Operator(4, 4);
    private static final CityMap cityMap = new CityMap();
    private static final IncidentGenerator generator = new IncidentGenerator();
    private static final JsonPersistence jsonPersistence = new JsonPersistence();
    private static final SerializationPersistence serializationPersistence = new SerializationPersistence();

    private static final String DEFAULT_VEHICLES_JSON = "data/vehicles.json";
    private static final String DEFAULT_INCIDENTS_JSON = "data/incidents.json";
    private static final String DEFAULT_STATE_DAT = "data/simulation_state.dat";

    // Punto de entrada: carga los vehiculos y repite el menu hasta que el usuario elija 0.
    public static void main(String[] args) {
        loadVehicles();
        int option = -1;
        while (option != 0) {
            showMenu();
            option = readInt("Seleccione una opcion: ");
            executeOption(option);
        }
        System.out.println("Hasta luego.");
    }

    // Precarga tres vehiculos iniciales: una patrulla, una ambulancia y un camion de bomberos.
    private static void loadVehicles() {
        vehicles.addLast(new Vehicle("P-01", VehicleType.PATROL, "Estacion Norte"));
        vehicles.addLast(new Vehicle("A-01", VehicleType.AMBULANCE, "Hospital Central"));
        vehicles.addLast(new Vehicle("F-01", VehicleType.FIRE_TRUCK, "Estacion Sur"));
    }

    // Imprime en consola los indicadores en tiempo real (RF-13, RF-14) y las opciones del menu principal.
    private static void showMenu() {
        System.out.println();
        System.out.println("===== SGMMS - Centro de Monitoreo =====");
        System.out.println("Operador: (" + operator.getRow() + ", " + operator.getColumn()
                + ") | Puntaje: " + operator.getScore() + " pts");
        System.out.println("Incidentes activos: " + manager.getActiveIncidentCount()
                + " (Accidentes: " + manager.getActiveAccidentCount()
                + ", Robos: " + manager.getActiveTheftCount()
                + ", Incendios: " + manager.getActiveFireCount() + ")"
                + " | Vehiculos disponibles: " + manager.countAvailableVehicles(vehicles));
        System.out.println("---------------------------------------");
        System.out.println("1. Registrar incidente");
        System.out.println("2. Ver incidentes por prioridad");
        System.out.println("3. Ver incidente de mayor prioridad");
        System.out.println("4. Buscar incidente por ID");
        System.out.println("5. Asignar vehiculo a incidente");
        System.out.println("6. Finalizar atencion de incidente");
        System.out.println("7. Liberar vehiculo");
        System.out.println("8. Ver vehiculos");
        System.out.println("9. Mover operador");
        System.out.println("10. Deshacer ultimo movimiento del operador");
        System.out.println("11. Ver mapa");
        System.out.println("12. Generar incidente automatico (RF-4)");
        System.out.println("13. Guardar / Cargar datos en JSON (RF-15)");
        System.out.println("14. Serializar / Deserializar estado del sistema (RF-16)");
        System.out.println("0. Salir");
    }

    // Llama al metodo que corresponde a la opcion elegida y atrapa las excepciones del modelo para mostrarlas como mensajes.
    private static void executeOption(int option) {
        try {
            if (option == 1) {
                registerIncident();
            } else if (option == 2) {
                showIncidentsByPriority();
            } else if (option == 3) {
                showHighestPriorityIncident();
            } else if (option == 4) {
                searchIncident();
            } else if (option == 5) {
                assignVehicle();
            } else if (option == 6) {
                finishAttention();
            } else if (option == 7) {
                releaseVehicle();
            } else if (option == 8) {
                showVehicles();
            } else if (option == 9) {
                moveOperator();
            } else if (option == 10) {
                undoMovement();
            } else if (option == 11) {
                printMap();
            } else if (option == 12) {
                generateAutomaticIncident();
            } else if (option == 13) {
                saveOrLoadJson();
            } else if (option == 14) {
                saveOrLoadSerialization();
            } else if (option != 0) {
                System.out.println("Opcion invalida.");
            }
        } catch (RuntimeException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    // Pide los datos del incidente (ID, tipo, ubicacion y gravedad) y lo registra en el manager.
    private static void registerIncident() {
        String id = readText("ID del incidente: ");
        IncidentType type = readIncidentType();
        String location = readText("Ubicacion: ");
        Severity severity = readSeverity();
        manager.registerIncident(new Incident(id, type, location, severity));
        System.out.println("Incidente " + id + " registrado.");
    }

    // Permite generar incidentes aleatorios (robo/incendio) o por los 3 criterios de accidentes (RF-4).
    private static void generateAutomaticIncident() {
        System.out.println("1. Robo aleatorio  2. Incendio aleatorio  3. Evaluar colision  4. Evaluar congestion  5. Evaluar vehiculo detenido");
        int option = readInt("Seleccione criterio de generacion: ");
        Incident generated = null;
        if (option == 1) {
            generated = generator.generateRandomTheft();
        } else if (option == 2) {
            generated = generator.generateRandomFire();
        } else if (option == 3) {
            String v1 = readText("ID del primer vehiculo: ");
            String v2 = readText("ID del segundo vehiculo: ");
            String location = readText("Ubicacion de la interseccion: ");
            generated = generator.checkCollisionAccident(v1, v2, location);
        } else if (option == 4) {
            String zone = readText("Nombre de la zona: ");
            int count = readInt("Cantidad de vehiculos en la zona: ");
            generated = generator.checkCongestionAccident(zone, count);
        } else if (option == 5) {
            String vehicleId = readText("ID del vehiculo detenido: ");
            String location = readText("Via donde esta detenido: ");
            int cycles = readInt("Ciclos detenido: ");
            generated = generator.checkStoppedVehicleAccident(vehicleId, location, cycles);
        } else {
            System.out.println("Opcion invalida.");
            return;
        }

        if (generated == null) {
            System.out.println("No se cumplieron las condiciones criticas para generar un incidente.");
            return;
        }
        manager.registerIncident(generated);
        System.out.println("Incidente generado y registrado: " + describeIncident(generated));
    }

    // Muestra todos los incidentes ordenados de mayor a menor prioridad.
    private static void showIncidentsByPriority() {
        LinkedList<Incident> sorted = manager.getIncidentsSortedByPriority();
        if (sorted.isEmpty()) {
            System.out.println("No hay incidentes registrados.");
            return;
        }
        int position = 1;
        for (Incident incident : sorted) {
            System.out.println(position + ". " + describeIncident(incident));
            position++;
        }
    }

    // Muestra el incidente de mayor prioridad entre los activos.
    private static void showHighestPriorityIncident() {
        Incident incident = manager.getHighestPriorityIncident();
        System.out.println("Mayor prioridad: " + describeIncident(incident));
    }

    // Pide un ID y muestra el incidente correspondiente.
    private static void searchIncident() {
        String id = readText("ID a buscar: ");
        Incident incident = manager.findIncidentById(id);
        System.out.println(describeIncident(incident));
    }

    // Pide el ID de un incidente, sugiere el vehiculo candidato (RF-7) y asigna el vehiculo al incidente.
    private static void assignVehicle() {
        Incident incident = manager.findIncidentById(readText("ID del incidente: "));
        try {
            Vehicle candidate = manager.proposeCandidateVehicle(incident, vehicles);
            System.out.println("Vehiculo candidato recomendado (RF-7): " + candidate.getId()
                    + " (" + candidate.getType() + " - " + candidate.getLocation() + ")");
        } catch (RuntimeException e) {
            System.out.println("Aviso de candidato: " + e.getMessage());
        }
        Vehicle vehicle = askVehicle();
        manager.assignVehicle(vehicle, incident);
        System.out.println("Vehiculo " + vehicle.getId() + " asignado al incidente " + incident.getId() + ".");
    }

    // Pide el ID de un incidente en progreso, finaliza su atencion y suma puntos al operador (RF-14).
    private static void finishAttention() {
        Incident incident = manager.findIncidentById(readText("ID del incidente: "));
        int pointsEarned = manager.finishAttention(incident, operator);
        System.out.println("Atencion del incidente " + incident.getId() + " finalizada. Puntos obtenidos: +"
                + pointsEarned + " | Puntaje total del operador: " + operator.getScore() + " pts.");
    }

    // Pide el ID de un vehiculo y lo libera, devolviendo su incidente al estado PENDING.
    private static void releaseVehicle() {
        Vehicle vehicle = askVehicle();
        manager.releaseVehicle(vehicle);
        System.out.println("Vehiculo " + vehicle.getId() + " liberado. El incidente vuelve a PENDING.");
    }

    // Muestra el ID, tipo, estado, ubicacion e incidente asignado de cada vehiculo (RF-6).
    private static void showVehicles() {
        for (Vehicle vehicle : vehicles) {
            String info = vehicle.getId() + " | " + vehicle.getType() + " | " + vehicle.getStatus()
                    + " | Ubicacion: " + vehicle.getLocation();
            if (vehicle.getAssignedIncident() != null) {
                info = info + " | Incidente: " + vehicle.getAssignedIncident().getId();
            }
            System.out.println(info);
        }
    }

    // Pide una direccion y mueve al operador solo si la celda destino esta dentro del mapa y es transitable.
    private static void moveOperator() {
        System.out.println("1. Arriba  2. Abajo  3. Izquierda  4. Derecha");
        int option = readInt("Direccion: ");
        Direction direction;
        if (option == 1) {
            direction = Direction.UP;
        } else if (option == 2) {
            direction = Direction.DOWN;
        } else if (option == 3) {
            direction = Direction.LEFT;
        } else if (option == 4) {
            direction = Direction.RIGHT;
        } else {
            System.out.println("Direccion invalida.");
            return;
        }

        // calculamos la celda a la que iria el operador antes de moverlo
        int newRow = operator.getRow();
        int newColumn = operator.getColumn();
        if (direction == Direction.UP) {
            newRow--;
        } else if (direction == Direction.DOWN) {
            newRow++;
        } else if (direction == Direction.LEFT) {
            newColumn--;
        } else {
            newColumn++;
        }

        if (!cityMap.isWalkable(newRow, newColumn)) {
            System.out.println("No se puede mover ahi: hay un obstaculo, una zona no transitable o el limite del mapa.");
            return;
        }
        operator.move(direction);
        System.out.println("Operador en (" + operator.getRow() + ", " + operator.getColumn() + ").");
    }

    // Deshace el ultimo movimiento del operador y muestra su posicion actual.
    private static void undoMovement() {
        Movement movement = operator.undoLastMovement();
        System.out.println("Se deshizo el movimiento " + movement.getDirection()
                + ". Operador en (" + operator.getRow() + ", " + operator.getColumn() + ").");
    }

    // Imprime la matriz del mapa con los codigos de la integradora y marca con O la posicion del operador.
    private static void printMap() {
        System.out.println("Mapa (0 = camino disponible, 1 = obstaculo, 2 = zona residencial, 3 = zona comercial, 4 = via principal, O = operador)");
        for (int i = 0; i < cityMap.getSize(); i++) {
            for (int j = 0; j < cityMap.getSize(); j++) {
                if (i == operator.getRow() && j == operator.getColumn()) {
                    System.out.print("O ");
                } else {
                    System.out.print(cityMap.getCell(i, j) + " ");
                }
            }
            System.out.println();
        }
    }

    // Permite guardar o cargar vehiculos e incidentes desde archivos JSON (RF-15).
    private static void saveOrLoadJson() {
        System.out.println("1. Guardar vehiculos en JSON  2. Cargar vehiculos desde JSON  3. Guardar incidentes en JSON  4. Cargar incidentes desde JSON");
        int option = readInt("Opcion: ");
        if (option == 1) {
            jsonPersistence.saveVehicles(vehicles, DEFAULT_VEHICLES_JSON);
            System.out.println("Vehiculos guardados en " + DEFAULT_VEHICLES_JSON);
        } else if (option == 2) {
            vehicles = jsonPersistence.loadVehicles(DEFAULT_VEHICLES_JSON);
            System.out.println("Se cargaron " + vehicles.size() + " vehiculos desde " + DEFAULT_VEHICLES_JSON);
        } else if (option == 3) {
            LinkedList<Incident> currentIncidents = manager.getIncidentsSortedByPriority();
            jsonPersistence.saveIncidents(currentIncidents, DEFAULT_INCIDENTS_JSON);
            System.out.println("Incidentes guardados en " + DEFAULT_INCIDENTS_JSON);
        } else if (option == 4) {
            LinkedList<Incident> loaded = jsonPersistence.loadIncidents(DEFAULT_INCIDENTS_JSON);
            for (Incident incident : loaded) {
                manager.registerIncident(incident);
            }
            System.out.println("Se cargaron y registraron " + loaded.size() + " incidentes desde " + DEFAULT_INCIDENTS_JSON);
        } else {
            System.out.println("Opcion invalida.");
        }
    }

    // Permite serializar o deserializar el estado completo del IncidentManager (RF-16).
    private static void saveOrLoadSerialization() {
        System.out.println("1. Serializar estado actual del sistema  2. Deserializar y restaurar estado del sistema");
        int option = readInt("Opcion: ");
        if (option == 1) {
            serializationPersistence.saveManagerState(manager, DEFAULT_STATE_DAT);
            System.out.println("Estado del sistema serializado en " + DEFAULT_STATE_DAT);
        } else if (option == 2) {
            manager = serializationPersistence.loadManagerState(DEFAULT_STATE_DAT);
            System.out.println("Estado restaurado desde " + DEFAULT_STATE_DAT
                    + ". Incidentes registrados: " + manager.getIncidentCount());
        } else {
            System.out.println("Opcion invalida.");
        }
    }

    // Muestra los vehiculos del sistema para que el usuario vea sus IDs y pide el ID de uno.
    private static Vehicle askVehicle() {
        System.out.println("Vehiculos disponibles en el sistema:");
        showVehicles();
        return findVehicle(readText("ID del vehiculo (por ejemplo P-01): "));
    }

    // Busca un vehiculo por ID en la lista; lanza IllegalArgumentException si no existe.
    private static Vehicle findVehicle(String id) {
        for (Vehicle vehicle : vehicles) {
            if (vehicle.getId().equals(id)) {
                return vehicle;
            }
        }
        throw new IllegalArgumentException("No existe un vehiculo con ID " + id);
    }

    // Devuelve una linea de texto con los datos del incidente (y su vehiculo asignado, si tiene).
    private static String describeIncident(Incident incident) {
        String text = incident.getId() + " | " + incident.getType() + " | " + incident.getLocation()
                + " | " + incident.getSeverity() + " | " + incident.getStatus();
        if (incident.getAssignedVehicle() != null) {
            text = text + " | Vehiculo: " + incident.getAssignedVehicle().getId();
        }
        return text;
    }

    // Muestra los tipos de incidente y repite la pregunta hasta que el usuario elija uno valido.
    private static IncidentType readIncidentType() {
        while (true) {
            System.out.println("Tipo: 1. Accidente  2. Robo  3. Incendio");
            int option = readInt("Opcion: ");
            if (option == 1) {
                return IncidentType.ACCIDENT;
            } else if (option == 2) {
                return IncidentType.THEFT;
            } else if (option == 3) {
                return IncidentType.FIRE;
            }
            System.out.println("Opcion invalida.");
        }
    }

    // Muestra los niveles de gravedad y repite la pregunta hasta que el usuario elija uno valido.
    private static Severity readSeverity() {
        while (true) {
            System.out.println("Gravedad: 1. Alta  2. Media  3. Baja");
            int option = readInt("Opcion: ");
            if (option == 1) {
                return Severity.HIGH;
            } else if (option == 2) {
                return Severity.MEDIUM;
            } else if (option == 3) {
                return Severity.LOW;
            }
            System.out.println("Opcion invalida.");
        }
    }

    // Muestra un mensaje y lee una linea de texto del usuario, sin espacios en los extremos.
    private static String readText(String message) {
        System.out.print(message);
        return scanner.nextLine().trim();
    }

    // Muestra un mensaje y lee un entero; repite la pregunta mientras el usuario no escriba un numero valido.
    private static int readInt(String message) {
        while (true) {
            String text = readText(message);
            try {
                return Integer.parseInt(text);
            } catch (NumberFormatException e) {
                System.out.println("Ingrese un numero valido.");
            }
        }
    }
}