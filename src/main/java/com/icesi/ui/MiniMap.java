package com.icesi.ui;

import com.icesi.controller.GameController;
import com.icesi.model.CityMap;
import com.icesi.model.Incident;
import com.icesi.model.Severity;
import com.icesi.model.Vehicle;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

// Version pequena del mapa para el Centro de Monitoreo: calles, incidentes que parpadean,
// vehiculos y el operador, todo como puntitos de colores.
public class MiniMap extends Canvas {

    private static final double DOT = 2.5; // cada celda del mapa es un cuadrito de 2.5 pixeles

    private GameController controller;
    private boolean blink;

    public MiniMap(GameController controller) {
        super(controller.getCityMap().getSize() * DOT, controller.getCityMap().getSize() * DOT);
        this.controller = controller;
        draw();
    }

    // Vuelve a dibujar; cada vez que se llama los incidentes cambian entre prendido y apagado.
    public void draw() {
        blink = !blink;
        GraphicsContext gc = getGraphicsContext2D();
        CityMap map = controller.getCityMap();
        int size = map.getSize();

        // fondo: calles claras y manzanas oscuras
        for (int row = 0; row < size; row++) {
            for (int column = 0; column < size; column++) {
                gc.setFill(colorOfCell(map.getCell(row, column)));
                gc.fillRect(column * DOT, row * DOT, DOT, DOT);
            }
        }

        for (Incident incident : controller.getActiveIncidentsByPriority()) {
            int[] cell = controller.getCellOf(incident);
            // los graves parpadean, los demas se quedan quietos
            if (incident.getSeverity() != Severity.HIGH || blink) {
                gc.setFill(colorOf(incident.getSeverity()));
                gc.fillRect(cell[1] * DOT - DOT, cell[0] * DOT - DOT, DOT * 3, DOT * 3);
            }
        }

        gc.setFill(RetroTheme.BLUE);
        for (Vehicle vehicle : controller.getVehicles()) {
            int[] cell = controller.getCellOf(vehicle);
            gc.fillRect(cell[1] * DOT, cell[0] * DOT, DOT * 2, DOT * 2);
        }

        gc.setFill(RetroTheme.YELLOW);
        int row = controller.getOperator().getRow();
        int column = controller.getOperator().getColumn();
        gc.fillRect(column * DOT - DOT, row * DOT - DOT, DOT * 3, DOT * 3);
    }

    private Color colorOfCell(int type) {
        if (type == CityMap.MAIN_ROAD) {
            return Color.web("#5f574f");
        }
        if (type == CityMap.PATH) {
            return Color.web("#3d3a36");
        }
        if (type == CityMap.OBSTACLE) {
            return Color.web("#0f3a26");
        }
        return Color.web("#141b38");
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
