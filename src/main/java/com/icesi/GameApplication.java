package com.icesi;

import com.icesi.controller.GameController;
import com.icesi.controller.IncidentSpawner;
import com.icesi.controller.ShiftTimer;
import com.icesi.persistence.HighScoreStore;
import com.icesi.ui.BinaryTreeScreen;
import com.icesi.ui.CityMapView;
import com.icesi.ui.CrtOverlay;
import com.icesi.ui.EventLog;
import com.icesi.ui.GameScreen;
import com.icesi.ui.HudBar;
import com.icesi.ui.IncidentPanelScreen;
import com.icesi.ui.MobilityDialog;
import com.icesi.ui.MonitoringCenterScreen;
import com.icesi.ui.StatisticsDialog;
import com.icesi.ui.PixelCursor;
import com.icesi.ui.PixelTransition;
import com.icesi.model.Incident;
import com.icesi.model.Vehicle;
import com.icesi.ui.RetroSound;
import com.icesi.ui.RetroTheme;
import com.icesi.ui.HelpView;
import com.icesi.ui.IncidentRegistrationDialog;
import com.icesi.ui.OptionsView;
import com.icesi.ui.TrafficSimulationDialog;
import com.icesi.ui.VehicleRegistrationDialog;
import com.icesi.ui.PauseView;
import com.icesi.ui.ShiftEndView;
import com.icesi.ui.SplitViewScreen;
import com.icesi.ui.SpriteRegistry;
import com.icesi.ui.Texts;
import com.icesi.ui.ToastLayer;
import com.icesi.ui.TitleScreen;
import javafx.animation.Animation;
import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

// Ventana principal del juego: pantalla de titulo, barra HUD y cambio entre las pantallas (vista del MVC).
public class GameApplication extends Application {

    private GameController controller;
    private HudBar hud;
    private StackPane content;
    private TitleScreen titleScreen;
    private ToastLayer toasts;
    private StackPane root;
    private MonitoringCenterScreen centerScreen;
    private EventLog eventLog = new EventLog();
    private boolean started;
    private boolean paused;

    // hilo que genera incidentes cada cierto tiempo (cada vez mas seguido segun el puntaje)
    private IncidentSpawner spawner;
    // reloj del turno: 5 minutos, tambien en su propio hilo
    private ShiftTimer shiftTimer;
    private ShiftEndView shiftEndView;
    private PauseView pauseView = new PauseView();
    private HelpView helpView = new HelpView();
    private OptionsView optionsView = new OptionsView();
    private IncidentRegistrationDialog registrationDialog = new IncidentRegistrationDialog();
    private VehicleRegistrationDialog vehicleDialog;
    private TrafficSimulationDialog trafficDialog;
    private MobilityDialog mobilityDialog;
    private StatisticsDialog statisticsDialog;
    private BinaryTreeScreen binaryTreeScreen;
    private CrtOverlay crtOverlay = new CrtOverlay();
    private static final String SAVE_FOLDER = "data/partida";
    private static final String BINARY_SAVE_FILE = "data/partida.dat";
    private HighScoreStore highScores = new HighScoreStore("data/highscore.txt");
    private boolean ended;
    private Stage stage;
    private CityMapView mapView;
    private Timeline refresher;

    private Map<String, GameScreen> screens = new HashMap<>();
    private GameScreen currentScreen;

