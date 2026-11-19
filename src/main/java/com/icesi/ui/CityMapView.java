package com.icesi.ui;

import com.icesi.controller.GameController;
import com.icesi.model.Incident;
import com.icesi.model.Severity;
import com.icesi.model.Vehicle;
import javafx.animation.AnimationTimer;
import javafx.animation.TranslateTransition;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.util.Duration;

// El mapa de la ciudad dibujado en pixel-art: ciudad, noche, incidentes, vehiculos y operador.
// Se usa una sola vez y se muestra en la pantalla que lo necesite (Mapa o vista dividida).
public class CityMapView {

    public static final double CELL = 11;

    private GameController controller;
    private StackPane mapFrame;
    private Canvas cityLayer;
    private Canvas spriteLayer;
    private Canvas nightLayer;
    private DayNightCycle dayNight;
    private VehicleAnimator vehicleAnimator;
    private double lastTime = -1;
    private double startTime = -1; // momento en que empezo el juego, para que el dia arranque de dia
    private String phaseName = "";
    private String focusedIncidentId; // incidente elegido en el panel: se resalta en el mapa

    // Arma las capas del mapa y arranca la animacion.
    public CityMapView(GameController controller) {
        this.controller = controller;
        int size = controller.getCityMap().getSize();
        CityRenderer renderer = new CityRenderer(controller.getCityMap(), CELL);
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
                // solo se dibuja cuando el mapa esta en la ventana
                if (mapFrame.getScene() != null) {
                    double dayTime = time - startTime;
                    dayNight.draw(nightLayer.getGraphicsContext2D(), nightLayer.getWidth(), nightLayer.getHeight(), dayTime);
                    phaseName = dayNight.phaseName(dayTime);
                    drawSprites(time);
                }
            }
        };
        timer.start();
    }

    public Node getView() {
        return mapFrame;
    }

    // Nombre del momento del dia (DIA, ATARDECER, NOCHE...) para mostrarlo en pantalla.
    public String getPhaseName() {
        return phaseName;
    }

    // Marca el incidente que se eligio en el panel (null para no resaltar ninguno).
    public void setFocusedIncident(String incidentId) {
        focusedIncidentId = incidentId;
    }

    // Sacude el mapa cuando el movimiento no se puede hacer.
    public void shake() {
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
            if (incident.getId().equals(focusedIncidentId)) {
                drawFocus(gc, cx, cy, time);
            }
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

    // Esquinas amarillas que parpadean y una flecha que rebota sobre el incidente elegido.
    private void drawFocus(GraphicsContext gc, double cx, double cy, double time) {
        double half = CELL * 2.2 + Math.round(Math.sin(time * 6) * 2);
        double arm = CELL;
        gc.setLineWidth(3);
        gc.setStroke((int) (time * 4) % 2 == 0 ? RetroTheme.YELLOW : RetroTheme.CREAM);
        double[][] corners = {{-1, -1}, {1, -1}, {-1, 1}, {1, 1}};
        for (double[] corner : corners) {
            double x = cx + corner[0] * half;
            double y = cy + corner[1] * half;
            gc.strokeLine(x, y, x - corner[0] * arm, y);
            gc.strokeLine(x, y, x, y - corner[1] * arm);
        }
        // flecha grande arriba, por encima del globo del "!"
        double top = cy - half - 22 + Math.round(Math.sin(time * 5) * 3);
        gc.setFill(RetroTheme.BLACK);
        gc.fillPolygon(new double[]{cx - 10, cx + 10, cx}, new double[]{top - 1, top - 1, top + 12}, 3);
        gc.setFill(RetroTheme.YELLOW);
        gc.fillPolygon(new double[]{cx - 8, cx + 8, cx}, new double[]{top, top, top + 10}, 3);
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

    private Color colorOf(Severity severity) {
        if (severity == Severity.HIGH) {
            return RetroTheme.RED;
        }
        if (severity == Severity.MEDIUM) {
            return RetroTheme.ORANGE;
        }
        return RetroTheme.GREEN;
    }
}
