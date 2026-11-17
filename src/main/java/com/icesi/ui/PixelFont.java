package com.icesi.ui;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import java.text.Normalizer;

// Fuente de pixeles de 5x7 dibujada a mano, para los titulos y numeros grandes del juego.
public final class PixelFont {

    public static final int GLYPH_WIDTH = 5;
    public static final int GLYPH_HEIGHT = 7;

    private static final String CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789 :!-./?>*<";

    // cada letra son 7 filas de 5 columnas, '#' es un pixel prendido
    private static final String[][] GLYPHS = {
            {" ### ", "#   #", "#   #", "#####", "#   #", "#   #", "#   #"}, // A
            {"#### ", "#   #", "#   #", "#### ", "#   #", "#   #", "#### "}, // B
            {" ### ", "#   #", "#    ", "#    ", "#    ", "#   #", " ### "}, // C
            {"#### ", "#   #", "#   #", "#   #", "#   #", "#   #", "#### "}, // D
            {"#####", "#    ", "#    ", "#### ", "#    ", "#    ", "#####"}, // E
            {"#####", "#    ", "#    ", "#### ", "#    ", "#    ", "#    "}, // F
            {" ### ", "#   #", "#    ", "# ###", "#   #", "#   #", " ####"}, // G
            {"#   #", "#   #", "#   #", "#####", "#   #", "#   #", "#   #"}, // H
            {" ### ", "  #  ", "  #  ", "  #  ", "  #  ", "  #  ", " ### "}, // I
            {"  ###", "   # ", "   # ", "   # ", "   # ", "#  # ", " ##  "}, // J
            {"#   #", "#  # ", "# #  ", "##   ", "# #  ", "#  # ", "#   #"}, // K
            {"#    ", "#    ", "#    ", "#    ", "#    ", "#    ", "#####"}, // L
            {"#   #", "## ##", "# # #", "# # #", "#   #", "#   #", "#   #"}, // M
            {"#   #", "##  #", "# # #", "#  ##", "#   #", "#   #", "#   #"}, // N
            {" ### ", "#   #", "#   #", "#   #", "#   #", "#   #", " ### "}, // O
            {"#### ", "#   #", "#   #", "#### ", "#    ", "#    ", "#    "}, // P
            {" ### ", "#   #", "#   #", "#   #", "# # #", "#  # ", " ## #"}, // Q
            {"#### ", "#   #", "#   #", "#### ", "# #  ", "#  # ", "#   #"}, // R
            {" ####", "#    ", "#    ", " ### ", "    #", "    #", "#### "}, // S
            {"#####", "  #  ", "  #  ", "  #  ", "  #  ", "  #  ", "  #  "}, // T
            {"#   #", "#   #", "#   #", "#   #", "#   #", "#   #", " ### "}, // U
            {"#   #", "#   #", "#   #", "#   #", "#   #", " # # ", "  #  "}, // V
            {"#   #", "#   #", "#   #", "# # #", "# # #", "# # #", " # # "}, // W
            {"#   #", "#   #", " # # ", "  #  ", " # # ", "#   #", "#   #"}, // X
            {"#   #", "#   #", " # # ", "  #  ", "  #  ", "  #  ", "  #  "}, // Y
            {"#####", "    #", "   # ", "  #  ", " #   ", "#    ", "#####"}, // Z
            {" ### ", "#   #", "#  ##", "# # #", "##  #", "#   #", " ### "}, // 0
            {"  #  ", " ##  ", "  #  ", "  #  ", "  #  ", "  #  ", " ### "}, // 1
            {" ### ", "#   #", "    #", "   # ", "  #  ", " #   ", "#####"}, // 2
            {"#####", "   # ", "  #  ", "   # ", "    #", "#   #", " ### "}, // 3
            {"   # ", "  ## ", " # # ", "#  # ", "#####", "   # ", "   # "}, // 4
            {"#####", "#    ", "#### ", "    #", "    #", "#   #", " ### "}, // 5
            {"  ## ", " #   ", "#    ", "#### ", "#   #", "#   #", " ### "}, // 6
            {"#####", "    #", "   # ", "  #  ", " #   ", " #   ", " #   "}, // 7
            {" ### ", "#   #", "#   #", " ### ", "#   #", "#   #", " ### "}, // 8
            {" ### ", "#   #", "#   #", " ####", "    #", "   # ", " ##  "}, // 9
            {"     ", "     ", "     ", "     ", "     ", "     ", "     "}, // espacio
            {"     ", "  #  ", "  #  ", "     ", "  #  ", "  #  ", "     "}, // :
            {"  #  ", "  #  ", "  #  ", "  #  ", "  #  ", "     ", "  #  "}, // !
            {"     ", "     ", "     ", "#####", "     ", "     ", "     "}, // -
            {"     ", "     ", "     ", "     ", "     ", " ##  ", " ##  "}, // .
            {"    #", "    #", "   # ", "  #  ", " #   ", "#    ", "#    "}, // /
            {" ### ", "#   #", "    #", "   # ", "  #  ", "     ", "  #  "}, // ?
            {" #   ", "  #  ", "   # ", "    #", "   # ", "  #  ", " #   "}, // >
            {"     ", "# # #", " ### ", "#####", " ### ", "# # #", "     "}, // *
            {"   # ", "  #  ", " #   ", "#    ", " #   ", "  #  ", "   # "}  // <
    };

    private PixelFont() {
    }

    // Dibuja el texto con pixeles cuadrados del tamano indicado.
    public static void draw(GraphicsContext gc, String text, double x, double y, double pixel, Color color) {
        String clean = normalize(text);
        gc.setFill(color);
        double cursor = x;
        for (int c = 0; c < clean.length(); c++) {
            int index = CHARS.indexOf(clean.charAt(c));
            if (index < 0) {
                index = CHARS.indexOf(' ');
            }
            String[] glyph = GLYPHS[index];
            for (int row = 0; row < GLYPH_HEIGHT; row++) {
                for (int col = 0; col < GLYPH_WIDTH; col++) {
                    if (glyph[row].charAt(col) == '#') {
                        gc.fillRect(cursor + col * pixel, y + row * pixel, pixel, pixel);
                    }
                }
            }
            // una columna de espacio entre letras
            cursor += (GLYPH_WIDTH + 1) * pixel;
        }
    }

    // Dibuja el texto con una sombra dura debajo, como en los juegos de 16 bits.
    public static void drawWithShadow(GraphicsContext gc, String text, double x, double y, double pixel, Color color, Color shadow) {
        draw(gc, text, x + pixel, y + pixel, pixel, shadow);
        draw(gc, text, x, y, pixel, color);
    }

    // Calcula el ancho en pantalla que ocupa el texto.
    public static double width(String text, double pixel) {
        return normalize(text).length() * (GLYPH_WIDTH + 1) * pixel - pixel;
    }

    // Crea un Canvas con el texto ya dibujado, para ponerlo dentro de un layout.
    public static Canvas createLabel(String text, double pixel, Color color, Color shadow) {
        Canvas canvas = new Canvas(width(text, pixel) + pixel, (GLYPH_HEIGHT + 1) * pixel);
        drawWithShadow(canvas.getGraphicsContext2D(), text, 0, 0, pixel, color, shadow);
        return canvas;
    }

    // Pasa a mayusculas y quita tildes, porque la fuente solo tiene letras basicas.
    private static String normalize(String text) {
        String upper = text == null ? "" : text.toUpperCase();
        return Normalizer.normalize(upper, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
    }
}
