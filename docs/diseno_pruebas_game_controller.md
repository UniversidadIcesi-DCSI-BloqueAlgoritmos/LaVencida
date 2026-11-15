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
