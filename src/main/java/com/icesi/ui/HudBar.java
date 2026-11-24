package com.icesi.ui;

import com.icesi.controller.GameController;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;

// Barra superior siempre visible: puntaje, incidentes activos, vehiculos libres y teclas para cambiar de pantalla.
public class HudBar extends HBox {

    private CountingLabel scoreLabel;
    private CountingLabel activeLabel;
    private CountingLabel freeLabel;
    private AlertMeter alertMeter;
    private Label soundLabel;
    private Label timerLabel;
    private Label centerKey;
    private Label mapKey;
    private Label incidentsKey;
    private Label treeKey;
    private Label corridorKey;

    // Arma la barra con sus etiquetas.
    public HudBar() {
        getStyleClass().add("hud-bar");
        setAlignment(Pos.CENTER_LEFT);

        scoreLabel = createItem("♥ SCORE %06d", "hud-score");
        activeLabel = createItem("★ ACTIVOS %02d", "hud-active");
        freeLabel = createItem("■ LIBRES %02d", "hud-free");
        alertMeter = new AlertMeter();
        soundLabel = new Label();
        timerLabel = new Label("⏱ 05:00");
        timerLabel.getStyleClass().addAll("hud-item", "hud-timer");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        centerKey = new Label("[C] CENTRO");
        mapKey = new Label("[M] MAPA");
        incidentsKey = new Label("[I] INCIDENTES");
        treeKey = new Label("[B] ARBOL BST");
        corridorKey = new Label("[E] VIAS");
        Label helpKey = new Label("[H] AYUDA");
        helpKey.getStyleClass().add("hud-key");

        getChildren().addAll(PixelFont.createLabel("SGMMS", 3, RetroTheme.YELLOW, RetroTheme.PURPLE),
                scoreLabel, activeLabel, freeLabel, timerLabel, alertMeter, spacer, soundLabel,
                centerKey, mapKey, incidentsKey, treeKey, corridorKey, helpKey);
        setActiveScreen("center");

        // ningun texto del HUD se puede encoger: si falta espacio se achica el hueco del medio
        for (javafx.scene.Node child : getChildren()) {
            if (child instanceof Region region && region != spacer) {
                region.setMinWidth(Region.USE_PREF_SIZE);
            }
        }
    }

    // Crea una etiqueta del HUD con su color.
    private CountingLabel createItem(String format, String colorClass) {
        CountingLabel label = new CountingLabel(format);
        label.getStyleClass().addAll("hud-item", colorClass);
        return label;
    }

    // Actualiza los numeros con lo que dice el controlador.
    public void refresh(GameController controller) {
        scoreLabel.setValue(controller.getScore());
        activeLabel.setValue(controller.getActiveIncidentCount());
        freeLabel.setValue(controller.getAvailableVehicleCount());
        alertMeter.setActiveIncidents(controller.getActiveIncidentCount());
        // icono de parlante: se ve apagado cuando el sonido esta silenciado
        soundLabel.setText(RetroSound.isMuted() ? "[N] ♪ OFF" : "[N] ♪ ON");
        markKey(soundLabel, !RetroSound.isMuted());
    }

    // Muestra el tiempo que le queda al turno; en el ultimo minuto se pone rojo.
    public void setShiftTime(String text, boolean lastMinute) {
        timerLabel.setText("⏱ " + text);
        timerLabel.getStyleClass().remove("hud-timer-low");
        if (lastMinute) {
            timerLabel.getStyleClass().add("hud-timer-low");
        }
    }

    // Resalta la tecla de la pantalla en la que esta el jugador.
    public void setActiveScreen(String screen) {
        markKey(centerKey, screen.equals("center"));
        markKey(mapKey, screen.equals("map"));
        markKey(incidentsKey, screen.equals("incidents"));
        markKey(treeKey, screen.equals("tree"));
    }

    // Resalta la tecla de corredores de emergencia cuando estan visibles en el mapa.
    public void setCorridorsActive(boolean active) {
        markKey(corridorKey, active);
    }

    private void markKey(Label key, boolean active) {
        key.getStyleClass().removeAll("hud-key", "hud-key-active");
        key.getStyleClass().add(active ? "hud-key-active" : "hud-key");
    }
}
