# Diseño de pruebas unitarias

**Requerimiento funcional / Historia de usuario:** Fin de turno - el turno del operador dura 5 minutos y termina antes si la ciudad se satura con 10 incidentes activos a la vez, Concurrencia - el reloj del turno corre en su propio hilo.

**Nombre del método:** tick(int seconds): void, getRemainingSeconds(): int, formatRemaining(): String, setPaused(boolean paused): void, evaluate(int activeIncidents): ShiftTimer.Result, start(): void, stop(): void

**Objetivo de la Prueba:** Verificar que el reloj del turno (ShiftTimer) empiece en 5:00, descuente los segundos que pasan sin bajar de 0, no avance en pausa, muestre el tiempo como MM:SS, y diga si el turno sigue (PLAYING), se acabó el tiempo (TIME_UP) o se perdió por saturación (OVERLOADED, que tiene prioridad); además, que su hilo avance solo y se detenga.

| Arrange (Inicialización) | Act (Acción) | Assert (Verificar) |
|---|---|---|
| **Caso 1: Un turno nuevo empieza con 300 segundos y se muestra como 05:00.** | | |
| Crear ShiftTimer nuevo. | int remaining = timer.getRemainingSeconds(); String text = timer.formatRemaining(); | assertEquals(300, remaining); assertEquals("05:00", text); |
| **Caso 2: Al pasar 61 segundos quedan 239 y se muestra 03:59.** | | |
| Crear ShiftTimer nuevo. | timer.tick(61); | assertEquals(239, timer.getRemainingSeconds()); assertEquals("03:59", timer.formatRemaining()); |
| **Caso 3: En pausa el reloj no avanza.** | | |
| Crear ShiftTimer y ponerlo en pausa. | timer.tick(30); | assertEquals(300, timer.getRemainingSeconds()); |
| **Caso 4: El tiempo restante nunca baja de 0.** | | |
| Crear ShiftTimer nuevo. | timer.tick(1000); | assertEquals(0, timer.getRemainingSeconds()); |
| **Caso 5: Con tiempo y menos de 10 incidentes el turno sigue.** | | |
| Crear ShiftTimer nuevo. | ShiftTimer.Result result = timer.evaluate(9); | assertEquals(ShiftTimer.Result.PLAYING, result); |
| **Caso 6: Con 10 incidentes activos se pierde por saturación aunque quede tiempo.** | | |
| Crear ShiftTimer nuevo. | ShiftTimer.Result result = timer.evaluate(10); | assertEquals(ShiftTimer.Result.OVERLOADED, result); |
| **Caso 7: Sin tiempo y sin saturación el turno termina por tiempo.** | | |
| Crear ShiftTimer y pasar 300 segundos. | ShiftTimer.Result result = timer.evaluate(3); | assertEquals(ShiftTimer.Result.TIME_UP, result); |
| **Caso 8: El hilo del reloj avanza solo y se detiene con stop.** | | |
| Crear ShiftTimer con un paso de 20 ms y un CountDownLatch de 3 que se descuenta en cada tic. | timer.start(); boolean ticked = latch.await(2, TimeUnit.SECONDS); timer.stop(); | assertTrue(ticked); assertTrue(timer.getRemainingSeconds() <= 297); assertFalse(timer.isRunning()); |
