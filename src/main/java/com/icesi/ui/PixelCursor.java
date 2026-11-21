package com.icesi.ui;

import javafx.scene.ImageCursor;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.ButtonBase;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;

// Cursores pixel-art para el mouse: una flecha normal y una manito cuando esta encima de un boton.
public final class PixelCursor {

    private static final int SCALE = 2; // cada pixel del dibujo se ve de 2x2

    // 'k' es borde negro, 'w' relleno crema, 'y' amarillo, '.' transparente
    private static final String[] ARROW = {
            "k...........",
            "kk..........",
            "kwk.........",
            "kwwk........",
            "kwwwk.......",
            "kwwwwk......",
            "kwwwwwk.....",
            "kwwwwwwk....",
            "kwwwwwwwk...",
            "kwwwwwkkkk..",
            "kwwkwwk.....",
            "kwk.kwwk....",
            "kk..kwwk....",
            "k....kwwk...",
            ".....kwwk...",
            "......kk...."
    };

    private static final String[] HAND = {
            "....kk......",
            "...kyyk.....",
            "...kyyk.....",
            "...kyyk.....",
            "...kyykkk...",
            "...kyykyykk.",
            ".kkkyykyykyk",
            "kyykyyyyyyyk",
            "kyyyyyyyyyyk",
            ".kyyyyyyyyyk",
            "..kyyyyyyyk.",
            "..kyyyyyyyk.",
            "...kyyyyyk..",
            "...kkkkkkk.."
    };

    private static final ImageCursor ARROW_CURSOR = build(ARROW, 0, 0);
    private static final ImageCursor HAND_CURSOR = build(HAND, 4 * SCALE, 0);

    private PixelCursor() {
    }

    // Pone la flecha en la escena y cambia a la manito cuando el mouse pasa por un boton.
    public static void install(Scene scene) {
        scene.setCursor(ARROW_CURSOR);
        scene.addEventFilter(MouseEvent.MOUSE_MOVED, event -> {
            boolean overButton = isInsideButton(event.getPickResult().getIntersectedNode());
            scene.setCursor(overButton ? HAND_CURSOR : ARROW_CURSOR);
        });
    }

    // Revisa si el nodo (o alguno de sus padres) es un boton.
    private static boolean isInsideButton(Node node) {
        while (node != null) {
            if (node instanceof ButtonBase) {
                return true;
            }
            node = node.getParent();
        }
        return false;
    }

    // Convierte el dibujo de letras en una imagen y la vuelve cursor; (hotX, hotY) es el punto que hace clic.
    private static ImageCursor build(String[] rows, double hotX, double hotY) {
        int width = rows[0].length() * SCALE;
        int height = rows.length * SCALE;
        WritableImage image = new WritableImage(width, height);
        PixelWriter writer = image.getPixelWriter();
        for (int row = 0; row < rows.length; row++) {
            for (int col = 0; col < rows[row].length(); col++) {
                Color color = colorOf(rows[row].charAt(col));
                for (int dy = 0; dy < SCALE; dy++) {
                    for (int dx = 0; dx < SCALE; dx++) {
                        writer.setColor(col * SCALE + dx, row * SCALE + dy, color);
                    }
                }
            }
        }
        return new ImageCursor(image, hotX, hotY);
    }

    private static Color colorOf(char letter) {
        if (letter == 'k') {
            return RetroTheme.BLACK;
        }
        if (letter == 'w') {
            return RetroTheme.CREAM;
        }
        if (letter == 'y') {
            return RetroTheme.YELLOW;
        }
        return Color.TRANSPARENT;
    }
}
