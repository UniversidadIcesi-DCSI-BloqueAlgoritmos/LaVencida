package com.icesi.ui;

import com.icesi.model.Vehicle;
import com.icesi.model.VehicleType;
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

// Dialogo modal retro para registrar nuevas unidades de vehiculos en la flota (Pantallas de Vehiculos).
public class VehicleRegistrationDialog extends StackPane {

    private TextField idField;
    private ComboBox<VehicleType> typeCombo;
    private ComboBox<String> stationCombo;
    private Label errorLabel;

    private Consumer<Vehicle> onVehicleRegistered = vehicle -> { };
    private Runnable onCancel = () -> { };

    public VehicleRegistrationDialog() {
        getStyleClass().add("shift-end-background");
        setVisible(false);

        VBox box = new VBox(16);
        box.getStyleClass().add("form-modal-box");
        box.setMaxSize(StackPane.USE_PREF_SIZE, StackPane.USE_PREF_SIZE);
        box.setAlignment(Pos.TOP_LEFT);

        Canvas headerTitle = PixelFont.createLabel("NUEVO VEHICULO", 4, RetroTheme.YELLOW, RetroTheme.PURPLE);
        Label subtitle = styled(new Label("REGISTRO DE UNIDADES DE FLOTA - PALMIRA"), "panel-subtitle");

        GridPane grid = new GridPane();
        grid.setHgap(14);
        grid.setVgap(12);

        // Campo ID
        Label idLabel = styled(new Label("CODIGO / ID:"), "form-label");
        idField = new TextField();
        idField.setPromptText("ej. P-03, A-03, F-03");
        idField.getStyleClass().add("pixel-input");
        grid.add(idLabel, 0, 0);
        grid.add(idField, 1, 0);

        // Campo Tipo de Vehículo
        Label typeLabel = styled(new Label("TIPO DE UNIDAD:"), "form-label");
        typeCombo = new ComboBox<>();
        typeCombo.getItems().addAll(VehicleType.values());
        typeCombo.getSelectionModel().select(VehicleType.PATROL);
        typeCombo.setConverter(new StringConverter<>() {
            @Override
            public String toString(VehicleType object) {
                return object != null ? Texts.of(object) : "";
            }

            @Override
            public VehicleType fromString(String string) {
                return null;
            }
        });
        typeCombo.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(VehicleType item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : Texts.of(item));
            }
        });
        typeCombo.getStyleClass().add("pixel-combo-box");
        grid.add(typeLabel, 0, 1);
        grid.add(typeCombo, 1, 1);

        // Campo Estación Base / Ubicación inicial
        Label stationLabel = styled(new Label("ESTACION BASE:"), "form-label");
        stationCombo = new ComboBox<>();
        stationCombo.setEditable(true);
        stationCombo.getItems().addAll(
                "Estacion Norte",
                "Estacion Sur",
                "Hospital Central",
                "Clinica Palmira",
                "Bomberos Centro",
                "Bomberos Sur"
        );
        stationCombo.getSelectionModel().selectFirst();
        stationCombo.getStyleClass().add("pixel-combo-box");
        grid.add(stationLabel, 0, 2);
        grid.add(stationCombo, 1, 2);

        errorLabel = styled(new Label(), "form-error");
        errorLabel.setVisible(false);
        errorLabel.setWrapText(true);

        Button saveButton = pixelButton("[✓ REGISTRAR]", "button-green");
        saveButton.setOnAction(e -> submit());

        Button cancelButton = pixelButton("[✕ CANCELAR]", "button-gray");
        cancelButton.setOnAction(e -> cancel());

        Region push = new Region();
        HBox.setHgrow(push, Priority.ALWAYS);
        HBox actions = new HBox(12, push, cancelButton, saveButton);
        actions.setAlignment(Pos.CENTER_RIGHT);

        box.getChildren().addAll(headerTitle, subtitle, grid, errorLabel, actions);
        getChildren().add(box);

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

    public void setOnVehicleRegistered(Consumer<Vehicle> onVehicleRegistered) {
        this.onVehicleRegistered = onVehicleRegistered;
    }

    public void setOnCancel(Runnable onCancel) {
        this.onCancel = onCancel;
    }

    public void open() {
        errorLabel.setVisible(false);
        errorLabel.setText("");
        idField.setText("");
        typeCombo.getSelectionModel().select(VehicleType.PATROL);
        stationCombo.getSelectionModel().selectFirst();
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
            showError("El código o ID del vehículo no puede estar vacío.");
            idField.requestFocus();
            return;
        }

        String location = stationCombo.getValue() != null ? stationCombo.getValue().trim() : "";
        if (location.isEmpty()) {
            showError("La estación base del vehículo no puede estar vacía.");
            stationCombo.requestFocus();
            return;
        }

        VehicleType type = typeCombo.getValue();
        Vehicle vehicle = new Vehicle(id, type, location);

        try {
            onVehicleRegistered.accept(vehicle);
            close();
        } catch (IllegalArgumentException e) {
            RetroSound.play(RetroSound.Effect.ERROR);
            showError("¡ERROR! " + e.getMessage());
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
