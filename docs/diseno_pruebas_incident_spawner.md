# Diseño de pruebas unitarias

**Requerimiento funcional / Historia de usuario:** RF-4 - Generar incidentes de forma automática, Concurrencia - la generación de incidentes corre en un hilo aparte para que la interfaz gráfica no se congele, Dificultad progresiva - los incidentes salen más seguido a medida que el operador gana puntos.

**Nombre del método:** intervalForScore(int score): long, spawnOnce(): Incident, setPaused(boolean paused): void, start(): void, stop(): void, isRunning(): boolean

**Objetivo de la Prueba:** Verificar que el generador automático (IncidentSpawner) calcule el tiempo de espera según el puntaje (12 s al inicio, 1 s menos por cada 300 puntos y nunca menos de 5 s), que cada generación registre un incidente activo y avise a la interfaz, que en pausa no genere nada, y que su hilo arranque, produzca incidentes por sí solo y se detenga sin dejar el programa colgado.

| Arrange (Inicialización) | Act (Acción) | Assert (Verificar) |
|---|---|---|
| **Caso 1: Con 0 puntos el intervalo es el inicial de 12 segundos.** | | |
| Ninguna (método estático). | long interval = IncidentSpawner.intervalForScore(0); | assertEquals(12000, interval); |
| **Caso 2: Cada 300 puntos el intervalo baja 1 segundo.** | | |
| Ninguna. | long interval = IncidentSpawner.intervalForScore(650); | assertEquals(10000, interval); |
| **Caso 3: El intervalo nunca baja de 5 segundos aunque el puntaje sea muy alto.** | | |
| Ninguna. | long interval = IncidentSpawner.intervalForScore(100000); | assertEquals(5000, interval); |
| **Caso 4: spawnOnce registra un incidente activo y avisa a quien escucha.** | | |
| Crear un GameController con semilla fija y un IncidentSpawner cuyo aviso guarda el incidente recibido. | Incident incident = spawner.spawnOnce(); | assertNotNull(incident); assertEquals(1, controller.getActiveIncidentCount()); assertSame(incident, recibido); |
| **Caso 5: En pausa spawnOnce no genera nada.** | | |
| Crear el spawner y ponerlo en pausa con setPaused(true). | Incident incident = spawner.spawnOnce(); | assertNull(incident); assertEquals(0, controller.getActiveIncidentCount()); |
| **Caso 6: El hilo genera incidentes por sí solo y se detiene con stop.** | | |
| Crear el spawner con un intervalo fijo de 50 ms y un CountDownLatch de 2 que se descuenta en cada aviso. | spawner.start(); boolean generated = latch.await(2, TimeUnit.SECONDS); spawner.stop(); | assertTrue(generated); assertTrue(controller.getActiveIncidentCount() >= 2); assertFalse(spawner.isRunning()); |
