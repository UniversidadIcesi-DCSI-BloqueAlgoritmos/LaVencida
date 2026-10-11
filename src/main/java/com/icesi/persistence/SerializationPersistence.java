package com.icesi.persistence;

import com.icesi.exceptions.PersistenceException;
import com.icesi.model.IncidentManager;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class SerializationPersistence {

    public SerializationPersistence() {
    }

    // Serializa el estado del IncidentManager en un archivo binario.
    public void saveManagerState(IncidentManager manager, String filePath) {
        if (manager == null || filePath == null || filePath.isBlank()) {
            throw new IllegalArgumentException("El gestor de incidentes y la ruta no pueden ser nulos");
        }
        try {
            Path path = Path.of(filePath);
            if (path.getParent() != null) {
                Files.createDirectories(path.getParent());
            }
            try (OutputStream fos = Files.newOutputStream(path);
                 ObjectOutputStream out = new ObjectOutputStream(fos)) {
                out.writeObject(manager);
            }
        } catch (IOException e) {
            throw new PersistenceException("Error al serializar el estado del sistema: " + e.getMessage(), e);
        }
    }

    // Deserializa y reconstruye el estado del IncidentManager desde un archivo binario.
    public IncidentManager loadManagerState(String filePath) {
        if (filePath == null || filePath.isBlank()) {
            throw new IllegalArgumentException("La ruta del archivo no puede ser nula");
        }
        try (InputStream fis = Files.newInputStream(Path.of(filePath));
             ObjectInputStream in = new ObjectInputStream(fis)) {
            Object loaded = in.readObject();
            if (!(loaded instanceof IncidentManager)) {
                throw new PersistenceException("El archivo no contiene un estado valido de IncidentManager");
            }
            return (IncidentManager) loaded;
        } catch (IOException | ClassNotFoundException e) {
            throw new PersistenceException("Error al deserializar el estado del sistema: " + e.getMessage(), e);
        }
    }
}
