package com.icesi.ui;

import com.icesi.controller.GameController;
import com.icesi.model.Incident;
import com.icesi.model.IncidentStatus;
import com.icesi.model.Vehicle;
import com.icesi.model.VehicleStatus;
import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.TranslateTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.time.format.DateTimeFormatter;

import static com.icesi.ui.MonitoringCenterScreen.pixelButton;
import static com.icesi.ui.MonitoringCenterScreen.severityBadge;
import static com.icesi.ui.MonitoringCenterScreen.styled;

// Panel de Incidentes: lista por prioridad, asignacion de vehiculos con caja de dialogo tipo RPG y resolucion con puntos.
public class IncidentPanelScreen implements GameScreen {

    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("HH:mm:ss");

    private GameController controller;
    private EventLog eventLog;
    private StackPane root;
    private Pane effects;
    private VBox cardList;
    private VBox dispatchBox;
    private Label dispatchText;
    private FlowPane dispatchOptions;

    // incidente que se esta despachando en la caja de dialogo (null si no hay ninguno)
    private Incident selected;
    private String lastSignature = "";

    // Arma la pantalla: la lista de incidentes a la izquierda y el despacho a la derecha.
    public IncidentPanelScreen(GameController controller, EventLog eventLog) {
        this.controller = controller;
        this.eventLog = eventLog;

        cardList = new VBox(10);
        ScrollPane scroll = new ScrollPane(cardList);
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        VBox.setVgrow(scroll, Priority.ALWAYS);

        VBox left = new VBox(14,
                PixelFont.createLabel("PANEL DE INCIDENTES", 5, RetroTheme.YELLOW, RetroTheme.PURPLE),
                styled(new Label("ORDENADOS POR GRAVEDAD (ALTA > MEDIA > BAJA) Y ANTIGUEDAD - ARBOL BINARIO DE BUSQUEDA"), "panel-subtitle"),
                scroll);
        HBox.setHgrow(left, Priority.ALWAYS);

        dispatchText = styled(new Label(), "dialog-text");
        dispatchText.setMaxWidth(340);
        dispatchOptions = new FlowPane(10, 10);
        dispatchBox = new VBox(14, styled(new Label("▶ DESPACHO"), "panel-title"), dispatchText, dispatchOptions);
        dispatchBox.getStyleClass().add("dialog-box");
        dispatchBox.setPrefWidth(380);
        dispatchBox.setMinWidth(380);
        showIdleDialog();

        VBox right = new VBox(16, dispatchBox);
        HBox layout = new HBox(24, left, right);
        layout.setPadding(new Insets(20, 24, 20, 24));
        layout.setStyle("-fx-background-color: #000000;");

        // capa transparente encima para los "+120 PTS" que flotan
        effects = new Pane();
        effects.setMouseTransparent(true);
        root = new StackPane(layout, effects);
        refresh();
    }

    @Override
    public Node getView() {
        return root;
    }

    @Override
    public void refresh() {
        // solo se reconstruyen las tarjetas si algo cambio, para no perder clics ni el scroll
        String signature = buildSignature();
        if (signature.equals(lastSignature)) {
            return;
        }
        lastSignature = signature;
        cardList.getChildren().clear();

        boolean first = true;
        for (Incident incident : controller.getActiveIncidentsByPriority()) {
            cardList.getChildren().add(createCard(incident, first));
            first = false;
        }
        if (cardList.getChildren().isEmpty()) {
            Label empty = styled(new Label("No hay incidentes activos.\nVe al Centro de Monitoreo y presiona [G] para simular uno."), "dialog-text");
            VBox box = new VBox(empty);
            box.getStyleClass().add("pixel-panel");
            cardList.getChildren().add(box);
        }
    }

    @Override
    public boolean handleKey(KeyCode code) {
        if (code == KeyCode.ESCAPE && selected != null) {
            showIdleDialog();
            return true;
        }
        if (code == KeyCode.ENTER && selected != null) {
            try {
                dispatch(controller.proposeVehicle(selected));
            } catch (RuntimeException e) {
                showError(e.getMessage());
            }
            return true;
        }
        return false;
    }

    // Crea la tarjeta de un incidente con su icono, datos y botones.
    private HBox createCard(Incident incident, boolean isTop) {
        Label id = styled(new Label(incident.getId()), "incident-id");
        Label type = styled(new Label(Texts.of(incident.getType())), "incident-detail");
        Label status = styled(new Label(Texts.of(incident.getStatus())), "badge");
        status.getStyleClass().add("badge-status");
        HBox titleRow = new HBox(8, id, severityBadge(incident.getSeverity()), status, type);
        titleRow.setAlignment(Pos.CENTER_LEFT);

        String vehicleText = incident.getAssignedVehicle() == null ? "SIN ASIGNAR" : incident.getAssignedVehicle().getId();
        String time = incident.getGeneratedAt() == null ? "--:--:--" : incident.getGeneratedAt().format(TIME);
        VBox info = new VBox(4, titleRow,
                styled(new Label("◆ " + incident.getLocation()), "incident-detail"),
                styled(new Label("HORA " + time + "   UNIDAD: " + vehicleText), "incident-detail"));
        if (isTop) {
            info.getChildren().add(0, styled(new Label("★ MAYOR PRIORIDAD"), "hud-key-active"));
        }

        Region push = new Region();
        HBox.setHgrow(push, Priority.ALWAYS);

        Button action;
        if (incident.getStatus() == IncidentStatus.PENDING) {
            action = pixelButton("ASIGNAR", "button-blue");
            action.setOnAction(e -> openDispatch(incident));
        } else {
            action = pixelButton("RESOLVER", "button-green");
            action.setOnAction(e -> resolve(incident, action));
        }

        HBox card = new HBox(14, SpriteRegistry.createIcon(SpriteRegistry.forIncident(incident.getType()), 48), info, push, action);
        card.setAlignment(Pos.CENTER_LEFT);
        card.getStyleClass().add("incident-card");
        if (isTop) {
            card.getStyleClass().add("incident-card-top");
        }
        return card;
    }

