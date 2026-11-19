package com.icesi.ui;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineEvent;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

// Sonidos de 8 bits hechos con codigo (ondas cuadradas), sin archivos de audio.
// Cada sonido es una lista de notas: frecuencia en Hz y duracion en milisegundos.
public final class RetroSound {

    public enum Effect { MENU, START, ALERT, DISPATCH, RESOLVE, ERROR }

    private static final float SAMPLE_RATE = 22050;
    private static final AudioFormat FORMAT = new AudioFormat(SAMPLE_RATE, 8, 1, true, false);
    private static final double VOLUME = 0.18;

    // los sonidos se tocan en un hilo aparte para que la interfaz no se congele
    private static final ExecutorService PLAYER = Executors.newSingleThreadExecutor(runnable -> {
        Thread thread = new Thread(runnable, "retro-sound");
        thread.setDaemon(true);
        return thread;
    });

    private static boolean muted;
    private static boolean available = true; // si el PC no tiene audio se apaga sin romper el juego

    private RetroSound() {
    }

    // Toca el efecto (si no esta silenciado).
    public static void play(Effect effect) {
        if (muted || !available) {
            return;
        }
        byte[] data = build(notesOf(effect));
        PLAYER.submit(() -> playData(data));
    }

    public static boolean isMuted() {
        return muted;
    }

    // Prende o apaga el sonido; devuelve como quedo.
    public static boolean toggleMute() {
        muted = !muted;
        return muted;
    }

    // Notas de cada efecto: {frecuencia, duracion}. Frecuencia 0 es silencio.
    private static int[][] notesOf(Effect effect) {
        switch (effect) {
            case MENU:
                return new int[][]{{880, 40}};
            case START:
                return new int[][]{{523, 90}, {659, 90}, {784, 90}, {1047, 220}};
            case ALERT:
                return new int[][]{{988, 110}, {740, 110}, {988, 110}, {740, 110}};
            case DISPATCH:
                return new int[][]{{392, 60}, {523, 60}, {659, 60}, {784, 120}};
            case RESOLVE:
                return new int[][]{{988, 70}, {1319, 200}};
            default: // ERROR
                return new int[][]{{196, 120}, {0, 40}, {147, 200}};
        }
    }

    // Convierte las notas en muestras de audio de onda cuadrada (el sonido tipico de las consolas viejas).
    private static byte[] build(int[][] notes) {
        int total = 0;
        for (int[] note : notes) {
            total += (int) (SAMPLE_RATE * note[1] / 1000);
        }
        byte[] data = new byte[total];
        int index = 0;
        for (int[] note : notes) {
            int samples = (int) (SAMPLE_RATE * note[1] / 1000);
            for (int i = 0; i < samples; i++) {
                double value = 0;
                if (note[0] > 0) {
                    double cycle = (i * note[0] / SAMPLE_RATE) % 1.0;
                    value = cycle < 0.5 ? 1 : -1;
                    // al final de cada nota baja el volumen para que no suene un "clic"
                    double fade = Math.min(1, (samples - i) / (SAMPLE_RATE * 0.01));
                    value *= fade;
                }
                data[index++] = (byte) (value * VOLUME * 127);
            }
        }
        return data;
    }

    private static void playData(byte[] data) {
        try {
            Clip clip = AudioSystem.getClip();
            clip.open(FORMAT, data, 0, data.length);
            // cuando termina de sonar se cierra para liberar el audio
            clip.addLineListener(event -> {
                if (event.getType() == LineEvent.Type.STOP) {
                    clip.close();
                }
            });
            clip.start();
        } catch (Exception e) {
            available = false;
        }
    }
}
