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
    public static final double FAST_FACTOR = 0.6;
    public static final long FAST_MIN_INTERVAL_MILLIS = 3000;

    private GameController controller;
    private Consumer<Incident> onSpawn;
    private long fixedInterval; // si es mayor que 0 se usa siempre este intervalo (sirve para las pruebas)
    // volatile: estas banderas las leen dos hilos (el del juego y el del generador)
    private volatile boolean paused;
    private volatile boolean running;
    private volatile boolean fastMode;
    private Thread thread;

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
        long levels = Math.max(0, score) / POINTS_PER_LEVEL;
        long interval = BASE_INTERVAL_MILLIS - levels * STEP_MILLIS;
        return Math.max(MIN_INTERVAL_MILLIS, interval);
    }

    // Intervalo con la velocidad RAPIDA: 40% menos de espera, pero nunca menos de 3 segundos.
    public static long intervalFor(int score, boolean fast) {
        long interval = intervalForScore(score);
        if (!fast) {
            return interval;
        }
        return Math.max(FAST_MIN_INTERVAL_MILLIS, Math.round(interval * FAST_FACTOR));
    }

    public void setFastMode(boolean fastMode) {
        this.fastMode = fastMode;
    }

    // Genera un incidente ahora (si no esta en pausa) y avisa.
    public Incident spawnOnce() {
        if (paused) {
            return null;
        }
        Incident incident = controller.spawnRandomIncident();
        onSpawn.accept(incident);
        return incident;
    }

    public void setPaused(boolean paused) {
        this.paused = paused;
    }

    public boolean isPaused() {
        return paused;
    }

    // Arranca el hilo del generador (si ya estaba corriendo no hace nada).
    public synchronized void start() {
        if (running) {
            return;
        }
        running = true;
        thread = new Thread(this, "generador-incidentes");
        thread.setDaemon(true); // asi no deja el programa abierto al cerrar la ventana
        thread.start();
    }

    // Detiene el hilo del generador y espera a que termine.
    public synchronized void stop() {
        running = false;
        if (thread != null) {
            thread.interrupt();
            try {
                thread.join(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            thread = null;
        }
    }

    public boolean isRunning() {
        return running;
    }

    // Lo que hace el hilo: esperar, generar un incidente y repetir hasta que lo detengan.
    @Override
    public void run() {
        while (running) {
            long wait = fixedInterval > 0 ? fixedInterval : intervalFor(controller.getScore(), fastMode);
            try {
                Thread.sleep(wait);
            } catch (InterruptedException e) {
                // nos despertaron para detenernos
                break;
            }
            if (running) {
                spawnOnce();
            }
        }
        running = false;
    }
}
