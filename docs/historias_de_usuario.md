# Historias de Usuario  (22 Historias) — SGMMS


## Tabla Resumen de Trazabilidad (22 Historias de Usuario $\leftrightarrow$ Requerimientos y Código)

| ID | Título de la Historia de Usuario | Req. Asociado | Clase(s) y Método(s) Principal(es) |
| :-: | :--- | :---: | :--- |
| **HU-01** | Navegación Interactiva en el Centro de Monitoreo | `RF-1` | `Main.main()`, `Main.showMenu()`, `Main.executeOption(int)` |
| **HU-02** | Representación y Visualización del Mapa Urbano 64×64 | `RF-2` | `CityMap`, `CityMap.buildDefaultMap()`, `Main.printMap()` |
| **HU-03** | Desplazamiento del Operador con Validación de Vías Transitables | `RF-2`, `RF-17` | `CityMap.isWithinBounds()`, `CityMap.isWalkable()`, `Operator.move(Direction)` |
| **HU-04** | Deshacer el Último Movimiento del Operador con Pila LIFO | `RF-17`, `RF-21` | `Operator.undoLastMovement()`, `Stack<Movement>.pop()`, `Movement` |
| **HU-05** | Registro Manual de Incidentes con Validación de Unicidad | `RF-3` | `IncidentManager.registerIncident(Incident)`, `DuplicateIncidentException` |
| **HU-06** | Búsqueda Binaria de Incidentes por Identificador | `RF-3` | `IncidentManager.findIncidentById(String)`, `LinkedList.binarySearch()`, `IncidentIdComparator` |
| **HU-07** | Generación Automática Aleatoria de Robos e Incendios | `RF-4` | `IncidentGenerator.generateRandomTheft()`, `IncidentGenerator.generateRandomFire()` |
| **HU-08** | Generación de Accidentes por Colisión de Dos Vehículos (Criterio 1) | `RF-4` | `IncidentGenerator.checkCollisionAccident(String, String, String)` |
| **HU-09** | Generación de Accidentes por Congestión Crítica en Zona (Criterio 2) | `RF-4` | `IncidentGenerator.checkCongestionAccident(String, int)` |
| **HU-10** | Generación de Accidentes por Vehículo Detenido Prolongadamente (Criterio 3) | `RF-4` | `IncidentGenerator.checkStoppedVehicleAccident(String, String, int)` |
| **HU-11** | Listado de Incidentes Ordenados por Prioridad y Antigüedad | `RF-5` | `IncidentManager.getIncidentsSortedByPriority()`, `LinkedList.sort()`, `IncidentPriorityComparator` |
| **HU-12** | Consulta Directa del Incidente de Mayor Prioridad | `RF-5` | `IncidentManager.getHighestPriorityIncident()`, `NoActiveIncidentsException` |
| **HU-13** | Registro Inicial y Consulta del Estado de la Flota de Vehículos | `RF-6` | `Vehicle`, `Main.loadVehicles()`, `Main.showVehicles()`, `Main.describeVehicle()` |
| **HU-14** | Propuesta Automática de Vehículo Candidato Compatible | `RF-7` | `IncidentManager.proposeCandidateVehicle(Incident, LinkedList<Vehicle>)` |
| **HU-15** | Asignación de Vehículo a Incidente con Validación de Compatibilidad | `RF-7`, `RF-18` | `IncidentManager.assignVehicle(Vehicle, Incident)`, `IncidentManager.isCompatible()` |
| **HU-16** | Liberación de Vehículo en Ruta y Retorno del Incidente a Pendiente | `RF-6`, `RF-18` | `IncidentManager.releaseVehicle(Vehicle)`, `VehicleReleaseException` |
| **HU-17** | Finalización de la Atención de un Incidente y Liberación de Unidad | `RF-8`, `RF-18` | `IncidentManager.finishAttention(Incident)`, `IncidentStateException` |
| **HU-18** | Cálculo de Puntaje del Operador y Bonificación por Tiempo | `RF-14` | `IncidentManager.calculateScore()`, `IncidentManager.finishAttention(Incident, Operator)`, `Operator.addScore()` |
| **HU-19** | Actualización y Consulta de Indicadores Operativos en Tiempo Real | `RF-13`, `RF-18` | `IncidentManager.getActiveIncidentCount()`, `getActiveAccidentCount()`, `getActiveTheftCount()`, `getActiveFireCount()`, `countAvailableVehicles()` |
| **HU-20** | Persistencia de Vehículos en Archivos JSON | `RF-15`, `RF-19` | `JsonPersistence.saveVehicles()`, `JsonPersistence.loadVehicles()` |
| **HU-21** | Persistencia de Incidentes en Archivos JSON | `RF-15`, `RF-19` | `JsonPersistence.saveIncidents()`, `JsonPersistence.loadIncidents()` |
| **HU-22** | Serialización Binaria y Restauración del Estado de la Simulación | `RF-16`, `RF-19` | `SerializationPersistence.saveManagerState()`, `SerializationPersistence.loadManagerState()`, `Serializable` |

