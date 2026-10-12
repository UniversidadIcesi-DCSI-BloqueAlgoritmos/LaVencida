# Diseño de pruebas unitarias

**Requerimiento funcional / Historia de usuario:** RF-13 - Indicadores en tiempo real: mostrar y consultar la cantidad de incidentes activos (totales y clasificados por accidente, robo e incendio) y la cantidad de vehículos disponibles.

**Nombre del método:** getActiveIncidentCount(): int, getActiveAccidentCount(): int, getActiveTheftCount(): int, getActiveFireCount(): int, countAvailableVehicles(LinkedList<Vehicle> vehicles): int

**Objetivo de la Prueba:** Verificar que los métodos de indicadores cuenten únicamente los incidentes activos (en estado PENDING o IN_PROGRESS), excluyendo los incidentes ya resueltos (RESOLVED), tanto en el conteo general como por cada tipo de incidente, y que countAvailableVehicles cuente solo los vehículos en estado AVAILABLE.

| Arrange (Inicialización) | Act (Acción) | Assert (Verificar) |
|---|---|---|
| **Caso 1: getActiveIncidentCount cuenta solo incidentes en estado PENDING o IN_PROGRESS, ignorando los RESOLVED.** | | |
| Registrar tres incidentes: inc1 (ACCIDENT, queda PENDING), inc2 (THEFT, se le asigna patrol y queda IN_PROGRESS) e inc3 (FIRE, se le asigna fireTruck y se finaliza con finishAttention quedando RESOLVED). | int activeCount = manager.getActiveIncidentCount(); | assertEquals(2, activeCount); |
| **Caso 2: Los contadores por tipo separan correctamente accidentes, robos e incendios activos.** | | |
| Registrar dos accidentes activos (A-01, A-02), un robo activo (T-01) y dos incendios (F-01 y F-02). Asignar vehículo y finalizar la atención de F-01 para que quede RESOLVED. | int accidents = manager.getActiveAccidentCount(); int thefts = manager.getActiveTheftCount(); int fires = manager.getActiveFireCount(); | assertEquals(2, accidents); assertEquals(1, thefts); assertEquals(1, fires); |
| **Caso 3: countAvailableVehicles cuenta únicamente los vehículos en estado AVAILABLE.** | | |
| Crear una lista fleet con tres vehículos: v1 (AVAILABLE), v2 (EN_ROUTE) y v3 (AVAILABLE). | int available = manager.countAvailableVehicles(fleet); | assertEquals(2, available); |
