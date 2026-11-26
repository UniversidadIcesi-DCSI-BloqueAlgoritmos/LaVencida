package com.icesi.ui;

import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.TranslateTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

// Capa de notificaciones: mensajes que entran deslizandose por la derecha, como los logros de un videojuego.
public class ToastLayer extends VBox {

    private static final int MAX_TOASTS = 4;

    // Crea la capa arriba a la derecha, sin bloquear los clics de lo que esta debajo.
    public ToastLayer() {
        super(10);
        setAlignment(Pos.TOP_RIGHT);
        setPadding(new Insets(70, 20, 0, 0));
        setPickOnBounds(false);
        setMouseTransparent(true);
    }

    // Muestra un mensaje con el icono del sprite indicado (puede ser null).
    public void show(String message, String sprite) {
        Label text = new Label(message);
        text.getStyleClass().add("toast-text");
        text.setWrapText(true);
        text.setMaxWidth(380);
        HBox toast = new HBox(10);
        if (sprite != null) {
            toast.getChildren().add(SpriteRegistry.createIcon(sprite, 28));
        }
        toast.getChildren().add(text);
        toast.setAlignment(Pos.CENTER_LEFT);
        toast.getStyleClass().add("toast");
        toast.setMaxWidth(460);

        // si hay muchas, se quita la mas vieja
        if (getChildren().size() >= MAX_TOASTS) {
            getChildren().remove(0);
        }
        getChildren().add(toast);

        TranslateTransition slideIn = new TranslateTransition(Duration.millis(250), toast);
        slideIn.setFromX(460);
        slideIn.setToX(0);
        PauseTransition stay = new PauseTransition(Duration.seconds(3));
        FadeTransition fadeOut = new FadeTransition(Duration.millis(400), toast);
        fadeOut.setToValue(0);
        SequentialTransition animation = new SequentialTransition(slideIn, stay, fadeOut);
        animation.setOnFinished(e -> getChildren().remove(toast));
        animation.play();
    }
}