---

## Detalle de las 22 Historias de Usuario

### HU-01: Navegación Interactiva en el Centro de Monitoreo (`RF-1`)
- **Como** operador del sistema SGMMS,
- **Quiero** contar con un menú interactivo en consola que centralice las 14 operaciones del Centro de Monitoreo, el Mapa Urbano y el Panel de Incidentes,
- **Para** navegar de forma continua entre las funciones operativas sin que el sistema se cierre hasta seleccionar la opción `0 (Salir)`.
- **Código asociado:** `Main.main(String[])`, `Main.showMenu()`, `Main.executeOption(int)`, `Main.readInt(String)`, `Main.readText(String)`.
- **Criterios de Aceptación:**
  1. **Dado** que se inicia la aplicación, **Cuando** se ejecuta `Main.main()`, **Entonces** se cargan los vehículos iniciales y se despliega el menú con las opciones `1` a `14` y `0`.
  2. **Dado** que el usuario ingresa un texto no numérico en el menú, **Cuando** `readInt()` procesa la entrada, **Entonces** captura `NumberFormatException`, solicita nuevamente un número válido y evita la caída del programa.
  3. **Dado** que el usuario ingresa una opción fuera del rango `0..14`, **Cuando** `executeOption(option)` se ejecuta, **Entonces** muestra `"Opcion no valida."` y vuelve a presentar el menú.

---

### HU-02: Representación y Visualización del Mapa Urbano 64×64 (`RF-2`)
- **Como** operador del centro de monitoreo,
- **Quiero** visualizar el mapa de la ciudad representado como una cuadrícula de 64×64 con distintos tipos de zonas urbanas y mi posición actual (`X`),
- **Para** identificar gráficamente las vías principales, calles secundarias, manzanas residenciales, zonas comerciales y obstáculos viales.
- **Código asociado:** `CityMap` (`MAP_SIZE = 64`, `PATH = 0`, `OBSTACLE = 1`, `RESIDENTIAL = 2`, `COMMERCIAL = 3`, `MAIN_ROAD = 4`, `buildDefaultMap()`, `fillZone()`, `getCell()`, `setCell()`), `Main.printMap()` (Opción `11`).
- **Criterios de Aceptación:**
  1. **Dado** que se instancia `CityMap`, **Cuando** se ejecuta `buildDefaultMap()`, **Entonces** se inicializa una matriz `grid[64][64]` con avenidas principales (`4`) en los bordes y ejes centrales (filas/columnas `0`, `32`, `63`), calles (`0`) cada 4 celdas, manzanas residenciales (`2`), comerciales (`3`) y obstáculos (`1`).
  2. **Dado** que el operador selecciona la opción `11 (Ver mapa)`, **Cuando** se ejecuta `printMap()`, **Entonces** se imprime la cuadrícula mostrando `X` en la coordenada `(operator.getRow(), operator.getColumn())`, `=` en vías principales, `.` en calles, `#` en obstáculos y `B` en edificios.

---

### HU-03: Desplazamiento del Operador con Validación de Vías Transitables (`RF-2`, `RF-17`)
- **Como** operador del centro de monitoreo,
- **Quiero** moverme una celda hacia arriba (`UP`), abajo (`DOWN`), izquierda (`LEFT`) o derecha (`RIGHT`) únicamente por calles y vías principales dentro de los límites del mapa,
- **Para** desplazarme por la ciudad respetando las restricciones físicas de edificios, obstáculos y bordes del mapa.
- **Código asociado:** `Main.moveOperator()` (Opción `9`), `CityMap.isWithinBounds(int, int)`, `CityMap.isWalkable(int, int)`, `Operator.move(Direction)`, `Direction`.
- **Criterios de Aceptación:**
  1. **Dado** que el operador está en `(row, column)`, **Cuando** elige una dirección cuya celda destino está dentro de `0..63` (`isWithinBounds == true`) y es `PATH (0)` o `MAIN_ROAD (4)` (`isWalkable == true`), **Entonces** `operator.move(direction)` actualiza `row` y `column` y registra el movimiento en su pila `movementHistory`.
  2. **Dado** que la celda destino está fuera de `0..63` o contiene `OBSTACLE (1)`, `RESIDENTIAL (2)` o `COMMERCIAL (3)`, **Cuando** se evalúa `cityMap.isWalkable(newRow, newColumn)`, **Entonces** retorna `false`, el operador no se mueve y se muestra el mensaje `"No se puede mover ahi: hay un obstaculo, una zona no transitable o el limite del mapa."`.