    // Abre la caja de dialogo para elegir que vehiculo enviar al incidente.
    private void openDispatch(Incident incident) {
        selected = incident;
        dispatchOptions.getChildren().clear();

        String suggestion;
        try {
            Vehicle proposed = controller.proposeVehicle(incident);
            suggestion = "Unidad sugerida: " + proposed.getId() + " (" + Texts.of(proposed.getType()) + ").";
            Button send = pixelButton("► ENVIAR " + proposed.getId(), "button-green");
            send.setOnAction(e -> dispatch(proposed));
            dispatchOptions.getChildren().add(send);
        } catch (RuntimeException e) {
            suggestion = "¡No hay unidades compatibles libres!";
        }

        dispatchText.setText("► " + incident.getId() + " · " + Texts.of(incident.getType())
                + " en " + incident.getLocation() + ".\n" + suggestion + "\nO elige otra unidad libre:");

        // las demas unidades libres; si no es compatible el modelo lo rechaza con su excepcion
        for (Vehicle vehicle : controller.getVehicles()) {
            if (vehicle.getStatus() == VehicleStatus.AVAILABLE) {
                Button option = pixelButton(vehicle.getId(), null);
                option.setGraphic(SpriteRegistry.createIcon(SpriteRegistry.forVehicle(vehicle.getType()), 20));
                option.setOnAction(e -> dispatch(vehicle));
                dispatchOptions.getChildren().add(option);
            }
        }
        Button cancel = pixelButton("✕ CANCELAR", "button-yellow");
        cancel.setOnAction(e -> showIdleDialog());
        dispatchOptions.getChildren().add(cancel);
    }

    // Envia el vehiculo al incidente seleccionado.
    private void dispatch(Vehicle vehicle) {
        if (selected == null) {
            return;
        }
        try {
            controller.assignVehicle(vehicle.getId(), selected.getId());
            String message = "¡" + vehicle.getId() + " va en camino a " + selected.getLocation() + "!";
            eventLog.add(vehicle.getId() + " despachada a " + selected.getId(), EventLog.DISPATCH);
            showIdleDialog();
            dispatchText.setText(message);
            refresh();
        } catch (RuntimeException e) {
            showError(e.getMessage());
        }
    }

    // Finaliza la atencion y muestra los puntos ganados flotando sobre el boton.
    private void resolve(Incident incident, Button source) {
        try {
            int points = controller.resolveIncident(incident.getId());
            dispatchText.setText("¡" + incident.getId() + " resuelto! Ganaste " + points + " puntos.");
            eventLog.add(incident.getId() + " resuelto  +" + points + " PTS", EventLog.RESOLVED);
            floatPoints(points, source);
            refresh();
        } catch (RuntimeException e) {
            showError(e.getMessage());
        }
    }

    // Muestra el error del modelo en la caja de dialogo sin cerrar el juego.
    private void showError(String message) {
        dispatchText.setText("✕ ¡ERROR! " + message + ".\nIntenta con otra unidad.");
    }

    private void showIdleDialog() {
        selected = null;
        dispatchOptions.getChildren().clear();
        dispatchText.setText("Selecciona un incidente y presiona ASIGNAR para despachar una unidad.\n\n[ENTER] envia la unidad sugerida\n[ESC] cancela");
    }

    // Animacion de "+N PTS" que sube y se desvanece.
    private void floatPoints(int points, Node source) {
        Label label = new Label("+" + points + " PTS");
        label.getStyleClass().add("stat-value");
        label.setTextFill(RetroTheme.YELLOW);
        var bounds = source.localToScene(source.getBoundsInLocal());
        var origin = effects.sceneToLocal(bounds.getMinX(), bounds.getMinY());
        label.setLayoutX(origin.getX() - 30);
        label.setLayoutY(origin.getY() - 10);
        effects.getChildren().add(label);

        TranslateTransition rise = new TranslateTransition(Duration.millis(1100), label);
        rise.setByY(-70);
        FadeTransition fade = new FadeTransition(Duration.millis(1100), label);
        fade.setFromValue(1);
        fade.setToValue(0);
        ParallelTransition animation = new ParallelTransition(rise, fade);
        animation.setOnFinished(e -> effects.getChildren().remove(label));
        animation.play();
    }

    // Texto que resume el estado actual; si no cambia no hace falta redibujar.
    private String buildSignature() {
        StringBuilder text = new StringBuilder();
        for (Incident incident : controller.getActiveIncidentsByPriority()) {
            text.append(incident.getId()).append(incident.getStatus()).append(';');
        }
        for (Vehicle vehicle : controller.getVehicles()) {
            text.append(vehicle.getStatus().ordinal());
        }
        return text.toString();
    }
}
