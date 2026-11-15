package com.icesi.ui;

import javafx.scene.Node;
import javafx.scene.input.KeyCode;

// Una pantalla del juego (Centro de Monitoreo, Mapa de Trafico o Panel de Incidentes).
public interface GameScreen {

    // Devuelve el nodo que se muestra en la ventana.
    Node getView();

    // Vuelve a leer el modelo y actualiza lo que se ve.
    void refresh();

    // Recibe una tecla; retorna true si la pantalla la uso.
    default boolean handleKey(KeyCode code) {
        return false;
    }
}
