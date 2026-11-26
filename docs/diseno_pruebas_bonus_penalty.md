# Diseño de pruebas unitarias

**Requerimiento funcional / Historia de usuario:** RF-8 - Finalizar la atención y sumar puntos, Bonus por rapidez - +50 puntos si el incidente se resuelve en menos de 30 segundos desde que llegó la alerta, Penalización por espera - -25 puntos por cada 30 segundos que un incidente de gravedad ALTA sigue sin unidad asignada.

**Nombre del método:** resolveIncident(String incidentId, LocalDateTime now): int, wasLastResolveFast(): boolean, applyWaitPenalties(LocalDateTime now): int

**Objetivo de la Prueba:** Verificar que el controlador sume el bonus de rapidez solo cuando el incidente se resuelve antes de 30 segundos, que la penalización se cobre una sola vez por cada bloque de 30 segundos de espera, solo a incidentes ALTOS pendientes, y que el puntaje nunca quede negativo.

| Arrange (Inicialización) | Act (Acción) | Assert (Verificar) |
|---|---|---|
| **Caso 1: Resolver 10 segundos después de la alerta suma el bonus de 50.** | | |
| Crear controller, generar un incidente, asignarle la unidad propuesta. Guardar los puntos normales con IncidentManager.calculateScore. | int points = controller.resolveIncident(id, generatedAt.plusSeconds(10)); | assertEquals(normal + 50, points); assertTrue(controller.wasLastResolveFast()); assertEquals(points, controller.getScore()); |
| **Caso 2: Resolver 45 segundos después no da bonus.** | | |
| Igual que el caso 1. | int points = controller.resolveIncident(id, generatedAt.plusSeconds(45)); | assertEquals(normal, points); assertFalse(controller.wasLastResolveFast()); |
| **Caso 3: Un incidente ALTO pendiente 65 segundos cobra dos bloques de 25.** | | |
| Crear controller con 200 puntos y registrar un incidente ALTO pendiente. | int penalty = controller.applyWaitPenalties(generatedAt.plusSeconds(65)); | assertEquals(50, penalty); assertEquals(150, controller.getScore()); |
| **Caso 4: El mismo bloque de espera no se cobra dos veces.** | | |
| Igual que el caso 3, ya aplicado a los 65 segundos. | int penalty = controller.applyWaitPenalties(generatedAt.plusSeconds(80)); | assertEquals(0, penalty); assertEquals(150, controller.getScore()); |
| **Caso 5: Los incidentes que no son ALTOS o ya tienen unidad no se penalizan.** | | |
| Crear controller con 200 puntos, un incidente MEDIO pendiente y uno ALTO ya asignado. | int penalty = controller.applyWaitPenalties(generatedAt.plusSeconds(120)); | assertEquals(0, penalty); assertEquals(200, controller.getScore()); |
| **Caso 6: La penalización nunca deja el puntaje negativo.** | | |
| Crear controller con 10 puntos y un incidente ALTO pendiente. | int penalty = controller.applyWaitPenalties(generatedAt.plusSeconds(95)); | assertEquals(10, penalty); assertEquals(0, controller.getScore()); |
