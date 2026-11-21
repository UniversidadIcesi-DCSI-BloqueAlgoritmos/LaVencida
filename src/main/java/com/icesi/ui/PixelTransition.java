package com.icesi.ui;

import javafx.animation.AnimationTimer;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.StackPane;

// Transicion estilo videojuego: la pantalla se llena de cuadros negros en desorden,
// se cambia lo que hay debajo y luego los cuadros se van quitando hasta mostrar lo nuevo.
public class PixelTransition {

    private static final double BLOCK = 32;          // tamano de cada cuadro
    private static final double HALF_SECONDS = 0.45; // lo que dura tapar (y lo mismo destapar)

    // orden en que aparecen los cuadros (matriz de Bayer 4x4), asi se ve como un "dithering"
    private static final int[][] ORDER = {{0, 8, 2, 10}, {12, 4, 14, 6}, {3, 11, 1, 9}, {15, 7, 13, 5}};

    private PixelTransition() {
    }

    // Hace la transicion encima de root; a la mitad (pantalla toda negra) ejecuta middle.
    public static void play(StackPane root, Runnable middle) {
        Canvas canvas = new Canvas(root.getWidth(), root.getHeight());
        canvas.setMouseTransparent(true);
        root.getChildren().add(canvas);

        AnimationTimer timer = new AnimationTimer() {
            private long start = -1;
            private boolean switched;

            @Override
            public void handle(long now) {
                if (start < 0) {
                    start = now;
                }
                double seconds = (now - start) / 1_000_000_000.0;

                // primera mitad: tapar (0 -> 1); segunda mitad: destapar (1 -> 0)
                double cover;
                if (seconds < HALF_SECONDS) {
                    cover = seconds / HALF_SECONDS;
                } else {
                    if (!switched) {
                        switched = true;
                        middle.run();
                    }
                    cover = 1 - (seconds - HALF_SECONDS) / HALF_SECONDS;
                }

                if (cover <= 0 && switched) {
                    stop();
                    root.getChildren().remove(canvas);
                    return;
                }
                drawBlocks(canvas, Math.max(0, Math.min(1, cover)));
            }
        };
        timer.start();
    }

    // Dibuja los cuadros negros: cover dice que fraccion de la pantalla debe quedar tapada.
    private static void drawBlocks(Canvas canvas, double cover) {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        gc.setFill(RetroTheme.BLACK);
        int columns = (int) Math.ceil(canvas.getWidth() / BLOCK);
        int rows = (int) Math.ceil(canvas.getHeight() / BLOCK);
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < columns; col++) {
                double threshold = (ORDER[row % 4][col % 4] + 0.5) / 16.0;
                if (threshold < cover) {
                    gc.fillRect(col * BLOCK, row * BLOCK, BLOCK, BLOCK);
                }
            }
        }
    }
}
