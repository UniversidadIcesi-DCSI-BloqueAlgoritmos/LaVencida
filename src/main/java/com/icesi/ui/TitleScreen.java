package com.icesi.ui;

import javafx.animation.AnimationTimer;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;

import java.util.Random;

// Pantalla de titulo animada: ciudad pixelada de noche, estrellas que titilan y "PRESIONA START" parpadeando.
public class TitleScreen {

    private static final double PIXEL = 4;

    private Pane root;
    private Canvas canvas;
    private AnimationTimer timer;
    private double[] starX;
    private double[] starY;
    private int[] buildingHeights;

    // Crea la pantalla y prepara las estrellas y los edificios (siempre los mismos gracias a la semilla).
    public TitleScreen() {
        canvas = new Canvas();
        root = new Pane(canvas);
        root.setStyle("-fx-background-color: #000000;");
        canvas.widthProperty().bind(root.widthProperty());
        canvas.heightProperty().bind(root.heightProperty());

        Random random = new Random(7);
        starX = new double[90];
        starY = new double[90];
        for (int i = 0; i < starX.length; i++) {
            starX[i] = random.nextDouble();
            starY[i] = random.nextDouble() * 0.55;
        }
        buildingHeights = new int[40];
        for (int i = 0; i < buildingHeights.length; i++) {
            buildingHeights[i] = 18 + random.nextInt(34);
        }

        timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                draw(now / 1_000_000_000.0);
            }
        };
    }

    public Node getView() {
        return root;
    }

    // Empieza la animacion.
    public void start() {
        timer.start();
    }

    // Detiene la animacion (cuando el jugador ya entro al juego).
    public void stop() {
        timer.stop();
    }

    // Dibuja un cuadro de la animacion; time son los segundos que han pasado.
    private void draw(double time) {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        double width = canvas.getWidth();
        double height = canvas.getHeight();
        if (width <= 0 || height <= 0) {
            return;
        }

        // cielo en franjas, como los degradados de 16 bits
        Color[] sky = {RetroTheme.BLACK, Color.web("#0b1230"), RetroTheme.NIGHT, Color.web("#3b2a5e"), RetroTheme.PURPLE};
        double band = height * 0.7 / sky.length;
        for (int i = 0; i < sky.length; i++) {
            gc.setFill(sky[i]);
            gc.fillRect(0, i * band, width, band + 1);
        }

        // estrellas que titilan a destiempo
        for (int i = 0; i < starX.length; i++) {
            boolean on = Math.sin(time * 3 + i * 1.7) > -0.3;
            gc.setFill(on ? RetroTheme.CREAM : RetroTheme.LAVENDER);
            double size = (i % 5 == 0) ? PIXEL * 1.5 : PIXEL;
            gc.fillRect(snap(starX[i] * width), snap(starY[i] * height), size, size);
        }

        // luna
        gc.setFill(RetroTheme.PEACH);
        gc.fillRect(snap(width * 0.82), snap(height * 0.1), PIXEL * 14, PIXEL * 14);
        gc.setFill(Color.web("#e8b896"));
        gc.fillRect(snap(width * 0.82) + PIXEL * 3, snap(height * 0.1) + PIXEL * 4, PIXEL * 3, PIXEL * 3);
        gc.fillRect(snap(width * 0.82) + PIXEL * 8, snap(height * 0.1) + PIXEL * 9, PIXEL * 2, PIXEL * 2);

        drawSkyline(gc, width, height, time);

        // titulo con un pequeno rebote
        double bounce = Math.round(Math.sin(time * 2) * 2) * PIXEL;
        double titlePixel = Math.max(8, Math.floor(width / 70));
        String title = "SGMMS";
        double titleX = snap((width - PixelFont.width(title, titlePixel)) / 2);
        double titleY = snap(height * 0.18) + bounce;
        PixelFont.drawWithShadow(gc, title, titleX, titleY, titlePixel, RetroTheme.YELLOW, RetroTheme.PURPLE);

        drawCentered(gc, "CENTRO DE MONITOREO", height * 0.18 + titlePixel * 9, 4, RetroTheme.CREAM, width);
        drawCentered(gc, "PALMIRA - VALLE DEL CAUCA", height * 0.18 + titlePixel * 9 + 44, 3, RetroTheme.PINK, width);

        // "PRESIONA START" prende y apaga cada medio segundo
        if ((int) (time * 2) % 2 == 0) {
            drawCentered(gc, "> PRESIONA START <", height * 0.62, 5, RetroTheme.CREAM, width);
        }
        drawCentered(gc, "ENTER / ESPACIO / CLIC", height * 0.62 + 52, 2, RetroTheme.LIGHT_GRAY, width);
        drawCentered(gc, "LA VENCIDA - APO II - 2026", height - 40, 2, RetroTheme.LAVENDER, width);
    }

    // Dibuja la fila de edificios con ventanas que se prenden y apagan.
    private void drawSkyline(GraphicsContext gc, double width, double height, double time) {
        double ground = height * 0.86;
        gc.setFill(Color.web("#0d0d1a"));
        gc.fillRect(0, ground, width, height - ground);

        double buildingWidth = Math.ceil(width / buildingHeights.length);
        for (int i = 0; i < buildingHeights.length; i++) {
            double h = buildingHeights[i] * PIXEL;
            double x = i * buildingWidth;
            gc.setFill(i % 2 == 0 ? Color.web("#141432") : Color.web("#1a1a3e"));
            gc.fillRect(x, ground - h, buildingWidth - PIXEL, h);

            // ventanas: algunas prendidas, cambian lento
            for (double wy = ground - h + PIXEL * 3; wy < ground - PIXEL * 3; wy += PIXEL * 4) {
                for (double wx = x + PIXEL * 2; wx < x + buildingWidth - PIXEL * 3; wx += PIXEL * 4) {
                    int seed = (int) (wx * 7 + wy * 13 + i);
                    boolean lit = Math.sin(seed + time * 0.4) > 0.35;
                    if (lit) {
                        gc.setFill(seed % 3 == 0 ? RetroTheme.ORANGE : RetroTheme.YELLOW);
                        gc.fillRect(snap(wx), snap(wy), PIXEL * 2, PIXEL * 2);
                    }
                }
            }
        }

        // calle con lineas que avanzan
        gc.setFill(RetroTheme.DARK_GRAY);
        gc.fillRect(0, ground, width, PIXEL * 6);
        gc.setFill(RetroTheme.YELLOW);
        double offset = (time * 120) % (PIXEL * 16);
        for (double x = -offset; x < width; x += PIXEL * 16) {
            gc.fillRect(snap(x), ground + PIXEL * 2.5, PIXEL * 8, PIXEL);
        }
    }

    private void drawCentered(GraphicsContext gc, String text, double y, double pixel, Color color, double width) {
        double x = snap((width - PixelFont.width(text, pixel)) / 2);
        PixelFont.drawWithShadow(gc, text, x, snap(y), pixel, color, RetroTheme.BLACK);
    }

    // Redondea a la rejilla de pixeles para que todo se vea nitido.
    private double snap(double value) {
        return Math.floor(value / PIXEL) * PIXEL;
    }
}
