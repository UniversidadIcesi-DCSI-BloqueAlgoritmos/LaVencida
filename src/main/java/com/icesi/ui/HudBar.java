package com.icesi.ui;

import com.icesi.controller.GameController;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;

// Barra superior siempre visible: puntaje, incidentes activos, vehiculos libres y teclas para cambiar de pantalla.
public class HudBar extends HBox {

    private Label scoreLabel;
    private Label activeLabel;
    private Label freeLabel;
    private Label centerKey;
    private Label mapKey;
    private Label incidentsKey;

    // Arma la barra con sus etiquetas.
    public HudBar() {
        getStyleClass().add("hud-bar");
        setAlignment(Pos.CENTER_LEFT);

        scoreLabel = createItem("hud-score");
        activeLabel = createItem("hud-active");
        freeLabel = createItem("hud-free");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        centerKey = new Label("[C] CENTRO");
        mapKey = new Label("[M] MAPA");
        incidentsKey = new Label("[I] INCIDENTES");

        getChildren().addAll(PixelFont.createLabel("SGMMS", 3, RetroTheme.YELLOW, RetroTheme.PURPLE),
                scoreLabel, activeLabel, freeLabel, spacer, centerKey, mapKey, incidentsKey);
        setActiveScreen("center");
    }

    // Crea una etiqueta del HUD con su color.
    private Label createItem(String colorClass) {
        Label label = new Label();
        label.getStyleClass().addAll("hud-item", colorClass);
        return label;
    }

    // Actualiza los numeros con lo que dice el controlador.
    public void refresh(GameController controller) {
        scoreLabel.setText(String.format("♥ SCORE %06d", controller.getScore()));
        activeLabel.setText(String.format("★ ACTIVOS %02d", controller.getActiveIncidentCount()));
        freeLabel.setText(String.format("■ LIBRES %02d", controller.getAvailableVehicleCount()));
    }

    // Resalta la tecla de la pantalla en la que esta el jugador.
    public void setActiveScreen(String screen) {
        markKey(centerKey, screen.equals("center"));
        markKey(mapKey, screen.equals("map"));
        markKey(incidentsKey, screen.equals("incidents"));
    }

    private void markKey(Label key, boolean active) {
        key.getStyleClass().removeAll("hud-key", "hud-key-active");
        key.getStyleClass().add(active ? "hud-key-active" : "hud-key");
    }
}