    @Override
    public void start(Stage stage) {
        this.stage = stage;
        controller = new GameController();
        hud = new HudBar();
        content = new StackPane();

        // el mapa se crea una sola vez para que los vehiculos y el dia sean los mismos en todas las pantallas
        mapView = new CityMapView(controller);
        centerScreen = new MonitoringCenterScreen(controller, this::showScreen, eventLog);
        centerScreen.setOnOpenRegistration(this::openRegistrationDialog);
        centerScreen.setOnOpenSimulation(this::openTrafficSimulationDialog);
        screens.put("center", centerScreen);
        IncidentPanelScreen incidentPanel = new IncidentPanelScreen(controller, eventLog);
        incidentPanel.setOnOpenRegistration(this::openRegistrationDialog);
        incidentPanel.setNavigator(this::showScreen);
        screens.put("incidents", incidentPanel);
        // el incidente elegido en el panel se resalta en el mapa
        incidentPanel.setOnFocus(mapView::setFocusedIncident);
        // solo se puede resolver cuando la unidad ya llego en el mapa
        incidentPanel.setArrivalCheck(mapView::hasArrived);

        // el Mapa de Trafico muestra la ciudad y al lado los incidentes para despachar sin cambiar de pantalla
        SplitViewScreen splitScreen = new SplitViewScreen(controller, mapView, incidentPanel);
        splitScreen.setOnOpenMobility(this::openMobilityDialog);
        screens.put("map", splitScreen);

        // Pantalla visual del arbol binario BST de prioridad de incidentes
        binaryTreeScreen = new BinaryTreeScreen(controller, this::showScreen);
        binaryTreeScreen.setOnOpenRegistration(this::openRegistrationDialog);
        screens.put("tree", binaryTreeScreen);

        registrationDialog.setOnRegister(this::handleManualIncidentRegistration);
        registrationDialog.setOnCancel(() -> setPaused(false));

        vehicleDialog = new VehicleRegistrationDialog();
        centerScreen.setOnOpenVehicleRegistration(this::openVehicleRegistrationDialog);
        vehicleDialog.setOnVehicleRegistered(this::handleVehicleRegistration);
        vehicleDialog.setOnCancel(() -> setPaused(false));

        trafficDialog = new TrafficSimulationDialog(controller);
        trafficDialog.setOnAccidentGenerated(this::handleAccidentGenerated);
        trafficDialog.setOnClose(() -> setPaused(false));

        mobilityDialog = new MobilityDialog(controller, mapView);
        mobilityDialog.setOnClose(() -> setPaused(false));
        centerScreen.setOnOpenMobility(this::openMobilityDialog);

        statisticsDialog = new StatisticsDialog(controller, highScores);
        statisticsDialog.setOnClose(() -> setPaused(false));
        centerScreen.setOnOpenStatistics(this::openStatisticsDialog);

        // Interaccion directa con el mapa (demo del curso): clic en celda crea accidente o resalta incidente
        mapView.setOnCellClicked(this::handleMapCellClicked);
        mapView.setOnIncidentSelected(incidentId -> {
            incidentPanel.handleKey(KeyCode.TAB);
        });

        BorderPane game = new BorderPane(content);
        game.setTop(hud);

        titleScreen = new TitleScreen();
        titleScreen.setRecord(highScores.load());
        toasts = new ToastLayer();
        // la capa CRT va encima de todo para que se vea como un monitor viejo
        shiftEndView = new ShiftEndView();
        root = new StackPane(game, pauseView, helpView, registrationDialog, vehicleDialog, trafficDialog, mobilityDialog, statisticsDialog, toasts, shiftEndView, titleScreen.getView(), optionsView, crtOverlay);
        // con el mouse: pasar por encima marca la opcion y el clic la confirma
        titleScreen.getView().setOnMouseMoved(e -> titleScreen.selectAt(e.getY()));
        titleScreen.getView().setOnMouseClicked(e -> {
            if (titleScreen.selectAt(e.getY())) {
                chooseMenuOption();
            }
        });

        Scene scene = new Scene(root, 1280, 820);
        RetroTheme.apply(scene);
        PixelCursor.install(scene);
        scene.setOnKeyPressed(e -> handleKey(e.getCode()));
        // JavaFX usa TAB para mover el foco entre botones; aqui lo usamos para elegir incidente
        scene.addEventFilter(KeyEvent.KEY_PRESSED, e -> {
            if (e.getCode() == KeyCode.TAB) {
                handleKey(KeyCode.TAB);
                e.consume();
            }
        });

        showScreen("center");

        // la HUD y la pantalla actual se refrescan cuatro veces por segundo
        refresher = new Timeline(new KeyFrame(Duration.millis(250), e -> refreshAll()));
        refresher.setCycleCount(Animation.INDEFINITE);
        refresher.play();

        // la simulacion corre en otro hilo; lo que se dibuja se pasa al hilo de JavaFX con Platform.runLater
        spawner = new IncidentSpawner(controller, incident -> Platform.runLater(() -> announceIncident(incident)));
        // al cerrar la ventana se detiene el hilo
        shiftTimer = new ShiftTimer(() -> Platform.runLater(this::updateShift));
        stage.setOnCloseRequest(e -> {
            spawner.stop();
            shiftTimer.stop();
        });

        stage.setTitle("SGMMS - Centro de Monitoreo de Palmira");
        stage.setScene(scene);
        stage.setMinWidth(1100);
        stage.setMinHeight(760);
        stage.show();
        titleScreen.start();
    }

