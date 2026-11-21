package com.icesi.ui;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.ScaleTransition;
import javafx.animation.Timeline;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.scene.control.Label;
import javafx.util.Duration;

// Etiqueta de numero estilo arcade: cuando el valor cambia, el numero "cuenta" hasta el nuevo valor
// y da un pequeno salto para que el jugador note el cambio.
public class CountingLabel extends Label {

    private String format;      // ejemplo: "%06d" o "★ ACTIVOS %02d"
    private int target = Integer.MIN_VALUE;
    private IntegerProperty shown = new SimpleIntegerProperty(0);
    private Timeline counting;

    public CountingLabel(String format) {
        this.format = format;
        // cada vez que cambia el numero que se muestra, se actualiza el texto
        shown.addListener((obs, before, now) -> setText(String.format(format, now.intValue())));
        setText(String.format(format, 0));
    }

    // Pone el nuevo valor; si es distinto al anterior, anima el conteo y el salto.
    public void setValue(int value) {
        if (value == target) {
            return;
        }
        boolean firstTime = target == Integer.MIN_VALUE;
        target = value;
        if (firstTime) {
            shown.set(value);
            setText(String.format(format, value));
            return;
        }

        if (counting != null) {
            counting.stop();
        }
        counting = new Timeline(new KeyFrame(Duration.millis(450), new KeyValue(shown, value)));
        counting.play();

        ScaleTransition pop = new ScaleTransition(Duration.millis(140), this);
        pop.setFromX(1.25);
        pop.setFromY(1.25);
        pop.setToX(1);
        pop.setToY(1);
        pop.play();
    }
}