---

### HU-04: Deshacer el Último Movimiento del Operador con Pila LIFO (`RF-17`, `RF-21`)
- **Como** operador del centro de monitoreo,
- **Quiero** deshacer mi último movimiento válido registrado en el historial,
- **Para** restaurar mi posición inmediatamente anterior siguiendo la política LIFO de una pila propia `Stack<Movement>`.
- **Código asociado:** `Main.undoMovement()` (Opción `10`), `Operator.undoLastMovement()`, `Operator.getMovementCount()`, `Movement` (`direction`, `previousRow`, `previousColumn`), `Stack<T>` (`push`, `pop`, `peek`, `isEmpty`, `size`), `EmptyStructureException`.
- **Criterios de Aceptación:**
  1. **Dado** que el operador ha realizado al menos un movimiento válido, **Cuando** invoca `operator.undoLastMovement()`, **Entonces** se desapila (`movementHistory.pop()`) el último objeto `Movement`, la posición del operador vuelve a `(previousRow, previousColumn)` y se retorna el movimiento deshecho.
  2. **Dado** que el historial de movimientos está vacío (`movementHistory.isEmpty()`), **Cuando** se invoca `operator.undoLastMovement()`, **Entonces** se lanza `EmptyStructureException` sin modificar las coordenadas del operador.

---

### HU-05: Registro Manual de Incidentes con Validación de Unicidad (`RF-3`)
- **Como** operador del centro de monitoreo,
- **Quiero** registrar manualmente un incidente indicando su ID, tipo (`ACCIDENT`, `THEFT`, `FIRE`), ubicación y gravedad (`HIGH`, `MEDIUM`, `LOW`),
- **Para** ingresarlo a la lista enlazada de incidentes activos en estado `PENDING` evitando duplicados.
- **Código asociado:** `Main.registerIncident()` (Opción `1`), `IncidentManager.registerIncident(Incident)`, `IncidentManager.existsIncidentWithId(String)`, `Incident`, `LinkedList<Incident>.addLast()`, `DuplicateIncidentException`.
- **Criterios de Aceptación:**
  1. **Dado** un `Incident` no nulo con un `id` único, **Cuando** se invoca `manager.registerIncident(incident)`, **Entonces** el incidente queda en estado `PENDING`, con `generatedAt = LocalDateTime.now()`, `assignedVehicle = null`, y se inserta al final de `incidents` (`LinkedList<Incident>`).
  2. **Dado** un `Incident` con un `id` ya registrado en `incidents`, **Cuando** se invoca `manager.registerIncident(incident)`, **Entonces** se lanza `DuplicateIncidentException("Ya existe un incidente registrado con el ID: " + id)`.
  3. **Dado** una referencia `null`, **Cuando** se invoca `manager.registerIncident(null)`, **Entonces** se lanza `IllegalArgumentException`.

---

### HU-06: Búsqueda Binaria de Incidentes por Identificador (`RF-3`)
- **Como** operador del centro de monitoreo,
- **Quiero** buscar un incidente específico a partir de su ID utilizando búsqueda binaria,
- **Para** localizar y consultar sus datos completos o saber si el ID no existe.
- **Código asociado:** `Main.searchIncident()` (Opción `4`), `IncidentManager.findIncidentById(String)`, `IncidentIdComparator`, `LinkedList.sort(Comparator<T>)`, `LinkedList.binarySearch(T, Comparator<T>)`, `IncidentNotFoundException`.
- **Criterios de Aceptación:**
  1. **Dado** que existen incidentes en el gestor, **Cuando** se llama a `manager.findIncidentById(id)` con un ID existente, **Entonces** se ordena una copia de la lista con `IncidentIdComparator`, se busca con `binarySearch` y se retorna el `Incident` encontrado.
  2. **Dado** un `id` que no existe en la lista (`binarySearch` retorna `-1`) o un `id` nulo, **Cuando** se llama a `manager.findIncidentById(id)`, **Entonces** se lanza `IncidentNotFoundException`.

