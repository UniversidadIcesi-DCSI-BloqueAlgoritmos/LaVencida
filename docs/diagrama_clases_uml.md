# Diagrama de Clases UML y Patrones de Diseño — SGMMS (Palmira)
## Entrega 3 (Final) — Algoritmos y Programación II (Icesi)
**Equipo:** LaVencida  
**Integrantes:** Juan Pablo Ceballos, Daniel Felipe Herrera Parra, Dayanna Fernández Núñez

> **Archivo fuente PlantUML (estilo estándar del curso):** [diagrama_clases_sgmms.puml](diagrama_clases_sgmms.puml)

---

## 1. Arquitectura MVC + JavaFX + Canvas y Patrones de Diseño

El sistema sigue estrictamente el patrón arquitectónico **Modelo-Vista-Controlador (MVC)** del curso:
- **El Modelo (`model`, `structures`, `persistence`, `exceptions`) NO importa nada de JavaFX.** Se prueba íntegramente con JUnit (`160` pruebas unitarias sin abrir ventanas).
- **La Vista (`ui`)** renderiza el estado en `Canvas` y componentes JavaFX, y delega las acciones al `GameController`.
- **El Controlador (`controller`)** traduce eventos del usuario en operaciones `synchronized` sobre el modelo, ejecuta cálculos de rutas BFS (`RouteFinder`) y gestiona la concurrencia (`IncidentSpawner`, `ShiftTimer`).

| Patrón de Diseño | Componentes Clave en SGMMS | Propósito en el Proyecto |
| :--- | :--- | :--- |
| **Model-View-Controller (MVC)** | `model.*` (Modelo), `ui.*` (Vista JavaFX), `GameController` (Controlador) | Desacopla la lógica de negocio, estructuras de datos y persistencia de la interfaz visual retro. |
| **Observer / Callback** | `GameApplication`, `Consumer<String>`, `AnimationTimer`, `Runnable` | Notificación asíncrona de eventos de interfaz (navegación entre pantallas, clics en mapa, actualización de turnos). |
| **Strategy (Comparadores y Búsqueda)** | `IncidentPriorityComparator`, `IncidentIdComparator`, `RouteFinder` (BFS) | Algoritmos intercambiables de ordenamiento, priorización BST y enrutamiento en grafos sobre la red vial. |
| **Active Object / Producer-Consumer** | `IncidentSpawner` (Hilo productor), `ShiftTimer` (Hilo temporizador) | Generación concurrente de incidentes y control del tiempo de simulación con sincronización thread-safe en `GameController`. |
| **Composite / Hierarchical Screen** | `GameScreen`, `MonitoringCenterScreen`, `IncidentPanelScreen`, `SplitViewScreen`, `BinaryTreeScreen` | Jerarquía unificada de pantallas intercambiables dentro del `StackPane` principal. |
| **Factory / Registry** | `SpriteRegistry`, `RetroSound` | Centralización de recursos gráficos pixel-art y síntesis procedimental de audio 8 bits. |

---

## 2. Diagrama de Clases UML Completo (100% Fiel al Código Actual)

