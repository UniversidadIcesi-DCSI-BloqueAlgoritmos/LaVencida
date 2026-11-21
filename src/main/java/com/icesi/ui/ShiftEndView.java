package com.icesi.ui;

import com.icesi.controller.ShiftTimer;
import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

// Ventana que aparece encima del juego cuando termina el turno:
// por tiempo (TURNO TERMINADO) o porque la ciudad se lleno de incidentes (CIUDAD SATURADA).
public class ShiftEndView extends StackPane {

    private VBox box;

    public ShiftEndView() {
        getStyleClass().add("shift-end-background");
        setVisible(false);
        box = new VBox(18);
        box.setAlignment(Pos.CENTER);
        box.getStyleClass().add("shift-end-box");
        box.setMaxSize(StackPane.USE_PREF_SIZE, StackPane.USE_PREF_SIZE);
        getChildren().add(box);
    }

    // Muestra el resultado del turno con el puntaje final.
    public void show(ShiftTimer.Result result, int score) {
        boolean lost = result == ShiftTimer.Result.OVERLOADED;
        String title = lost ? "CIUDAD SATURADA" : "TURNO TERMINADO";
        String message = lost
                ? "Llegaste a " + ShiftTimer.MAX_ACTIVE_INCIDENTS + " incidentes activos a la vez.\nPalmira necesita refuerzos..."
                : "¡Sobreviviste los 5 minutos del turno!\nBuen trabajo, operador.";

        Label messageLabel = new Label(message);
        messageLabel.getStyleClass().add("dialog-text");
        messageLabel.setStyle("-fx-text-alignment: center;");
        Label scoreLabel = new Label(String.format("PUNTAJE FINAL  %06d", score));
        scoreLabel.getStyleClass().add("shift-end-score");
        Label keys = new Label("[ENTER] JUGAR OTRA VEZ      [ESC] SALIR");
        keys.getStyleClass().add("hud-key-active");

        box.getChildren().setAll(
                PixelFont.createLabel(title, 5, lost ? RetroTheme.RED : RetroTheme.YELLOW, RetroTheme.PURPLE),
                messageLabel, scoreLabel, keys);

        setVisible(true);
        // entra con un pequeno zoom y desvanecido
        FadeTransition fade = new FadeTransition(Duration.millis(400), this);
        fade.setFromValue(0);
        fade.setToValue(1);
        ScaleTransition zoom = new ScaleTransition(Duration.millis(400), box);
        zoom.setFromX(0.7);
        zoom.setFromY(0.7);
        zoom.setToX(1);
        zoom.setToY(1);
        fade.play();
        zoom.play();
    }

    // Agrega una linea mas debajo del puntaje (por ejemplo el record).
    public void addLine(Label line) {
        box.getChildren().add(box.getChildren().size() - 1, line);
    }
}
