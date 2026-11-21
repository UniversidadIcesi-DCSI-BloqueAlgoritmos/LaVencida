package com.icesi.persistence;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class HighScoreStoreTest {

    @TempDir
    Path folder;

    // Caso 1: Si no hay archivo el record es 0.
    @Test
    void loadWithoutFileReturnsZero() {
        // Arrange
        HighScoreStore store = new HighScoreStore(folder.resolve("record.txt").toString());

        // Act
        int record = store.load();

        // Assert
        assertEquals(0, record);
    }

    // Caso 2: Un puntaje mayor se guarda y se puede volver a leer.
    @Test
    void higherScoreIsSavedAndLoaded() {
        // Arrange
        HighScoreStore store = new HighScoreStore(folder.resolve("record.txt").toString());

        // Act
        boolean isRecord = store.saveIfHigher(850);

        // Assert
        assertTrue(isRecord);
        assertEquals(850, new HighScoreStore(folder.resolve("record.txt").toString()).load());
    }

    // Caso 3: Un puntaje menor no reemplaza el record.
    @Test
    void lowerScoreDoesNotReplaceRecord() {
        // Arrange
        HighScoreStore store = new HighScoreStore(folder.resolve("record.txt").toString());
        store.saveIfHigher(850);

        // Act
        boolean isRecord = store.saveIfHigher(300);

        // Assert
        assertFalse(isRecord);
        assertEquals(850, store.load());
    }

    // Caso 4: Si el archivo esta danado el record es 0 y el juego no se cae.
    @Test
    void damagedFileReturnsZero() throws IOException {
        // Arrange
        Path file = folder.resolve("record.txt");
        Files.writeString(file, "no es un numero");
        HighScoreStore store = new HighScoreStore(file.toString());

        // Act
        int record = store.load();

        // Assert
        assertEquals(0, record);
    }
}
