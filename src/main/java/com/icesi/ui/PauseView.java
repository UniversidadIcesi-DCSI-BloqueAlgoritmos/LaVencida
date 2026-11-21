package com.icesi.ui;

import javafx.animation.Animation;
import javafx.animation.FadeTransition;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

// Capa que oscurece el juego y muestra "PAUSA" titilando mientras la simulacion esta detenida.
public class PauseView extends StackPane {

    private FadeTransition blink;

    public PauseView() {
        getStyleClass().add("shift-end-background");
        setVisible(false);
        setMouseTransparent(false); // mientras esta en pausa no se puede hacer clic en el juego

        Node title = PixelFont.createLabel("PAUSA", 8, RetroTheme.YELLOW, RetroTheme.PURPLE);
        Label hint = new Label("[P] PARA CONTINUAR");
        hint.getStyleClass().add("hud-key-active");
        VBox box = new VBox(24, title, hint);
        box.setAlignment(Pos.CENTER);
        getChildren().add(box);

        // el titulo titila despacio
        blink = new FadeTransition(Duration.millis(700), title);
        blink.setFromValue(1);
        blink.setToValue(0.3);
        blink.setAutoReverse(true);
        blink.setCycleCount(Animation.INDEFINITE);
    }

    public void setPaused(boolean paused) {
        setVisible(paused);
        if (paused) {
            blink.play();
        } else {
            blink.stop();
        }
    }
}
