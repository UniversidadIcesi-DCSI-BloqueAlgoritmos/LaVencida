# Diseño de pruebas unitarias

**Requerimiento funcional / Historia de usuario:** RF-2 - Representar el mapa de la ciudad: modelar la ciudad mediante una cuadrícula que distinga calles, avenidas principales, zonas residenciales, zonas comerciales y obstáculos, permitiendo validar los límites del mapa y qué celdas son transitables por el operador.

**Nombre del método:** getSize(): int, isWithinBounds(int row, int column): boolean, isWalkable(int row, int column): boolean, getCell(int row, int column): int, setCell(int row, int column, int zoneType): void

**Objetivo de la Prueba:** Verificar que el mapa de la ciudad se inicialice con dimensiones 64x64 y sus zonas urbanas por defecto, que `isWithinBounds` identifique correctamente coordenadas dentro y fuera de los límites, que `isWalkable` permita el paso únicamente por calles (`PATH`) y vías principales (`MAIN_ROAD`) bloqueando obstáculos y edificios, y que `getCell`/`setCell` actualicen celdas válidas o lancen `IndexOutOfBoundsException` fuera del rango.

| Arrange (Inicialización) | Act (Acción) | Assert (Verificar) |
|---|---|---|
| **Caso 1: El mapa por defecto se inicializa con tamaño 64x64 y contiene vías principales, calles y zonas edificadas.** | | |
| Instanciar `cityMap = new CityMap()`. | `int size = cityMap.getSize(); int mainRoadCell = cityMap.getCell(0, 0); int pathCell = cityMap.getCell(4, 4); int buildingCell = cityMap.getCell(2, 2);` | `assertEquals(64, size); assertEquals(CityMap.MAIN_ROAD, mainRoadCell); assertEquals(CityMap.PATH, pathCell); assertTrue(buildingCell == CityMap.RESIDENTIAL \|\| buildingCell == CityMap.COMMERCIAL);` |
| **Caso 2: `isWithinBounds` retorna `true` para coordenadas dentro de 0..63 y `false` fuera de los límites.** | | |
| Instanciar `cityMap = new CityMap()`. | `boolean originValid = cityMap.isWithinBounds(0, 0); boolean maxValid = cityMap.isWithinBounds(63, 63); boolean negativeRow = cityMap.isWithinBounds(-1, 5); boolean overflowCol = cityMap.isWithinBounds(10, 64);` | `assertTrue(originValid); assertTrue(maxValid); assertFalse(negativeRow); assertFalse(overflowCol);` |
| **Caso 3: `isWalkable` permite transitar por calles y vías principales pero rechaza edificios, obstáculos y coordenadas fuera del mapa.** | | |
| Instanciar `cityMap = new CityMap()`. | `boolean canWalkPath = cityMap.isWalkable(4, 4); boolean canWalkMainRoad = cityMap.isWalkable(0, 10); boolean canWalkObstacle = cityMap.isWalkable(4, 8); boolean canWalkBuilding = cityMap.isWalkable(2, 2); boolean canWalkOutOfBounds = cityMap.isWalkable(-1, 0);` | `assertTrue(canWalkPath); assertTrue(canWalkMainRoad); assertFalse(canWalkObstacle); assertFalse(canWalkBuilding); assertFalse(canWalkOutOfBounds);` |
| **Caso 4: `setCell` actualiza el tipo de zona en coordenadas válidas y lanza `IndexOutOfBoundsException` fuera de los límites.** | | |
| Instanciar `cityMap = new CityMap()`. | `cityMap.setCell(4, 5, CityMap.OBSTACLE); int updated = cityMap.getCell(4, 5);` | `assertEquals(CityMap.OBSTACLE, updated); assertFalse(cityMap.isWalkable(4, 5)); assertThrows(IndexOutOfBoundsException.class, () -> cityMap.getCell(64, 0)); assertThrows(IndexOutOfBoundsException.class, () -> cityMap.setCell(0, -1, CityMap.PATH));` |
