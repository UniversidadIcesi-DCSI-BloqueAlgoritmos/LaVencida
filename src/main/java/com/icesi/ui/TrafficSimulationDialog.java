package com.icesi.ui;

import com.icesi.controller.GameController;
import com.icesi.model.Incident;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.util.function.Consumer;

import static com.icesi.ui.MonitoringCenterScreen.pixelButton;
import static com.icesi.ui.MonitoringCenterScreen.styled;

// Dialogo modal retro para simular y detonar accidentes de transito bajo los 3 criterios operativos (RF-4 / HU-08 a HU-10).
public class TrafficSimulationDialog extends StackPane {

    private GameController controller;
    private Consumer<Incident> onAccidentGenerated = accident -> { };
    private Runnable onClose = () -> { };

    private Label feedbackLabel;

    // Criterio 1: Colisión
    private ComboBox<String> colVehicle1Combo;
    private ComboBox<String> colVehicle2Combo;
    private TextField colLocationField;

    // Criterio 2: Congestión
    private ComboBox<String> congZoneCombo;
    private TextField congCountField;

    // Criterio 3: Vehículo Detenido
    private ComboBox<String> stopVehicleCombo;
    private TextField stopLocationField;
    private TextField stopCyclesField;

    public TrafficSimulationDialog(GameController controller) {
        this.controller = controller;
        getStyleClass().add("shift-end-background");
        setVisible(false);

        VBox box = new VBox(14);
        box.getStyleClass().add("form-modal-box");
        box.setMaxSize(StackPane.USE_PREF_SIZE, StackPane.USE_PREF_SIZE);
        box.setAlignment(Pos.TOP_LEFT);

        Canvas headerTitle = PixelFont.createLabel("SIMULADOR DE TRANSITO", 4, RetroTheme.YELLOW, RetroTheme.PURPLE);
        Label subtitle = styled(new Label("DETONAR ACCIDENTES POR CRITERIOS OPERATIVOS (RF-4 / HU-08..10)"), "panel-subtitle");

        feedbackLabel = styled(new Label(), "form-error");
        feedbackLabel.setVisible(false);

        // Secciones de criterios
        VBox collisionCard = createCollisionCard();
        VBox congestionCard = createCongestionCard();
        VBox stoppedCard = createStoppedCard();

        Button closeButton = pixelButton("[✕ CERRAR]", "button-gray");
        closeButton.setOnAction(e -> closeDialog());

        Region push = new Region();
        HBox.setHgrow(push, Priority.ALWAYS);
        HBox bottomRow = new HBox(push, closeButton);
        bottomRow.setAlignment(Pos.CENTER_RIGHT);

        box.getChildren().addAll(headerTitle, subtitle, feedbackLabel, collisionCard, congestionCard, stoppedCard, bottomRow);
        getChildren().add(box);

        addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.ESCAPE) {
                closeDialog();
                event.consume();
            }
        });
    }

    // Tarjeta Criterio 1: Colisión entre dos vehículos (HU-08)
    private VBox createCollisionCard() {
        Label title = styled(new Label("1. COLISION ENTRE DOS VEHICULOS (HU-08) - ALTA GRAVEDAD"), "form-label");
        title.setStyle("-fx-text-fill: #ff004d;");

        colVehicle1Combo = new ComboBox<>();
        colVehicle1Combo.getItems().addAll("P-01", "P-02", "A-01", "A-02", "F-01", "F-02");
        colVehicle1Combo.getSelectionModel().select("P-01");
        colVehicle1Combo.getStyleClass().add("pixel-combo-box");

        colVehicle2Combo = new ComboBox<>();
        colVehicle2Combo.getItems().addAll("P-01", "P-02", "A-01", "A-02", "F-01", "F-02");
        colVehicle2Combo.getSelectionModel().select("A-01");
        colVehicle2Combo.getStyleClass().add("pixel-combo-box");

        colLocationField = new TextField("Cruce Calle 30 Cra 19");
        colLocationField.getStyleClass().add("pixel-input");
        colLocationField.setPromptText("Lugar de la colisión");

        Button triggerBtn = pixelButton("DETONAR COLISION", "button-blue");
        triggerBtn.getStyleClass().add("button-small");
        triggerBtn.setOnAction(e -> handleCollision());

        HBox controls = new HBox(8,
                styled(new Label("Veh 1:"), "stat-name"), colVehicle1Combo,
                styled(new Label("Veh 2:"), "stat-name"), colVehicle2Combo,
                colLocationField, triggerBtn);
        controls.setAlignment(Pos.CENTER_LEFT);

        VBox card = new VBox(6, title, controls);
        card.setStyle("-fx-background-color: #000000; -fx-padding: 8; -fx-border-color: #5f574f; -fx-border-width: 2;");
        return card;
    }

    // Tarjeta Criterio 2: Congestión Crítica (HU-09)
    private VBox createCongestionCard() {
        Label title = styled(new Label("2. CONGESTION CRITICA EN ZONA (HU-09) - UMBRAL > 5 VEHICULOS"), "form-label");
        title.setStyle("-fx-text-fill: #ffa300;");

        congZoneCombo = new ComboBox<>();
        congZoneCombo.getItems().addAll(
                "Sector La Estacion",
                "Zona Comercial Centro",
                "Parque Bolivar",
                "Zona Industrial Cencar",
                "Barrio El Prado"
        );
        congZoneCombo.getSelectionModel().selectFirst();
        congZoneCombo.getStyleClass().add("pixel-combo-box");

        congCountField = new TextField("6");
        congCountField.setPrefWidth(55);
        congCountField.getStyleClass().add("pixel-input");
        congCountField.setPromptText("Cant.");

        Button triggerBtn = pixelButton("DETONAR CONGESTION", "button-yellow");
        triggerBtn.getStyleClass().add("button-small");
        triggerBtn.setOnAction(e -> handleCongestion());

        HBox controls = new HBox(8,
                styled(new Label("Zona:"), "stat-name"), congZoneCombo,
                styled(new Label("Vehiculos acumulados:"), "stat-name"), congCountField,
                triggerBtn);
        controls.setAlignment(Pos.CENTER_LEFT);

        VBox card = new VBox(6, title, controls);
        card.setStyle("-fx-background-color: #000000; -fx-padding: 8; -fx-border-color: #5f574f; -fx-border-width: 2;");
        return card;
    }

    // Tarjeta Criterio 3: Vehículo Detenido (HU-10)
    private VBox createStoppedCard() {
        Label title = styled(new Label("3. VEHICULO DETENIDO EN VIA (HU-10) - UMBRAL >= 3 CICLOS"), "form-label");
        title.setStyle("-fx-text-fill: #00e436;");

        stopVehicleCombo = new ComboBox<>();
        stopVehicleCombo.getItems().addAll("P-01", "P-02", "A-01", "A-02", "F-01", "F-02");
        stopVehicleCombo.getSelectionModel().select("F-01");
        stopVehicleCombo.getStyleClass().add("pixel-combo-box");

        stopLocationField = new TextField("Zona Residencial Las Palmas");
        stopLocationField.getStyleClass().add("pixel-input");
        stopLocationField.setPromptText("Vía o sector");

        stopCyclesField = new TextField("4");
        stopCyclesField.setPrefWidth(55);
        stopCyclesField.getStyleClass().add("pixel-input");
        stopCyclesField.setPromptText("Ciclos");

        Button triggerBtn = pixelButton("DETONAR DETENIDO", "button-green");
        triggerBtn.getStyleClass().add("button-small");
        triggerBtn.setOnAction(e -> handleStoppedVehicle());

        HBox controls = new HBox(8,
                styled(new Label("Vehículo:"), "stat-name"), stopVehicleCombo,
                stopLocationField,
                styled(new Label("Ciclos:"), "stat-name"), stopCyclesField,
                triggerBtn);
        controls.setAlignment(Pos.CENTER_LEFT);

        VBox card = new VBox(6, title, controls);
        card.setStyle("-fx-background-color: #000000; -fx-padding: 8; -fx-border-color: #5f574f; -fx-border-width: 2;");
        return card;
    }

    public void setOnAccidentGenerated(Consumer<Incident> onAccidentGenerated) {
        this.onAccidentGenerated = onAccidentGenerated;
    }

    public void setOnClose(Runnable onClose) {
        this.onClose = onClose;
    }

    public void open() {
        feedbackLabel.setVisible(false);
        feedbackLabel.setText("");
        setVisible(true);
    }

    public void closeDialog() {
        setVisible(false);
        onClose.run();
    }

    private void handleCollision() {
        String v1 = colVehicle1Combo.getValue();
        String v2 = colVehicle2Combo.getValue();
        if (v1 == null || v2 == null || v1.equals(v2)) {
            showError("Para una colisión debes seleccionar dos vehículos diferentes.");
            return;
        }
        String loc = colLocationField.getText() != null && !colLocationField.getText().isBlank()
                ? colLocationField.getText().trim() : "Cruce Calle 30 Cra 19";

        Incident accident = controller.triggerCollisionAccident(v1, v2, loc);
        if (accident != null) {
            closeDialog();
            onAccidentGenerated.accept(accident);
        } else {
            showError("No se pudo generar la colisión con los datos dados.");
        }
    }

    private void handleCongestion() {
        String zone = congZoneCombo.getValue();
        int count;
        try {
            count = Integer.parseInt(congCountField.getText().trim());
        } catch (NumberFormatException e) {
            showError("La cantidad de vehículos acumulados debe ser un número entero.");
            return;
        }
        if (count <= 5) {
            showError("El umbral de congestión crítica debe ser mayor a 5 vehículos (ingresaste " + count + ").");
            return;
        }

        Incident accident = controller.triggerCongestionAccident(zone, count);
        if (accident != null) {
            closeDialog();
            onAccidentGenerated.accept(accident);
        } else {
            showError("No se pudo generar el accidente por congestión.");
        }
    }

    private void handleStoppedVehicle() {
        String veh = stopVehicleCombo.getValue();
        String loc = stopLocationField.getText() != null && !stopLocationField.getText().isBlank()
                ? stopLocationField.getText().trim() : "Zona Residencial Las Palmas";
        int cycles;
        try {
            cycles = Integer.parseInt(stopCyclesField.getText().trim());
        } catch (NumberFormatException e) {
            showError("La cantidad de ciclos debe ser un número entero.");
            return;
        }
        if (cycles < 3) {
            showError("El umbral de detención debe ser al menos de 3 ciclos (ingresaste " + cycles + ").");
            return;
        }

        Incident accident = controller.triggerStoppedVehicleAccident(veh, loc, cycles);
        if (accident != null) {
            closeDialog();
            onAccidentGenerated.accept(accident);
        } else {
            showError("No se pudo generar el accidente por vehículo detenido.");
        }
    }

    private void showError(String message) {
        RetroSound.play(RetroSound.Effect.ERROR);
        feedbackLabel.setText("✕ " + message);
        feedbackLabel.setVisible(true);
    }
}
