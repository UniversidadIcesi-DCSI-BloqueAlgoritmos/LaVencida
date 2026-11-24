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

    private CountingLabel scoreValue;
    private CountingLabel activeValue;
    private CountingLabel accidentValue;
    private CountingLabel theftValue;
    private CountingLabel fireValue;
    private CountingLabel freeValue;

    private VBox priorityBox;
    private VBox fleetBox;
    private Label eventText;
    private EventLog eventLog;
    private VBox logLines;
    private MiniMap miniMap;
    private Runnable onOpenRegistration;
    private Runnable onOpenSimulation;
    private Runnable onOpenVehicleRegistration;
    private Runnable onOpenMobility;

    // Arma la pantalla; navigator sirve para cambiar a "map" o "incidents".
    public MonitoringCenterScreen(GameController controller, Consumer<String> navigator, EventLog eventLog) {
        this.controller = controller;
        this.navigator = navigator;
        this.eventLog = eventLog;

        VBox left = new VBox(18);
        left.setPadding(new Insets(4));
        HBox.setHgrow(left, Priority.ALWAYS);

        VBox header = new VBox(6,
                PixelFont.createLabel("CENTRO DE MONITOREO", 5, RetroTheme.YELLOW, RetroTheme.PURPLE),
                styled(new Label("SISTEMA DE GESTION Y MONITOREO DE MOVILIDAD Y SEGURIDAD - PALMIRA"), "panel-subtitle"));

        // numeros que cuentan hacia arriba o hacia abajo cuando cambian
        scoreValue = new CountingLabel("%06d");
        activeValue = new CountingLabel("%02d");
        accidentValue = new CountingLabel("%02d");
        theftValue = new CountingLabel("%02d");
        fireValue = new CountingLabel("%02d");
        freeValue = new CountingLabel("%02d");

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
        Button registerButton = pixelButton("[+] REGISTRAR INCIDENTE", "button-green");
        registerButton.setOnAction(e -> {
            if (onOpenRegistration != null) {
                onOpenRegistration.run();
            }
        });
        Button simButton = pixelButton("[⚡] SIMULAR ACCIDENTE", null);
        simButton.setOnAction(e -> {
            if (onOpenSimulation != null) {
                onOpenSimulation.run();
            }
        });
        Button treeButton = pixelButton("[B] ARBOL BST", "button-yellow");
        treeButton.setOnAction(e -> navigator.accept("tree"));
        Button mobilityBtn = pixelButton("[V] MOVILIDAD", "button-blue");
        mobilityBtn.setOnAction(e -> {
            if (onOpenMobility != null) {
                onOpenMobility.run();
            }
        });
        HBox buttons = new HBox(8, mapButton, incidentsButton, treeButton, spawnButton, registerButton, simButton, mobilityBtn);

        // los controles van en una sola columna para que quepa el registro de eventos al lado
        GridPane keys = new GridPane();
        keys.setHgap(10);
        keys.setVgap(2);
        String[][] controls = {
                {"W A S D / FLECHAS", "MOVER AL OPERADOR"}, {"U", "DESHACER MOVIMIENTO"},
                {"G", "GENERAR INCIDENTE"}, {"[+] / INSERT", "REGISTRAR INCIDENTE"},
                {"[⚡] / [T]", "SIMULAR ACCIDENTES (RF-4)"}, {"L", "LIBERAR UNIDAD / CANCELAR"},
                {"C  M  I  B", "CAMBIAR DE PANTALLA"}, {"E / V", "CORREDORES Y MOVILIDAD"},
                {"P", "PAUSAR SIMULACION"}, {"ENTER / ESC", "DESPACHAR / CANCELAR"},
                {"N", "SONIDO ON / OFF"},
                {"F5 / F9", "GUARDAR / CARGAR JSON"}, {"F6 / F10", "GUARDAR / CARGAR .DAT"}
        };
        for (int i = 0; i < controls.length; i++) {
            Label key = styled(new Label(controls[i][0]), "hud-key-active");
            Label action = styled(new Label(controls[i][1]), "stat-name");
            keys.add(key, 0, i);
            keys.add(action, 1, i);
        }
        VBox controlsPanel = new VBox(10, styled(new Label("▶ CONTROLES"), "panel-title"), keys);
        controlsPanel.getStyleClass().add("pixel-panel");

        // registro de eventos tipo consola: las ultimas cosas que pasaron, la mas nueva arriba
        logLines = new VBox(4);
        VBox logPanel = new VBox(10, styled(new Label("▶ REGISTRO DE EVENTOS"), "panel-title"), logLines);
        logPanel.getStyleClass().add("pixel-panel");
        HBox.setHgrow(logPanel, Priority.ALWAYS);
        eventLog.setOnChange(this::refreshLog);

        HBox bottom = new HBox(16, controlsPanel, logPanel);
        left.getChildren().addAll(header, stats, dialog, buttons, bottom);

        // columna derecha: el incidente mas urgente y la flota
        priorityBox = new VBox(10);
        VBox priorityPanel = new VBox(10, styled(new Label("★ MAYOR PRIORIDAD"), "panel-title"), priorityBox);
        priorityPanel.getStyleClass().add("pixel-panel");

        fleetBox = new VBox(2);
        Region pushFleet = new Region();
        HBox.setHgrow(pushFleet, Priority.ALWAYS);
        Button addVehicleBtn = pixelButton("[+] NUEVO", "button-blue");
        addVehicleBtn.getStyleClass().add("button-small");
        addVehicleBtn.setOnAction(e -> {
            if (onOpenVehicleRegistration != null) {
                onOpenVehicleRegistration.run();
            }
        });
        HBox fleetHeader = new HBox(8, styled(new Label("■ FLOTA"), "panel-title"), pushFleet, addVehicleBtn);
        fleetHeader.setAlignment(Pos.CENTER_LEFT);
        VBox fleetPanel = new VBox(10, fleetHeader, fleetBox);
        fleetPanel.getStyleClass().add("pixel-panel");

        // mini-mapa con la leyenda de colores abajo
        miniMap = new MiniMap(controller);
        HBox miniLegend = new HBox(10, legendDot(RetroTheme.YELLOW, "TU"), legendDot(RetroTheme.BLUE, "UNIDAD"),
                legendDot(RetroTheme.RED, "ALTA"), legendDot(RetroTheme.ORANGE, "MEDIA"), legendDot(RetroTheme.GREEN, "BAJA"));
        HBox miniCenter = new HBox(miniMap);
        miniCenter.setAlignment(Pos.CENTER);
        VBox miniPanel = new VBox(10, styled(new Label("◆ MINI-MAPA"), "panel-title"), miniCenter, miniLegend);
        miniPanel.getStyleClass().add("pixel-panel");

        VBox right = new VBox(14, priorityPanel, fleetPanel, miniPanel);
        right.setMinWidth(360);
        right.setPrefWidth(360);

        root = new HBox(24, left, right);
        root.setPadding(new Insets(16, 24, 16, 24));
        root.getStyleClass().add("crt-background");
        refresh();
    }

    @Override
    public Node getView() {
        return root;
    }

    public void setOnOpenRegistration(Runnable onOpenRegistration) {
        this.onOpenRegistration = onOpenRegistration;
    }

    public void setOnOpenSimulation(Runnable onOpenSimulation) {
        this.onOpenSimulation = onOpenSimulation;
    }

    public void setOnOpenVehicleRegistration(Runnable onOpenVehicleRegistration) {
        this.onOpenVehicleRegistration = onOpenVehicleRegistration;
    }

    public void setOnOpenMobility(Runnable onOpenMobility) {
        this.onOpenMobility = onOpenMobility;
    }

    @Override
    public void refresh() {
        scoreValue.setValue(controller.getScore());
        activeValue.setValue(controller.getActiveIncidentCount());
        accidentValue.setValue(controller.getActiveAccidentCount());
        theftValue.setValue(controller.getActiveTheftCount());
        fireValue.setValue(controller.getActiveFireCount());
        freeValue.setValue(controller.getAvailableVehicleCount());
        refreshPriority();
        refreshFleet();
        refreshLog();
        miniMap.draw();
    }

    // Cuadrito de color con su nombre, para la leyenda del mini-mapa.
    private HBox legendDot(javafx.scene.paint.Color color, String text) {
        Region dot = new Region();
        dot.setMinSize(8, 8);
        dot.setMaxSize(8, 8);
        dot.setStyle("-fx-background-color: " + toHex(color) + ";");
        HBox item = new HBox(4, dot, styled(new Label(text), "stat-name"));
        item.setAlignment(Pos.CENTER_LEFT);
        return item;
    }

    // Muestra los ultimos eventos del registro con su hora y su color.
    private void refreshLog() {
        logLines.getChildren().clear();
        int shown = 0;
        for (EventLog.Entry entry : eventLog.getEntries()) {
            if (shown == 6) {
                break;
            }
            Label time = styled(new Label(entry.getTime()), "stat-name");
            Label text = styled(new Label(entry.getText()), "incident-detail");
            // se usa estilo en linea porque la hoja de estilos le gana a setTextFill
            text.setStyle("-fx-text-fill: " + toHex(entry.getColor()) + ";");
            // los eventos viejos se ven mas apagados
            HBox line = new HBox(8, time, text);
            line.setOpacity(1 - shown * 0.12);
            logLines.getChildren().add(line);
            shown++;
        }
        if (shown == 0) {
            logLines.getChildren().add(styled(new Label("Sin eventos todavia."), "incident-detail"));
        }
    }

    @Override
    public boolean handleKey(KeyCode code) {
        if (code == KeyCode.G) {
            spawnIncident();
            return true;
        }
        if (code == KeyCode.B) {
            navigator.accept("tree");
            return true;
        }
        if (code == KeyCode.V) {
            if (onOpenMobility != null) {
                onOpenMobility.run();
                return true;
            }
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
        RetroSound.play(RetroSound.Effect.ALERT);
        showEvent("¡ALERTA! " + incident.getDescription() + " (" + incident.getId() + ")");
        eventLog.add(Texts.of(incident.getType()) + " " + incident.getId() + " en " + incident.getLocation(), EventLog.ALERT);
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
            HBox row = new HBox(10, SpriteRegistry.createIcon(SpriteRegistry.forVehicle(vehicle.getType()), 24), name, push, status);
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

    // Convierte un color a texto "#rrggbb" para usarlo en los estilos.
    static String toHex(javafx.scene.paint.Color color) {
        return String.format("#%02x%02x%02x", (int) Math.round(color.getRed() * 255),
                (int) Math.round(color.getGreen() * 255), (int) Math.round(color.getBlue() * 255));
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
