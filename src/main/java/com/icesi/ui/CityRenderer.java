package com.icesi.ui;

import com.icesi.model.CityMap;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

// Dibuja el mapa de la ciudad celda por celda con estilo pixel-art: calles, pasto, plazas, casas, tiendas y parques.
public class CityRenderer {

    private static final Color ROAD = Color.web("#3d3a36");
    private static final Color PATH_DARK = Color.web("#4a443e");
    private static final Color GRASS_LIGHT = Color.web("#1fa84f");
    private static final Color PLAZA = Color.web("#b9a28a");
    private static final Color PLAZA_LINE = Color.web("#9c8670");

    private CityMap cityMap;
    private double cell;

    // Crea el dibujante para el mapa con el tamano de celda en pixeles.
    public CityRenderer(CityMap cityMap, double cell) {
        this.cityMap = cityMap;
        this.cell = cell;
    }

    // Dibuja todo el mapa fijo (suelo y edificios); se hace una sola vez porque el mapa no cambia.
    public void drawCity(GraphicsContext gc) {
        int size = cityMap.getSize();
        for (int row = 0; row < size; row++) {
            for (int column = 0; column < size; column++) {
                drawTile(gc, row, column);
            }
        }
        drawBuildings(gc);
    }

    // Dibuja el suelo de una celda segun su tipo.
    private void drawTile(GraphicsContext gc, int row, int column) {
        double x = column * cell;
        double y = row * cell;
        int type = cityMap.getCell(row, column);

        if (type == CityMap.MAIN_ROAD) {
            gc.setFill(ROAD);
            gc.fillRect(x, y, cell, cell);
            boolean horizontal = row % 16 == 8;
            boolean vertical = column % 16 == 8;
            if (horizontal && vertical) {
                // cruce: paso de cebra
                gc.setFill(RetroTheme.CREAM);
                for (double stripe = 1; stripe < cell - 1; stripe += 3) {
                    gc.fillRect(x + stripe, y + 1, 1.5, cell - 2);
                }
            } else if (horizontal && column % 2 == 0) {
                gc.setFill(RetroTheme.YELLOW);
                gc.fillRect(x + 1, y + cell / 2 - 1, cell - 2, 2);
            } else if (vertical && row % 2 == 0) {
                gc.setFill(RetroTheme.YELLOW);
                gc.fillRect(x + cell / 2 - 1, y + 1, 2, cell - 2);
            }
        } else if (type == CityMap.PATH) {
            gc.setFill(RetroTheme.DARK_GRAY);
            gc.fillRect(x, y, cell, cell);
            gc.setFill(PATH_DARK);
            gc.fillRect(x, y, cell, 1);
            gc.fillRect(x, y, 1, cell);
        } else if (type == CityMap.RESIDENTIAL) {
            gc.setFill(RetroTheme.DARK_GREEN);
            gc.fillRect(x, y, cell, cell);
            // manchitas de pasto que siempre quedan en el mismo lugar
            if ((row * 7 + column * 13) % 4 == 0) {
                gc.setFill(GRASS_LIGHT);
                gc.fillRect(x + 3, y + 3, 2, 2);
                gc.fillRect(x + 7, y + 6, 2, 2);
            }
        } else if (type == CityMap.COMMERCIAL) {
            gc.setFill(PLAZA);
            gc.fillRect(x, y, cell, cell);
            gc.setFill(PLAZA_LINE);
            gc.fillRect(x, y + cell / 2, cell, 1);
            gc.fillRect(x + cell / 2, y, 1, cell);
        } else {
            drawObstacle(gc, row, column, x, y);
        }
    }

