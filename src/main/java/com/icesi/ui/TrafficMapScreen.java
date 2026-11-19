package com.icesi.ui;

import com.icesi.controller.GameController;
import com.icesi.model.Direction;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import static com.icesi.ui.MonitoringCenterScreen.pixelButton;
import static com.icesi.ui.MonitoringCenterScreen.styled;

// Mapa de Trafico: la ciudad dibujada en pixel-art, con incidentes, vehiculos y el operador que se mueve con WASD o flechas.
public class TrafficMapScreen implements GameScreen {

    private GameController controller;
    private CityMapView mapView;
    private StackPane mapSlot;
    private HBox root;
    private Label timeOfDayLabel;

    private Label positionLabel;
    private Label stackLabel;
    private Label actionLabel;

    // Arma la pantalla: el mapa a la izquierda y la informacion a la derecha.
    // El mapa (mapView) es compartido con la vista dividida, por eso se pone en un espacio aparte.
    public TrafficMapScreen(GameController controller, CityMapView mapView) {
        this.controller = controller;
        this.mapView = mapView;
        mapSlot = new StackPane();
        mapSlot.setAlignment(Pos.TOP_LEFT);

        positionLabel = styled(new Label(), "incident-id");
        stackLabel = styled(new Label(), "incident-detail");
        timeOfDayLabel = styled(new Label(), "incident-detail");
        actionLabel = styled(new Label("Usa W A S D o las flechas para patrullar."), "dialog-text");
        actionLabel.setMaxWidth(300);

        VBox infoPanel = new VBox(8, styled(new Label("◆ OPERADOR"), "panel-title"), positionLabel, stackLabel, timeOfDayLabel);
        infoPanel.getStyleClass().add("pixel-panel");

        VBox dialog = new VBox(actionLabel);
        dialog.getStyleClass().add("dialog-box");

        Button undoButton = pixelButton("[U] DESHACER", "button-yellow");
        undoButton.setOnAction(e -> undo());

        VBox right = new VBox(16,
                PixelFont.createLabel("MAPA DE TRAFICO", 4, RetroTheme.YELLOW, RetroTheme.PURPLE),
                infoPanel, dialog, undoButton, createLegend());
        right.setPrefWidth(380);
        right.setMinWidth(380);

        root = new HBox(24, mapSlot, right);
        root.setPadding(new Insets(8, 24, 8, 24));
        root.setAlignment(Pos.TOP_LEFT);
        root.getStyleClass().add("crt-background");
        refresh();
    }

    @Override
    public Node getView() {
        // el mapa se trae a esta pantalla (si estaba en la vista dividida se mueve solo)
        mapSlot.getChildren().setAll(mapView.getView());
        return root;
    }

    @Override
    public void refresh() {
        positionLabel.setText(String.format("POSICION: (%02d, %02d)",
                controller.getOperator().getRow(), controller.getOperator().getColumn()));
        stackLabel.setText("MOVIMIENTOS EN LA PILA: " + controller.getOperator().getMovementCount());
        timeOfDayLabel.setText("CIUDAD: " + mapView.getPhaseName());
    }

    @Override
    public boolean handleKey(KeyCode code) {
        Direction direction = null;
        if (code == KeyCode.W || code == KeyCode.UP) {
            direction = Direction.UP;
        } else if (code == KeyCode.S || code == KeyCode.DOWN) {
            direction = Direction.DOWN;
        } else if (code == KeyCode.A || code == KeyCode.LEFT) {
            direction = Direction.LEFT;
        } else if (code == KeyCode.D || code == KeyCode.RIGHT) {
            direction = Direction.RIGHT;
        } else if (code == KeyCode.U) {
            undo();
            return true;
        }
        if (direction == null) {
            return false;
        }

        if (controller.moveOperator(direction)) {
            actionLabel.setText("Patrullando... " + describe(direction) + ".");
        } else {
            actionLabel.setText("¡BLOQUEADO! No puedes pasar por ahi.");
            RetroSound.play(RetroSound.Effect.ERROR);
            mapView.shake();
        }
        refresh();
        return true;
    }

    // Deshace el ultimo movimiento usando la pila del operador.
    private void undo() {
        if (controller.undoOperatorMove()) {
            actionLabel.setText("Movimiento deshecho. ¡Rebobinando!");
        } else {
            actionLabel.setText("La pila esta vacia: no hay movimientos para deshacer.");
            mapView.shake();
        }
        refresh();
    }

    // Arma la leyenda en dos columnas con un cuadrito de cada elemento del mapa.
    private VBox createLegend() {
        String[][] items = {
                {SpriteRegistry.OPERATOR, "OPERADOR (TU)"}, {SpriteRegistry.HOUSE, "CASAS"},
                {SpriteRegistry.SHOP, "COMERCIO"}, {SpriteRegistry.TREE, "PARQUE"},
                {SpriteRegistry.FOUNTAIN, "PLAZA / FUENTE"}, {SpriteRegistry.HOSPITAL, "HOSPITAL"},
                {SpriteRegistry.POLICE_STATION, "POLICIA"}, {SpriteRegistry.FIRE_STATION, "BOMBEROS"},
                {SpriteRegistry.FIRE, "INCENDIO"}, {SpriteRegistry.THEFT, "ROBO"},
                {SpriteRegistry.ACCIDENT, "ACCIDENTE"}, {SpriteRegistry.PATROL, "UNIDADES"}
        };
        GridPane grid = new GridPane();
        grid.setHgap(18);
        grid.setVgap(6);
        for (int i = 0; i < items.length; i++) {
            grid.add(legendRow(items[i][0], items[i][1]), i % 2, i / 2);
        }
        VBox legend = new VBox(8, styled(new Label("▶ LEYENDA"), "panel-title"), grid);
        legend.getStyleClass().add("pixel-panel");
        return legend;
    }

    private HBox legendRow(String sprite, String text) {
        HBox row = new HBox(10, SpriteRegistry.createIcon(sprite, 22), styled(new Label(text), "stat-name"));
        row.setAlignment(Pos.CENTER_LEFT);
        return row;
    }

    private String describe(Direction direction) {
        if (direction == Direction.UP) {
            return "hacia el norte";
        }
        if (direction == Direction.DOWN) {
            return "hacia el sur";
        }
        if (direction == Direction.LEFT) {
            return "hacia el oeste";
        }
        return "hacia el este";
    }
}