    // Quita la pantalla de titulo con una transicion de cuadros pixelados y empieza el juego.
    private void enterGame() {
        if (started) {
            return;
        }
        started = true;
        RetroSound.play(RetroSound.Effect.START);
        PixelTransition.play(root, () -> {
            // esto pasa cuando la pantalla esta toda negra
            titleScreen.stop();
            titleScreen.getView().setVisible(false);
            toasts.show("¡TURNO INICIADO! Protege Palmira.", SpriteRegistry.OPERATOR);
            eventLog.add("Turno iniciado. Unidades listas.", EventLog.SYSTEM);
            spawner.setFastMode(optionsView.isFastMode());
            spawner.start();
            shiftTimer.start();
        });
    }

    // Hace lo que dice la opcion marcada del menu de titulo.
    private void chooseMenuOption() {
        if (titleScreen.getSelectedOption().equals("SALIR")) {
            Platform.exit();
        } else if (titleScreen.getSelectedOption().equals("OPCIONES")) {
            RetroSound.play(RetroSound.Effect.MENU);
            optionsView.open();
        } else {
            enterGame();
        }
    }

    // Cada segundo: actualiza el reloj del HUD y revisa si el turno termino.
    private void updateShift() {
        hud.setShiftTime(shiftTimer.formatRemaining(), shiftTimer.getRemainingSeconds() <= 60);
        applyPenalties();
        checkShiftEnd();
    }

    // Quita puntos si hay incidentes ALTOS esperando unidad hace rato, y avisa.
    private void applyPenalties() {
        if (paused || ended) {
            return;
        }
        int penalty = controller.applyWaitPenalties(LocalDateTime.now());
        if (penalty > 0) {
            RetroSound.play(RetroSound.Effect.ERROR);
            toasts.show("-" + penalty + " PTS  ¡Hay un incidente ALTO sin unidad!", null);
            eventLog.add("Penalizacion -" + penalty + " PTS por espera", EventLog.ALERT);
            hud.refresh(controller);
        }
    }

    // Si se acabo el tiempo o la ciudad se saturo, se detiene todo y se muestra el resultado.
    private void checkShiftEnd() {
        if (ended || !started) {
            return;
        }
        ShiftTimer.Result result = shiftTimer.evaluate(controller.getActiveIncidentCount());
        if (result == ShiftTimer.Result.PLAYING) {
            return;
        }
        ended = true;
        spawner.stop();
        shiftTimer.stop();
        RetroSound.play(result == ShiftTimer.Result.OVERLOADED ? RetroSound.Effect.ERROR : RetroSound.Effect.RESOLVE);
        eventLog.add(result == ShiftTimer.Result.OVERLOADED ? "¡Ciudad saturada! Fin del turno." : "Fin del turno.", EventLog.SYSTEM);
        shiftEndView.show(result, controller.getScore());
        // record: si se supero se celebra, si no se muestra cual es
        int previous = highScores.load();
        Label recordLine = new Label();
        try {
            if (highScores.saveIfHigher(controller.getScore())) {
                recordLine.setText("★ ¡NUEVO RECORD! ★");
                recordLine.getStyleClass().add("shift-end-record-new");
            } else {
                recordLine.setText(String.format("RECORD  %06d", previous));
                recordLine.getStyleClass().add("shift-end-record");
            }
        } catch (RuntimeException e) {
            recordLine.setText("No se pudo guardar el record.");
            recordLine.getStyleClass().add("shift-end-record");
        }
        shiftEndView.addLine(recordLine);
    }

    // Teclas de la ventana de opciones: flechas para moverse, ENTER para cambiar, ESC para volver.
    private void handleOptionsKey(KeyCode code) {
        if (code == KeyCode.UP || code == KeyCode.W) {
            optionsView.move(-1);
            RetroSound.play(RetroSound.Effect.MENU);
        } else if (code == KeyCode.DOWN || code == KeyCode.S) {
            optionsView.move(1);
            RetroSound.play(RetroSound.Effect.MENU);
        } else if (code == KeyCode.ESCAPE) {
            optionsView.close();
        } else if (code == KeyCode.ENTER || code == KeyCode.SPACE) {
            String option = optionsView.getSelected();
            if (option.equals(OptionsView.SOUND)) {
                RetroSound.toggleMute();
                RetroSound.play(RetroSound.Effect.MENU);
            } else if (option.equals(OptionsView.CRT)) {
                // las lineas del monitor viejo se pueden quitar
                optionsView.setCrtOn(!optionsView.isCrtOn());
                crtOverlay.setVisible(optionsView.isCrtOn());
                RetroSound.play(RetroSound.Effect.MENU);
            } else if (option.equals(OptionsView.SPEED)) {
                // en RAPIDA los incidentes llegan mas seguido
                optionsView.setFastMode(!optionsView.isFastMode());
                RetroSound.play(RetroSound.Effect.MENU);
            } else {
                optionsView.close();
            }
            optionsView.refresh();
            hud.refresh(controller);
        }
    }

