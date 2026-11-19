package com.icesi.ui;

import com.icesi.controller.GameController;
import com.icesi.model.Direction;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import static com.icesi.ui.MonitoringCenterScreen.pixelButton;
import static com.icesi.ui.MonitoringCenterScreen.styled;

// Mapa de Trafico: el mapa a la izquierda y el panel de incidentes a la derecha,
// para despachar y resolver mientras se mira lo que pasa en la ciudad.
public class SplitViewScreen implements GameScreen {

    private GameController controller;
    private CityMapView mapView;
    private IncidentPanelScreen incidentPanel;
    private StackPane mapSlot;
    private StackPane panelSlot;
    private HBox root;
    private Label positionLabel;
    private Label stackLabel;
    private Label timeLabel;

    // El mapa y el panel son los mismos de las otras pantallas, solo se acomodan juntos aqui.
    public SplitViewScreen(GameController controller, CityMapView mapView, IncidentPanelScreen incidentPanel) {
        this.controller = controller;
        this.mapView = mapView;
        this.incidentPanel = incidentPanel;

        mapSlot = new StackPane();
        mapSlot.setAlignment(Pos.TOP_LEFT);
        panelSlot = new StackPane();
        panelSlot.setAlignment(Pos.TOP_LEFT);
        VBox.setVgrow(panelSlot, Priority.ALWAYS);

        VBox right = new VBox(8, createOperatorBar(), panelSlot);
        right.setPadding(new Insets(0, 16, 0, 0));
        HBox.setHgrow(right, Priority.ALWAYS);

        root = new HBox(16, mapSlot, right);
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

    // Barra fina con los datos del operador: posicion, pila de movimientos, momento del dia y deshacer.
    private HBox createOperatorBar() {
        positionLabel = styled(new Label(), "operator-bar-item");
        stackLabel = styled(new Label(), "operator-bar-item");
        timeLabel = styled(new Label(), "operator-bar-item");
        Button undoButton = pixelButton("[U] DESHACER", "button-yellow");
        undoButton.getStyleClass().add("button-small");
        undoButton.setOnAction(e -> undo());
        Region push = new Region();
        HBox.setHgrow(push, Priority.ALWAYS);

        HBox bar = new HBox(14, styled(new Label("◆"), "operator-bar-icon"), positionLabel, stackLabel, timeLabel, push, undoButton);
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.getStyleClass().add("operator-bar");
        return bar;
    }

    @Override
    public void refresh() {
        incidentPanel.refresh();
        positionLabel.setText(String.format("POS (%02d, %02d)",
                controller.getOperator().getRow(), controller.getOperator().getColumn()));
        stackLabel.setText("PILA " + controller.getOperator().getMovementCount());
        timeLabel.setText(mapView.getPhaseName());
    }

    // Deshace el ultimo movimiento del operador usando su pila.
    private void undo() {
        if (!controller.undoOperatorMove()) {
            RetroSound.play(RetroSound.Effect.ERROR);
            mapView.shake();
        }
        refresh();
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
            undo();
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
