package com.icesi.ui;

import com.icesi.controller.GameController;
import com.icesi.controller.ShiftTimer;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

// Ventana de ayuda (tecla H): como se juega, los controles y las reglas de puntos.
public class HelpView extends StackPane {

    public HelpView() {
        getStyleClass().add("shift-end-background");
        setVisible(false);

        VBox box = new VBox(14);
        box.getStyleClass().add("shift-end-box");
        box.setMaxSize(StackPane.USE_PREF_SIZE, StackPane.USE_PREF_SIZE);
        box.setAlignment(Pos.TOP_LEFT);

        Label goal = text("Eres el operador del centro de monitoreo de Palmira. Llegan robos e incendios:\n"
                + "asigna la unidad correcta, espera a que llegue y resuelve el incidente.");

        GridPane keys = new GridPane();
        keys.setHgap(14);
        keys.setVgap(4);
        String[][] controls = {
                {"W A S D / FLECHAS", "Mover al operador"}, {"U", "Deshacer movimiento"},
                {"C  M  I  B", "Centro, Mapa, Incidentes, Arbol BST"}, {"E / V / K", "Corredores, Movilidad, Estadisticas"},
                {"ENTER / ESC", "Despachar la sugerida / cancelar"}, {"TAB / R", "Elegir incidente / resolver"},
                {"L", "Liberar unidad / cancelar despacho"}, {"G", "Generar un incidente"},
                {"[+] / INSERT", "Registrar incidente manual"}, {"T / CLIC MAPA", "Simular accidentes (RF-4)"},
                {"P", "Pausar"}, {"N", "Sonido on / off"},
                {"F5 / F9", "Guardar / cargar JSON"}, {"F6 / F10", "Guardar / cargar (.dat binario)"}
        };
        for (int i = 0; i < controls.length; i++) {
            Label key = new Label(controls[i][0]);
            key.getStyleClass().add("hud-key-active");
            keys.add(key, (i % 2) * 2, i / 2);
            keys.add(text(controls[i][1]), (i % 2) * 2 + 1, i / 2);
        }

        Label rules = text("★ Resolver en menos de " + GameController.FAST_LIMIT_SECONDS + " s: +" + GameController.FAST_BONUS_POINTS + " puntos de bonus.\n"
                + "✕ Un incidente ALTO sin unidad: -" + GameController.WAIT_PENALTY_POINTS + " cada " + GameController.WAIT_PERIOD_SECONDS + " s.\n"
                + "⏱ El turno dura 5 minutos. Si llegas a " + ShiftTimer.MAX_ACTIVE_INCIDENTS + " incidentes activos, pierdes.\n"
                + "▲ Mientras mas puntos tengas, mas seguido llegan incidentes.");

        Label close = new Label("[H] o [ESC] PARA VOLVER AL JUEGO");
        close.getStyleClass().add("hud-key-active");

        box.getChildren().addAll(
                PixelFont.createLabel("AYUDA", 5, RetroTheme.YELLOW, RetroTheme.PURPLE),
                title("▶ OBJETIVO"), goal, title("▶ CONTROLES"), keys, title("▶ PUNTOS Y TURNO"), rules, close);
        getChildren().add(box);
    }

    private Label text(String value) {
        Label label = new Label(value);
        label.getStyleClass().add("help-text");
        return label;
    }

    private Label title(String value) {
        Label label = new Label(value);
        label.getStyleClass().add("panel-title");
        return label;
    }
}
