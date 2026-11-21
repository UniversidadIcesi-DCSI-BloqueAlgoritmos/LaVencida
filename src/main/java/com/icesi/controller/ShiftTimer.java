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

    // Crea el reloj; onTick se llama cada vez que pasa un segundo.
    public ShiftTimer(Runnable onTick) {
        this(onTick, 1000);
    }

    public ShiftTimer(Runnable onTick, long stepMillis) {
        this.onTick = onTick;
        this.stepMillis = stepMillis;
    }

    // Descuenta segundos del turno.
    public void tick(int seconds) {
    }

    public int getRemainingSeconds() {
        return 0;
    }

    // Tiempo restante como MM:SS.
    public String formatRemaining() {
        return "";
    }

    public void setPaused(boolean paused) {
    }

    // Dice como va el turno segun el tiempo y los incidentes activos.
    public Result evaluate(int activeIncidents) {
        return null;
    }

    public void start() {
    }

    public void stop() {
    }

    public boolean isRunning() {
        return false;
    }

    @Override
    public void run() {
    }
}