    // Borde de la ciudad: muro de ladrillo; obstaculo suelto en una calle: barrera de obra; dentro de manzana: suelo de parque.
    private void drawObstacle(GraphicsContext gc, int row, int column, double x, double y) {
        int last = cityMap.getSize() - 1;
        if (row == 0 || column == 0 || row == last || column == last) {
            gc.setFill(RetroTheme.BROWN);
            gc.fillRect(x, y, cell, cell);
            gc.setFill(RetroTheme.BLACK);
            gc.fillRect(x, y + cell / 2, cell, 1);
            gc.fillRect(x + ((row % 2 == 0) ? cell / 3 : cell * 2 / 3), y, 1, cell / 2);
            gc.fillRect(x + ((row % 2 == 0) ? cell * 2 / 3 : cell / 3), y + cell / 2, 1, cell / 2);
        } else if (isInsideBlock(row, column)) {
            gc.setFill(Color.web("#0f5a36"));
            gc.fillRect(x, y, cell, cell);
        } else {
            gc.setFill(RetroTheme.DARK_GRAY);
            gc.fillRect(x, y, cell, cell);
            gc.setFill(RetroTheme.ORANGE);
            gc.fillRect(x + 1, y + 2, cell - 2, cell - 4);
            gc.setFill(RetroTheme.BLACK);
            for (double stripe = 2; stripe < cell - 2; stripe += 4) {
                gc.fillRect(x + stripe, y + 2, 2, cell - 4);
            }
        }
    }

    // Dibuja una casa, tienda o arbol encima de cada manzana de 3x3 que sea de un solo tipo.
    private void drawBuildings(GraphicsContext gc) {
        int size = cityMap.getSize();
        int blockNumber = 0;
        for (int row = 1; row + 2 < size; row += 4) {
            for (int column = 1; column + 2 < size; column += 4) {
                int type = cityMap.getCell(row, column);
                if (!isUniformBlock(row, column, type)) {
                    continue;
                }
                blockNumber++;
                String sprite = chooseSprite(type, blockNumber);
                if (sprite == null) {
                    continue;
                }
                SpriteRegistry.draw(gc, sprite, column * cell, row * cell, cell * 3, cell * 3);
            }
        }
    }

    // Elige que dibujar en la manzana: algunas son edificios especiales (hospital, policia, bomberos)
    // y las demas alternan colores y parques para que la ciudad se vea variada.
    private String chooseSprite(int type, int blockNumber) {
        // edificios especiales: pocos y repartidos por la ciudad
        if (type == CityMap.RESIDENTIAL || type == CityMap.COMMERCIAL) {
            if (blockNumber == 38 || blockNumber == 171) {
                return SpriteRegistry.HOSPITAL;
            }
            if (blockNumber == 22 || blockNumber == 118 || blockNumber == 213) {
                return SpriteRegistry.POLICE_STATION;
            }
            if (blockNumber == 75 || blockNumber == 190) {
                return SpriteRegistry.FIRE_STATION;
            }
        }
        if (type == CityMap.COMMERCIAL) {
            if (blockNumber % 3 == 1) {
                return SpriteRegistry.SHOP + "_pink";
            }
            return (blockNumber % 3 == 2) ? SpriteRegistry.SHOP + "_blue" : SpriteRegistry.SHOP;
        }
        if (type == CityMap.OBSTACLE) {
            return (blockNumber % 2 == 0) ? SpriteRegistry.FOUNTAIN : SpriteRegistry.TREE;
        }
        if (type == CityMap.RESIDENTIAL) {
            if (blockNumber % 9 == 0) {
                return SpriteRegistry.FOUNTAIN;
            }
            if (blockNumber % 5 == 0) {
                return SpriteRegistry.TREE;
            }
            if (blockNumber % 3 == 1) {
                return SpriteRegistry.HOUSE + "_purple";
            }
            return (blockNumber % 3 == 2) ? SpriteRegistry.HOUSE + "_brown" : SpriteRegistry.HOUSE;
        }
        return null;
    }

    // Indica si las 9 celdas de la manzana son del mismo tipo.
    private boolean isUniformBlock(int row, int column, int type) {
        for (int i = row; i < row + 3; i++) {
            for (int j = column; j < column + 3; j++) {
                if (cityMap.getCell(i, j) != type) {
                    return false;
                }
            }
        }
        return true;
    }

    // Las manzanas son las celdas que no caen en las calles (cada 4 filas y columnas).
    private boolean isInsideBlock(int row, int column) {
        return row % 4 != 0 && column % 4 != 0;
    }

    public double getCell() {
        return cell;
    }
}