```mermaid
classDiagram
    direction TB

    %% ==========================================
    %% PUNTO DE ENTRADA JAVAFX (com.icesi)
    %% ==========================================
    class Launcher {
        +main(args: String[])$ void
    }
    class Application {
        <<JavaFX>>
    }
    class Canvas {
        <<JavaFX>>
    }
    class GameApplication {
        -controller: GameController
        -hud: HudBar
        -content: StackPane
        -titleScreen: TitleScreen
        -centerScreen: MonitoringCenterScreen
        -spawner: IncidentSpawner
        -shiftTimer: ShiftTimer
        -shiftEndView: ShiftEndView
        -pauseView: PauseView
        -helpView: HelpView
        -optionsView: OptionsView
        -registrationDialog: IncidentRegistrationDialog
        -vehicleDialog: VehicleRegistrationDialog
        -trafficDialog: TrafficSimulationDialog
        -mobilityDialog: MobilityDialog
        -statisticsDialog: StatisticsDialog
        -binaryTreeScreen: BinaryTreeScreen
        -mapView: CityMapView
        -highScores: HighScoreStore
        -screens: Map~String, GameScreen~
        -currentScreen: GameScreen
        +start(stage: Stage) void
        +showScreen(name: String) void
        +openRegistrationDialog() void
        +openVehicleRegistrationDialog() void
        +openTrafficSimulationDialog() void
        +openMobilityDialog() void
        +openStatisticsDialog() void
        +toggleEmergencyCorridors() void
        -handleKey(code: KeyCode) void
        -refreshAll() void
    }

    Launcher ..> GameApplication : delega main()
    Application <|-- GameApplication

    %% ==========================================
    %% CAPA VISTA (com.icesi.ui)
    %% ==========================================
    class GameScreen {
        <<interface>>
        +getView()* Node
        +refresh()* void
        +handleKey(code: KeyCode) boolean
    }
    class TitleScreen {
        -root: Pane
        -canvas: Canvas
        -selected: int
        -record: int
        +getView() Node
        +start() void
        +stop() void
        +moveSelection(step: int) void
        +getSelectedOption() String
        +selectAt(y: double) boolean
    }
    class MonitoringCenterScreen {
        -controller: GameController
        -navigator: Consumer~String~
        -eventLog: EventLog
        -miniMap: MiniMap
        +getView() Node
        +refresh() void
        +handleKey(code: KeyCode) boolean
        +showEvent(message: String) void
    }
    class IncidentPanelScreen {
        -controller: GameController
        -eventLog: EventLog
        -selected: Incident
        -focusedId: String
        -searchInput: TextField
        +getView() Node
        +getCompactView() Node
        +executeBinarySearch() void
        +clearSearch() void
        +refresh() void
        +handleKey(code: KeyCode) boolean
    }
    class SplitViewScreen {
        -controller: GameController
        -mapView: CityMapView
        -incidentPanel: IncidentPanelScreen
        +getView() Node
        +refresh() void
        +handleKey(code: KeyCode) boolean
    }
    class BinaryTreeScreen {
        -controller: GameController
        -navigator: Consumer~String~
        -treeCanvas: Canvas
        -currentMode: TraversalMode
        -selectedIncident: Incident
        +getView() Node
        +refresh() void
        +handleKey(code: KeyCode) boolean
        -drawTree() void
    }
    class CityMapView {
        +CELL: double = 11$
        -controller: GameController
        -cityLayer: Canvas
        -spriteLayer: Canvas
        -nightLayer: Canvas
        -dayNight: DayNightCycle
        -vehicleAnimator: VehicleAnimator
        -emergencyCorridorsVisible: boolean
        +getView() Node
        +toggleEmergencyCorridors() void
        +isEmergencyCorridorsVisible() boolean
        +hasArrived(vehicle: Vehicle) boolean
        +setFocusedIncident(incidentId: String) void
        +shake() void
        -drawSprites(time: double) void
        -drawEmergencyCorridors(gc: GraphicsContext, time: double) void
    }
    class CityRenderer {
        -cityMap: CityMap
        -cell: double
        +drawCity(gc: GraphicsContext) void
    }
    class DayNightCycle {
        -cityMap: CityMap
        -cell: double
        +phaseName(time: double) String
        +draw(gc: GraphicsContext, width: double, height: double, time: double) void
    }
    class MiniMap {
        -controller: GameController
        +draw() void
    }
    class VehicleAnimator {
        -controller: GameController
        -routeFinder: RouteFinder
        +update(seconds: double) void
        +positionOf(vehicle: Vehicle) double[]
        +isMoving(vehicle: Vehicle) boolean
        +hasArrived(vehicle: Vehicle) boolean
    }
    class HudBar {
        +refresh(controller: GameController) void
        +setShiftTime(text: String, lastMinute: boolean) void
        +setActiveScreen(screen: String) void
        +setCorridorsActive(active: boolean) void
    }
    class MobilityDialog {
        -controller: GameController
        -mapView: CityMapView
        +updateStatus() void
        +open() void
    }
    class StatisticsDialog {
        -controller: GameController
        -highScores: HighScoreStore
        +refreshStats() void
        +open() void
    }
    class IncidentRegistrationDialog {
        +open() void
    }
    class VehicleRegistrationDialog {
        +open() void
    }
    class TrafficSimulationDialog {
        -controller: GameController
        +open() void
    }
    class ShiftEndView {
        +show(result: ShiftTimer.Result, score: int) void
    }

    GameScreen <|.. MonitoringCenterScreen
    GameScreen <|.. IncidentPanelScreen
    GameScreen <|.. SplitViewScreen
    GameScreen <|.. BinaryTreeScreen
    SplitViewScreen o-- "1" CityMapView
    SplitViewScreen o-- "1" IncidentPanelScreen
    CityMapView --> "1" CityRenderer : usa
    CityMapView --> "1" DayNightCycle : usa
    CityMapView --> "1" VehicleAnimator : anima
    CityMapView ..> Canvas : dibuja en
    BinaryTreeScreen ..> Canvas : dibuja en
    Canvas <|-- MiniMap
    MonitoringCenterScreen o-- "1" MiniMap

    %% ==========================================
    %% CAPA CONTROLADOR (com.icesi.controller)
    %% ==========================================
    class GameController {
        +FAST_LIMIT_SECONDS: int = 30$
        +FAST_BONUS_POINTS: int = 50$
        +WAIT_PENALTY_POINTS: int = 25$
        +WAIT_PERIOD_SECONDS: int = 30$
        -cityMap: CityMap
        -operator: Operator
        -vehicles: LinkedList~Vehicle~
        -manager: IncidentManager
        -generator: IncidentGenerator
        -resolver: MapPositionResolver
        -random: Random
        -lastResolveFast: boolean
        -chargedBlocks: Map~String, Integer~
        +createInitialVehicles()$ LinkedList~Vehicle~
        +moveOperator(direction: Direction) boolean
        +undoOperatorMove() boolean
        +spawnRandomIncident() Incident
        +triggerCollisionAccident(vehicle1Id: String, vehicle2Id: String, location: String) Incident
        +triggerCongestionAccident(zone: String, vehicleCount: int) Incident
        +triggerStoppedVehicleAccident(vehicleId: String, location: String, stoppedCycles: int) Incident
        +spawnAccidentAt(row: int, column: int) Incident
        +proposeVehicle(incident: Incident) Vehicle
        +assignVehicle(vehicleId: String, incidentId: String) void
        +releaseVehicle(vehicleId: String) void
        +cancelIncidentDispatch(incidentId: String) void
        +resolveIncident(incidentId: String) int
        +resolveIncident(incidentId: String, now: LocalDateTime) int
        +applyWaitPenalties(now: LocalDateTime) int
        +wasLastResolveFast() boolean
        +registerIncident(incident: Incident) void
        +registerVehicle(vehicle: Vehicle) void
        +findIncidentById(incidentId: String) Incident
        +findVehicleById(vehicleId: String) Vehicle
        +getHighestPriorityIncidentOrNull() Incident
        +getCellOf(incident: Incident) int[]
        +getCellOf(vehicle: Vehicle) int[]
        +getActiveIncidentsTree() BinarySearchTree~Incident~
        +getActiveIncidentsByPriority() LinkedList~Incident~
        +getActiveIncidentCount() int
        +getResolvedIncidentCount() int
        +getTotalIncidentCount() int
        +isCorridorCell(row: int, column: int) boolean
        +countCorridorCells() int
        +saveGame(folder: String) void
        +loadGame(folder: String) void
        +saveGameBinary(filePath: String) void
        +loadGameBinary(filePath: String) void
    }
    class IncidentSpawner {
        -controller: GameController
        -onSpawn: Consumer~Incident~
        -fixedInterval: long
        -paused: boolean
        -running: boolean
        -fastMode: boolean
        -thread: Thread
        +intervalForScore(score: int)$ long
        +intervalFor(score: int, fast: boolean)$ long
        +setFastMode(fastMode: boolean) void
        +spawnOnce() Incident
        +setPaused(paused: boolean) void
        +isPaused() boolean
        +start() void
        +stop() void
        +isRunning() boolean
        +run() void
    }
    class ShiftTimer {
        -onTick: Runnable
        -stepMillis: long
        -elapsedSeconds: int
        -paused: boolean
        -running: boolean
        -thread: Thread
        +tick(seconds: int) void
        +getRemainingSeconds() int
        +formatRemaining() String
        +setPaused(paused: boolean) void
        +evaluate(activeIncidents: int) Result
        +start() void
        +stop() void
        +isRunning() boolean
        +run() void
    }
    class RouteFinder {
        -cityMap: CityMap
        +findRoute(from: int[], to: int[]) LinkedList~int[]~
    }
    class MapPositionResolver {
        -cityMap: CityMap
        +resolve(location: String) int[]
    }

    GameApplication o-- "1" GameController : controller
    GameApplication o-- "*" GameScreen : screens
    GameApplication o-- "1" MobilityDialog
    GameApplication o-- "1" StatisticsDialog
    GameApplication o-- "1" TitleScreen
    GameApplication o-- "1" ShiftEndView
    GameApplication --> "1" IncidentSpawner
    GameApplication --> "1" ShiftTimer
    GameApplication --> "1" HighScoreStore
    IncidentSpawner --> "1" GameController : spawnRandomIncident()
    VehicleAnimator --> "1" RouteFinder : calcula BFS

    %% ==========================================
    %% CAPA MODELO Y ESTRUCTURAS (SIN JAVAFX)
    %% ==========================================
    class CityMap {
        +MAP_SIZE: int = 64$
        +PATH: int = 0$
        +OBSTACLE: int = 1$
        +RESIDENTIAL: int = 2$
        +COMMERCIAL: int = 3$
        +MAIN_ROAD: int = 4$
        -grid: int[][]
        +getSize() int
        +isInsideBounds(row: int, column: int) boolean
        +getCell(row: int, column: int) int
        +isWalkable(row: int, column: int) boolean
    }
    class Operator {
        -row: int
        -column: int
        -movementHistory: Stack~Movement~
        -score: int
        +move(direction: Direction) void
        +undoLastMovement() Movement
        +addScore(points: int) void
        +getScore() int
        +getMovementCount() int
    }
    class Movement {
        -direction: Direction
        -previousRow: int
        -previousColumn: int
        +getDirection() Direction
        +getPreviousRow() int
        +getPreviousColumn() int
    }
    class Vehicle {
        -id: String
        -type: VehicleType
        -location: String
        -status: VehicleStatus
        -assignedIncident: Incident
        +getId() String
        +getType() VehicleType
        +getLocation() String
        +getStatus() VehicleStatus
        +getAssignedIncident() Incident
    }
    class Incident {
        -id: String
        -type: IncidentType
        -location: String
        -severity: Severity
        -status: IncidentStatus
        -generatedAt: LocalDateTime
        -resolvedAt: LocalDateTime
        -description: String
        -assignedVehicle: Vehicle
        +getId() String
        +getType() IncidentType
        +getSeverity() Severity
        +getStatus() IncidentStatus
    }
    class IncidentManager {
        -incidents: LinkedList~Incident~
        -activeIncidentsTree: BinarySearchTree~Incident~
        +registerIncident(incident: Incident) void
        +getIncidentCount() int
        +getIncidentAt(index: int) Incident
        +findIncidentById(id: String) Incident
        +getHighestPriorityIncident() Incident
        +proposeCandidateVehicle(incident: Incident, vehicles: LinkedList~Vehicle~) Vehicle
        +assignVehicle(vehicle: Vehicle, incident: Incident) void
        +releaseVehicle(vehicle: Vehicle) void
        +finishAttention(incident: Incident) void
        +finishAttention(incident: Incident, operator: Operator, resolvedAt: LocalDateTime) int
        +calculateScore(incident: Incident, resolvedAt: LocalDateTime) int
        +getIncidentsSortedByPriority() LinkedList~Incident~
        +getActiveIncidentsByPriority() LinkedList~Incident~
        +getActiveIncidentsTree() BinarySearchTree~Incident~
        +getActiveIncidentCount() int
        +getResolvedIncidentCount() int
        +getTotalIncidentCount() int
        +countAvailableVehicles(vehicles: LinkedList~Vehicle~) int
    }
    class IncidentGenerator {
        +CRITICAL_CONGESTION_THRESHOLD: int = 5$
        +STOPPED_CYCLES_THRESHOLD: int = 3$
        -accidentCounter: int
        -theftCounter: int
        -fireCounter: int
        -random: Random
        +registerExistingId(id: String) void
        +generateRandomTheft() Incident
        +generateRandomFire() Incident
        +checkCollisionAccident(vehicle1Id: String, vehicle2Id: String, location: String) Incident
        +checkCongestionAccident(zone: String, vehicleCount: int) Incident
        +checkStoppedVehicleAccident(vehicleId: String, location: String, stoppedCycles: int) Incident
    }
    class BinarySearchTree~T~ {
        -root: BSTNode~T~
        -comparator: Comparator~T~
        -size: int
        +insert(value: T) void
        +contains(value: T) boolean
        +search(value: T) T
        +delete(value: T) boolean
        +findMin() T
        +findMax() T
        +inOrder() LinkedList~T~
        +preOrder() LinkedList~T~
        +postOrder() LinkedList~T~
        +height() int
        +size() int
        +clear() void
        +getRoot() BSTNode~T~
    }
    class BSTNode~T~ {
        -value: T
        -left: BSTNode~T~
        -right: BSTNode~T~
    }
    class LinkedList~T~ {
        -head: Node~T~
        -tail: Node~T~
        -size: int
        +addFirst(value: T) void
        +addLast(value: T) void
        +getFirst() T
        +removeFirst() T
        +get(index: int) T
        +remove(index: int) T
        +contains(value: T) boolean
        +sort(comparator: Comparator~T~) void
        +binarySearch(target: T, comparator: Comparator~T~) int
        +size() int
    }
    class Stack~T~ {
        -elements: LinkedList~T~
        +push(value: T) void
        +pop() T
        +peek() T
        +isEmpty() boolean
        +size() int
    }
    class JsonPersistence {
        +saveVehicles(vehicles: LinkedList~Vehicle~, filePath: String) void
        +loadVehicles(filePath: String) LinkedList~Vehicle~
        +saveIncidents(incidents: LinkedList~Incident~, filePath: String) void
        +loadIncidents(filePath: String) LinkedList~Incident~
        +loadIncidents(filePath: String, vehicles: LinkedList~Vehicle~) LinkedList~Incident~
        +saveOperator(operator: Operator, filePath: String) void
        +loadOperator(filePath: String) Operator
    }
    class SerializationPersistence {
        +saveManagerState(manager: IncidentManager, filePath: String) void
        +loadManagerState(filePath: String) IncidentManager
    }
    class HighScoreStore {
        -filePath: String
        +load() int
        +saveIfHigher(score: int) boolean
    }

    GameController o-- "1" CityMap : cityMap
    GameController o-- "1" Operator : operator
    GameController o-- "1" IncidentManager : manager
    GameController o-- "1" IncidentGenerator : generator
    GameController o-- "1" MapPositionResolver : resolver
    GameController o-- "1" LinkedList : vehicles
    GameController ..> JsonPersistence : F5 / F9
    GameController ..> SerializationPersistence : F6 / F10

    IncidentManager o-- "1" BinarySearchTree : activeIncidentsTree
    IncidentManager o-- "1" LinkedList : incidents
    BinarySearchTree *-- "0..1" BSTNode
    Operator *-- "1" Stack : movementHistory
    Stack *-- "1" LinkedList : elements
    Stack ..> Movement
    Vehicle "0..1" --> "0..1" Incident : assignedIncident
    IncidentManager o-- "0..*" Incident
```

