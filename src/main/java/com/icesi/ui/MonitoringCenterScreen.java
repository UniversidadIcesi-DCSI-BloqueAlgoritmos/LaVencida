package com.icesi.ui;

import com.icesi.controller.GameController;
import com.icesi.model.Incident;
import com.icesi.model.Severity;
import com.icesi.model.Vehicle;
import com.icesi.model.VehicleStatus;
import javafx.animation.FadeTransition;
import javafx.animation.Animation;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.util.function.Consumer;

// Centro de Monitoreo (pantalla principal): indicadores en tiempo real, incidente mas urgente, flota y accesos a las otras pantallas.
public class MonitoringCenterScreen implements GameScreen {

    private GameController controller;
    private Consumer<String> navigator;
    private HBox root;

    private Label scoreValue;
    private Label activeValue;
    private Label accidentValue;
    private Label theftValue;
    private Label fireValue;
    private Label freeValue;

    private VBox priorityBox;
    private VBox fleetBox;
    private Label eventText;

    // Arma la pantalla; navigator sirve para cambiar a "map" o "incidents".
    public MonitoringCenterScreen(GameController controller, Consumer<String> navigator) {
        this.controller = controller;
        this.navigator = navigator;

        VBox left = new VBox(18);
        left.setPadding(new Insets(4));
        HBox.setHgrow(left, Priority.ALWAYS);

        VBox header = new VBox(6,
                PixelFont.createLabel("CENTRO DE MONITOREO", 5, RetroTheme.YELLOW, RetroTheme.PURPLE),
                styled(new Label("SISTEMA DE GESTION Y MONITOREO DE MOVILIDAD Y SEGURIDAD - PALMIRA"), "panel-subtitle"));

        scoreValue = new Label();
        activeValue = new Label();
        accidentValue = new Label();
        theftValue = new Label();
        fireValue = new Label();
        freeValue = new Label();

        GridPane stats = new GridPane();
        stats.setHgap(16);
        stats.setVgap(16);
        stats.add(createStatCard(SpriteRegistry.OPERATOR, scoreValue, "PUNTAJE DEL OPERADOR", RetroTheme.YELLOW), 0, 0);
        stats.add(createStatCard(SpriteRegistry.SIREN, activeValue, "INCIDENTES ACTIVOS", RetroTheme.PINK), 1, 0);
        stats.add(createStatCard(SpriteRegistry.PATROL, freeValue, "VEHICULOS LIBRES", RetroTheme.GREEN), 2, 0);
        stats.add(createStatCard(SpriteRegistry.ACCIDENT, accidentValue, "ACCIDENTES", RetroTheme.ORANGE), 0, 1);
        stats.add(createStatCard(SpriteRegistry.THEFT, theftValue, "ROBOS", RetroTheme.PEACH), 1, 1);
        stats.add(createStatCard(SpriteRegistry.FIRE, fireValue, "INCENDIOS", RetroTheme.RED), 2, 1);

        // caja de dialogo con el ultimo evento, con la flechita parpadeando
        Label arrow = styled(new Label("▼"), "dialog-arrow");
        FadeTransition blink = new FadeTransition(Duration.millis(450), arrow);
        blink.setFromValue(1);
        blink.setToValue(0.1);
        blink.setAutoReverse(true);
        blink.setCycleCount(Animation.INDEFINITE);
        blink.play();
        eventText = styled(new Label("¡Bienvenido, operador! La ciudad esta tranquila... por ahora."), "dialog-text");
        Region push = new Region();
        HBox.setHgrow(push, Priority.ALWAYS);
        HBox dialogBottom = new HBox(push, arrow);
        VBox dialog = new VBox(eventText, dialogBottom);
        dialog.getStyleClass().add("dialog-box");

        Button mapButton = pixelButton("[M] MAPA DE TRAFICO", "button-blue");
        mapButton.setOnAction(e -> navigator.accept("map"));
        Button incidentsButton = pixelButton("[I] PANEL DE INCIDENTES", "button-yellow");
        incidentsButton.setOnAction(e -> navigator.accept("incidents"));
        Button spawnButton = pixelButton("[G] GENERAR INCIDENTE", null);
        spawnButton.setOnAction(e -> spawnIncident());
        HBox buttons = new HBox(14, mapButton, incidentsButton, spawnButton);

        GridPane keys = new GridPane();
        keys.setHgap(28);
        keys.setVgap(6);
        String[][] controls = {
                {"W A S D / FLECHAS", "MOVER AL OPERADOR"}, {"U", "DESHACER MOVIMIENTO"},
                {"G", "GENERAR INCIDENTE"}, {"C  M  I", "CAMBIAR DE PANTALLA"},
                {"P", "PAUSAR SIMULACION"}, {"ENTER / ESC", "DESPACHAR / CANCELAR"}
        };
        for (int i = 0; i < controls.length; i++) {
            Label key = styled(new Label(controls[i][0]), "hud-key-active");
            Label action = styled(new Label(controls[i][1]), "stat-name");
            keys.add(new HBox(10, key, action), i % 2, i / 2);
        }
        VBox controlsPanel = new VBox(10, styled(new Label("▶ CONTROLES"), "panel-title"), keys);
        controlsPanel.getStyleClass().add("pixel-panel");

        left.getChildren().addAll(header, stats, dialog, buttons, controlsPanel);

        // columna derecha: el incidente mas urgente y la flota
        priorityBox = new VBox(10);
        VBox priorityPanel = new VBox(10, styled(new Label("★ MAYOR PRIORIDAD"), "panel-title"), priorityBox);
        priorityPanel.getStyleClass().add("pixel-panel");

        fleetBox = new VBox(6);
        VBox fleetPanel = new VBox(10, styled(new Label("■ FLOTA"), "panel-title"), fleetBox);
        fleetPanel.getStyleClass().add("pixel-panel");

        VBox right = new VBox(18, priorityPanel, fleetPanel);
        right.setMinWidth(360);
        right.setPrefWidth(360);

        root = new HBox(24, left, right);
        root.setPadding(new Insets(24));
        root.setStyle("-fx-background-color: #000000;");
        refresh();
    }