---

### HU-07: Generación Automática Aleatoria de Robos e Incendios (`RF-4`)
- **Como** operador del centro de monitoreo,
- **Quiero** generar automáticamente incidentes aleatorios de robo (`THEFT`) y de incendio (`FIRE`),
- **Para** simular emergencias espontáneas en distintas zonas de la ciudad con IDs secuenciales únicos.
- **Código asociado:** `Main.generateAutomaticIncident()` (Opción `12`, subopciones `1` y `2`), `IncidentGenerator.generateRandomTheft()`, `IncidentGenerator.generateRandomFire()`, `IncidentGenerator.getTheftCounter()`, `IncidentGenerator.getFireCounter()`.
- **Criterios de Aceptación:**
  1. **Dado** el generador `IncidentGenerator`, **Cuando** se invoca `generateRandomTheft()`, **Entonces** incrementa `theftCounter` y retorna un nuevo `Incident` de tipo `THEFT`, en estado `PENDING`, con ID formato `THEFT-001`, ubicación tomada de `THEFT_LOCATIONS` y gravedad aleatoria.
  2. **Dado** el generador `IncidentGenerator`, **Cuando** se invoca `generateRandomFire()`, **Entonces** incrementa `fireCounter` y retorna un nuevo `Incident` de tipo `FIRE`, en estado `PENDING`, con ID formato `FIRE-001`, ubicación tomada de `FIRE_LOCATIONS` y gravedad aleatoria.

---

### HU-08: Generación de Accidentes por Colisión de Dos Vehículos — Criterio 1 (`RF-4`)
- **Como** operador del centro de monitoreo,
- **Quiero** que el sistema evalúe si dos vehículos distintos coinciden en una misma intersección y genere automáticamente un accidente de tránsito por colisión,
- **Para** atender de inmediato siniestros viales de alta gravedad (`HIGH`).
- **Código asociado:** `Main.generateAutomaticIncident()` (Opción `12`, subopción `3`), `IncidentGenerator.checkCollisionAccident(String, String, String)`.
- **Criterios de Aceptación:**
  1. **Dado** dos IDs de vehículos no vacíos y diferentes (`!v1.equals(v2)`) y una ubicación válida, **Cuando** se ejecuta `checkCollisionAccident(v1, v2, location)`, **Entonces** se incrementa `accidentCounter` y se retorna un `Incident` de tipo `ACCIDENT`, gravedad `Severity.HIGH`, estado `PENDING`, ID `ACC-XXX` y descripción detallando la colisión entre ambos vehículos.
  2. **Dado** dos IDs iguales (`v1.equals(v2)`), nulos o en blanco, **Cuando** se ejecuta `checkCollisionAccident(...)`, **Entonces** retorna `null` y no se registra ningún incidente.

---

### HU-09: Generación de Accidentes por Congestión Crítica en Zona — Criterio 2 (`RF-4`)
- **Como** operador del centro de monitoreo,
- **Quiero** que el sistema evalúe el volumen vehicular en una zona y dispare un accidente cuando se alcance o supere el umbral de congestión crítica (`5` vehículos),
- **Para** detectar accidentes ocasionados por saturación vial en la ciudad.
- **Código asociado:** `Main.generateAutomaticIncident()` (Opción `12`, subopción `4`), `IncidentGenerator.checkCongestionAccident(String, int)`, `CRITICAL_CONGESTION_THRESHOLD = 5`.
- **Criterios de Aceptación:**
  1. **Dado** una zona válida y una cantidad de vehículos `vehicleCount >= 5`, **Cuando** se invoca `checkCongestionAccident(zone, vehicleCount)`, **Entonces** se retorna un `Incident` de tipo `ACCIDENT` con ID `ACC-XXX`, gravedad `MEDIUM` (o `HIGH` si `vehicleCount >= 10`) y descripción de congestión crítica.
  2. **Dado** una cantidad de vehículos `vehicleCount < 5` o una zona vacía/nula, **Cuando** se invoca `checkCongestionAccident(...)`, **Entonces** retorna `null`.

---

