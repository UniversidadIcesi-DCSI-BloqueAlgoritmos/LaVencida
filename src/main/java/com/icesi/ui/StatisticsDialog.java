package com.icesi.ui;

import com.icesi.controller.GameController;
import com.icesi.persistence.HighScoreStore;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import static com.icesi.ui.MonitoringCenterScreen.pixelButton;
import static com.icesi.ui.MonitoringCenterScreen.styled;

// Pantalla modal de estadisticas en tiempo real: puntaje, record, resolucion y uso de flota.
public class StatisticsDialog extends StackPane {

    private final GameController controller;
    private final HighScoreStore highScores;
    private Runnable onClose = () -> { };

    private Label scoreValue;
    private Label recordValue;
    private Label resolvedValue;
    private Label activeValue;
    private Label resolutionRateValue;
    private Label fleetUsageValue;
    private Label bstMetricsValue;
    private Label breakdownValue;

    public StatisticsDialog(GameController controller, HighScoreStore highScores) {
        this.controller = controller;
        this.highScores = highScores;
        getStyleClass().add("shift-end-background");
        setVisible(false);

        VBox box = new VBox(14);
        box.getStyleClass().add("form-modal-box");
        box.setMaxSize(StackPane.USE_PREF_SIZE, StackPane.USE_PREF_SIZE);
        box.setAlignment(Pos.TOP_LEFT);

        Canvas headerTitle = PixelFont.createLabel("ESTADISTICAS", 4, RetroTheme.YELLOW, RetroTheme.PURPLE);
        Label subtitle = styled(new Label("RESUMEN OPERATIVO DEL TURNO Y DESEMPENO EN TIEMPO REAL"), "panel-subtitle");

        GridPane grid = new GridPane();
        grid.setHgap(16);
        grid.setVgap(12);
        grid.setPadding(new Insets(10, 0, 10, 0));

        scoreValue = styled(new Label("000000"), "stat-value");
        recordValue = styled(new Label("000000"), "stat-value");
        resolvedValue = styled(new Label("0"), "stat-value");
        activeValue = styled(new Label("0"), "stat-value");
        resolutionRateValue = styled(new Label("0%"), "stat-value");
        fleetUsageValue = styled(new Label("0%"), "stat-value");
        bstMetricsValue = styled(new Label("0 NODOS / H=0"), "stat-value");
        breakdownValue = styled(new Label("ACC: 0 · ROB: 0 · INC: 0"), "stat-value");

        grid.add(createCard("PUNTAJE ACTUAL", scoreValue, "Puntos acumulados en el turno"), 0, 0);
        grid.add(createCard("RECORD HISTORICO", recordValue, "Mejor puntaje guardado"), 1, 0);
        grid.add(createCard("INCIDENTES RESUELTOS", resolvedValue, "Atendidos con exito"), 0, 1);
        grid.add(createCard("INCIDENTES ACTIVOS", activeValue, "Pendientes o en atencion"), 1, 1);
        grid.add(createCard("TASA DE RESOLUCION", resolutionRateValue, "Resueltos / Total generados"), 0, 2);
        grid.add(createCard("USO DE LA FLOTA", fleetUsageValue, "Unidades despachadas / Total"), 1, 2);
        grid.add(createCard("ARBOL BST ACTIVO", bstMetricsValue, "Nodos activos y altura"), 0, 3);
        grid.add(createCard("ACTIVOS POR TIPO", breakdownValue, "Accidentes, robos e incendios"), 1, 3);

        Button closeBtn = pixelButton("[ESC] CERRAR", "button-yellow");
        closeBtn.setOnAction(e -> close());

        Region push = new Region();
        HBox.setHgrow(push, Priority.ALWAYS);
        HBox actions = new HBox(12, push, closeBtn);
        actions.setAlignment(Pos.CENTER_RIGHT);

        box.getChildren().addAll(headerTitle, subtitle, grid, actions);
        getChildren().add(box);

        addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.ESCAPE || event.getCode() == KeyCode.K) {
                close();
                event.consume();
            }
        });
    }

    private VBox createCard(String title, Label value, String caption) {
        VBox card = new VBox(4);
        card.getStyleClass().add("stat-card");
        card.setPadding(new Insets(8, 12, 8, 12));
        card.setMinWidth(260);
        card.getChildren().addAll(
                styled(new Label(title), "panel-subtitle"),
                value,
                styled(new Label(caption), "stat-name")
        );
        return card;
    }

    public void refreshStats() {
        int score = controller.getScore();
        int best = Math.max(score, highScores.load());
        int resolved = controller.getResolvedIncidentCount();
        int active = controller.getActiveIncidentCount();
        int total = controller.getTotalIncidentCount();
        int rate = total == 0 ? 100 : (int) Math.round((resolved * 100.0) / total);

        int totalVehicles = controller.getVehicles().size();
        int available = controller.getAvailableVehicleCount();
        int busy = Math.max(0, totalVehicles - available);
        int fleetPct = totalVehicles == 0 ? 0 : (int) Math.round((busy * 100.0) / totalVehicles);

        int treeSize = controller.getActiveIncidentsTree().size();
        int treeHeight = controller.getActiveIncidentsTree().height();

        scoreValue.setText(String.format("%06d", score));
        recordValue.setText(String.format("%06d", best));
        resolvedValue.setText(String.valueOf(resolved));
        activeValue.setText(String.valueOf(active));
        resolutionRateValue.setText(rate + "% (" + resolved + "/" + total + ")");
        fleetUsageValue.setText(fleetPct + "% (" + busy + "/" + totalVehicles + ")");
        bstMetricsValue.setText(treeSize + " NODOS / H=" + treeHeight);
        breakdownValue.setText("ACC: " + controller.getActiveAccidentCount()
                + " · ROB: " + controller.getActiveTheftCount()
                + " · INC: " + controller.getActiveFireCount());
    }

    public void open() {
        refreshStats();
        setVisible(true);
        requestFocus();
    }

    public void close() {
        setVisible(false);
        onClose.run();
    }

    public void setOnClose(Runnable onClose) {
        this.onClose = onClose;
    }
}
