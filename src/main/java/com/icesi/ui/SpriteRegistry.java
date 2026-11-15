package com.icesi.ui;

import com.icesi.model.IncidentType;
import com.icesi.model.VehicleType;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

// Guarda los sprites del juego. Si existe un PNG en /com/icesi/images/<nombre>.png lo usa;
// si no, dibuja el sprite pixel-art que esta definido aqui con letras (cada letra es un color).
public final class SpriteRegistry {

    public static final String OPERATOR = "operator";
    public static final String PATROL = "patrol";
    public static final String AMBULANCE = "ambulance";
    public static final String FIRE_TRUCK = "fire_truck";
    public static final String THEFT = "theft";
    public static final String FIRE = "fire";
    public static final String ACCIDENT = "accident";
    public static final String HOUSE = "house";
    public static final String SHOP = "shop";
    public static final String TREE = "tree";

    private static final String IMAGES_FOLDER = "/com/icesi/images/";

    // '.' es transparente; las demas letras son colores de la paleta
    private static final Map<Character, Color> PALETTE = new HashMap<>();
    private static final Map<String, String[]> PIXEL_ART = new HashMap<>();
    // imagenes ya buscadas (si no existe el PNG se guarda null para no buscarlo cada vez)
    private static final Map<String, Image> IMAGES = new HashMap<>();

    static {
        PALETTE.put('k', RetroTheme.BLACK);
        PALETTE.put('n', RetroTheme.NIGHT);
        PALETTE.put('u', RetroTheme.PURPLE);
        PALETTE.put('G', RetroTheme.DARK_GREEN);
        PALETTE.put('B', RetroTheme.BROWN);
        PALETTE.put('d', RetroTheme.DARK_GRAY);
        PALETTE.put('l', RetroTheme.LIGHT_GRAY);
        PALETTE.put('w', RetroTheme.CREAM);
        PALETTE.put('r', RetroTheme.RED);
        PALETTE.put('o', RetroTheme.ORANGE);
        PALETTE.put('y', RetroTheme.YELLOW);
        PALETTE.put('g', RetroTheme.GREEN);
        PALETTE.put('b', RetroTheme.BLUE);
        PALETTE.put('v', RetroTheme.LAVENDER);
        PALETTE.put('p', RetroTheme.PINK);
        PALETTE.put('e', RetroTheme.PEACH);

        PIXEL_ART.put(OPERATOR, new String[]{
                "....kkkk....",
                "...knnnnk...",
                "..knnyynnk..",
                "..kkkkkkkk..",
                "...keeeek...",
                "...kekkek...",
                "...keeeek...",
                "..kbbbbbbk..",
                ".kebbybbbek.",
                "..kbbbbbbk..",
                "...knnnnk...",
                "...kk..kk..."
        });
        PIXEL_ART.put(PATROL, new String[]{
                "............",
                "............",
                "....kkkk....",
                "....kbrk....",
                "..kkkkkkkk..",
                ".kwwkllkwwk.",
                "kwwwwwwwwwwk",
                "knnnnnnnnnnk",
                "kwwwwwwwwwwk",
                "kkdddkkdddkk",
                "..kdk..kdk..",
                "............"
        });
        PIXEL_ART.put(AMBULANCE, new String[]{
                "............",
                ".....kk.....",
                "....krbk....",
                ".kkkkkkkkk..",
                ".kwwwwwwwkk.",
                ".kwwwrwwwkbk",
                ".kwwrrrwwkbk",
                ".kwwwrwwwkkk",
                ".kwwwwwwwwwk",
                ".kkdddkkdddk",
                "..kdk..kdk..",
                "............"
        });
        PIXEL_ART.put(FIRE_TRUCK, new String[]{
                "............",
                ".lklklklk...",
                ".kkkkkkkkk..",
                ".krrrrrrrkk.",
                ".krrrrrrrkbk",
                ".kryyyyyrkbk",
                ".krrrrrrrkkk",
                ".krrrrrrrrrk",
                ".kkdddkkdddk",
                "..kdk..kdk..",
                "............",
                "............"
        });
        PIXEL_ART.put(FIRE, new String[]{
                ".....k......",
                "....kok.....",
                "....koyk....",
                "...koyok....",
                "..koyyyok...",
                ".kooyyyook..",
                ".koyywyyok..",
                ".koyywwyok..",
                ".krooyyook..",
                "..krrooork..",
                "...kkkkkk...",
                "............"
        });
        PIXEL_ART.put(THEFT, new String[]{
                "....kkkk....",
                ".....kk.....",
                "....kBBk....",
                "...kBBBBk...",
                "..kBBkkBBk..",
                ".kBBkBBBBBk.",
                ".kBBBkkBBBk.",
                ".kBBBBBkBBk.",
                ".kBBkkkBBBk.",
                "..kBBBBBBk..",
                "...kkkkkk...",
                "............"
        });
        PIXEL_ART.put(ACCIDENT, new String[]{
                ".....y......",
                ".....yy...y.",
                ".y..yrry.yy.",
                ".yyyrrrryy..",
                "..yrrwwrry..",
                ".yyrwwwwry..",
                "..yrrwwrryy.",
                "..yyrrrryy..",
                ".yy.yrry.y..",
                ".y...yy.....",
                ".....y......",
                "............"
        });
        PIXEL_ART.put(HOUSE, new String[]{
                "....kkk....",
                "...krrrk...",
                "..krrrrrk..",
                ".krrrrrrrk.",
                "kkkkkkkkkkk",
                ".kwwwwwwwk.",
                ".kwbwwwbwk.",
                ".kwbwBwbwk.",
                ".kwwwBwwwk.",
                ".kkkkkkkkk.",
                "..........."
        });
        PIXEL_ART.put(SHOP, new String[]{
                "kkkkkkkkkkk",
                "koyoyoyoyok",
                "kkkkkkkkkkk",
                "klllllllllk",
                "klbblllbblk",
                "klbblllbblk",
                "klllllllllk",
                "klllkkklllk",
                "klllkdklllk",
                "klllkdklllk",
                "kkkkkkkkkkk"
        });
        PIXEL_ART.put(TREE, new String[]{
                "...kkkkk...",
                "..kgggggk..",
                ".kggGgggGk.",
                ".kgggggggk.",
                ".kgGggGggk.",
                "..kgggggk..",
                "...kkBkk...",
                "....kBk....",
                "....kBk....",
                "...kkkkk...",
                "..........."
        });
    }