### HU-10: Generación de Accidentes por Vehículo Detenido Prolongadamente — Criterio 3 (`RF-4`)
- **Como** operador del centro de monitoreo,
- **Quiero** que el sistema evalúe cuántos ciclos lleva detenido un vehículo en una vía y genere un accidente si iguala o supera el umbral (`3` ciclos),
- **Para** identificar obstrucciones viales o averías que producen accidentes de tránsito.
- **Código asociado:** `Main.generateAutomaticIncident()` (Opción `12`, subopción `5`), `IncidentGenerator.checkStoppedVehicleAccident(String, String, int)`, `STOPPED_CYCLES_THRESHOLD = 3`.
- **Criterios de Aceptación:**
  1. **Dado** un ID de vehículo, una vía y `stoppedCycles >= 3`, **Cuando** se ejecuta `checkStoppedVehicleAccident(vehicleId, location, stoppedCycles)`, **Entonces** retorna un `Incident` de tipo `ACCIDENT` con ID `ACC-XXX`, gravedad `LOW` (o `MEDIUM` si `stoppedCycles >= 6`) y descripción indicando los ciclos detenido.
  2. **Dado** `stoppedCycles < 3` o datos vacíos/nulos, **Cuando** se ejecuta `checkStoppedVehicleAccident(...)`, **Entonces** retorna `null`.

---

### HU-11: Listado de Incidentes Ordenados por Prioridad y Antigüedad (`RF-5`)
- **Como** operador del centro de monitoreo,
- **Quiero** consultar la lista completa de incidentes ordenada de mayor a menor gravedad (`HIGH` > `MEDIUM` > `LOW`) y desempatada por orden de antigüedad (`generatedAt`),
- **Para** visualizar la cola de atención priorizada sin alterar el orden original de registro en el sistema.
- **Código asociado:** `Main.showIncidentsByPriority()` (Opción `2`), `IncidentManager.getIncidentsSortedByPriority()`, `IncidentManager.copyIncidents()`, `IncidentPriorityComparator`, `LinkedList.sort(Comparator<T>)`.
- **Criterios de Aceptación:**
  1. **Dado** varios incidentes registrados con distintas gravedades y fechas, **Cuando** se invoca `getIncidentsSortedByPriority()`, **Entonces** se clona la lista enlazada y se ordena con `LinkedList.sort(new IncidentPriorityComparator())` dejando primero los `HIGH`, luego `MEDIUM` y por último `LOW`.
  2. **Dado** dos incidentes con la misma gravedad, **Cuando** `IncidentPriorityComparator.compare(first, second)` los evalúa, **Entonces** ubica primero el que tenga fecha `generatedAt` anterior (o el que fue registrado primero).

---

### HU-12: Consulta Directa del Incidente de Mayor Prioridad (`RF-5`)
- **Como** operador del centro de monitoreo,
- **Quiero** consultar directamente cuál es el incidente individual de mayor prioridad en el sistema,
- **Para** conocer de forma inmediata la emergencia más urgente sin tener que inspeccionar toda la lista.
- **Código asociado:** `Main.showHighestPriorityIncident()` (Opción `3`), `IncidentManager.getHighestPriorityIncident()`, `IncidentManager.isHigherPriority(Incident, Incident)`, `NoActiveIncidentsException`.
- **Criterios de Aceptación:**
  1. **Dado** que hay al menos un incidente registrado, **Cuando** se invoca `getHighestPriorityIncident()`, **Entonces** recorre la `LinkedList<Incident>` y retorna el de mayor gravedad (y más antiguo en caso de empate).
  2. **Dado** que no hay ningún incidente registrado (`incidents.isEmpty()`), **Cuando** se invoca `getHighestPriorityIncident()`, **Entonces** se lanza `NoActiveIncidentsException("No hay incidentes registrados en el sistema.")`.

---

