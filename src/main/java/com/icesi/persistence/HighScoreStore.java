package com.icesi.persistence;

import com.icesi.exceptions.PersistenceException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

// Guarda el mejor puntaje (record) en un archivo de texto para que no se pierda al cerrar el juego.
public class HighScoreStore {

    private String filePath;

    public HighScoreStore(String filePath) {
        this.filePath = filePath;
    }

    // Lee el record guardado; si no hay archivo o esta danado retorna 0.
    public int load() {
        Path path = Path.of(filePath);
        if (!Files.exists(path)) {
            return 0;
        }
        try {
            return Math.max(0, Integer.parseInt(Files.readString(path).trim()));
        } catch (IOException | NumberFormatException e) {
            // un archivo danado no debe cerrar el juego
            return 0;
        }
    }

    // Guarda el puntaje solo si supera el record; retorna true si fue un record nuevo.
    public boolean saveIfHigher(int score) {
        if (score <= load()) {
            return false;
        }
        try {
            Path path = Path.of(filePath);
            if (path.getParent() != null) {
                Files.createDirectories(path.getParent());
            }
            Files.writeString(path, String.valueOf(score));
            return true;
        } catch (IOException e) {
            throw new PersistenceException("No se pudo guardar el record: " + e.getMessage(), e);
        }
    }
}
