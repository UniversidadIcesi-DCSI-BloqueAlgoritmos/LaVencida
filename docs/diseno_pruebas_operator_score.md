# Diseño de pruebas unitarias

**Requerimiento funcional / Historia de usuario:** RF-14 - Sistema de puntuación para el operador: otorgar puntos por cada incidente atendido según su gravedad (100 para alta, 70 para media y 40 para baja) y una bonificación adicional de 20 puntos si se resuelve dentro del tiempo esperado.

**Nombre del método:** getScore(): int, addScore(int points): void, calculateScore(Incident incident, LocalDateTime resolvedAt): int, finishAttention(Incident incident, Operator operator, LocalDateTime resolvedAt): int

**Objetivo de la Prueba:** Verificar que el operador inicie con puntaje 0, acumule puntos correctamente al llamar addScore, que calculateScore otorgue los puntos base según la gravedad del incidente y sume la bonificación de 20 puntos únicamente cuando el tiempo de atención no supere el umbral permitido, y que finishAttention actualice tanto el estado del incidente y del vehículo como el puntaje acumulado del operador.

| Arrange (Inicialización) | Act (Acción) | Assert (Verificar) |
|---|---|---|
| **Caso 1: El puntaje inicial del operador debe ser cero.** | | |
| — | Operator operator1 = new Operator(0, 0); | assertEquals(0, operator1.getScore()); |
| **Caso 2: Agregar puntos incrementa el puntaje acumulado del operador.** | | |
| Crear operator1 en (0, 0). | operator1.addScore(100); operator1.addScore(20); | assertEquals(120, operator1.getScore()); |
| **Caso 3: calculateScore otorga el puntaje base según la gravedad (100 alta, 70 media, 40 baja).** | | |
| Definir fecha base y resolvedLate (20 minutos después, fuera del tiempo de bonificación). Crear tres incidentes: high (Severity.HIGH), med (Severity.MEDIUM) y low (Severity.LOW) con fecha base. | int highScore = manager.calculateScore(high, resolvedLate); int medScore = manager.calculateScore(med, resolvedLate); int lowScore = manager.calculateScore(low, resolvedLate); | assertEquals(100, highScore); assertEquals(70, medScore); assertEquals(40, lowScore); |
| **Caso 4: calculateScore suma bonificación de 20 puntos si se atiende dentro del tiempo límite.** | | |
| Definir fecha base y resolvedOnTime (2 minutos después, dentro del límite de 5 minutos para gravedad alta). Crear incidente high (Severity.HIGH) con fecha base. | int score = manager.calculateScore(high, resolvedOnTime); | assertEquals(120, score); |
| **Caso 5: calculateScore no suma bonificación si se supera el tiempo límite.** | | |
| Definir fecha base y resolvedLate (6 minutos después, superando el límite de 5 minutos para gravedad alta). Crear incidente high (Severity.HIGH) con fecha base. | int score = manager.calculateScore(high, resolvedLate); | assertEquals(100, score); |
| **Caso 6: finishAttention con Operador calcula puntos y actualiza el puntaje del operador.** | | |
| Crear incident (Severity.MEDIUM, fecha base), vehicle (PATROL) y operator en (0, 0). Registrar el incidente y asignarle el vehículo. Definir resolved = base.plusMinutes(2). | int points = manager.finishAttention(incident, operator, resolved); | assertEquals(90, points); assertEquals(90, operator.getScore()); assertEquals(IncidentStatus.RESOLVED, incident.getStatus()); assertEquals(VehicleStatus.AVAILABLE, vehicle.getStatus()); assertEquals(resolved, incident.getResolvedAt()); |
| **Caso 7: finishAttention con Operador acumula el puntaje tras resolver múltiples incidentes.** | | |
| Crear operator en (0, 0), incident1 (Severity.HIGH) con vehicle1 (PATROL) e incident2 (Severity.LOW) con vehicle2 (AMBULANCE). Registrar ambos incidentes y asignar sus vehículos. | manager.finishAttention(incident1, operator, base.plusMinutes(2)); manager.finishAttention(incident2, operator, base.plusMinutes(20)); | assertEquals(160, operator.getScore()); |
