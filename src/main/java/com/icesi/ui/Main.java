package com.icesi.ui;

import com.icesi.model.Direction;
import com.icesi.model.Incident;
import com.icesi.model.IncidentManager;
import com.icesi.model.IncidentType;
import com.icesi.model.Movement;
import com.icesi.model.Operator;
import com.icesi.model.Severity;
import com.icesi.model.Vehicle;
import com.icesi.model.VehicleType;
import com.icesi.structures.LinkedList;

import java.util.Scanner;

public class Main {

    private static final Scanner scanner = new Scanner(System.in);
    private static final IncidentManager manager = new IncidentManager();
    private static final LinkedList<Vehicle> vehicles = new LinkedList<>();
    private static final Operator operator = new Operator(0, 0);
    private static final int MAP_SIZE = 64;
    private static final int WALL = 0;
    private static final int VEHICLE = 1;
    private static final int ROAD = 2;
    private static final int BUILDING = 3;
    private static final int[][] map = new int[MAP_SIZE][MAP_SIZE];

    // Punto de entrada: carga los vehiculos y repite el menu hasta que el usuario elija 0.
    public static void main(String[] args) {
        loadVehicles();
        buildMap();
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
        vehicles.addLast(new Vehicle("P-01", VehicleType.PATROL));
        vehicles.addLast(new Vehicle("A-01", VehicleType.AMBULANCE));
        vehicles.addLast(new Vehicle("F-01", VehicleType.FIRE_TRUCK));
    }

    // Imprime en consola las opciones del menu principal.
    private static void showMenu() {
        System.out.println();
        System.out.println("===== SGMMS - Centro de Monitoreo =====");
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

    // Muestra todos los incidentes ordenados de mayor a menor prioridad.
    private static void showIncidentsByPriority() {
        LinkedList<Incident> sorted = manager.getIncidentsSortedByPriority();
        if (sorted.isEmpty()) {
            System.out.println("No hay incidentes registrados.");
            return;
        }
        for (int i = 0; i < sorted.size(); i++) {
            System.out.println((i + 1) + ". " + describeIncident(sorted.get(i)));
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

    // Pide el ID de un incidente y el de un vehiculo, y asigna el vehiculo al incidente.
    private static void assignVehicle() {
        Incident incident = manager.findIncidentById(readText("ID del incidente: "));
        Vehicle vehicle = findVehicle(readText("ID del vehiculo: "));
        manager.assignVehicle(vehicle, incident);
        System.out.println("Vehiculo " + vehicle.getId() + " asignado al incidente " + incident.getId() + ".");
    }

    // Pide el ID de un incidente en progreso y finaliza su atencion, dejando el vehiculo disponible.
    private static void finishAttention() {
        Incident incident = manager.findIncidentById(readText("ID del incidente: "));
        manager.finishAttention(incident);
        System.out.println("Atencion del incidente " + incident.getId() + " finalizada.");
    }

    // Pide el ID de un vehiculo y lo libera, devolviendo su incidente al estado PENDING.
    private static void releaseVehicle() {
        Vehicle vehicle = findVehicle(readText("ID del vehiculo: "));
        manager.releaseVehicle(vehicle);
        System.out.println("Vehiculo " + vehicle.getId() + " liberado. El incidente vuelve a PENDING.");
    }

    // Muestra el ID, tipo y estado de cada vehiculo.
    private static void showVehicles() {
        for (int i = 0; i < vehicles.size(); i++) {
            Vehicle v = vehicles.get(i);
            System.out.println(v.getId() + " | " + v.getType() + " | " + v.getStatus());
        }
    }

    // Pide una direccion y mueve al operador en esa direccion, mostrando su nueva posicion.
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
        operator.move(direction);
        System.out.println("Operador en (" + operator.getRow() + ", " + operator.getColumn() + ").");
    }

    // Deshace el ultimo movimiento del operador y muestra su posicion actual.
    private static void undoMovement() {
        Movement movement = operator.undoLastMovement();
        System.out.println("Se deshizo el movimiento " + movement.getDirection()
                + ". Operador en (" + operator.getRow() + ", " + operator.getColumn() + ").");
    }

    // Construye el mapa: paredes en el borde, una calle cada 8 celdas, edificios en el resto y vehiculos sobre las calles.
    private static void buildMap() {
        for (int i = 0; i < MAP_SIZE; i++) {
            for (int j = 0; j < MAP_SIZE; j++) {
                if (i == 0 || j == 0 || i == MAP_SIZE - 1 || j == MAP_SIZE - 1) {
                    map[i][j] = WALL;
                } else if (i % 8 == 4 || j % 8 == 4) {
                    map[i][j] = ROAD;
                } else {
                    map[i][j] = BUILDING;
                }
            }
        }
        map[4][4] = VEHICLE;
        map[4][36] = VEHICLE;
        map[36][20] = VEHICLE;
    }

    // Imprime la matriz del mapa con numeros: 0 pared, 1 vehiculo, 2 carretera y 3 edificio.
    private static void printMap() {
        System.out.println("Mapa (0 = pared, 1 = vehiculo, 2 = carretera, 3 = edificio)");
        for (int i = 0; i < MAP_SIZE; i++) {
            for (int j = 0; j < MAP_SIZE; j++) {
                System.out.print(map[i][j] + " ");
            }
            System.out.println();
        }
    }

    // Busca un vehiculo por ID en la lista; lanza IllegalArgumentException si no existe.
    private static Vehicle findVehicle(String id) {
        for (int i = 0; i < vehicles.size(); i++) {
            if (vehicles.get(i).getId().equals(id)) {
                return vehicles.get(i);
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