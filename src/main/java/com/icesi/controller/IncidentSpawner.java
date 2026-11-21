package com.icesi.controller;

import com.icesi.model.Incident;

import java.util.function.Consumer;

// Generador automatico de incidentes: corre en su propio hilo para que la interfaz no se congele
// y cada vez espera menos entre incidente e incidente segun el puntaje (dificultad progresiva).
public class IncidentSpawner implements Runnable {

    public static final long BASE_INTERVAL_MILLIS = 12000;
    public static final long MIN_INTERVAL_MILLIS = 5000;
    public static final int POINTS_PER_LEVEL = 300;
    public static final long STEP_MILLIS = 1000;

    private GameController controller;
    private Consumer<Incident> onSpawn;
    private long fixedInterval; // si es mayor que 0 se usa siempre este intervalo (sirve para las pruebas)

    // Crea el generador; onSpawn recibe cada incidente nuevo.
    public IncidentSpawner(GameController controller, Consumer<Incident> onSpawn) {
        this(controller, onSpawn, 0);
    }

    // Crea el generador con un intervalo fijo en milisegundos.
    public IncidentSpawner(GameController controller, Consumer<Incident> onSpawn, long fixedInterval) {
        this.controller = controller;
        this.onSpawn = onSpawn;
        this.fixedInterval = fixedInterval;
    }

    // Calcula cuanto se espera entre incidentes con ese puntaje.
    public static long intervalForScore(int score) {
        return 0;
    }

    // Genera un incidente ahora (si no esta en pausa) y avisa.
    public Incident spawnOnce() {
        return null;
    }

    public void setPaused(boolean paused) {
    }

    public boolean isPaused() {
        return false;
    }

    // Arranca el hilo del generador.
    public void start() {
    }

    // Detiene el hilo del generador.
    public void stop() {
    }

    public boolean isRunning() {
        return false;
    }

    @Override
    public void run() {
    }
}
