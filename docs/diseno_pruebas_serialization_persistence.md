# Diseño de pruebas unitarias

**Requerimiento funcional / Historia de usuario:** RF-16 - Serialización del estado de la simulación y RF-18 - Manejo de excepciones de persistencia: permitir serializar y deserializar objetos del sistema (`IncidentManager`, `Incident`, `Vehicle`, `LinkedList`, `Node`) en archivos binarios para guardar y restaurar el estado completo de la ejecución, lanzando `PersistenceException` ante errores de entrada/salida o archivos corruptos.

**Nombre del método:** saveManagerState(IncidentManager manager, Path filePath): void, loadManagerState(Path filePath): IncidentManager

**Objetivo de la Prueba:** Verificar que `SerializationPersistence` serialice y restaure el grafo completo de objetos de `IncidentManager` (incluyendo sus incidentes activos, vehículos asignados e indicadores en tiempo real) en un archivo binario `.dat`, y que lance `PersistenceException` cuando el archivo no exista, esté corrupto o se reciba un estado nulo.

| Arrange (Inicialización) | Act (Acción) | Assert (Verificar) |
|---|---|---|
| **Caso 1: Serializar y deserializar `IncidentManager` restaura todos sus incidentes, vehículos asignados e indicadores.** | | |
| Crear `manager = new IncidentManager()`, registrar `inc1` (`FIRE`, `HIGH`) e `inc2` (`THEFT`, `MEDIUM`), y asignar `fireTruck` (`F-01`) a `inc1`. Definir `filePath = tempDir.resolve("state.dat")`. | `persistence.saveManagerState(manager, filePath); IncidentManager restored = persistence.loadManagerState(filePath);` | `assertNotNull(restored); assertEquals(2, restored.getIncidentCount()); assertEquals(2, restored.getActiveIncidentCount()); assertEquals(IncidentStatus.IN_PROGRESS, restored.findIncidentById("INC-01").getStatus()); assertEquals("F-01", restored.findIncidentById("INC-01").getAssignedVehicle().getId());` |
| **Caso 2: Deserializar desde un archivo inexistente lanza `PersistenceException`.** | | |
| Instanciar `persistence = new SerializationPersistence()` y definir `missingFile = tempDir.resolve("missing.dat")`. | `persistence.loadManagerState(missingFile);` | `assertThrows(PersistenceException.class, () -> persistence.loadManagerState(missingFile));` |
| **Caso 3: Deserializar desde un archivo binario corrupto lanza `PersistenceException`.** | | |
| Instanciar `persistence = new SerializationPersistence()`, crear `corruptedFile = tempDir.resolve("corrupted.dat")` y escribir bytes no válidos `"not a valid serialized object stream"`. | `persistence.loadManagerState(corruptedFile);` | `assertThrows(PersistenceException.class, () -> persistence.loadManagerState(corruptedFile));` |
| **Caso 4: Intentar serializar un `IncidentManager` nulo o una ruta nula lanza `PersistenceException`.** | | |
| Instanciar `persistence = new SerializationPersistence()` y definir `filePath = tempDir.resolve("null_state.dat")`. | `persistence.saveManagerState(null, filePath); persistence.saveManagerState(new IncidentManager(), null);` | `assertThrows(PersistenceException.class, () -> persistence.saveManagerState(null, filePath)); assertThrows(PersistenceException.class, () -> persistence.saveManagerState(new IncidentManager(), null));` |
