package com.icesi.ui;

import javafx.animation.AnimationTimer;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
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
    private static final String[] MENU = {"JUGAR", "OPCIONES", "SALIR"};

    private int selected;          // opcion del menu que esta marcada
    private double[] menuTop = new double[MENU.length]; // donde quedo dibujada cada opcion (para el clic)
    private int record;            // mejor puntaje guardado, se muestra abajo
    private WritableImage skyImage; // el cielo se dibuja una sola vez y se reutiliza en cada cuadro

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
    // El mejor puntaje que se muestra en la portada.
    public void setRecord(int record) {
        this.record = record;
    }

    public void start() {
        timer.start();
    }

    // Detiene la animacion (cuando el jugador ya entro al juego).
    public void stop() {
        timer.stop();
    }

    // Mueve la seleccion del menu hacia arriba (-1) o hacia abajo (+1), dando la vuelta en los extremos.
    public void moveSelection(int step) {
        selected = Math.floorMod(selected + step, MENU.length);
    }

    // Devuelve el texto de la opcion marcada: "JUGAR" o "SALIR".
    public String getSelectedOption() {
        return MENU[selected];
    }

    // Marca la opcion que esta a la altura y del mouse; devuelve false si el mouse no esta sobre ninguna.
    public boolean selectAt(double y) {
        for (int i = 0; i < MENU.length; i++) {
            if (y >= menuTop[i] - 8 && y <= menuTop[i] + 48) {
                selected = i;
                return true;
            }
        }
        return false;
    }

    // Dibuja un cuadro de la animacion; time son los segundos que han pasado.
    private void draw(double time) {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        double width = canvas.getWidth();
        double height = canvas.getHeight();
        if (width <= 0 || height <= 0) {
            return;
        }

        // cielo degradado; solo se vuelve a calcular si la ventana cambia de tamano
        if (skyImage == null || skyImage.getWidth() != Math.ceil(width) || skyImage.getHeight() != Math.ceil(height)) {
            skyImage = createSky((int) Math.ceil(width), (int) Math.ceil(height));
        }
        gc.drawImage(skyImage, 0, 0);

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

        drawMenu(gc, width, height, time);
        drawCentered(gc, "LA VENCIDA - APO II - 2026", height - 40, 2, RetroTheme.LAVENDER, width);
        if (record > 0) {
            drawCentered(gc, String.format("RECORD %06d", record), height - 70, 3, RetroTheme.YELLOW, width);
        }
    }

    // Crea la imagen del cielo: un degradado de noche con "dithering" (mezcla de pixeles en cuadricula)
    // para que el paso de un color a otro se vea suave pero siga siendo pixel-art.
    private WritableImage createSky(int width, int height) {
        Color[] stops = {RetroTheme.BLACK, Color.web("#0b1230"), RetroTheme.NIGHT, Color.web("#3b2a5e"),
                RetroTheme.PURPLE, Color.web("#b8456b")};
        // matriz de Bayer 4x4: decide en que orden se "prenden" los pixeles del color siguiente
        int[][] bayer = {{0, 8, 2, 10}, {12, 4, 14, 6}, {3, 11, 1, 9}, {15, 7, 13, 5}};

        WritableImage image = new WritableImage(width, height);
        PixelWriter writer = image.getPixelWriter();
        int block = (int) PIXEL;
        double skyHeight = height * 0.86; // el cielo llega hasta el piso de la ciudad

        for (int by = 0; by * block < height; by++) {
            // t va de 0 (arriba) a 1 (horizonte)
            double t = Math.min(1, (by * block) / skyHeight);
            double position = t * (stops.length - 1);
            int index = Math.min(stops.length - 2, (int) position);
            double mix = position - index;

            for (int bx = 0; bx * block < width; bx++) {
                double threshold = (bayer[by % 4][bx % 4] + 0.5) / 16.0;
                Color color = mix > threshold ? stops[index + 1] : stops[index];
                int argb = toArgb(color);
                for (int y = by * block; y < Math.min(height, (by + 1) * block); y++) {
                    for (int x = bx * block; x < Math.min(width, (bx + 1) * block); x++) {
                        writer.setArgb(x, y, argb);
                    }
                }
            }
        }
        return image;
    }

    // Convierte un color de JavaFX al numero ARGB que usa PixelWriter.
    private int toArgb(Color color) {
        int r = (int) Math.round(color.getRed() * 255);
        int g = (int) Math.round(color.getGreen() * 255);
        int b = (int) Math.round(color.getBlue() * 255);
        return 0xFF000000 | (r << 16) | (g << 8) | b;
    }

    // Dibuja el menu: la opcion marcada va en amarillo con flechas que se mueven, las demas en gris.
    private void drawMenu(GraphicsContext gc, double width, double height, double time) {
        double pixel = 5;
        double top = height * 0.50;
        for (int i = 0; i < MENU.length; i++) {
            double y = top + i * 52;
            menuTop[i] = y;
            if (i == selected) {
                // las flechas se acercan y se alejan del texto
                double wiggle = Math.round(Math.sin(time * 6)) * PIXEL;
                String text = MENU[i];
                double textWidth = PixelFont.width(text, pixel);
                double x = snap((width - textWidth) / 2);
                PixelFont.drawWithShadow(gc, text, x, snap(y), pixel, RetroTheme.YELLOW, RetroTheme.PURPLE);
                PixelFont.drawWithShadow(gc, ">", x - pixel * 10 + wiggle, snap(y), pixel, RetroTheme.YELLOW, RetroTheme.BLACK);
                PixelFont.drawWithShadow(gc, "<", x + textWidth + pixel * 5 - wiggle, snap(y), pixel, RetroTheme.YELLOW, RetroTheme.BLACK);
            } else {
                drawCentered(gc, MENU[i], y, pixel, RetroTheme.LAVENDER, width);
            }
        }
        drawCentered(gc, "FLECHAS PARA ELEGIR - ENTER PARA CONFIRMAR", top + MENU.length * 52 + 6, 2, RetroTheme.LIGHT_GRAY, width);
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

        drawTraffic(gc, width, ground, time);
    }

    // Dibuja las patrullas, ambulancias y bomberos que pasan por la avenida con la sirena prendida.
    private void drawTraffic(GraphicsContext gc, double width, double ground, double time) {
        String[] vehicles = {SpriteRegistry.PATROL, SpriteRegistry.AMBULANCE, SpriteRegistry.FIRE_TRUCK, SpriteRegistry.PATROL};
        double speed = 130;                        // pixeles por segundo, todos iguales para que no se choquen
        double size = PIXEL * 9;
        double lap = width + size * 2;             // distancia de un lado al otro de la pantalla
        double gap = lap / vehicles.length;        // separacion entre un carro y el siguiente

        for (int i = 0; i < vehicles.length; i++) {
            double x = snap((time * speed + i * gap) % lap - size);
            double y = ground + PIXEL * 6 - size + PIXEL; // las ruedas quedan sobre la calle
            SpriteRegistry.draw(gc, vehicles[i], x, y, size, size);

            // luz de la sirena: cambia entre rojo y azul varias veces por segundo
            boolean red = (int) (time * 6 + i) % 2 == 0;
            gc.setFill(red ? RetroTheme.RED : RetroTheme.BLUE);
            gc.setGlobalAlpha(0.35);
            gc.fillRect(x + size / 2 - PIXEL * 3, y - PIXEL * 2, PIXEL * 6, PIXEL * 3);
            gc.setGlobalAlpha(1);
            gc.fillRect(x + size / 2 - PIXEL, y, PIXEL * 2, PIXEL);
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
