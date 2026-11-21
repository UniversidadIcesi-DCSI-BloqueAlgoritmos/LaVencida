package com.icesi.persistence;

// Guarda el mejor puntaje (record) en un archivo de texto para que no se pierda al cerrar el juego.
public class HighScoreStore {

    private String filePath;

    public HighScoreStore(String filePath) {
        this.filePath = filePath;
    }

    // Lee el record guardado; si no hay archivo o esta danado retorna 0.
    public int load() {
        return -1;
    }

    // Guarda el puntaje solo si supera el record; retorna true si fue un record nuevo.
    public boolean saveIfHigher(int score) {
        return false;
    }
}
