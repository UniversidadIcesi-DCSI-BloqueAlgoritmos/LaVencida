package com.icesi;

import com.icesi.controller.GameController;
import com.icesi.ui.GameScreen;
import com.icesi.ui.HudBar;
import com.icesi.ui.IncidentPanelScreen;
import com.icesi.ui.MonitoringCenterScreen;
import com.icesi.ui.RetroTheme;
import com.icesi.ui.TitleScreen;
import com.icesi.ui.TrafficMapScreen;
import javafx.animation.Animation;
import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.HashMap;
import java.util.Map;

// Ventana principal del juego: pantalla de titulo, barra HUD y cambio entre las pantallas (vista del MVC).
public class GameApplication extends Application {

    private GameController controller;
    private HudBar hud;
    private StackPane content;
    private TitleScreen titleScreen;
    private boolean started;

    private Map<String, GameScreen> screens = new HashMap<>();
    private GameScreen currentScreen;

    @Override
    public void start(Stage stage) {
        controller = new GameController();
        hud = new HudBar();
        content = new StackPane();

        screens.put("center", new MonitoringCenterScreen(controller, this::showScreen));
        screens.put("map", new TrafficMapScreen(controller));
        screens.put("incidents", new IncidentPanelScreen(controller));

        BorderPane game = new BorderPane(content);
        game.setTop(hud);

        titleScreen = new TitleScreen();
        StackPane root = new StackPane(game, titleScreen.getView());
        titleScreen.getView().setOnMouseClicked(e -> enterGame());

        Scene scene = new Scene(root, 1280, 820);
        RetroTheme.apply(scene);
        scene.setOnKeyPressed(e -> handleKey(e.getCode()));

        showScreen("center");

        // la HUD y la pantalla actual se refrescan cuatro veces por segundo
        Timeline refresher = new Timeline(new KeyFrame(Duration.millis(250), e -> refreshAll()));
        refresher.setCycleCount(Animation.INDEFINITE);
        refresher.play();

        stage.setTitle("SGMMS - Centro de Monitoreo de Palmira");
        stage.setScene(scene);
        stage.setMinWidth(1100);
        stage.setMinHeight(760);
        stage.show();
        titleScreen.start();
    }

    // Quita la pantalla de titulo con un desvanecido y empieza el juego.
    private void enterGame() {
        if (started) {
            return;
        }
        started = true;
        FadeTransition fade = new FadeTransition(Duration.millis(600), titleScreen.getView());
        fade.setFromValue(1);
        fade.setToValue(0);
        fade.setOnFinished(e -> {
            titleScreen.stop();
            titleScreen.getView().setVisible(false);
        });
        fade.play();
    }

    // Teclas globales: navegacion entre pantallas; las demas se las pasa a la pantalla actual.
    private void handleKey(KeyCode code) {
        if (!started) {
            if (code == KeyCode.ENTER || code == KeyCode.SPACE) {
                enterGame();
            }
            return;
        }
        if (currentScreen != null && currentScreen.handleKey(code)) {
            refreshAll();
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
