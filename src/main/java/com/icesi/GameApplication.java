package com.icesi;

import com.icesi.controller.GameController;
import com.icesi.controller.IncidentSpawner;
import com.icesi.controller.ShiftTimer;
import com.icesi.ui.CityMapView;
import com.icesi.ui.CrtOverlay;
import com.icesi.ui.EventLog;
import com.icesi.ui.GameScreen;
import com.icesi.ui.HudBar;
import com.icesi.ui.IncidentPanelScreen;
import com.icesi.ui.MonitoringCenterScreen;
import com.icesi.ui.PixelCursor;
import com.icesi.ui.PixelTransition;
import com.icesi.model.Incident;
import com.icesi.ui.RetroSound;
import com.icesi.ui.RetroTheme;
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
import javafx.scene.input.KeyCode;
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
        screens.put("center", centerScreen);
        IncidentPanelScreen incidentPanel = new IncidentPanelScreen(controller, eventLog);
        screens.put("incidents", incidentPanel);
        // el incidente elegido en el panel se resalta en el mapa
        incidentPanel.setOnFocus(mapView::setFocusedIncident);
        // solo se puede resolver cuando la unidad ya llego en el mapa
        incidentPanel.setArrivalCheck(mapView::hasArrived);
        // el Mapa de Trafico muestra la ciudad y al lado los incidentes para despachar sin cambiar de pantalla
        screens.put("map", new SplitViewScreen(controller, mapView, incidentPanel));

        BorderPane game = new BorderPane(content);
        game.setTop(hud);

        titleScreen = new TitleScreen();
        toasts = new ToastLayer();
        // la capa CRT va encima de todo para que se vea como un monitor viejo
        shiftEndView = new ShiftEndView();
        root = new StackPane(game, toasts, shiftEndView, titleScreen.getView(), new CrtOverlay());
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
            spawner.start();
            shiftTimer.start();
        });
    }

    // Hace lo que dice la opcion marcada del menu de titulo.
    private void chooseMenuOption() {
        if (titleScreen.getSelectedOption().equals("SALIR")) {
            Platform.exit();
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
    }

    // Empieza una partida nueva desde la pantalla de titulo, en la misma ventana.
    private void restart() {
        refresher.stop();
        mapView.stop();
        spawner.stop();
        shiftTimer.stop();
        new GameApplication().start(stage);
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
        if (currentScreen != null && currentScreen.handleKey(code)) {
            refreshAll();
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
            paused = !paused;
            spawner.setPaused(paused);
            shiftTimer.setPaused(paused);
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
        if (currentScreen != null) {
            currentScreen.refresh();
        }
    }
}