    // Pausa o reanuda los dos hilos (incidentes y reloj).
    private void setPaused(boolean value) {
        paused = value;
        spawner.setPaused(value);
        shiftTimer.setPaused(value);
    }

    // F5: guarda la partida en la carpeta data/partida.
    private void saveGame() {
        try {
            controller.saveGame(SAVE_FOLDER);
            RetroSound.play(RetroSound.Effect.RESOLVE);
            toasts.show("PARTIDA GUARDADA  [F9] para cargarla", null);
            eventLog.add("Partida guardada en " + SAVE_FOLDER, EventLog.SYSTEM);
        } catch (RuntimeException e) {
            RetroSound.play(RetroSound.Effect.ERROR);
            toasts.show("No se pudo guardar: " + e.getMessage(), null);
        }
    }

    // F9: carga la ultima partida guardada (si existe).
    private void loadGame() {
        try {
            controller.loadGame(SAVE_FOLDER);
            RetroSound.play(RetroSound.Effect.START);
            toasts.show("PARTIDA CARGADA", null);
            eventLog.add("Partida cargada.", EventLog.SYSTEM);
            refreshAll();
        } catch (RuntimeException e) {
            RetroSound.play(RetroSound.Effect.ERROR);
            toasts.show("No hay partida guardada para cargar (usa F5 primero).", null);
        }
    }

    // F6: guarda el estado en archivo binario serializable (.dat).
    private void saveGameBinary() {
        try {
            controller.saveGameBinary(BINARY_SAVE_FILE);
            RetroSound.play(RetroSound.Effect.RESOLVE);
            toasts.show("PARTIDA SERIALIZADA (.dat)  [F10] para cargarla", null);
            eventLog.add("Estado serializado en " + BINARY_SAVE_FILE, EventLog.SYSTEM);
        } catch (RuntimeException e) {
            RetroSound.play(RetroSound.Effect.ERROR);
            toasts.show("No se pudo serializar: " + e.getMessage(), null);
        }
    }

    // F10: carga el estado desde archivo binario serializable (.dat).
    private void loadGameBinary() {
        try {
            controller.loadGameBinary(BINARY_SAVE_FILE);
            RetroSound.play(RetroSound.Effect.START);
            toasts.show("PARTIDA BINARIA CARGADA (.dat)", null);
            eventLog.add("Estado binario cargado desde " + BINARY_SAVE_FILE, EventLog.SYSTEM);
            refreshAll();
        } catch (RuntimeException e) {
            RetroSound.play(RetroSound.Effect.ERROR);
            toasts.show("No hay partida binaria para cargar (usa F6 primero).", null);
        }
    }

    // Abre el formulario de registro de nuevos vehiculos.
    public void openVehicleRegistrationDialog() {
        if (!started || ended) {
            return;
        }
        setPaused(true);
        RetroSound.play(RetroSound.Effect.MENU);
        vehicleDialog.open();
    }

    // Procesa el nuevo vehiculo registrado.
    private void handleVehicleRegistration(Vehicle vehicle) {
        controller.registerVehicle(vehicle);
        setPaused(false);
        RetroSound.play(RetroSound.Effect.ALERT);
        toasts.show("¡NUEVA UNIDAD REGISTRADA! " + vehicle.getId() + " (" + Texts.of(vehicle.getType()) + ")",
                SpriteRegistry.forVehicle(vehicle.getType()));
        eventLog.add("Vehiculo " + vehicle.getId() + " agregado a la flota.", EventLog.SYSTEM);
        refreshAll();
    }

    // Empieza una partida nueva desde la pantalla de titulo, en la misma ventana.
    private void restart() {
        refresher.stop();
        mapView.stop();
        spawner.stop();
        shiftTimer.stop();
        new GameApplication().start(stage);
    }

