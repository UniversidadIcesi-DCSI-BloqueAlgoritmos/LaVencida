# Diseño de pruebas unitarias

**Requerimiento funcional / Historia de usuario:** RF-2 - Representar el mapa de la ciudad: mostrar zonas residenciales, zonas comerciales, vías principales, rutas, vehículos e incidentes activos.

**Nombre del método:** resolve(String location): int[]

**Objetivo de la Prueba:** Verificar que la interfaz gráfica pueda ubicar en el mapa 64x64 cualquier incidente o vehículo a partir del nombre de su ubicación (por ejemplo "Parque Bolivar"), devolviendo siempre la misma celda para el mismo nombre, una celda transitable (camino o vía principal) dentro de los límites del mapa, y lanzando una excepción si la ubicación es nula o vacía.

| Arrange (Inicialización) | Act (Acción) | Assert (Verificar) |
|---|---|---|
| **Caso 1: Resolver dos veces la misma ubicación devuelve la misma celda.** | | |
| Crear cityMap y resolver = new MapPositionResolver(cityMap). | int[] first = resolver.resolve("Parque Bolivar"); int[] second = resolver.resolve("Parque Bolivar"); | assertArrayEquals(first, second); |
| **Caso 2: La celda resuelta siempre es transitable y está dentro del mapa.** | | |
| Crear resolver y un arreglo con varias ubicaciones del generador ("Zona Comercial Centro", "Barrio El Prado", "Hospital Central", "Estacion Norte"). | int[] cell = resolver.resolve(location); (para cada ubicación) | assertTrue(cityMap.isWalkable(cell[0], cell[1])); |
| **Caso 3: Ubicaciones distintas quedan en celdas distintas.** | | |
| Crear resolver. | int[] park = resolver.resolve("Parque Bolivar"); int[] prado = resolver.resolve("Barrio El Prado"); | assertFalse(park[0] == prado[0] && park[1] == prado[1]); |
| **Caso 4: Resolver una ubicación nula o vacía lanza una excepción.** | | |
| Crear resolver. | resolver.resolve(null); resolver.resolve("   "); | assertThrows(IllegalArgumentException.class, () -> resolver.resolve(null)); assertThrows(IllegalArgumentException.class, () -> resolver.resolve("   ")); |
