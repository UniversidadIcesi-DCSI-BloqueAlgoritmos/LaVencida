package com.icesi.ui;

import javafx.scene.Scene;
import javafx.scene.paint.Color;

// Paleta de colores y hoja de estilos del juego (estilo pixel-art retro de 16 bits).
public final class RetroTheme {

    // colores de la paleta (inspirada en las consolas de 8 y 16 bits)
    public static final Color BLACK = Color.web("#000000");
    public static final Color NIGHT = Color.web("#1d2b53");
    public static final Color PURPLE = Color.web("#7e2553");
    public static final Color DARK_GREEN = Color.web("#008751");
    public static final Color BROWN = Color.web("#ab5236");
    public static final Color DARK_GRAY = Color.web("#5f574f");
    public static final Color LIGHT_GRAY = Color.web("#c2c3c7");
    public static final Color CREAM = Color.web("#fff1e8");
    public static final Color RED = Color.web("#ff004d");
    public static final Color ORANGE = Color.web("#ffa300");
    public static final Color YELLOW = Color.web("#ffec27");
    public static final Color GREEN = Color.web("#00e436");
    public static final Color BLUE = Color.web("#29adff");
    public static final Color LAVENDER = Color.web("#83769c");
    public static final Color PINK = Color.web("#ff77a8");
    public static final Color PEACH = Color.web("#ffccaa");

    public static final String STYLESHEET = "/com/icesi/css/retro.css";

    private RetroTheme() {
    }

    // Le pone la hoja de estilos retro a la escena.
    public static void apply(Scene scene) {
        scene.getStylesheets().add(RetroTheme.class.getResource(STYLESHEET).toExternalForm());
    }
}
