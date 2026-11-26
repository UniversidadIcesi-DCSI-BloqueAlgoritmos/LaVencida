# Diseño de pruebas unitarias

**Requerimiento funcional / Historia de usuario:** RF-15 - Guardar y cargar el estado del sistema en JSON, ahora desde la interfaz gráfica con F5 (guardar) y F9 (cargar).

**Nombre del método:** JsonPersistence.saveOperator(Operator, String): void, JsonPersistence.loadOperator(String): Operator, JsonPersistence.loadIncidents(String, LinkedList<Vehicle>): LinkedList<Incident>, IncidentGenerator.registerExistingId(String): void, GameController.saveGame(String): void, GameController.loadGame(String): void

**Objetivo de la Prueba:** Verificar que la partida completa se pueda guardar y volver a cargar: el operador conserva posición y puntaje, los incidentes en proceso vuelven a quedar unidos a su vehículo, y después de cargar los incidentes nuevos no repiten ids.

| Arrange (Inicialización) | Act (Acción) | Assert (Verificar) |
|---|---|---|
| **Caso 1: Guardar y cargar el operador conserva fila, columna y puntaje.** | | |
| Crear un Operator en (8, 10) con 340 puntos. | jsonPersistence.saveOperator(operator, ruta); Operator loaded = jsonPersistence.loadOperator(ruta); | assertEquals(8, loaded.getRow()); assertEquals(10, loaded.getColumn()); assertEquals(340, loaded.getScore()); |
| **Caso 2: Un incidente en proceso se carga unido a su vehículo.** | | |
| Crear la patrulla P-01 asignada al robo THEFT-001 (IN_PROGRESS) y guardar ambos en JSON. | LinkedList<Vehicle> vehicles = loadVehicles(...); LinkedList<Incident> incidents = loadIncidents(ruta, vehicles); | assertEquals("P-01", incidents.get(0).getAssignedVehicle().getId()); assertSame(incidents.get(0), vehicles.get(0).getAssignedIncident()); |
| **Caso 3: Guardar y cargar la partida en el controlador conserva puntaje, incidentes activos y estado de los vehículos.** | | |
| Crear controller, generar dos incidentes, asignar una unidad al primero y sumar 120 puntos; guardar en una carpeta temporal. | GameController other = new GameController(...); other.loadGame(carpeta); | assertEquals(120, other.getScore()); assertEquals(2, other.getActiveIncidentCount()); assertEquals(VehicleStatus.EN_ROUTE, other.findVehicleById(idAsignado).getStatus()); |
| **Caso 4: Después de cargar, un incidente nuevo no repite el id de los cargados.** | | |
| Igual que el caso 3 con la partida ya cargada. | Incident nuevo = other.spawnRandomIncident(); | assertEquals(3, other.getActiveIncidentCount()); (no lanza DuplicateIncidentException) |
