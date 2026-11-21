package com.icesi.ui;

import javafx.animation.AnimationTimer;
import javafx.geometry.Pos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

// Lo que se ve en el Panel de Incidentes cuando no hay ninguno activo:
// el operador descansando con "Z" que flotan y un mensaje de ciudad tranquila.
public class EmptyStateView extends VBox {

    private Canvas scene = new Canvas(220, 130);

    public EmptyStateView() {
        super(14);
        setAlignment(Pos.CENTER);
        getStyleClass().add("pixel-panel");
        setMinHeight(360);

        Label message = new Label("No hay incidentes activos en Palmira.\nVe al Centro de Monitoreo y presiona [G] para simular uno.");
        message.getStyleClass().add("dialog-text");
        message.setStyle("-fx-text-alignment: center;");

        getChildren().addAll(scene,
                PixelFont.createLabel("CIUDAD TRANQUILA", 4, RetroTheme.GREEN, RetroTheme.DARK_GREEN),
                message);

        AnimationTimer timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                // solo se anima mientras esta en pantalla
                if (getScene() != null) {
                    draw(now / 1_000_000_000.0);
                }
            }
        };
        timer.start();
    }

    // Dibuja el operador respirando despacio y tres "Z" que suben y se desvanecen.
    private void draw(double time) {
        GraphicsContext gc = scene.getGraphicsContext2D();
        gc.clearRect(0, 0, scene.getWidth(), scene.getHeight());

        // piso y sombra
        gc.setFill(RetroTheme.NIGHT);
        gc.fillRect(40, 116, 140, 6);
        double breathe = Math.round(Math.sin(time * 1.5) * 2);
        SpriteRegistry.draw(gc, SpriteRegistry.OPERATOR, 70, 36 + breathe, 80, 80);

        for (int i = 0; i < 3; i++) {
            double life = (time * 0.35 + i / 3.0) % 1.0; // cada Z va en un momento distinto
            double x = 150 + life * 30 + Math.sin(life * 8) * 4;
            double y = 60 - life * 55;
            double pixel = 2 + life * 2;
            gc.setGlobalAlpha(1 - life);
            PixelFont.draw(gc, "Z", Math.round(x), Math.round(y), pixel, RetroTheme.LAVENDER);
        }
        gc.setGlobalAlpha(1);
    }
}
