package com.icesi.ui;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

// Ventana de opciones que se abre desde el menu de la portada.
// Se mueve con las flechas y ENTER cambia la opcion marcada.
public class OptionsView extends StackPane {

    public static final String SOUND = "SONIDO";
    public static final String CRT = "EFECTO CRT";
    public static final String SPEED = "VELOCIDAD";
    public static final String BACK = "VOLVER";

    private String[] items = {SOUND, CRT, SPEED, BACK};
    private Label[] lines;
    private int selected;
    private boolean crtOn = true;
    private boolean fastMode;

    public OptionsView() {
        getStyleClass().add("shift-end-background");
        setVisible(false);
        VBox box = new VBox(16);
        box.setAlignment(Pos.CENTER);
        box.getStyleClass().add("shift-end-box");
        box.setMaxSize(StackPane.USE_PREF_SIZE, StackPane.USE_PREF_SIZE);
        box.getChildren().add(PixelFont.createLabel("OPCIONES", 5, RetroTheme.YELLOW, RetroTheme.PURPLE));
        lines = new Label[items.length];
        for (int i = 0; i < items.length; i++) {
            lines[i] = new Label();
            box.getChildren().add(lines[i]);
        }
        Label hint = new Label("FLECHAS PARA ELEGIR - ENTER PARA CAMBIAR - ESC PARA VOLVER");
        hint.getStyleClass().add("help-text");
        box.getChildren().add(hint);
        getChildren().add(box);
        refresh();
    }

    public void open() {
        selected = 0;
        refresh();
        setVisible(true);
    }

    public void close() {
        setVisible(false);
    }

    public void move(int step) {
        selected = Math.floorMod(selected + step, items.length);
        refresh();
    }

    public String getSelected() {
        return items[selected];
    }

    public boolean isCrtOn() {
        return crtOn;
    }

    public void setCrtOn(boolean crtOn) {
        this.crtOn = crtOn;
        refresh();
    }

    public boolean isFastMode() {
        return fastMode;
    }

    public void setFastMode(boolean fastMode) {
        this.fastMode = fastMode;
        refresh();
    }

    // Vuelve a escribir cada linea con su valor actual; la marcada va resaltada.
    public void refresh() {
        for (int i = 0; i < items.length; i++) {
            String text = items[i];
            if (items[i].equals(SOUND)) {
                text += ":  " + (RetroSound.isMuted() ? "OFF" : "ON");
            } else if (items[i].equals(CRT)) {
                text += ":  " + (crtOn ? "ON" : "OFF");
            } else if (items[i].equals(SPEED)) {
                text += ":  " + (fastMode ? "RAPIDA" : "NORMAL");
            }
            lines[i].setText(i == selected ? "> " + text + " <" : text);
            lines[i].getStyleClass().setAll("label", i == selected ? "options-selected" : "options-item");
        }
    }
}
