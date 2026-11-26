package com.icesi.ui;

import javafx.animation.Animation;
import javafx.animation.FadeTransition;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.util.Duration;

// Barra de "nivel de alerta" del HUD: se llena con los incidentes activos y cambia de color.
// Verde de 0 a 2 incidentes, amarillo de 3 a 5 y rojo (parpadeando) con 6 o mas.
public class AlertMeter extends HBox {

    private static final int SEGMENTS = 8; // la barra tiene 8 cuadritos

    private Region[] segments = new Region[SEGMENTS];
    private Label levelLabel;
    private FadeTransition blink;

    public AlertMeter() {
        super(6);
        setAlignment(Pos.CENTER_LEFT);

        Label title = new Label("ALERTA");
        title.getStyleClass().add("hud-key");
        HBox bar = new HBox(2);
        bar.getStyleClass().add("alert-bar");
        for (int i = 0; i < SEGMENTS; i++) {
            segments[i] = new Region();
            segments[i].setMinSize(10, 14);
            segments[i].setMaxSize(10, 14);
            bar.getChildren().add(segments[i]);
        }
        levelLabel = new Label();
        levelLabel.getStyleClass().add("hud-level");
        getChildren().addAll(title, bar, levelLabel);

        // el nivel rojo parpadea para llamar la atencion
        blink = new FadeTransition(Duration.millis(300), bar);
        blink.setFromValue(1);
        blink.setToValue(0.35);
        blink.setAutoReverse(true);
        blink.setCycleCount(Animation.INDEFINITE);
    }

    // Actualiza la barra con la cantidad de incidentes activos.
    public void setActiveIncidents(int active) {
        String color;
        String level;
        if (active <= 2) {
            color = "#00e436";
            level = "NORMAL";
        } else if (active <= 5) {
            color = "#ffec27";
            level = "PRECAUCION";
        } else {
            color = "#ff004d";
            level = "CRITICO";
        }

        for (int i = 0; i < SEGMENTS; i++) {
            // los cuadritos llenos van del color del nivel y los vacios en gris oscuro
            segments[i].setStyle("-fx-background-color: " + (i < active ? color : "#2a2f55") + ";");
        }
        levelLabel.setText(level);
        levelLabel.setStyle("-fx-text-fill: " + color + ";");

        if (active >= 6) {
            blink.play();
        } else {
            blink.stop();
            blink.getNode().setOpacity(1);
        }
    }
}