### HU-13: Registro Inicial y Consulta del Estado de la Flota de Vehículos (`RF-6`)
- **Como** operador del centro de monitoreo,
- **Quiero** contar con el registro de la flota de vehículos de emergencia y consultar en cualquier momento su ID, tipo (`PATROL`, `AMBULANCE`, `FIRE_TRUCK`), ubicación, estado (`AVAILABLE`, `EN_ROUTE`, `ATTENDING`, `OUT_OF_SERVICE`) e incidente asignado,
- **Para** supervisar los recursos de respuesta disponibles y ocupados en la ciudad.
- **Código asociado:** `Main.loadVehicles()`, `Main.showVehicles()` (Opción `8`), `Main.describeVehicle(Vehicle)`, `Vehicle`, `VehicleType`, `VehicleStatus`.
- **Criterios de Aceptación:**
  1. **Dado** el inicio del sistema, **Cuando** se ejecuta `loadVehicles()`, **Entonces** se registran en la `LinkedList<Vehicle>` las unidades iniciales (`P-01` en `Estacion Norte`, `A-01` en `Hospital Central`, `F-01` en `Estacion Sur`) en estado `AVAILABLE`.
  2. **Dado** que el operador selecciona la opción `8 (Ver vehiculos)`, **Cuando** se recorre `vehicles`, **Entonces** se muestra para cada unidad su ID, tipo, estado, ubicación y, si tiene un `assignedIncident`, el ID de dicho incidente.

---

### HU-14: Propuesta Automática de Vehículo Candidato Compatible (`RF-7`)
- **Como** operador del centro de monitoreo,
- **Quiero** que antes de asignar un vehículo a un incidente el sistema me proponga automáticamente la unidad disponible más adecuada según el tipo de emergencia,
- **Para** agilizar la toma de decisiones de despacho cumpliendo las reglas de compatibilidad.
- **Código asociado:** `Main.assignVehicle()` (Opción `5`), `IncidentManager.proposeCandidateVehicle(Incident, LinkedList<Vehicle>)`, `VehicleAssignmentException`.
- **Criterios de Aceptación:**
  1. **Dado** un incidente de tipo `FIRE` o `THEFT` y la lista `vehicles`, **Cuando** se invoca `proposeCandidateVehicle(incident, vehicles)`, **Entonces** retorna el primer `FIRE_TRUCK` disponible para incendios o el primer `PATROL` disponible para robos.
  2. **Dado** un incidente de tipo `ACCIDENT`, **Cuando** se invoca `proposeCandidateVehicle(incident, vehicles)`, **Entonces** busca y retorna con prioridad una `AMBULANCE` disponible y, solo si no hay ambulancias disponibles, retorna una `PATROL` disponible como apoyo.
  3. **Dado** que no existe en la flota ningún vehículo en estado `AVAILABLE` compatible con el incidente, **Cuando** se invoca `proposeCandidateVehicle(...)`, **Entonces** lanza `VehicleAssignmentException`.

---

### HU-15: Asignación de Vehículo a Incidente con Validación de Compatibilidad (`RF-7`, `RF-18`)
- **Como** operador del centro de monitoreo,
- **Quiero** asignar un vehículo disponible a un incidente pendiente validando que sus tipos sean compatibles,
- **Para** iniciar la atención del incidente (`IN_PROGRESS`) y despachar la unidad (`EN_ROUTE`) bloqueando asignaciones prohibidas.
- **Código asociado:** `Main.assignVehicle()` (Opción `5`), `IncidentManager.assignVehicle(Vehicle, Incident)`, `IncidentManager.isCompatible(VehicleType, IncidentType)`, `VehicleAssignmentException`.
- **Criterios de Aceptación:**
  1. **Dado** un `Vehicle` en estado `AVAILABLE` y un `Incident` en estado `PENDING` cuyos tipos cumplen `isCompatible(...) == true`, **Cuando** se ejecuta `assignVehicle(vehicle, incident)`, **Entonces** el incidente cambia a `IN_PROGRESS`, el vehículo cambia a `EN_ROUTE`, `incident.getAssignedVehicle()` apunta al vehículo y `vehicle.getAssignedIncident()` apunta al incidente.
  2. **Dado** un vehículo cuyo estado no es `AVAILABLE`, un incidente cuyo estado no es `PENDING`, o una combinación incompatible (ej. `AMBULANCE` para `FIRE`), **Cuando** se ejecuta `assignVehicle(vehicle, incident)`, **Entonces** se lanza `VehicleAssignmentException` sin modificar los estados.

---

