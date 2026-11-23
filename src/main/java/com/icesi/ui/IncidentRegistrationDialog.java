package com.icesi.ui;

import com.icesi.exceptions.DuplicateIncidentException;
import com.icesi.model.Incident;
import com.icesi.model.IncidentType;
import com.icesi.model.Severity;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

import java.util.function.Consumer;

import static com.icesi.ui.MonitoringCenterScreen.pixelButton;
import static com.icesi.ui.MonitoringCenterScreen.styled;

// Dialogo modal retro para registrar incidentes manualmente (RF-3 / HU-05).
public class IncidentRegistrationDialog extends StackPane {

    private TextField idField;
    private ComboBox<IncidentType> typeCombo;
    private ComboBox<Severity> severityCombo;
    private ComboBox<String> locationCombo;
    private TextField descriptionField;
    private Label errorLabel;

    private Consumer<Incident> onRegister = incident -> { };
    private Runnable onCancel = () -> { };

    public IncidentRegistrationDialog() {
        getStyleClass().add("shift-end-background");
        setVisible(false);

        VBox box = new VBox(16);
        box.getStyleClass().add("form-modal-box");
        box.setMaxSize(StackPane.USE_PREF_SIZE, StackPane.USE_PREF_SIZE);
        box.setAlignment(Pos.TOP_LEFT);

        Canvas headerTitle = PixelFont.createLabel("NUEVO INCIDENTE", 4, RetroTheme.YELLOW, RetroTheme.PURPLE);
        Label subtitle = styled(new Label("FORMULARIO DE REGISTRO MANUAL - CENTRO DE MONITOREO PALMIRA"), "panel-subtitle");

        GridPane grid = new GridPane();
        grid.setHgap(14);
        grid.setVgap(12);

        // Campo ID
        Label idLabel = styled(new Label("CODIGO / ID:"), "form-label");
        idField = new TextField();
        idField.setPromptText("ej. INC-101");
        idField.getStyleClass().add("pixel-input");
        grid.add(idLabel, 0, 0);
        grid.add(idField, 1, 0);

        // Campo Tipo
        Label typeLabel = styled(new Label("TIPO DE INCIDENTE:"), "form-label");
        typeCombo = new ComboBox<>();
        typeCombo.getItems().addAll(IncidentType.values());
        typeCombo.getSelectionModel().select(IncidentType.THEFT);
        typeCombo.setConverter(new StringConverter<>() {
            @Override
            public String toString(IncidentType object) {
                return object != null ? Texts.of(object) : "";
            }

            @Override
            public IncidentType fromString(String string) {
                return null;
            }
        });
        typeCombo.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(IncidentType item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : Texts.of(item));
            }
        });
        typeCombo.getStyleClass().add("pixel-combo-box");
        grid.add(typeLabel, 0, 1);
        grid.add(typeCombo, 1, 1);

        // Campo Severidad
        Label severityLabel = styled(new Label("GRAVEDAD:"), "form-label");
        severityCombo = new ComboBox<>();
        severityCombo.getItems().addAll(Severity.values());
        severityCombo.getSelectionModel().select(Severity.HIGH);
        severityCombo.setConverter(new StringConverter<>() {
            @Override
            public String toString(Severity object) {
                return object != null ? Texts.of(object) : "";
            }

            @Override
            public Severity fromString(String string) {
                return null;
            }
        });
        severityCombo.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Severity item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : Texts.of(item));
            }
        });
        severityCombo.getStyleClass().add("pixel-combo-box");
        grid.add(severityLabel, 0, 2);
        grid.add(severityCombo, 1, 2);

        // Campo Ubicación (Sectores conocidos de Palmira o escritura libre)
        Label locationLabel = styled(new Label("UBICACION:"), "form-label");
        locationCombo = new ComboBox<>();
        locationCombo.setEditable(true);
        locationCombo.getItems().addAll(
                "Parque Bolivar",
                "Centro Comercial Unicentro",
                "Sector La Estacion",
                "Zona Comercial Centro",
                "Zona Industrial Cencar",
                "Zona Residencial Las Palmas",
                "Barrio El Prado",
                "Barrio Zamorano",
                "Calle 30 Carrera 19",
                "Calle 42 Carrera 28"
        );
        locationCombo.getSelectionModel().selectFirst();
        locationCombo.getStyleClass().add("pixel-combo-box");
        grid.add(locationLabel, 0, 3);
        grid.add(locationCombo, 1, 3);

        // Campo Descripción
        Label descriptionLabel = styled(new Label("DESCRIPCION:"), "form-label");
        descriptionField = new TextField();
        descriptionField.setPromptText("ej. Alerta ciudadana en via principal");
        descriptionField.getStyleClass().add("pixel-input");
        grid.add(descriptionLabel, 0, 4);
        grid.add(descriptionField, 1, 4);

        // Etiqueta para mensajes de error
        errorLabel = styled(new Label(), "form-error");
        errorLabel.setVisible(false);
        errorLabel.setWrapText(true);

        // Botones de acción
        Button saveButton = pixelButton("[✓ GUARDAR]", "button-green");
        saveButton.setOnAction(e -> submit());

        Button cancelButton = pixelButton("[✕ CANCELAR]", "button-gray");
        cancelButton.setOnAction(e -> cancel());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox actionRow = new HBox(12, spacer, cancelButton, saveButton);
        actionRow.setAlignment(Pos.CENTER_RIGHT);

        box.getChildren().addAll(headerTitle, subtitle, grid, errorLabel, actionRow);
        getChildren().add(box);

        // Atajos de teclado en el dialogo: ESC cancela, ENTER guarda
        addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.ESCAPE) {
                cancel();
                event.consume();
            } else if (event.getCode() == KeyCode.ENTER) {
                submit();
                event.consume();
            }
        });
    }

    public void setOnRegister(Consumer<Incident> onRegister) {
        this.onRegister = onRegister;
    }

    public void setOnCancel(Runnable onCancel) {
        this.onCancel = onCancel;
    }

    public void open() {
        errorLabel.setVisible(false);
        errorLabel.setText("");
        idField.setText("");
        descriptionField.setText("");
        typeCombo.getSelectionModel().select(IncidentType.THEFT);
        severityCombo.getSelectionModel().select(Severity.HIGH);
        locationCombo.getSelectionModel().selectFirst();
        setVisible(true);
        idField.requestFocus();
    }

    public void close() {
        setVisible(false);
    }

    private void cancel() {
        close();
        onCancel.run();
    }

    private void submit() {
        String id = idField.getText() != null ? idField.getText().trim() : "";
        if (id.isEmpty()) {
            showError("El código/ID del incidente no puede estar vacío.");
            idField.requestFocus();
            return;
        }

        String location = locationCombo.getValue() != null ? locationCombo.getValue().trim() : "";
        if (location.isEmpty()) {
            showError("La ubicación del incidente no puede estar vacía.");
            locationCombo.requestFocus();
            return;
        }

        IncidentType type = typeCombo.getValue();
        Severity severity = severityCombo.getValue();
        String description = descriptionField.getText() != null ? descriptionField.getText().trim() : "";
        if (description.isEmpty()) {
            description = Texts.of(type) + " reportado en " + location;
        }

        Incident incident = new Incident(id, type, location, severity);
        incident.setDescription(description);

        try {
            onRegister.accept(incident);
            close();
        } catch (DuplicateIncidentException e) {
            RetroSound.play(RetroSound.Effect.ERROR);
            showError("¡ERROR! Ya existe un incidente registrado con el ID: " + id);
            idField.requestFocus();
        } catch (RuntimeException e) {
            RetroSound.play(RetroSound.Effect.ERROR);
            showError("Error al registrar: " + e.getMessage());
        }
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }
}
