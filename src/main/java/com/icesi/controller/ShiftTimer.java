package com.icesi.controller;

// Reloj del turno del operador: dura 5 minutos y corre en su propio hilo.
// Tambien dice si el turno sigue, si se acabo el tiempo o si la ciudad se saturo de incidentes.
public class ShiftTimer implements Runnable {

    public static final int SHIFT_SECONDS = 300;
    public static final int MAX_ACTIVE_INCIDENTS = 10;

    // Como va el turno.
    public enum Result {
        PLAYING, TIME_UP, OVERLOADED
    }

    private Runnable onTick;
    private long stepMillis; // cada cuanto avanza un segundo (1000 en el juego, menos en las pruebas)
    // el hilo del reloj escribe y la interfaz lee: por eso volatile y metodos synchronized
    private int elapsedSeconds;
    private volatile boolean paused;
    private volatile boolean running;
    private Thread thread;

    // Crea el reloj; onTick se llama cada vez que pasa un segundo.
    public ShiftTimer(Runnable onTick) {
        this(onTick, 1000);
    }

    public ShiftTimer(Runnable onTick, long stepMillis) {
        this.onTick = onTick;
        this.stepMillis = stepMillis;
    }

    // Descuenta segundos del turno.
    public synchronized void tick(int seconds) {
        if (paused || seconds <= 0) {
            return;
        }
        elapsedSeconds = Math.min(SHIFT_SECONDS, elapsedSeconds + seconds);
    }

    public synchronized int getRemainingSeconds() {
        return SHIFT_SECONDS - elapsedSeconds;
    }

    // Tiempo restante como MM:SS.
    public String formatRemaining() {
        int remaining = getRemainingSeconds();
        return String.format("%02d:%02d", remaining / 60, remaining % 60);
    }

    public void setPaused(boolean paused) {
        this.paused = paused;
    }

    // Dice como va el turno segun el tiempo y los incidentes activos.
    public Result evaluate(int activeIncidents) {
        // perder por saturacion pesa mas que acabar el tiempo
        if (activeIncidents >= MAX_ACTIVE_INCIDENTS) {
            return Result.OVERLOADED;
        }
        if (getRemainingSeconds() == 0) {
            return Result.TIME_UP;
        }
        return Result.PLAYING;
    }

    // Arranca el hilo del reloj (si ya estaba corriendo no hace nada).
    public synchronized void start() {
        if (running) {
            return;
        }
        running = true;
        thread = new Thread(this, "reloj-turno");
        thread.setDaemon(true);
        thread.start();
    }

    // Detiene el hilo del reloj y espera a que termine.
    public void stop() {
        running = false;
        Thread current = thread;
        if (current != null) {
            current.interrupt();
            try {
                current.join(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            thread = null;
        }
    }

    public boolean isRunning() {
        return running;
    }

    // Lo que hace el hilo: esperar un segundo, descontarlo y avisar, hasta que se acabe el tiempo o lo detengan.
    @Override
    public void run() {
        while (running) {
            try {
                Thread.sleep(stepMillis);
            } catch (InterruptedException e) {
                break;
            }
            if (!paused) {
                tick(1);
                onTick.run();
            }
            if (getRemainingSeconds() == 0) {
                break;
            }
        }
        running = false;
    }
}