---

## 3. Estructuras, Enumeraciones, Excepciones y Complejidades

- **Enumeraciones (`model`)**: `Direction (UP, DOWN, LEFT, RIGHT)`, `IncidentType (ACCIDENT, THEFT, FIRE)`, `IncidentStatus (PENDING, IN_PROGRESS, RESOLVED)`, `Severity (HIGH, MEDIUM, LOW)`, `VehicleType (PATROL, AMBULANCE, FIRE_TRUCK)`, `VehicleStatus (AVAILABLE, EN_ROUTE, ATTENDING, OUT_OF_SERVICE)`, además de `ShiftTimer.Result (PLAYING, TIME_UP, OVERLOADED)` en `controller`.
- **Excepciones propias (`exceptions`)**: `DuplicateIncidentException`, `EmptyStructureException`, `IncidentNotFoundException`, `IncidentStateException`, `NoActiveIncidentsException`, `PersistenceException`, `VehicleAssignmentException`, `VehicleReleaseException`.
- **Complejidades**:
  1. `BinarySearchTree<Incident>`: Inserción/búsqueda $O(\log n)$ promedio; recorridos `inOrder`, `preOrder`, `postOrder` $O(n)$.
  2. `LinkedList<T>`: Inserción al inicio/final $O(1)$; ordenamiento (`sort` por burbuja optimizada) $O(n^2)$ / $O(n)$ mejor caso y `binarySearch` $O(n \log n)$ (debido a `get(middle)` sobre lista enlazada).
  3. `Stack<Movement>`: Respaldada por `LinkedList<Movement>`, `push` y `pop` en $O(1)$ para deshacer movimientos.
  4. `RouteFinder` (BFS): $O(V + E)$ sobre la matriz $64 \times 64 = 4096$ celdas de Palmira.
