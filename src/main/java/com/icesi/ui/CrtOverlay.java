package com.icesi.ui;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;

// Capa transparente encima de todo el juego que imita un monitor viejo (CRT):
// lineas de escaneo horizontales y las esquinas un poco mas oscuras.
public class CrtOverlay extends Pane {

    private static final double LINE_GAP = 3;          // cada cuantos pixeles hay una linea
    private static final double LINE_DARKNESS = 0.16;  // que tan oscuras son las lineas

    private Canvas canvas = new Canvas();

    public CrtOverlay() {
        getChildren().add(canvas);
        setMouseTransparent(true); // los clics pasan a los botones de abajo
        // se redibuja solo cuando la ventana cambia de tamano
        widthProperty().addListener((obs, before, now) -> redraw());
        heightProperty().addListener((obs, before, now) -> redraw());
    }

    private void redraw() {
        double width = getWidth();
        double height = getHeight();
        canvas.setWidth(width);
        canvas.setHeight(height);
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.clearRect(0, 0, width, height);

        // lineas de escaneo
        gc.setFill(Color.rgb(0, 0, 0, LINE_DARKNESS));
        for (double y = 0; y < height; y += LINE_GAP) {
            gc.fillRect(0, y, width, 1);
        }

        // vineta: el centro queda igual y los bordes se oscurecen
        RadialGradient vignette = new RadialGradient(0, 0, 0.5, 0.5, 0.75, true, CycleMethod.NO_CYCLE,
                new Stop(0.6, Color.rgb(0, 0, 0, 0)),
                new Stop(1, Color.rgb(0, 0, 0, 0.45)));
        gc.setFill(vignette);
        gc.fillRect(0, 0, width, height);
    }
}
