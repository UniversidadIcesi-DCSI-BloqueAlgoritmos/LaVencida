package com.icesi.ui;

import com.icesi.controller.GameController;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import static com.icesi.ui.MonitoringCenterScreen.pixelButton;
import static com.icesi.ui.MonitoringCenterScreen.styled;

// Dialogo modal retro para consultar informacion de movilidad y el estado de corredores de emergencia.
public class MobilityDialog extends StackPane {

    private GameController controller;
    private CityMapView mapView;
    private Runnable onClose = () -> { };

    private Label corridorStatusLabel;
    private Label networkStatusLabel;
    private Label unitsInTransitLabel;
    private Label activeAccidentsLabel;
    private Label corridorCellsLabel;
    private Label routingAlgorithmLabel;

    public MobilityDialog(GameController controller, CityMapView mapView) {
        this.controller = controller;
        this.mapView = mapView;
        getStyleClass().add("shift-end-background");
        setVisible(false);

        VBox box = new VBox(14);
        box.getStyleClass().add("form-modal-box");
        box.setMaxSize(StackPane.USE_PREF_SIZE, StackPane.USE_PREF_SIZE);
        box.setAlignment(Pos.TOP_LEFT);

        Canvas headerTitle = PixelFont.createLabel("INFO DE MOVILIDAD", 4, RetroTheme.YELLOW, RetroTheme.PURPLE);
        Label subtitle = styled(new Label("RED VIAL DE PALMIRA Y CORREDORES DE EMERGENCIA"), "panel-subtitle");

        GridPane grid = new GridPane();
        grid.setHgap(16);
        grid.setVgap(12);
        grid.setPadding(new Insets(10, 0, 10, 0));

        networkStatusLabel = styled(new Label("100% OPERATIVA"), "stat-value");
        corridorStatusLabel = styled(new Label("4 EJES PRINCIPALES"), "stat-value");
        unitsInTransitLabel = styled(new Label("0 UNIDADES"), "stat-value");
        activeAccidentsLabel = styled(new Label("0 ACTIVOS"), "stat-value");
        corridorCellsLabel = styled(new Label("256 CELDAS"), "stat-value");
        routingAlgorithmLabel = styled(new Label("BFS ARTERIAL"), "stat-value");

        grid.add(createCard("ESTADO DE RED VIAL", networkStatusLabel, "Vías despejadas sin bloqueos"), 0, 0);
        grid.add(createCard("CORREDORES DE EMERGENCIA", corridorStatusLabel, "Norte-Sur, Oriente-Occidente"), 1, 0);
        grid.add(createCard("CELDAS ARTERIALES", corridorCellsLabel, "Vías principales MAIN_ROAD"), 0, 1);
        grid.add(createCard("UNIDADES EN DESPACHO", unitsInTransitLabel, "Vehículos atendiendo emergencias"), 1, 1);
        grid.add(createCard("ACCIDENTES EN VÍA", activeAccidentsLabel, "Impactando movilidad urbana"), 0, 2);
        grid.add(createCard("ALGORITMO DE RUTAS", routingAlgorithmLabel, "BFS con prioridad de corredores"), 1, 2);

        // Barra inferior de botones interactivos
        Button toggleCorridorsBtn = pixelButton("[E] CONMUTAR CORREDORES EN MAPA", "button-blue");
        toggleCorridorsBtn.setOnAction(e -> {
            mapView.toggleEmergencyCorridors();
            RetroSound.play(RetroSound.Effect.MENU);
            updateStatus();
        });

        Button closeBtn = pixelButton("[ESC] CERRAR", "button-yellow");
        closeBtn.setOnAction(e -> close());

        Region push = new Region();
        HBox.setHgrow(push, Priority.ALWAYS);
        HBox actions = new HBox(12, toggleCorridorsBtn, push, closeBtn);
        actions.setAlignment(Pos.CENTER_LEFT);

        box.getChildren().addAll(headerTitle, subtitle, grid, actions);
        getChildren().add(box);

        addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.ESCAPE) {
                close();
                event.consume();
            } else if (event.getCode() == KeyCode.E) {
                mapView.toggleEmergencyCorridors();
                RetroSound.play(RetroSound.Effect.MENU);
                updateStatus();
                event.consume();
            }
        });
    }

    private VBox createCard(String title, Label value, String caption) {
        VBox card = new VBox(4);
        card.getStyleClass().add("stat-card");
        card.setPadding(new Insets(8, 12, 8, 12));
        card.setMinWidth(260);

        Label titleLabel = styled(new Label(title), "panel-subtitle");
        Label capLabel = styled(new Label(caption), "stat-name");

        card.getChildren().addAll(titleLabel, value, capLabel);
        return card;
    }

    public void updateStatus() {
        int corridorCells = controller.countCorridorCells();
        corridorCellsLabel.setText(corridorCells + " CELDAS");

        int activeAccidents = controller.getActiveAccidentCount();
        activeAccidentsLabel.setText(activeAccidents + " REPORTADOS");

        int totalVehicles = controller.getVehicles().size();
        int availableVehicles = controller.getAvailableVehicleCount();
        int inTransit = totalVehicles - availableVehicles;
        unitsInTransitLabel.setText(inTransit + " / " + totalVehicles + " EN RUTA");

        boolean corridorsOn = mapView.isEmergencyCorridorsVisible();
        corridorStatusLabel.setText(corridorsOn ? "4 ACTIVOS (VISIBLES)" : "4 EJES (NORMAL)");

        networkStatusLabel.setText(activeAccidents > 3 ? "CONGESTION MEDIA" : "100% OPERATIVA");
    }

    public void open() {
        updateStatus();
        setVisible(true);
        requestFocus();
    }

    public void close() {
        setVisible(false);
        onClose.run();
    }

    public void setOnClose(Runnable onClose) {
        this.onClose = onClose;
    }
}