    // Abre el formulario de registro manual de incidentes y pausa el juego mientras se llena.
    public void openRegistrationDialog() {
        if (!started || ended) {
            return;
        }
        setPaused(true);
        RetroSound.play(RetroSound.Effect.MENU);
        registrationDialog.open();
    }

    // Registra el incidente manual ingresado por el operador.
    private void handleManualIncidentRegistration(Incident incident) {
        controller.registerIncident(incident);
        setPaused(false);
        announceIncident(incident);
    }

    // Abre el simulador de accidentes bajo criterios operativos de transito (RF-4).
    public void openTrafficSimulationDialog() {
        if (!started || ended) {
            return;
        }
        setPaused(true);
        RetroSound.play(RetroSound.Effect.MENU);
        trafficDialog.open();
    }

    // Abre el dialogo de consulta de movilidad y red vial.
    public void openMobilityDialog() {
        if (!started || ended) {
            return;
        }
        setPaused(true);
        RetroSound.play(RetroSound.Effect.MENU);
        mobilityDialog.open();
    }

    // Abre la pantalla de estadisticas en tiempo real.
    public void openStatisticsDialog() {
        if (!started || ended) {
            return;
        }
        setPaused(true);
        RetroSound.play(RetroSound.Effect.MENU);
        statisticsDialog.open();
    }

    // Conmuta la visualizacion de corredores de emergencia en el mapa.
    public void toggleEmergencyCorridors() {
        if (!started || ended) {
            return;
        }
        mapView.toggleEmergencyCorridors();
        boolean active = mapView.isEmergencyCorridorsVisible();
        hud.setCorridorsActive(active);
        RetroSound.play(RetroSound.Effect.MENU);
        toasts.show(active ? "⚡ CORREDORES DE EMERGENCIA ACTIVADOS" : "CORREDORES DESACTIVADOS (VISTA NORMAL)", null);
        eventLog.add(active ? "Corredores de emergencia visibles." : "Corredores de emergencia ocultos.", EventLog.SYSTEM);
        refreshAll();
    }

    // Procesa el accidente detonado desde el simulador de transito.
    private void handleAccidentGenerated(Incident accident) {
        setPaused(false);
        announceIncident(accident);
    }

    // Genera un accidente directamente al hacer clic en una celda del mapa (demo del curso).
    private void handleMapCellClicked(int row, int col) {
        if (!started || ended || paused) {
            return;
        }
        try {
            Incident accident = controller.spawnAccidentAt(row, col);
            RetroSound.play(RetroSound.Effect.ALERT);
            announceIncident(accident);
            toasts.show("¡ACCIDENTE REPORTADO EN (" + row + ", " + col + ")!", SpriteRegistry.forIncident(accident.getType()));
        } catch (RuntimeException e) {
            toasts.show("No se pudo crear accidente: " + e.getMessage(), null);
        }
    }

    // Avisa al jugador del incidente que acaba de generar el hilo (corre en el hilo de JavaFX).
    private void announceIncident(Incident incident) {
        try {
            RetroSound.play(RetroSound.Effect.ALERT);
            toasts.show("¡NUEVO " + Texts.of(incident.getType()) + "! " + incident.getId() + " · " + incident.getLocation(),
                    SpriteRegistry.forIncident(incident.getType()));
            centerScreen.showEvent("¡ALERTA! " + incident.getDescription() + " (" + incident.getId() + ")");
            eventLog.add(Texts.of(incident.getType()) + " " + incident.getId() + " en " + incident.getLocation(), EventLog.ALERT);
            refreshAll();
            checkShiftEnd();
        } catch (RuntimeException e) {
            toasts.show("Error en la simulacion: " + e.getMessage(), null);
        }
    }

