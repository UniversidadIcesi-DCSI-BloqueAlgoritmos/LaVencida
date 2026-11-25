# Diseño de pruebas unitarias

**Requerimiento funcional / Historia de usuario:** RF-1 - Gestionar los escenarios del sistema, RF-2 - Desplazamiento del operador en el mapa (W, A, S, D o flechas, sin salir del mapa ni atravesar obstáculos), RF-21 - Deshacer el último movimiento, RF-4 - Generar incidentes, RF-7 y RF-8 - Asignar vehículos y finalizar la atención desde la interfaz gráfica.

**Nombre del método:** moveOperator(Direction direction): boolean, undoOperatorMove(): boolean, spawnRandomIncident(): Incident, assignVehicle(String vehicleId, String incidentId): void, resolveIncident(String incidentId): int, getHighestPriorityIncidentOrNull(): Incident

**Objetivo de la Prueba:** Verificar que el controlador de la interfaz gráfica (patrón MVC) traduzca las acciones del jugador en operaciones del modelo: el operador solo se mueve a celdas transitables, deshacer funciona sin cerrar la aplicación aunque no haya historial, los incidentes generados quedan registrados como activos, y asignar y resolver por ID actualiza los estados y suma puntos al operador.

| Arrange (Inicialización) | Act (Acción) | Assert (Verificar) |
|---|---|---|
| **Caso 1: Mover al operador hacia una celda transitable lo mueve y retorna true.** | | |
| Crear controller con el operador en (4, 4), que es un camino. | boolean moved = controller.moveOperator(Direction.UP); | assertTrue(moved); assertEquals(3, controller.getOperator().getRow()); assertEquals(4, controller.getOperator().getColumn()); |
| **Caso 2: Mover al operador hacia el borde del mapa (obstáculo) no lo mueve y retorna false.** | | |
| Crear controller con el operador en (1, 4), justo debajo del borde superior. | boolean moved = controller.moveOperator(Direction.UP); | assertFalse(moved); assertEquals(1, controller.getOperator().getRow()); assertEquals(0, controller.getOperator().getMovementCount()); |
| **Caso 3: Deshacer sin movimientos retorna false y, después de moverse, deshacer devuelve al operador a su posición.** | | |
| Crear controller con el operador en (4, 4). | boolean emptyUndo = controller.undoOperatorMove(); controller.moveOperator(Direction.RIGHT); boolean undo = controller.undoOperatorMove(); | assertFalse(emptyUndo); assertTrue(undo); assertEquals(4, controller.getOperator().getColumn()); |
| **Caso 4: Generar un incidente lo registra como activo y es un robo o un incendio.** | | |
| Crear controller sin incidentes. | Incident incident = controller.spawnRandomIncident(); | assertEquals(1, controller.getActiveIncidentCount()); assertTrue(incident.getType() == IncidentType.THEFT \|\| incident.getType() == IncidentType.FIRE); |
| **Caso 5: Asignar un vehículo compatible por ID y resolver el incidente suma puntos y libera el vehículo.** | | |
| Crear controller y generar un incidente. Buscar el ID del vehículo compatible con controller.proposeVehicle(incident). | controller.assignVehicle(vehicleId, incident.getId()); int points = controller.resolveIncident(incident.getId()); | assertTrue(points > 0); assertEquals(points, controller.getScore()); assertEquals(0, controller.getActiveIncidentCount()); assertEquals(VehicleStatus.AVAILABLE, controller.findVehicleById(vehicleId).getStatus()); |
| **Caso 6: Sin incidentes activos, el incidente de mayor prioridad es null (la interfaz no se cierra).** | | |
| Crear controller sin incidentes. | Incident highest = controller.getHighestPriorityIncidentOrNull(); | assertNull(highest); |
| **Caso 7: Buscar un vehículo con un ID que no existe lanza una excepción.** | | |
| Crear controller con los vehículos iniciales. | controller.findVehicleById("X-99"); | assertThrows(IllegalArgumentException.class, () -> controller.findVehicleById("X-99")); |
| **Caso 8: Asignar un vehículo incompatible a un incidente lanza VehicleAssignmentException.** | | |
| Crear controller con incidente de tipo FIRE y una patrulla (PATROL). | controller.assignVehicle("P-01", fire.getId()); | assertThrows(VehicleAssignmentException.class, () -> controller.assignVehicle("P-01", fire.getId())); |
| **Caso 9: Resolver un incidente en estado PENDING (sin vehículo asignado) lanza IncidentStateException.** | | |
| Crear controller y generar incidente pendiente. | controller.resolveIncident(incident.getId()); | assertThrows(IncidentStateException.class, () -> controller.resolveIncident(incident.getId())); |
| **Caso 10: Deshacer varios movimientos los revierte en orden LIFO (último en entrar, primero en salir).** | | |
| Crear controller en (8, 8) y mover UP y RIGHT. | controller.undoOperatorMove(); controller.undoOperatorMove(); | Retorna al operador exactamente a (8, 8) con 0 movimientos restantes. |
| **Caso 11: Guardar y cargar partida en JSON restaura operador, puntaje, vehículos e incidentes.** | | |
| Crear controller, mover operador, generar incidente y guardar en carpeta temporal. | controller.saveGame(dir); restored.loadGame(dir); | assertEquals(pos, restored.pos); assertNotNull(restored.findIncidentById(inc.getId())); |
| **Caso 12: Registro manual de incidente (HU-01) agrega el incidente y rechaza IDs duplicados.** | | |
| Crear controller e incidente "MAN-01". | controller.registerIncident(inc); | assertNotNull(controller.findIncidentById("MAN-01")); assertThrows(DuplicateIncidentException.class, ...); |
| **Caso 13: Búsqueda binaria por ID (RF-3 / HU-06) encuentra el incidente o lanza IncidentNotFoundException.** | | |
| Registrar incidentes "Z-01" y "A-01". | controller.findIncidentById("A-01"); | assertEquals("A-01", found.getId()); assertThrows(IncidentNotFoundException.class, () -> findIncidentById("NO-EXISTE")); |
| **Caso 14: Criterios operativos de tránsito (HU-08..10) y clic en mapa generan accidentes.** | | |
| Crear controller en (8, 8). | triggerCollisionAccident(...), triggerCongestionAccident(...), triggerStoppedVehicleAccident(...), spawnAccidentAt(8, 12). | assertEquals(4, controller.getActiveAccidentCount()); |
| **Caso 15: Liberación manual de vehículo (HU-16) cancela el despacho y devuelve el vehículo a AVAILABLE.** | | |
| Crear incidente, proponer y asignar vehículo. | controller.cancelIncidentDispatch(inc.getId()); | assertEquals(VehicleStatus.AVAILABLE, vehicle.getStatus()); assertEquals(IncidentStatus.PENDING, inc.getStatus()); |
| **Caso 16: Registro de nuevo vehículo lo agrega a la flota y rechaza duplicados.** | | |
| Crear vehículo "A-99" (AMBULANCE). | controller.registerVehicle(newAmbulance); | assertNotNull(controller.findVehicleById("A-99")); assertThrows(IllegalArgumentException.class, ...); |
| **Caso 17: Guardado y carga binaria (.dat) (HU-22) persiste y restaura incidentes serializados.** | | |
| Crear incidente y guardar en archivo `.dat`. | controller.saveGameBinary(dat); restored.loadGameBinary(dat); | assertEquals(1, restored.getActiveIncidentCount()); assertNotNull(restored.findIncidentById(inc.getId())); |
| **Caso 18: getActiveIncidentsTree retorna el BST activo con tamaño y altura consistentes.** | | |
| Registrar 2 incidentes ("INC-A" LOW, "INC-B" HIGH). | BinarySearchTree<Incident> tree = controller.getActiveIncidentsTree(); | assertEquals(2, tree.size()); assertTrue(tree.height() >= 1); assertTrue(tree.contains(inc1)); |
| **Caso 19: isCorridorCell y countCorridorCells detectan las vías arteriales MAIN_ROAD.** | | |
| Crear controller con mapa 64×64. | controller.isCorridorCell(8, 4); controller.countCorridorCells(); | assertTrue(isCorridorCell(8, 4)); assertFalse(isCorridorCell(0, 0)); assertTrue(countCorridorCells() > 0); |
| **Caso 20: Recorrido inOrder del BST organiza los incidentes de mayor a menor urgencia.** | | |
| Registrar incidentes LOW, MEDIUM y HIGH. | LinkedList<Incident> inOrder = controller.getActiveIncidentsTree().inOrder(); | assertEquals(Severity.HIGH, inOrder.get(0).getSeverity()); assertEquals(Severity.LOW, inOrder.get(2).getSeverity()); |
