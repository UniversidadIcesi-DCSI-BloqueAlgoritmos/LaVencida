package com.icesi.ui;

import com.icesi.controller.GameController;
import com.icesi.model.Direction;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;

// Vista dividida: el mapa a la izquierda y el panel de incidentes a la derecha,
// para despachar y resolver mientras se mira lo que pasa en la ciudad.
public class SplitViewScreen implements GameScreen {

    private GameController controller;
    private CityMapView mapView;
    private IncidentPanelScreen incidentPanel;
    private StackPane mapSlot;
    private StackPane panelSlot;
    private HBox root;

    // El mapa y el panel son los mismos de las otras pantallas, solo se acomodan juntos aqui.
    public SplitViewScreen(GameController controller, CityMapView mapView, IncidentPanelScreen incidentPanel) {
        this.controller = controller;
        this.mapView = mapView;
        this.incidentPanel = incidentPanel;

        mapSlot = new StackPane();
        mapSlot.setAlignment(Pos.TOP_LEFT);
        panelSlot = new StackPane();
        panelSlot.setAlignment(Pos.TOP_LEFT);
        HBox.setHgrow(panelSlot, Priority.ALWAYS);

        root = new HBox(16, mapSlot, panelSlot);
        root.setPadding(new Insets(8, 0, 8, 24));
        root.setAlignment(Pos.TOP_LEFT);
        root.getStyleClass().add("crt-background");
    }

    @Override
    public Node getView() {
        // se traen el mapa y el panel a esta pantalla (salen solos de donde estaban)
        mapSlot.getChildren().setAll(mapView.getView());
        panelSlot.getChildren().setAll(incidentPanel.getCompactView());
        return root;
    }

    @Override
    public void refresh() {
        incidentPanel.refresh();
    }

    // ENTER y ESC son del panel; WASD o flechas mueven al operador y U deshace.
    @Override
    public boolean handleKey(KeyCode code) {
        if (incidentPanel.handleKey(code)) {
            return true;
        }
        Direction direction = null;
        if (code == KeyCode.W || code == KeyCode.UP) {
            direction = Direction.UP;
        } else if (code == KeyCode.S || code == KeyCode.DOWN) {
            direction = Direction.DOWN;
        } else if (code == KeyCode.A || code == KeyCode.LEFT) {
            direction = Direction.LEFT;
        } else if (code == KeyCode.D || code == KeyCode.RIGHT) {
            direction = Direction.RIGHT;
        } else if (code == KeyCode.U) {
            if (!controller.undoOperatorMove()) {
                mapView.shake();
            }
            return true;
        }
        if (direction == null) {
            return false;
        }
        if (!controller.moveOperator(direction)) {
            RetroSound.play(RetroSound.Effect.ERROR);
            mapView.shake();
        }
        return true;
    }
}
