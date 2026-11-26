package com.icesi.ui;

import com.icesi.model.IncidentType;
import javafx.scene.canvas.GraphicsContext;

// Efectos animados encima de los incidentes del mapa: humo y chispas en los incendios,
// chispas que saltan en los accidentes y luces de alarma que giran en los robos.
public final class IncidentEffects {

    private IncidentEffects() {
    }

    // Dibuja el efecto del tipo de incidente alrededor del centro (cx, cy).
    // seed hace que cada incidente vaya a su propio ritmo y no todos iguales.
    public static void draw(GraphicsContext gc, IncidentType type, double cx, double cy, double time, int seed) {
        if (type == IncidentType.FIRE) {
            drawSmoke(gc, cx, cy, time, seed);
            drawEmbers(gc, cx, cy, time, seed);
        } else if (type == IncidentType.ACCIDENT) {
            drawSparks(gc, cx, cy, time, seed);
        } else {
            drawAlarm(gc, cx, cy, time);
        }
    }

    // Bocanadas de humo que suben, crecen y se desvanecen.
    private static void drawSmoke(GraphicsContext gc, double cx, double cy, double time, int seed) {
        for (int i = 0; i < 4; i++) {
            double life = (time * 0.6 + i * 0.25 + seed * 0.13) % 1.0; // 0 = recien sale, 1 = desaparece
            double size = 4 + life * 8;
            // el humo sale un poco a la derecha para no tapar el globo de alerta
            double x = cx + 6 + life * 8 - size / 2 + Math.sin(life * 6 + i) * 3;
            double y = cy - 8 - life * 28;
            gc.setFill(RetroTheme.LIGHT_GRAY.deriveColor(0, 1, 1, 0.85 * (1 - life)));
            gc.fillRect(Math.round(x), Math.round(y), Math.round(size), Math.round(size));
        }
    }

    // Chispitas naranjas y amarillas que saltan del fuego.
    private static void drawEmbers(GraphicsContext gc, double cx, double cy, double time, int seed) {
        for (int i = 0; i < 5; i++) {
            double life = (time * 1.3 + i * 0.2 + seed * 0.07) % 1.0;
            double x = cx + Math.sin(i * 2.3 + seed) * 10 * life;
            double y = cy - 4 - life * 18;
            gc.setFill((i % 2 == 0 ? RetroTheme.ORANGE : RetroTheme.YELLOW).deriveColor(0, 1, 1, 1 - life));
            gc.fillRect(Math.round(x), Math.round(y), 2, 2);
        }
    }

    // Chispas que salen disparadas en todas direcciones, como un choque.
    private static void drawSparks(GraphicsContext gc, double cx, double cy, double time, int seed) {
        double life = (time * 1.6 + seed * 0.11) % 1.0;
        for (int i = 0; i < 8; i++) {
            double angle = i * Math.PI / 4 + seed;
            double distance = 6 + life * 14;
            double x = cx + Math.cos(angle) * distance;
            double y = cy + Math.sin(angle) * distance;
            gc.setFill((i % 2 == 0 ? RetroTheme.YELLOW : RetroTheme.CREAM).deriveColor(0, 1, 1, 1 - life));
            gc.fillRect(Math.round(x) - 1, Math.round(y) - 1, 3, 3);
        }
    }

    // Dos rayos de luz de alarma (rojo y azul) que giran alrededor del robo.
    private static void drawAlarm(GraphicsContext gc, double cx, double cy, double time) {
        double angle = time * 4;
        for (int i = 0; i < 2; i++) {
            double a = angle + i * Math.PI;
            gc.setFill(i == 0 ? RetroTheme.RED : RetroTheme.BLUE);
            // cada rayo son 4 cuadritos en linea que se van haciendo transparentes
            for (int step = 1; step <= 5; step++) {
                double x = cx + Math.cos(a) * (10 + step * 5);
                double y = cy + Math.sin(a) * (10 + step * 5);
                gc.setGlobalAlpha(1.0 - step * 0.15);
                gc.fillRect(Math.round(x) - 2, Math.round(y) - 2, 4, 4);
            }
        }
        gc.setGlobalAlpha(1);
    }
}