    @Override
    public Node getView() {
        return root;
    }

    @Override
    public void refresh() {
        scoreValue.setText(String.format("%06d", controller.getScore()));
        activeValue.setText(String.format("%02d", controller.getActiveIncidentCount()));
        accidentValue.setText(String.format("%02d", controller.getActiveAccidentCount()));
        theftValue.setText(String.format("%02d", controller.getActiveTheftCount()));
        fireValue.setText(String.format("%02d", controller.getActiveFireCount()));
        freeValue.setText(String.format("%02d", controller.getAvailableVehicleCount()));
        refreshPriority();
        refreshFleet();
    }

    @Override
    public boolean handleKey(KeyCode code) {
        if (code == KeyCode.G) {
            spawnIncident();
            return true;
        }
        return false;
    }

    // Cambia el texto de la caja de dialogo.
    public void showEvent(String message) {
        eventText.setText(message);
    }

    // Genera un incidente aleatorio y lo cuenta en la caja de dialogo.
    private void spawnIncident() {
        Incident incident = controller.spawnRandomIncident();
        showEvent("¡ALERTA! " + incident.getDescription() + " (" + incident.getId() + ")");
        refresh();
    }

    // Muestra el incidente activo mas grave, o un mensaje si no hay ninguno.
    private void refreshPriority() {
        priorityBox.getChildren().clear();
        Incident top = controller.getHighestPriorityIncidentOrNull();
        if (top == null) {
            priorityBox.getChildren().add(styled(new Label("Sin incidentes activos.\nPresiona [G] para simular uno."), "incident-detail"));
            return;
        }
        Label id = styled(new Label(top.getId()), "incident-id");
        Label severity = severityBadge(top.getSeverity());
        VBox text = new VBox(4, new HBox(8, id, severity),
                styled(new Label(top.getLocation()), "incident-detail"),
                styled(new Label("ESTADO: " + Texts.of(top.getStatus())), "incident-detail"));
        HBox card = new HBox(12, SpriteRegistry.createIcon(SpriteRegistry.forIncident(top.getType()), 48), text);
        card.setAlignment(Pos.CENTER_LEFT);
        card.getStyleClass().addAll("incident-card", "incident-card-top");
        priorityBox.getChildren().add(card);
    }

    // Lista los vehiculos con su icono y estado.
    private void refreshFleet() {
        fleetBox.getChildren().clear();
        for (Vehicle vehicle : controller.getVehicles()) {
            Label name = styled(new Label(vehicle.getId()), "incident-id");
            Label status = styled(new Label(Texts.of(vehicle.getStatus())), "badge");
            status.getStyleClass().add(vehicle.getStatus() == VehicleStatus.AVAILABLE ? "badge-low" : "badge-status");
            Region push = new Region();
            HBox.setHgrow(push, Priority.ALWAYS);
            HBox row = new HBox(10, SpriteRegistry.createIcon(SpriteRegistry.forVehicle(vehicle.getType()), 28), name, push, status);
            row.setAlignment(Pos.CENTER_LEFT);
            fleetBox.getChildren().add(row);
        }
    }

    // Crea una tarjeta de indicador: icono, numero grande y nombre.
    private VBox createStatCard(String sprite, Label value, String name, javafx.scene.paint.Color color) {
        value.getStyleClass().add("stat-value");
        value.setTextFill(color);
        HBox top = new HBox(12, SpriteRegistry.createIcon(sprite, 40), value);
        top.setAlignment(Pos.CENTER_LEFT);
        VBox card = new VBox(top, styled(new Label(name), "stat-name"));
        card.getStyleClass().add("stat-card");
        return card;
    }

    static Label severityBadge(Severity severity) {
        Label badge = new Label(Texts.of(severity));
        badge.getStyleClass().add("badge");
        if (severity == Severity.HIGH) {
            badge.getStyleClass().add("badge-high");
        } else if (severity == Severity.MEDIUM) {
            badge.getStyleClass().add("badge-medium");
        } else {
            badge.getStyleClass().add("badge-low");
        }
        return badge;
    }

    static Button pixelButton(String text, String colorClass) {
        Button button = new Button(text);
        button.getStyleClass().add("pixel-button");
        if (colorClass != null) {
            button.getStyleClass().add(colorClass);
        }
        button.setFocusTraversable(false);
        return button;
    }

    static Label styled(Label label, String styleClass) {
        label.getStyleClass().add(styleClass);
        return label;
    }
}
