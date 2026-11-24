# Diagrama de Clases UML y Patrones de Diseño — SGMMS (Palmira)
## Entrega 3 (Final) — Algoritmos y Programación II (Icesi)
**Equipo:** LaVencida  
**Integrantes:** Juan Pablo Ceballos, Daniel Felipe Herrera Parra, Dayanna Fernández Núñez

---

## 1. Arquitectura General y Patrones de Diseño

El sistema está estructurado bajo el patrón arquitectónico **Modelo-Vista-Controlador (MVC)**, complementado con patrones de diseño orientados a eventos, concurrencia y estructuras de datos:

| Patrón de Diseño | Componentes Clave en SGMMS | Propósito en el Proyecto |
| :--- | :--- | :--- |
| **Model-View-Controller (MVC)** | `model.*` (Modelo), `ui.*` (Vista JavaFX), `GameController` (Controlador) | Desacopla la lógica de negocio, estructuras de datos y persistencia de la interfaz visual retro. |
| **Observer / Callback** | `GameApplication`, `Consumer<String>`, `AnimationTimer`, `Runnable` | Notificación asíncrona de eventos de interfaz (navegación entre pantallas, clics en mapa, actualización de turnos). |
| **Strategy (Comparadores y Búsqueda)** | `IncidentPriorityComparator`, `IncidentIdComparator`, `RouteFinder` (BFS) | Algoritmos intercambiables de ordenamiento, priorización BST y enrutamiento en grafos sobre la red vial. |
| **Active Object / Producer-Consumer** | `IncidentSpawner` (Hilo productor), `ShiftTimer` (Hilo temporizador) | Generación concurrente de incidentes y control del tiempo de simulación con sincronización thread-safe en el controlador. |
| **Composite / Hierarchical Screen** | `GameScreen`, `MonitoringCenterScreen`, `IncidentPanelScreen`, `SplitViewScreen`, `BinaryTreeScreen` | Jerarquía unificada de pantallas intercambiables dentro del `StackPane` principal. |
| **Factory / Registry** | `SpriteRegistry`, `RetroSound` | Centralización de recursos gráficos pixel-art y síntesis procedimental de audio 8 bits. |

---

## 2. Diagrama de Clases UML (PlantUML / Mermaid)

