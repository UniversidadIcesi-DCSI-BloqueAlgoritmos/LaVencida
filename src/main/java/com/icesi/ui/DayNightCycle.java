package com.icesi.ui;

import com.icesi.model.CityMap;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

// Ciclo de dia y noche del mapa: con el tiempo la ciudad se oscurece y se prenden
// las ventanas y los postes de luz; luego vuelve a amanecer.
public class DayNightCycle {

    private static final double CYCLE_SECONDS = 120; // un dia completo dura 2 minutos
    private static final double MAX_DARKNESS = 0.6;

    private CityMap cityMap;
    private double cell;

    public DayNightCycle(CityMap cityMap, double cell) {
        this.cityMap = cityMap;
        this.cell = cell;
    }

    // Que tan oscuro esta: 0 es mediodia y 1 es medianoche.
    public double darkness(double time) {
        return (1 - Math.cos(2 * Math.PI * time / CYCLE_SECONDS)) / 2;
    }

    // Nombre del momento del dia para mostrarlo en el panel.
    public String phaseName(double time) {
        double dark = darkness(time);
        if (dark < 0.3) {
            return "DIA";
        }
        if (dark < 0.7) {
            // si se esta oscureciendo es atardecer, si se esta aclarando es amanecer
            return Math.sin(2 * Math.PI * time / CYCLE_SECONDS) > 0 ? "ATARDECER" : "AMANECER";
        }
        return "NOCHE";
    }

    // Dibuja la capa de oscuridad y las luces de la ciudad.
    public void draw(GraphicsContext gc, double width, double height, double time) {
        gc.clearRect(0, 0, width, height);
        double dark = darkness(time);
        if (dark < 0.05) {
            return;
        }

        // velo azul de noche (en el atardecer tira un poco a morado)
        Color tint = dark < 0.7 ? Color.web("#3b1d4a") : RetroTheme.NIGHT;
        gc.setFill(tint.deriveColor(0, 1, 0.6, dark * MAX_DARKNESS));
        gc.fillRect(0, 0, width, height);

        if (dark < 0.35) {
            return;
        }
        double lights = Math.min(1, (dark - 0.35) / 0.3); // las luces se prenden poco a poco
        drawWindows(gc, lights);
        drawStreetLamps(gc, lights);
    }

    // Ventanitas amarillas encima de las casas y tiendas; siempre las mismas para que no titilen.
    private void drawWindows(GraphicsContext gc, double lights) {
        int size = cityMap.getSize();
        for (int row = 1; row < size - 1; row++) {
            for (int column = 1; column < size - 1; column++) {
                int type = cityMap.getCell(row, column);
                boolean building = type == CityMap.RESIDENTIAL || type == CityMap.COMMERCIAL;
                if (building && (row * 31 + column * 17) % 5 == 0) {
                    gc.setFill(RetroTheme.YELLOW.deriveColor(0, 1, 1, 0.8 * lights));
                    gc.fillRect(column * cell + cell / 2 - 1, row * cell + cell / 2 - 1, 2, 2);
                }
            }
        }
    }

    // Postes de luz en los cruces de las vias principales, con un circulo de luz alrededor.
    private void drawStreetLamps(GraphicsContext gc, double lights) {
        int size = cityMap.getSize();
        for (int row = 0; row < size; row++) {
            for (int column = 0; column < size; column++) {
                if (cityMap.getCell(row, column) == CityMap.MAIN_ROAD && row % 8 == 0 && column % 8 == 0) {
                    double cx = column * cell + cell / 2;
                    double cy = row * cell + cell / 2;
                    // el halo se hace con cuadrados que se van aclarando, estilo pixel-art
                    gc.setFill(RetroTheme.ORANGE.deriveColor(0, 1, 1, 0.12 * lights));
                    gc.fillRect(cx - cell * 1.5, cy - cell * 1.5, cell * 3, cell * 3);
                    gc.setFill(RetroTheme.YELLOW.deriveColor(0, 1, 1, 0.18 * lights));
                    gc.fillRect(cx - cell, cy - cell, cell * 2, cell * 2);
                    gc.setFill(RetroTheme.CREAM.deriveColor(0, 1, 1, lights));
                    gc.fillRect(cx - 1.5, cy - 1.5, 3, 3);
                }
            }
        }
    }
}
