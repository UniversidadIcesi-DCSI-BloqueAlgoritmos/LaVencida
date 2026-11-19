package com.icesi.ui;

import com.icesi.controller.GameController;
import com.icesi.model.Incident;
import com.icesi.model.IncidentStatus;
import com.icesi.model.Vehicle;
import com.icesi.model.VehicleStatus;
import com.icesi.model.VehicleType;
import javafx.animation.Animation;
import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
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
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;

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
    // incidentes que ya tienen tarjeta en pantalla: los nuevos entran con animacion
    private Set<String> shownIds = new HashSet<>();
    private boolean pulseOn;
    private EmptyStateView emptyState;
    private boolean cardLeaving; // mientras una tarjeta sale volando no se redibuja la lista

    // modo compacto: el despacho va debajo de la lista para que quepa al lado del mapa
    private boolean compact;
    private StackPane layoutSlot;
    private VBox left;
    private Node fullTitle;
    private Node compactTitle;
    private Label subtitle;

    // avisa a quien le interese (el mapa) cual incidente se eligio; null si ninguno
    private Consumer<String> onFocus = id -> { };
    // pregunta al mapa si la unidad ya llego; solo entonces se puede resolver
    private Predicate<Vehicle> hasArrived = vehicle -> true;
    // incidentes cuya unidad ya llego, para avisar una sola vez
    private Set<String> arrivedIds = new HashSet<>();

    // Arma la pantalla: la lista de incidentes a la izquierda y el despacho a la derecha.
    public IncidentPanelScreen(GameController controller, EventLog eventLog) {
        this.controller = controller;
        this.eventLog = eventLog;

        cardList = new VBox(10);
        ScrollPane scroll = new ScrollPane(cardList);
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        VBox.setVgrow(scroll, Priority.ALWAYS);

        fullTitle = PixelFont.createLabel("PANEL DE INCIDENTES", 5, RetroTheme.YELLOW, RetroTheme.PURPLE);
        compactTitle = PixelFont.createLabel("INCIDENTES", 3, RetroTheme.YELLOW, RetroTheme.PURPLE);
        subtitle = styled(new Label("ORDENADOS POR GRAVEDAD (ALTA > MEDIA > BAJA) Y ANTIGUEDAD - ARBOL BINARIO DE BUSQUEDA"), "panel-subtitle");
        left = new VBox(14, fullTitle, subtitle, scroll);
        HBox.setHgrow(left, Priority.ALWAYS);
        VBox.setVgrow(left, Priority.ALWAYS);

        dispatchText = styled(new Label(), "dialog-text");
        dispatchText.setMaxWidth(340);
        dispatchOptions = new FlowPane(10, 10);
        dispatchBox = new VBox(14, styled(new Label("▶ DESPACHO"), "panel-title"), dispatchText, dispatchOptions);
        dispatchBox.getStyleClass().add("dialog-box");
        dispatchBox.setPrefWidth(380);
        dispatchBox.setMinWidth(380);
        showIdleDialog();

        layoutSlot = new StackPane();
        layoutSlot.getStyleClass().add("crt-background");
        arrangeLayout();

        // capa transparente encima para los "+120 PTS" que flotan
        effects = new Pane();
        effects.setMouseTransparent(true);
        root = new StackPane(layoutSlot, effects);

        // las tarjetas de gravedad ALTA titilan en rojo para que se vean urgentes
        Timeline pulse = new Timeline(new KeyFrame(Duration.millis(450), e -> pulseHighCards()));
        pulse.setCycleCount(Animation.INDEFINITE);
        pulse.play();
        refresh();
    }

    // El mapa usa esto para resaltar el incidente elegido.
    public void setOnFocus(Consumer<String> onFocus) {
        this.onFocus = onFocus;
    }

    // El mapa dice cuando una unidad ya llego al incidente.
    public void setArrivalCheck(Predicate<Vehicle> hasArrived) {
        this.hasArrived = hasArrived;
    }

    @Override
    public Node getView() {
        setCompact(false);
        return root;
    }

    // La misma pantalla pero angosta, para ponerla al lado del mapa en la vista dividida.
    public Node getCompactView() {
        setCompact(true);
        return root;
    }

    private void setCompact(boolean value) {
        if (compact != value) {
            compact = value;
            arrangeLayout();
            // las tarjetas se vuelven a armar con el tamano nuevo
            lastSignature = "";
            refresh();
        }
    }

    // Normal: lista a la izquierda y despacho a la derecha. Compacto: despacho debajo de la lista.
    private void arrangeLayout() {
        if (compact) {
            left.getChildren().set(0, compactTitle);
            subtitle.setVisible(false);
            subtitle.setManaged(false);
            dispatchBox.setMinWidth(0);
            dispatchBox.setPrefWidth(Region.USE_COMPUTED_SIZE);
            VBox layout = new VBox(12, left, dispatchBox);
            layout.setPadding(new Insets(8, 16, 8, 0));
            layoutSlot.getChildren().setAll(layout);
        } else {
            left.getChildren().set(0, fullTitle);
            subtitle.setVisible(true);
            subtitle.setManaged(true);
            dispatchBox.setPrefWidth(380);
            dispatchBox.setMinWidth(380);
            HBox layout = new HBox(24, left, new VBox(16, dispatchBox));
            layout.setPadding(new Insets(20, 24, 20, 24));
            layoutSlot.getChildren().setAll(layout);
        }
    }

    @Override
    public void refresh() {
        // solo se reconstruyen las tarjetas si algo cambio, para no perder clics ni el scroll
        checkArrivals();
        String signature = buildSignature();
        if (cardLeaving || signature.equals(lastSignature)) {
            return;
        }
        lastSignature = signature;
        cardList.getChildren().clear();

        boolean first = true;
        int newCards = 0;
        Set<String> currentIds = new HashSet<>();
        for (Incident incident : controller.getActiveIncidentsByPriority()) {
            HBox card = createCard(incident, first);
            cardList.getChildren().add(card);
            currentIds.add(incident.getId());
            if (!shownIds.contains(incident.getId())) {
                slideIn(card, newCards * 80);
                newCards++;
            }
            first = false;
        }
        shownIds = currentIds;
        if (cardList.getChildren().isEmpty()) {
            // sin incidentes: se muestra el operador descansando en vez de una lista vacia
            if (emptyState == null) {
                emptyState = new EmptyStateView();
            }
            cardList.getChildren().add(emptyState);
        }
    }

    @Override
    public boolean handleKey(KeyCode code) {
        if (code == KeyCode.ESCAPE && selected != null) {
            showIdleDialog();
            onFocus.accept(null);
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
        HBox titleRow = new HBox(8, id, severityBadge(incident.getSeverity()), status);
        if (!compact) {
            titleRow.getChildren().add(type);
        }
        titleRow.setAlignment(Pos.CENTER_LEFT);
        // los textos de la fila de arriba nunca se cortan con "..."
        for (Node node : titleRow.getChildren()) {
            ((Region) node).setMinWidth(Region.USE_PREF_SIZE);
        }

        String vehicleText = incident.getAssignedVehicle() == null ? "SIN ASIGNAR" : incident.getAssignedVehicle().getId();
        String time = incident.getGeneratedAt() == null ? "--:--:--" : incident.getGeneratedAt().format(TIME);
        // en modo compacto el tipo va junto al lugar para ahorrar espacio
        String place = compact ? Texts.of(incident.getType()) + " · " + incident.getLocation() : incident.getLocation();
        VBox info = new VBox(4, titleRow,
                styled(new Label("◆ " + place), "incident-detail"),
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
        } else if (!arrived(incident)) {
            // la unidad todavia va por las calles: no se puede resolver aun
            action = pixelButton("EN CAMINO...", "button-gray");
            action.setDisable(true);
        } else {
            action = pixelButton("RESOLVER", "button-green");
            action.setOnAction(e -> resolve(incident, action));
        }

        action.setMinWidth(Region.USE_PREF_SIZE);
        int iconSize = compact ? 32 : 48;
        HBox card = new HBox(compact ? 10 : 14, SpriteRegistry.createIcon(SpriteRegistry.forIncident(incident.getType()), iconSize), info, push, action);
        card.setAlignment(Pos.CENTER_LEFT);
        card.getStyleClass().add("incident-card");
        // clic en la tarjeta: se resalta ese incidente en el mapa
        card.setOnMouseClicked(e -> onFocus.accept(incident.getId()));
        if (isTop) {
            card.getStyleClass().add("incident-card-top");
        }
        if (incident.getSeverity() == com.icesi.model.Severity.HIGH) {
            card.getStyleClass().add("incident-card-high");
        }
        return card;
    }

    // Abre la caja de dialogo para elegir que vehiculo enviar al incidente.
    private void openDispatch(Incident incident) {
        RetroSound.play(RetroSound.Effect.MENU);
        selected = incident;
        onFocus.accept(incident.getId());
        dispatchOptions.getChildren().clear();

        String suggestion;
        Vehicle proposed = null;
        try {
            proposed = controller.proposeVehicle(incident);
            suggestion = "Unidad sugerida: " + proposed.getId() + " (" + Texts.of(proposed.getType()) + ").";
            Vehicle suggested = proposed;
            Button send = pixelButton("► ENVIAR " + proposed.getId(), "button-green");
            send.setOnAction(e -> dispatch(suggested));
            dispatchOptions.getChildren().add(send);
        } catch (RuntimeException e) {
            suggestion = "¡No hay unidades compatibles libres!";
        }

        dispatchText.setText("► " + incident.getId() + " · " + Texts.of(incident.getType())
                + " en " + incident.getLocation() + ".\n" + suggestion + "\nO elige otra unidad libre:");

        // las demas unidades libres (sin repetir la sugerida); si no es compatible el modelo lo rechaza con su excepcion
        for (Vehicle vehicle : controller.getVehicles()) {
            if (vehicle.getStatus() == VehicleStatus.AVAILABLE && vehicle != proposed) {
                Button option = pixelButton(vehicle.getId(), buttonClassOf(vehicle.getType()));
                option.setGraphic(SpriteRegistry.createIcon(SpriteRegistry.forVehicle(vehicle.getType()), 20));
                option.setOnAction(e -> dispatch(vehicle));
                dispatchOptions.getChildren().add(option);
            }
        }
        Button cancel = pixelButton("✕ CANCELAR", "button-yellow");
        cancel.setOnAction(e -> {
            showIdleDialog();
            onFocus.accept(null);
        });
        dispatchOptions.getChildren().add(cancel);
    }

    // Cada tipo de unidad tiene su color: patrullas azul, ambulancias blanco con rojo y bomberos rojo.
    private String buttonClassOf(VehicleType type) {
        if (type == VehicleType.AMBULANCE) {
            return "button-ambulance";
        }
        if (type == VehicleType.FIRE_TRUCK) {
            return "button-fire";
        }
        return "button-patrol";
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
            RetroSound.play(RetroSound.Effect.DISPATCH);
            showIdleDialog();
            dispatchText.setText(message);
            refresh();
        } catch (RuntimeException e) {
            showError(e.getMessage());
        }
    }

    // La tarjeta nueva entra deslizandose desde la izquierda; delay hace que entren una tras otra.
    private void slideIn(Node card, double delay) {
        card.setOpacity(0);
        TranslateTransition move = new TranslateTransition(Duration.millis(260), card);
        move.setFromX(-60);
        move.setToX(0);
        FadeTransition fade = new FadeTransition(Duration.millis(260), card);
        fade.setFromValue(0);
        fade.setToValue(1);
        ParallelTransition enter = new ParallelTransition(move, fade);
        enter.setDelay(Duration.millis(delay));
        enter.play();
    }

    // Prende y apaga el borde rojo de las tarjetas de gravedad ALTA.
    private void pulseHighCards() {
        pulseOn = !pulseOn;
        for (Node node : cardList.getChildren()) {
            if (node.getStyleClass().contains("incident-card-high")) {
                node.getStyleClass().remove("incident-card-pulse");
                if (pulseOn) {
                    node.getStyleClass().add("incident-card-pulse");
                }
            }
        }
    }

    // Finaliza la atencion y muestra los puntos ganados flotando sobre el boton.
    // La tarjeta sale volando a la derecha antes de desaparecer de la lista.
    private void resolve(Incident incident, Button source) {
        try {
            int points = controller.resolveIncident(incident.getId());
            onFocus.accept(null);
            Node card = source.getParent();
            source.setDisable(true);
            TranslateTransition move = new TranslateTransition(Duration.millis(320), card);
            move.setToX(500);
            FadeTransition fade = new FadeTransition(Duration.millis(320), card);
            fade.setToValue(0);
            ParallelTransition exit = new ParallelTransition(move, fade);
            cardLeaving = true;
            exit.setOnFinished(e -> {
                cardLeaving = false;
                refresh();
            });
            exit.play();
            dispatchText.setText("¡" + incident.getId() + " resuelto! Ganaste " + points + " puntos.");
            eventLog.add(incident.getId() + " resuelto  +" + points + " PTS", EventLog.RESOLVED);
            RetroSound.play(RetroSound.Effect.RESOLVE);
            floatPoints(points, source);
        } catch (RuntimeException e) {
            showError(e.getMessage());
        }
    }

    // Muestra el error del modelo en la caja de dialogo sin cerrar el juego.
    private void showError(String message) {
        RetroSound.play(RetroSound.Effect.ERROR);
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

    private boolean arrived(Incident incident) {
        Vehicle vehicle = incident.getAssignedVehicle();
        return vehicle == null || hasArrived.test(vehicle);
    }

    // Avisa con un sonido y en el registro cuando una unidad llega a su incidente.
    private void checkArrivals() {
        Set<String> nowArrived = new HashSet<>();
        for (Incident incident : controller.getActiveIncidentsByPriority()) {
            if (incident.getStatus() != IncidentStatus.PENDING && arrived(incident)) {
                nowArrived.add(incident.getId());
                if (!arrivedIds.contains(incident.getId())) {
                    eventLog.add(incident.getAssignedVehicle().getId() + " llego a " + incident.getId(), EventLog.DISPATCH);
                    RetroSound.play(RetroSound.Effect.MENU);
                }
            }
        }
        arrivedIds = nowArrived;
    }

    // Texto que resume el estado actual; si no cambia no hace falta redibujar.
    private String buildSignature() {
        StringBuilder text = new StringBuilder();
        for (Incident incident : controller.getActiveIncidentsByPriority()) {
            text.append(incident.getId()).append(incident.getStatus()).append(arrived(incident)).append(';');
        }
        for (Vehicle vehicle : controller.getVehicles()) {
            text.append(vehicle.getStatus().ordinal());
        }
        return text.toString();
    }
}