### HU-16: Liberación de Vehículo en Ruta y Retorno del Incidente a Pendiente (`RF-6`, `RF-18`)
- **Como** operador del centro de monitoreo,
- **Quiero** liberar manualmente un vehículo que está asignado a un incidente en progreso,
- **Para** dejar el vehículo nuevamente `AVAILABLE` y devolver el incidente a estado `PENDING` para poder reasignarle otra unidad.
- **Código asociado:** `Main.releaseVehicle()` (Opción `7`), `IncidentManager.releaseVehicle(Vehicle)`, `IncidentManager.findInProgressIncidentOf(Vehicle)`, `VehicleReleaseException`.
- **Criterios de Aceptación:**
  1. **Dado** un vehículo asignado a un incidente en estado `IN_PROGRESS`, **Cuando** se ejecuta `manager.releaseVehicle(vehicle)`, **Entonces** el incidente vuelve a `IncidentStatus.PENDING` con `assignedVehicle = null`, y el vehículo vuelve a `VehicleStatus.AVAILABLE` con `assignedIncident = null`.
  2. **Dado** un vehículo que ya está `AVAILABLE` o que no está atendiendo ningún incidente en progreso, **Cuando** se ejecuta `manager.releaseVehicle(vehicle)`, **Entonces** se lanza `VehicleReleaseException`.

---

### HU-17: Finalización de la Atención de un Incidente y Liberación Automática de Unidad (`RF-8`, `RF-18`)
- **Como** operador del centro de monitoreo,
- **Quiero** finalizar la atención de un incidente que se encuentra en progreso (`IN_PROGRESS`),
- **Para** marcarlo como `RESOLVED`, registrar su fecha/hora de cierre (`resolvedAt`) y dejar el vehículo asignado automáticamente en estado `AVAILABLE`.
- **Código asociado:** `Main.finishAttention()` (Opción `6`), `IncidentManager.finishAttention(Incident)`, `IncidentStateException`.
- **Criterios de Aceptación:**
  1. **Dado** un incidente en estado `IN_PROGRESS` con un vehículo asignado, **Cuando** se invoca `finishAttention(...)`, **Entonces** el incidente pasa a `IncidentStatus.RESOLVED`, se registra `resolvedAt`, y su vehículo pasa a `VehicleStatus.AVAILABLE` con `assignedIncident = null`.
  2. **Dado** un incidente en estado `PENDING` o que ya fue `RESOLVED`, **Cuando** se invoca `finishAttention(...)`, **Entonces** se lanza `IncidentStateException("Solo se puede finalizar un incidente que este en proceso.")`.

---

### HU-18: Cálculo de Puntaje del Operador y Bonificación por Tiempo de Respuesta (`RF-14`)
- **Como** operador del centro de monitoreo,
- **Quiero** recibir puntos cada vez que finalizo la atención de un incidente según su gravedad (`100` Alta, `70` Media, `40` Baja) más una bonificación de `+20` puntos si se resuelve dentro del tiempo límite,
- **Para** cuantificar mi eficiencia operativa durante la simulación.
- **Código asociado:** `Main.finishAttention()` (Opción `6`), `IncidentManager.calculateScore(Incident, LocalDateTime)`, `IncidentManager.finishAttention(Incident, Operator, LocalDateTime)`, `Operator.getScore()`, `Operator.addScore(int)`.
- **Criterios de Aceptación:**
  1. **Dado** que se resuelve un incidente con `finishAttention(incident, operator, resolvedAt)`, **Cuando** `calculateScore(incident, resolvedAt)` evalúa la gravedad, **Entonces** otorga `100` puntos para `HIGH`, `70` para `MEDIUM` y `40` para `LOW`.
  2. **Dado** el tiempo transcurrido en segundos entre `generatedAt` y `resolvedAt`, **Cuando** la duración es $\le 300\text{s}$ (`HIGH`), $\le 600\text{s}$ (`MEDIUM`) o $\le 900\text{s}$ (`LOW`), **Entonces** se suman `+20` puntos adicionales (`TIME_BONUS_POINTS`) y el total se acumula en el operador mediante `operator.addScore(points)`.

---

### HU-19: Actualización y Consulta de Indicadores Operativos en Tiempo Real (`RF-13`, `RF-18`)
- **Como** operador del centro de monitoreo,
- **Quiero** visualizar en tiempo real en el encabezado del menú mi posición, mi puntaje acumulado, el total de incidentes activos clasificados por tipo (`Accidentes`, `Robos`, `Incendios`) y la cantidad de vehículos disponibles,
- **Para** monitorear el estado global de la simulación después de cada evento.
- **Código asociado:** `Main.showMenu()`, `IncidentManager.getActiveIncidentCount()`, `IncidentManager.getActiveAccidentCount()`, `IncidentManager.getActiveTheftCount()`, `IncidentManager.getActiveFireCount()`, `IncidentManager.countAvailableVehicles(LinkedList<Vehicle>)`.
- **Criterios de Aceptación:**
  1. **Dado** incidentes registrados en estados `PENDING`, `IN_PROGRESS` y `RESOLVED`, **Cuando** se consultan `getActiveIncidentCount()` y los contadores por tipo, **Entonces** solo se contabilizan los incidentes en estado `PENDING` o `IN_PROGRESS`, excluyendo los `RESOLVED`.
  2. **Dado** la flota `vehicles`, **Cuando** se invoca `countAvailableVehicles(vehicles)`, **Entonces** retorna exclusivamente el número de vehículos cuyo estado es `AVAILABLE`.