    private SpriteRegistry() {
    }

    // Dibuja el sprite en el rectangulo indicado: usa el PNG si existe y si no el pixel-art.
    public static void draw(GraphicsContext gc, String name, double x, double y, double width, double height) {
        Image image = findImage(name);
        if (image != null) {
            gc.setImageSmoothing(false);
            gc.drawImage(image, x, y, width, height);
            return;
        }
        String[] rows = PIXEL_ART.get(name);
        if (rows == null) {
            return;
        }
        double pixelWidth = width / rows[0].length();
        double pixelHeight = height / rows.length;
        for (int row = 0; row < rows.length; row++) {
            for (int col = 0; col < rows[row].length(); col++) {
                Color color = PALETTE.get(rows[row].charAt(col));
                if (color != null) {
                    gc.setFill(color);
                    // +0.5 para que no queden lineas entre pixeles al escalar
                    gc.fillRect(x + col * pixelWidth, y + row * pixelHeight, pixelWidth + 0.5, pixelHeight + 0.5);
                }
            }
        }
    }

    // Crea un Canvas pequeno con el sprite, para usarlo como icono en los paneles.
    public static Canvas createIcon(String name, double size) {
        Canvas canvas = new Canvas(size, size);
        draw(canvas.getGraphicsContext2D(), name, 0, 0, size, size);
        return canvas;
    }

    // Devuelve el nombre del sprite que corresponde al tipo de incidente.
    public static String forIncident(IncidentType type) {
        if (type == IncidentType.FIRE) {
            return FIRE;
        }
        if (type == IncidentType.THEFT) {
            return THEFT;
        }
        return ACCIDENT;
    }

    // Devuelve el nombre del sprite que corresponde al tipo de vehiculo.
    public static String forVehicle(VehicleType type) {
        if (type == VehicleType.AMBULANCE) {
            return AMBULANCE;
        }
        if (type == VehicleType.FIRE_TRUCK) {
            return FIRE_TRUCK;
        }
        return PATROL;
    }

    // Busca el PNG del sprite una sola vez y lo guarda.
    private static Image findImage(String name) {
        if (!IMAGES.containsKey(name)) {
            Image image = null;
            InputStream stream = SpriteRegistry.class.getResourceAsStream(IMAGES_FOLDER + name + ".png");
            if (stream != null) {
                image = new Image(stream);
            }
            IMAGES.put(name, image);
        }
        return IMAGES.get(name);
    }
}