    // Teclas globales: navegacion entre pantallas; las demas se las pasa a la pantalla actual.
    private void handleKey(KeyCode code) {
        if (optionsView.isVisible()) {
            handleOptionsKey(code);
            return;
        }
        if (!started) {
            if (code == KeyCode.UP || code == KeyCode.W) {
                titleScreen.moveSelection(-1);
                RetroSound.play(RetroSound.Effect.MENU);
            } else if (code == KeyCode.DOWN || code == KeyCode.S) {
                titleScreen.moveSelection(1);
                RetroSound.play(RetroSound.Effect.MENU);
            } else if (code == KeyCode.ENTER || code == KeyCode.SPACE) {
                chooseMenuOption();
            }
            return;
        }
        if (ended) {
            // con el turno terminado solo sirven ENTER (otra vez) y ESC (salir)
            if (code == KeyCode.ENTER) {
                restart();
            } else if (code == KeyCode.ESCAPE) {
                Platform.exit();
            }
            return;
        }
        if (helpView.isVisible()) {
            // con la ayuda abierta solo sirven H y ESC para cerrarla
            if (code == KeyCode.H || code == KeyCode.ESCAPE) {
                helpView.setVisible(false);
                setPaused(false);
            }
            return;
        }
        if (registrationDialog.isVisible() || (vehicleDialog != null && vehicleDialog.isVisible()) || (trafficDialog != null && trafficDialog.isVisible()) || (mobilityDialog != null && mobilityDialog.isVisible()) || (statisticsDialog != null && statisticsDialog.isVisible())) {
            // los dialogos modales manejan sus propias teclas (ESC / ENTER)
            return;
        }
        if (code == KeyCode.H && !paused) {
            // la ayuda pausa el juego mientras se lee
            setPaused(true);
            helpView.setVisible(true);
            RetroSound.play(RetroSound.Effect.MENU);
            return;
        }
        if ((code == KeyCode.INSERT || code == KeyCode.ADD || code == KeyCode.PLUS) && !paused) {
            openRegistrationDialog();
            return;
        }
        if (code == KeyCode.T && !paused) {
            openTrafficSimulationDialog();
            return;
        }
        if (code == KeyCode.V && !paused) {
            openMobilityDialog();
            return;
        }
        if (code == KeyCode.E && !paused) {
            toggleEmergencyCorridors();
            return;
        }
        if (code == KeyCode.K && !paused) {
            openStatisticsDialog();
            return;
        }
        if (paused && code != KeyCode.P && code != KeyCode.N && code != KeyCode.F5 && code != KeyCode.F9 && code != KeyCode.F6 && code != KeyCode.F10) {
            return;
        }
        if (currentScreen != null && currentScreen.handleKey(code)) {
            refreshAll();
            return;
        }
        if (code == KeyCode.F5) {
            saveGame();
            return;
        }
        if (code == KeyCode.F9) {
            loadGame();
            return;
        }
        if (code == KeyCode.F6) {
            saveGameBinary();
            return;
        }
        if (code == KeyCode.F10) {
            loadGameBinary();
            return;
        }
        if (code == KeyCode.N) {
            // N prende o apaga los sonidos
            boolean muted = RetroSound.toggleMute();
            hud.refresh(controller);
            toasts.show(muted ? "SONIDO APAGADO  [N] para prender" : "SONIDO PRENDIDO", null);
            if (!muted) {
                RetroSound.play(RetroSound.Effect.MENU);
            }
            return;
        }
        if (code == KeyCode.P) {
            setPaused(!paused);
            pauseView.setPaused(paused);
            toasts.show(paused ? "|| SIMULACION EN PAUSA" : "► SIMULACION REANUDADA", null);
            eventLog.add(paused ? "Simulacion en pausa." : "Simulacion reanudada.", EventLog.SYSTEM);
            return;
        }
        if (code == KeyCode.C || code == KeyCode.DIGIT1) {
            showScreen("center");
        } else if (code == KeyCode.M || code == KeyCode.DIGIT2) {
            showScreen("map");
        } else if (code == KeyCode.I || code == KeyCode.DIGIT3) {
            showScreen("incidents");
        } else if (code == KeyCode.B || code == KeyCode.DIGIT4) {
            showScreen("tree");
        }
    }

    // Muestra la pantalla pedida con un pequeno desvanecido.
    public void showScreen(String name) {
        GameScreen screen = screens.get(name);
        if (screen == null) {
            return;
        }
        currentScreen = screen;
        screen.refresh();
        content.getChildren().setAll(screen.getView());
        FadeTransition fade = new FadeTransition(Duration.millis(220), screen.getView());
        fade.setFromValue(0.2);
        fade.setToValue(1);
        fade.play();
        hud.setActiveScreen(name);
        hud.refresh(controller);
    }

    private void refreshAll() {
        hud.refresh(controller);
        hud.setCorridorsActive(mapView.isEmergencyCorridorsVisible());
        if (currentScreen != null) {
            currentScreen.refresh();
        }
    }
}