```mermaid
classDiagram
    direction TB

    %% ==========================================
    %% CAPA CONTROLADOR Y CONCURRENCIA
    %% ==========================================
    class GameController {
        -cityMap: CityMap
        -operator: Operator
        -vehicles: LinkedList~Vehicle~
        -manager: IncidentManager
        -random: Random
        +spawnRandomIncident() Incident
        +triggerCollisionAccident(v1, v2, loc) Incident
        +triggerCongestionAccident(zone, count) Incident
        +triggerStoppedVehicleAccident(v, loc, c) Incident
        +spawnAccidentAt(r, c) Incident
        +proposeVehicle(incident: Incident) Vehicle
        +assignVehicle(vehicleId: String, incidentId: String) void
        +releaseVehicle(vehicleId: String) void
        +cancelIncidentDispatch(incidentId: String) void
        +resolveIncident(incidentId: String) int
        +registerIncident(incident: Incident) void
        +registerVehicle(vehicle: Vehicle) void
        +findIncidentById(id: String) Incident
        +findVehicleById(id: String) Vehicle
        +getActiveIncidentsTree() BinarySearchTree~Incident~
        +getActiveIncidentsByPriority() LinkedList~Incident~
        +isCorridorCell(row: int, col: int) boolean
        +countCorridorCells() int
        +saveGame(folder: String) void
        +loadGame(folder: String) void
        +saveGameBinary(file: String) void
        +loadGameBinary(file: String) void
    }

    class IncidentSpawner {
        -controller: GameController
        -consumer: Consumer~Incident~
        -running: boolean
        +run() void
        +stop() void
    }

    class ShiftTimer {
        -consumer: Consumer~String~
        -onShiftEnd: Runnable
        -remainingSeconds: int
        +run() void
        +stop() void
    }

    IncidentSpawner --> GameController : interacts
    ShiftTimer --> GameController : interacts

    %% ==========================================
    %% CAPA MODELO Y ESTRUCTURAS
    %% ==========================================
    class CityMap {
        +MAP_SIZE: int = 64
        +PATH: int = 0
        +OBSTACLE: int = 1
        +RESIDENTIAL: int = 2
        +COMMERCIAL: int = 3
        +MAIN_ROAD: int = 4
        -grid: int[][]
        +getCell(r, c) int
        +isWalkable(r, c) boolean
    }

    class Incident {
        -id: String
        -type: IncidentType
        -location: String
        -severity: Severity
        -status: IncidentStatus
        -generatedAt: LocalDateTime
        -resolvedAt: LocalDateTime
        -assignedVehicle: Vehicle
        +getId() String
        +getSeverity() Severity
        +getType() IncidentType
    }

    class Vehicle {
        -id: String
        -type: VehicleType
        -baseLocation: String
        -status: VehicleStatus
        -assignedIncident: Incident
        +getId() String
        +getType() VehicleType
        +getStatus() VehicleStatus
    }

    class Operator {
        -row: int
        -column: int
        -score: int
        -movements: Stack~Direction~
        +move(dir: Direction) void
        +undo() boolean
    }

    class IncidentManager {
        -activeIncidentsTree: BinarySearchTree~Incident~
        -allIncidents: LinkedList~Incident~
        +registerIncident(inc: Incident) void
        +assignVehicle(v: Vehicle, inc: Incident) void
        +releaseVehicle(v: Vehicle) void
        +resolveIncident(id: String) Incident
        +getActiveIncidentsTree() BinarySearchTree~Incident~
    }

    class BinarySearchTree~T~ {
        -root: BSTNode~T~
        -comparator: Comparator~T~
        -size: int
        +insert(value: T) void
        +search(value: T) T
        +inOrder() LinkedList~T~
        +preOrder() LinkedList~T~
        +postOrder() LinkedList~T~
        +height() int
        +size() int
    }

    class BSTNode~T~ {
        -value: T
        -left: BSTNode~T~
        -right: BSTNode~T~
        +getValue() T
        +getLeft() BSTNode~T~
        +getRight() BSTNode~T~
    }

    class LinkedList~T~ {
        -head: Node~T~
        -size: int
        +addLast(value: T) void
        +sort(cmp: Comparator~T~) void
        +binarySearch(target: T, cmp: Comparator~T~) int
    }

    class Stack~T~ {
        -top: Node~T~
        -size: int
        +push(value: T) void
        +pop() T
    }

    BinarySearchTree *-- BSTNode
    IncidentManager o-- BinarySearchTree
    IncidentManager o-- LinkedList
    Operator o-- Stack
    GameController o-- CityMap
    GameController o-- Operator
    GameController o-- IncidentManager
    GameController o-- LinkedList

    %% ==========================================
    %% CAPA VISTA (JAVAFX RETRO 16-BIT)
    %% ==========================================
    class GameScreen {
        <<interface>>
        +getView()* Node
        +refresh()* void
        +handleKey(code: KeyCode) boolean
    }

    class MonitoringCenterScreen {
        -controller: GameController
        -navigator: Consumer~String~
        +refresh() void
    }

    class IncidentPanelScreen {
        -controller: GameController
        -searchInput: TextField
        +executeBinarySearch() void
        +refresh() void
    }

    class SplitViewScreen {
        -controller: GameController
        -mapView: CityMapView
        -incidentPanel: IncidentPanelScreen
        +refresh() void
    }

    class BinaryTreeScreen {
        -controller: GameController
        -treeCanvas: Canvas
        -currentMode: TraversalMode
        +drawTree() void
        +refresh() void
    }

    class CityMapView {
        -controller: GameController
        -emergencyCorridorsVisible: boolean
        +toggleEmergencyCorridors() void
        +drawEmergencyCorridors(gc, time) void
    }

    class MobilityDialog {
        -controller: GameController
        -mapView: CityMapView
        +updateStatus() void
        +open() void
    }

    class VehicleRegistrationDialog {
        -idField: TextField
        +open() void
    }

    class TrafficSimulationDialog {
        -controller: GameController
        +open() void
    }

    GameScreen <|.. MonitoringCenterScreen
    GameScreen <|.. IncidentPanelScreen
    GameScreen <|.. SplitViewScreen
    GameScreen <|.. BinaryTreeScreen

    SplitViewScreen o-- CityMapView
    SplitViewScreen o-- IncidentPanelScreen

    %% ==========================================
    %% ORQUESTADOR PRINCIPAL (JAVAFX APPLICATION)
    %% ==========================================
    class GameApplication {
        -controller: GameController
        -screens: Map~String, GameScreen~
        -mapView: CityMapView
        -spawner: IncidentSpawner
        -shiftTimer: ShiftTimer
        -mobilityDialog: MobilityDialog
        -binaryTreeScreen: BinaryTreeScreen
        +start(stage: Stage) void
        +showScreen(name: String) void
        +toggleEmergencyCorridors() void
        +openMobilityDialog() void
    }

    GameApplication o-- GameController
    GameApplication o-- GameScreen
    GameApplication o-- MobilityDialog
```

---

## 3. Resumen de Estructuras y Complejidades

1. **Árbol Binario de Búsqueda (`BinarySearchTree<Incident>`)**:
   - Inserción y búsqueda por prioridad: $O(\log n)$ promedio, $O(n)$ peor caso.
   - Recorridos In-Order, Pre-Order, Post-Order: $O(n)$ garantizado.
   - Se utiliza para gestionar y renderizar visualmente la jerarquía de incidentes activos por urgencia en `BinaryTreeScreen`.
2. **Lista Enlazada Doble/Simple (`LinkedList<T>`)**:
   - Inserción al inicio/final: $O(1)$.
   - Búsqueda binaria (`binarySearch`): $O(\log n)$ sobre lista indexada/ordenada por MergeSort ($O(n \log n)$).
3. **Pila LIFO (`Stack<Direction>`)**:
   - Push y Pop de movimientos del operador: $O(1)$. Permite la función de deshacer (`undo`).
4. **Enrutamiento sobre Grafo de Red Vial (`RouteFinder` / BFS)**:
   - Complejidad: $O(V + E)$ sobre las $64 \times 64 = 4096$ celdas de la ciudad de Palmira, priorizando los corredores arteriales `MAIN_ROAD`.
