package com.icesi.ui;

import com.icesi.controller.GameController;
import com.icesi.model.Direction;
import com.icesi.model.Incident;
import com.icesi.model.Severity;
import com.icesi.model.Vehicle;
import javafx.animation.AnimationTimer;
import javafx.animation.TranslateTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.util.Duration;

import static com.icesi.ui.MonitoringCenterScreen.pixelButton;
import static com.icesi.ui.MonitoringCenterScreen.styled;

// Mapa de Trafico: la ciudad dibujada en pixel-art, con incidentes, vehiculos y el operador que se mueve con WASD o flechas.
public class TrafficMapScreen implements GameScreen {

    private static final double CELL = 11;

    private GameController controller;
    private CityRenderer renderer;
    private HBox root;
    private StackPane mapFrame;
    private Canvas cityLayer;
    private Canvas spriteLayer;
    private Canvas nightLayer;
    private DayNightCycle dayNight;
    private Label timeOfDayLabel;

    private VehicleAnimator vehicleAnimator;
    private double lastTime = -1;
    private double startTime = -1; // momento en que empezo el juego, para que el dia arranque de dia

    private Label positionLabel;
    private Label stackLabel;
    private Label actionLabel;

    // Arma la pantalla: el mapa a la izquierda y la informacion a la derecha.
    public TrafficMapScreen(GameController controller) {
        this.controller = controller;
        int size = controller.getCityMap().getSize();
        renderer = new CityRenderer(controller.getCityMap(), CELL);
        vehicleAnimator = new VehicleAnimator(controller);
        vehicleAnimator.update(0); // cada vehiculo empieza en su estacion

        // capa fija (la ciudad) y capa animada (incidentes, vehiculos y operador)
        cityLayer = new Canvas(size * CELL, size * CELL);
        renderer.drawCity(cityLayer.getGraphicsContext2D());
        spriteLayer = new Canvas(size * CELL, size * CELL);
        // capa de noche entre la ciudad y los sprites: los incidentes y vehiculos se siguen viendo claros
        nightLayer = new Canvas(size * CELL, size * CELL);
        dayNight = new DayNightCycle(controller.getCityMap(), CELL);
        mapFrame = new StackPane(cityLayer, nightLayer, spriteLayer);
        mapFrame.getStyleClass().add("pixel-panel");
        mapFrame.setPadding(new Insets(4));
        mapFrame.setMaxSize(StackPane.USE_PREF_SIZE, StackPane.USE_PREF_SIZE);

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

        root = new HBox(24, mapFrame, right);
        root.setPadding(new Insets(8, 24, 8, 24));
        root.setAlignment(Pos.TOP_LEFT);
        root.getStyleClass().add("crt-background");

        AnimationTimer timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                double time = now / 1_000_000_000.0;
                // los vehiculos siguen viajando aunque se este viendo otra pantalla
                if (startTime < 0) {
                    startTime = time;
                }
                double delta = lastTime < 0 ? 0 : Math.min(0.25, time - lastTime);
                lastTime = time;
                vehicleAnimator.update(delta);
                // solo se dibuja cuando la pantalla esta en la ventana
                if (root.getScene() != null) {
                    double dayTime = time - startTime;
                    dayNight.draw(nightLayer.getGraphicsContext2D(), nightLayer.getWidth(), nightLayer.getHeight(), dayTime);
                    timeOfDayLabel.setText("CIUDAD: " + dayNight.phaseName(dayTime));
                    drawSprites(time);
                }
            }
        };
        timer.start();
        refresh();
    }

    @Override
    public Node getView() {
        return root;
    }

    @Override
    public void refresh() {
        positionLabel.setText(String.format("POSICION: (%02d, %02d)",
                controller.getOperator().getRow(), controller.getOperator().getColumn()));
        stackLabel.setText("MOVIMIENTOS EN LA PILA: " + controller.getOperator().getMovementCount());
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
            shake();
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
            shake();
        }
        refresh();
    }

    // Sacude el mapa cuando el movimiento no se puede hacer.
    private void shake() {
        TranslateTransition shake = new TranslateTransition(Duration.millis(40), mapFrame);
        shake.setFromX(-6);
        shake.setToX(6);
        shake.setCycleCount(6);
        shake.setAutoReverse(true);
        shake.setOnFinished(e -> mapFrame.setTranslateX(0));
        shake.play();
    }

    // Dibuja todo lo que se mueve: incidentes que pulsan, vehiculos y el operador.
    private void drawSprites(double time) {
        GraphicsContext gc = spriteLayer.getGraphicsContext2D();
        gc.clearRect(0, 0, spriteLayer.getWidth(), spriteLayer.getHeight());

        for (Incident incident : controller.getActiveIncidentsByPriority()) {
            int[] cell = controller.getCellOf(incident);
            double cx = cell[1] * CELL + CELL / 2;
            double cy = cell[0] * CELL + CELL / 2;

            // anillo que crece y se apaga, del color de la gravedad
            double phase = (time * 1.2 + cell[0] * 0.1) % 1.0;
            double radius = CELL * (1 + phase * 2.2);
            gc.setStroke(colorOf(incident.getSeverity()).deriveColor(0, 1, 1, 1 - phase));
            gc.setLineWidth(2);
            gc.strokeRect(cx - radius, cy - radius, radius * 2, radius * 2);

            double bob = Math.round(Math.sin(time * 4 + cell[1]) * 1.5);
            double spriteSize = CELL * 2.6;
            SpriteRegistry.draw(gc, SpriteRegistry.forIncident(incident.getType()),
                    cx - spriteSize / 2, cy - spriteSize / 2 + bob, spriteSize, spriteSize);
            // humo, chispas o alarma encima del sprite
            IncidentEffects.draw(gc, incident.getType(), cx, cy, time, cell[0] + cell[1]);
            drawAlertBubble(gc, cx, cy - spriteSize / 2 - 10 + bob, colorOf(incident.getSeverity()));
        }

        for (Vehicle vehicle : controller.getVehicles()) {
            double[] position = vehicleAnimator.positionOf(vehicle);
            double size = CELL * 2;
            double x = position[1] * CELL + CELL / 2 - size / 2;
            double y = position[0] * CELL + CELL / 2 - size / 2;
            boolean moving = vehicleAnimator.isMoving(vehicle);
            // cuando ya llego al incidente se corre un poco al lado para no taparlo
            if (vehicle.getAssignedIncident() != null && !moving) {
                x += CELL * 1.2;
            }
            if (moving) {
                // brinquito y luz de sirena roja/azul mientras va en camino
                y -= Math.abs(Math.round(Math.sin(time * 18)));
                gc.setFill((int) (time * 8) % 2 == 0 ? RetroTheme.RED : RetroTheme.BLUE);
                gc.fillRect(x + size / 2 - 2, y - 3, 4, 3);
            }
            SpriteRegistry.draw(gc, SpriteRegistry.forVehicle(vehicle.getType()), x, y, size, size);
        }

        int row = controller.getOperator().getRow();
        int column = controller.getOperator().getColumn();
        double bob = Math.round(Math.sin(time * 6)) * 1.5;
        double size = CELL * 2.4;
        double x = column * CELL + CELL / 2 - size / 2;
        double y = row * CELL + CELL / 2 - size / 2 - 3 + bob;
        // sombrita en el piso y marco amarillo que parpadea
        gc.setFill(Color.rgb(0, 0, 0, 0.5));
        gc.fillRect(column * CELL - 2, row * CELL + CELL - 2, CELL + 4, 3);
        if ((int) (time * 3) % 2 == 0) {
            gc.setStroke(RetroTheme.YELLOW);
            gc.setLineWidth(2);
            gc.strokeRect(column * CELL - 3, row * CELL - 3, CELL + 6, CELL + 6);
        }
        SpriteRegistry.draw(gc, SpriteRegistry.OPERATOR, x, y, size, size);

        // flechita amarilla que rebota encima del operador para encontrarlo rapido
        double arrowY = y - 12 + Math.round(Math.sin(time * 5) * 2);
        double arrowX = column * CELL + CELL / 2;
        gc.setFill(RetroTheme.BLACK);
        gc.fillPolygon(new double[]{arrowX - 7, arrowX + 7, arrowX}, new double[]{arrowY - 1, arrowY - 1, arrowY + 8}, 3);
        gc.setFill(RetroTheme.YELLOW);
        gc.fillPolygon(new double[]{arrowX - 5, arrowX + 5, arrowX}, new double[]{arrowY, arrowY, arrowY + 6}, 3);
    }

    // Globo con un "!" encima del incidente, del color de su gravedad.
    private void drawAlertBubble(GraphicsContext gc, double cx, double top, Color color) {
        gc.setFill(RetroTheme.BLACK);
        gc.fillRect(cx - 6, top - 1, 12, 13);
        gc.setFill(color);
        gc.fillRect(cx - 5, top, 10, 11);
        gc.setFill(RetroTheme.CREAM);
        gc.fillRect(cx - 1, top + 2, 2, 5);
        gc.fillRect(cx - 1, top + 8, 2, 2);
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

    private Color colorOf(Severity severity) {
        if (severity == Severity.HIGH) {
            return RetroTheme.RED;
        }
        if (severity == Severity.MEDIUM) {
            return RetroTheme.ORANGE;
        }
        return RetroTheme.GREEN;
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
