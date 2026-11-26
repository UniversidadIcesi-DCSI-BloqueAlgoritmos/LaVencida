package com.icesi.ui;

import com.icesi.controller.GameController;
import com.icesi.exceptions.IncidentNotFoundException;
import com.icesi.model.Incident;
import com.icesi.model.IncidentStatus;
import com.icesi.model.Vehicle;
import com.icesi.model.VehicleStatus;
import com.icesi.model.VehicleType;
import com.icesi.structures.LinkedList;
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
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
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
    // incidente marcado con TAB o con clic: se resalta en el mapa y R lo resuelve
    private String focusedId;
    private Map<String, Button> actionButtons = new HashMap<>();
    private Map<String, Node> cards = new HashMap<>();
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

    // Busqueda binaria por ID y boton de registro manual
    private TextField searchInput;
    private Label searchFeedback;
    private Runnable onOpenRegistration;
    private Consumer<Incident> onOpenEdit;
    private Consumer<String> navigator;

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

        // Barra de busqueda binaria por ID y acceso a registrar
        searchInput = new TextField();
        searchInput.setPromptText("Buscar ID (ej. THEFT-01)...");
        searchInput.getStyleClass().add("pixel-input");
        HBox.setHgrow(searchInput, Priority.ALWAYS);

        Button searchBtn = pixelButton("BUSCAR", "button-blue");
        searchBtn.getStyleClass().add("button-small");
        searchBtn.setOnAction(e -> executeBinarySearch());

        Button registerBtn = pixelButton("+ REGISTRAR", "button-green");
        registerBtn.getStyleClass().add("button-small");
        registerBtn.setOnAction(e -> {
            if (onOpenRegistration != null) {
                onOpenRegistration.run();
            }
        });

        Button bstBtn = pixelButton("[B] VER BST", "button-yellow");
        bstBtn.getStyleClass().add("button-small");
        bstBtn.setOnAction(e -> {
            if (navigator != null) {
                navigator.accept("tree");
            }
        });

        Button clearBtn = pixelButton("LIMPIAR", "button-gray");
        clearBtn.getStyleClass().add("button-small");
        clearBtn.setOnAction(e -> clearSearch());

        searchInput.setOnAction(e -> executeBinarySearch());

        HBox searchRow = new HBox(8, searchInput, searchBtn, clearBtn);
        searchRow.setAlignment(Pos.CENTER_LEFT);

        FlowPane quickActions = new FlowPane(8, 6, registerBtn, bstBtn);
        quickActions.setAlignment(Pos.CENTER_LEFT);

        searchFeedback = styled(new Label(), "search-feedback-found");
        searchFeedback.setVisible(false);
        searchFeedback.setManaged(false);
        searchFeedback.setWrapText(true);

        VBox searchSection = new VBox(6, searchRow, quickActions, searchFeedback);
        searchSection.getStyleClass().add("search-bar");

        left = new VBox(14, fullTitle, subtitle, searchSection, scroll);
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

    // Configura la accion para abrir el formulario de registro manual.
    public void setOnOpenRegistration(Runnable onOpenRegistration) {
        this.onOpenRegistration = onOpenRegistration;
    }

    // Configura la accion para abrir el formulario en modo actualizacion/edicion.
    public void setOnOpenEdit(Consumer<Incident> onOpenEdit) {
        this.onOpenEdit = onOpenEdit;
    }

    public void setNavigator(Consumer<String> navigator) {
        this.navigator = navigator;
    }

    // RF3 / HU-06: Ejecuta la busqueda binaria por ID en el modelo y resalta el incidente.
    public void executeBinarySearch() {
        String query = searchInput.getText() != null ? searchInput.getText().trim() : "";
        if (query.isEmpty()) {
            clearSearch();
            return;
        }
        try {
            Incident found = controller.findIncidentById(query);
            RetroSound.play(RetroSound.Effect.START);
            searchFeedback.getStyleClass().setAll("search-feedback-found");
            searchFeedback.setText("★ INCIDENTE ENCONTRADO: " + found.getId() + " · " + Texts.of(found.getType())
                    + " (" + Texts.of(found.getSeverity()) + ") · " + Texts.of(found.getStatus()) + " · " + found.getLocation());
            searchFeedback.setVisible(true);
            searchFeedback.setManaged(true);

            if (found.getStatus() != IncidentStatus.RESOLVED) {
                focus(found.getId());
                if (selected == null && found.getStatus() == IncidentStatus.PENDING) {
                    openDispatch(found);
                }
            }
        } catch (IncidentNotFoundException e) {
            RetroSound.play(RetroSound.Effect.ERROR);
            searchFeedback.getStyleClass().setAll("search-feedback-error");
            searchFeedback.setText("✕ No existe ningún incidente con ID: '" + query + "'");
            searchFeedback.setVisible(true);
            searchFeedback.setManaged(true);
        } catch (RuntimeException e) {
            RetroSound.play(RetroSound.Effect.ERROR);
            searchFeedback.getStyleClass().setAll("search-feedback-error");
            searchFeedback.setText("✕ Error en búsqueda: " + e.getMessage());
            searchFeedback.setVisible(true);
            searchFeedback.setManaged(true);
        }
    }

    // Limpia la busqueda binaria y oculta el resultado.
    public void clearSearch() {
        searchInput.setText("");
        searchFeedback.setText("");
        searchFeedback.setVisible(false);
        searchFeedback.setManaged(false);
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
            layout.setPadding(new Insets(6, 0, 8, 0));
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
        actionButtons.clear();
        cards.clear();

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
        markFocusedCard();
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
            focus(null);
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
        if (code == KeyCode.TAB) {
            focusNext();
            return true;
        }
        if (code == KeyCode.B) {
            if (navigator != null) {
                navigator.accept("tree");
                return true;
            }
        }
        Incident focused = findActive(focusedId);
        if (code == KeyCode.ENTER && focused != null && focused.getStatus() == IncidentStatus.PENDING) {
            // ENTER sobre un incidente marcado con TAB abre su despacho
            openDispatch(focused);
            return true;
        }
        if (code == KeyCode.R && focused != null) {
            Button button = actionButtons.get(focusedId);
            if (button != null && !button.isDisabled() && focused.getStatus() != IncidentStatus.PENDING) {
                button.fire();
            } else {
                RetroSound.play(RetroSound.Effect.ERROR);
                dispatchText.setText(focused.getStatus() == IncidentStatus.PENDING
                        ? "Primero asigna una unidad a " + focused.getId() + " (ENTER)."
                        : "La unidad aun va en camino a " + focused.getId() + ".");
            }
            return true;
        }
        if (code == KeyCode.L && focused != null && focused.getStatus() != IncidentStatus.PENDING) {
            releaseVehicle(focused);
            return true;
        }
        return false;
    }

    // Marca un incidente: se resalta su tarjeta y se avisa al mapa.
    private void focus(String incidentId) {
        focusedId = incidentId;
        onFocus.accept(incidentId);
        markFocusedCard();
    }

    // TAB: pasa al siguiente incidente activo (vuelve al primero despues del ultimo).
    private void focusNext() {
        LinkedList<Incident> active = controller.getActiveIncidentsByPriority();
        if (active.isEmpty()) {
            return;
        }
        int next = 0;
        for (int i = 0; i < active.size(); i++) {
            if (active.get(i).getId().equals(focusedId)) {
                next = (i + 1) % active.size();
            }
        }
        RetroSound.play(RetroSound.Effect.MENU);
        focus(active.get(next).getId());
    }

    private Incident findActive(String incidentId) {
        if (incidentId == null) {
            return null;
        }
        for (Incident incident : controller.getActiveIncidentsByPriority()) {
            if (incident.getId().equals(incidentId)) {
                return incident;
            }
        }
        return null;
    }

    // Pone el brillo azul solo en la tarjeta marcada.
    private void markFocusedCard() {
        for (Map.Entry<String, Node> entry : cards.entrySet()) {
            entry.getValue().getStyleClass().remove("incident-card-focus");
            if (entry.getKey().equals(focusedId)) {
                entry.getValue().getStyleClass().add("incident-card-focus");
            }
        }
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

        Node actionNode;
        if (incident.getStatus() == IncidentStatus.PENDING) {
            Button action = pixelButton("ASIGNAR", "button-blue");
            action.setOnAction(e -> openDispatch(incident));
            action.setMinWidth(Region.USE_PREF_SIZE);
            actionButtons.put(incident.getId(), action);

            Button editBtn = pixelButton("✎ EDITAR", "button-yellow");
            editBtn.getStyleClass().add("button-small");
            editBtn.setOnAction(e -> {
                if (onOpenEdit != null) {
                    onOpenEdit.accept(incident);
                }
            });
            editBtn.setMinWidth(Region.USE_PREF_SIZE);

            HBox actionsBox = new HBox(6, action, editBtn);
            actionsBox.setAlignment(Pos.CENTER_RIGHT);
            actionNode = actionsBox;
        } else {
            Button action;
            if (!arrived(incident)) {
                // la unidad todavia va por las calles: no se puede resolver aun
                action = pixelButton("EN CAMINO...", "button-gray");
                action.setDisable(true);
            } else {
                action = pixelButton("RESOLVER", "button-green");
                action.setOnAction(e -> resolve(incident, action));
            }
            action.setMinWidth(Region.USE_PREF_SIZE);
            actionButtons.put(incident.getId(), action);

            // RF6 / HU-16: Boton para liberar manualmente la unidad y cancelar el despacho
            Button releaseBtn = pixelButton("[✕] LIBERAR", "button-gray");
            releaseBtn.getStyleClass().add("button-small");
            releaseBtn.setOnAction(e -> releaseVehicle(incident));
            releaseBtn.setMinWidth(Region.USE_PREF_SIZE);

            HBox actionsBox = new HBox(6, action, releaseBtn);
            actionsBox.setAlignment(Pos.CENTER_RIGHT);
            actionNode = actionsBox;
        }

        int iconSize = compact ? 32 : 48;
        HBox card = new HBox(compact ? 10 : 14, SpriteRegistry.createIcon(SpriteRegistry.forIncident(incident.getType()), iconSize), info, push, actionNode);
        card.setAlignment(Pos.CENTER_LEFT);
        card.getStyleClass().add("incident-card");
        // clic en la tarjeta: se resalta ese incidente en el mapa
        card.setOnMouseClicked(e -> focus(incident.getId()));
        cards.put(incident.getId(), card);
        if (isTop) {
            card.getStyleClass().add("incident-card-top");
        }
        if (incident.getSeverity() == com.icesi.model.Severity.HIGH) {
            card.getStyleClass().add("incident-card-high");
        }
        return card;
    }

    // RF6 / HU-16: Cancela el despacho del incidente y libera la unidad de vuelta a disponible.
    private void releaseVehicle(Incident incident) {
        if (incident == null || incident.getAssignedVehicle() == null) {
            return;
        }
        String vehicleId = incident.getAssignedVehicle().getId();
        controller.cancelIncidentDispatch(incident.getId());
        RetroSound.play(RetroSound.Effect.MENU);
        eventLog.add("Unidad " + vehicleId + " liberada de " + incident.getId() + " (vuelve a pendiente)", EventLog.SYSTEM);
        lastSignature = ""; // Forzar refresco
        refresh();
    }

    // Abre la caja de dialogo para elegir que vehiculo enviar al incidente.
    private void openDispatch(Incident incident) {
        RetroSound.play(RetroSound.Effect.MENU);
        selected = incident;
        focus(incident.getId());
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
            focus(null);
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
            int points = controller.resolveIncident(incident.getId(), LocalDateTime.now());
            boolean fast = controller.wasLastResolveFast();
            focus(null);
            Node card = cards.getOrDefault(incident.getId(), source.getParent());
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
            dispatchText.setText("¡" + incident.getId() + " resuelto! Ganaste " + points + " puntos."
                    + (fast ? " ¡BONUS POR RAPIDEZ +" + GameController.FAST_BONUS_POINTS + "!" : ""));
            eventLog.add(incident.getId() + " resuelto  +" + points + " PTS" + (fast ? " (RAPIDO)" : ""), EventLog.RESOLVED);
            RetroSound.play(RetroSound.Effect.RESOLVE);
            floatPoints("+" + points + " PTS", source, 0);
            if (fast) {
                // el "¡RAPIDO!" sale un poco mas abajo para que no se encime
                floatPoints("¡RAPIDO!", source, 30);
            }
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
    private void floatPoints(String text, Node source, double offsetY) {
        Label label = new Label(text);
        label.getStyleClass().add("stat-value");
        label.setTextFill(RetroTheme.YELLOW);
        var bounds = source.localToScene(source.getBoundsInLocal());
        var origin = effects.sceneToLocal(bounds.getMinX(), bounds.getMinY());
        label.setLayoutX(origin.getX() - 70);
        label.setLayoutY(origin.getY() - 10 + offsetY);
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
            text.append(incident.getId()).append('|')
                    .append(incident.getType()).append('|')
                    .append(incident.getSeverity()).append('|')
                    .append(incident.getLocation()).append('|')
                    .append(incident.getStatus()).append('|')
                    .append(arrived(incident)).append(';');
        }
        for (Vehicle vehicle : controller.getVehicles()) {
            text.append(vehicle.getStatus().ordinal());
        }
        return text.toString();
    }
}