---

### HU-20: Persistencia de Vehículos en Archivos JSON (`RF-15`, `RF-19`)
- **Como** operador del centro de monitoreo,
- **Quiero** guardar y cargar la flota de vehículos desde un archivo JSON (`data/vehicles.json`),
- **Para** almacenar y restaurar la configuración de vehículos validando la integridad del archivo.
- **Código asociado:** `Main.saveOrLoadJson()` (Opción `13`, subopciones `1` y `2`), `JsonPersistence.saveVehicles(LinkedList<Vehicle>, Path)`, `JsonPersistence.loadVehicles(Path)`, `PersistenceException`.
- **Criterios de Aceptación:**
  1. **Dado** una `LinkedList<Vehicle>`, **Cuando** se ejecuta `saveVehicles(vehicles, filePath)` y luego `loadVehicles(filePath)`, **Entonces** se escribe y reconstruye cada `Vehicle` con su `id`, `type`, `location` y `status`.
  2. **Dado** que el archivo `vehicles.json` no existe o contiene JSON inválido, **Cuando** se ejecuta `loadVehicles(filePath)`, **Entonces** se lanza `PersistenceException`.

---

### HU-21: Persistencia de Incidentes en Archivos JSON (`RF-15`, `RF-19`)
- **Como** operador del centro de monitoreo,
- **Quiero** exportar y cargar el listado de incidentes desde un archivo JSON (`data/incidents.json`),
- **Para** respaldar y recuperar los incidentes registrados con todas sus fechas, estados y descripciones.
- **Código asociado:** `Main.saveOrLoadJson()` (Opción `13`, subopciones `3` y `4`), `JsonPersistence.saveIncidents(LinkedList<Incident>, Path)`, `JsonPersistence.loadIncidents(Path)`, `PersistenceException`.
- **Criterios de Aceptación:**
  1. **Dado** una `LinkedList<Incident>`, **Cuando** se ejecuta `saveIncidents(incidents, filePath)` y luego `loadIncidents(filePath)`, **Entonces** se serializan en JSON y se reconstruyen todos los incidentes conservando `id`, `type`, `location`, `severity`, `status`, `generatedAt`, `resolvedAt` y `description`.
  2. **Dado** un archivo inexistente o con estructura JSON corrupta, **Cuando** se ejecuta `loadIncidents(filePath)`, **Entonces** lanza `PersistenceException`.

---

### HU-22: Serialización Binaria y Restauración del Estado de la Simulación (`RF-16`, `RF-19`)
- **Como** operador del centro de monitoreo,
- **Quiero** guardar y recuperar mediante serialización binaria de objetos el estado completo de `IncidentManager` en `data/simulation_state.dat`,
- **Para** continuar exactamente una ejecución previamente almacenada con sus estructuras enlazadas y asignaciones intactas.
- **Código asociado:** `Main.saveOrLoadSerialization()` (Opción `14`, subopciones `1` y `2`), `SerializationPersistence.saveManagerState(IncidentManager, Path)`, `SerializationPersistence.loadManagerState(Path)`, `Serializable` implementado en `Node`, `LinkedList`, `Stack`, `Movement`, `Vehicle`, `Incident`, `Operator`, `IncidentManager`, `PersistenceException`.
- **Criterios de Aceptación:**
  1. **Dado** un `IncidentManager` en ejecución, **Cuando** se invoca `saveManagerState(manager, filePath)` y luego `loadManagerState(filePath)`, **Entonces** se deserializa y retorna el `IncidentManager` con todos sus incidentes, estados y vehículos asignados.
  2. **Dado** un archivo `.dat` inexistente, un flujo binario corrupto o argumentos nulos, **Cuando** se invoca `saveManagerState` o `loadManagerState`, **Entonces** se cierran los flujos de archivo de forma segura y se lanza `PersistenceException`.
