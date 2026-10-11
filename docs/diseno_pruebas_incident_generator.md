# Diseño de pruebas unitarias

**Requerimiento funcional / Historia de usuario:** RF-4 - Generación de incidentes: los robos e incendios se generan de forma aleatoria, mientras que los accidentes de tránsito se disparan al cumplirse tres criterios (colisión de dos vehículos en la misma posición, congestión crítica en una zona o vehículo detenido prolongadamente en una vía).

**Nombre del método:** generateRandomTheft(): Incident, generateRandomFire(): Incident, checkCollisionAccident(String vehicleId1, String vehicleId2, String location): Incident, checkCongestionAccident(String zone, int vehicleCountInZone): Incident, checkStoppedVehicleAccident(String vehicleId, String location, int stoppedCycles): Incident

**Objetivo de la Prueba:** Verificar que el generador cree incidentes aleatorios válidos de robo (THEFT) e incendio (FIRE) con IDs secuenciales únicos, y que evalúe correctamente los tres criterios de accidentes de tránsito generando un incidente de tipo ACCIDENT únicamente cuando se cumpla la condición crítica (o retornando null en caso contrario).

| Arrange (Inicialización) | Act (Acción) | Assert (Verificar) |
|---|---|---|
| **Caso 1: Generar robo aleatorio crea un incidente de tipo THEFT en estado PENDING con ID válido.** | | |
| Instanciar generator = new IncidentGenerator(). | Incident theft = generator.generateRandomTheft(); | assertNotNull(theft); assertEquals(IncidentType.THEFT, theft.getType()); assertEquals(IncidentStatus.PENDING, theft.getStatus()); assertTrue(theft.getId().startsWith("THEFT-")); assertNotNull(theft.getLocation()); assertNotNull(theft.getDescription()); |
| **Caso 2: Generar incendio aleatorio crea un incidente de tipo FIRE en estado PENDING con ID válido.** | | |
| Instanciar generator = new IncidentGenerator(). | Incident fire = generator.generateRandomFire(); | assertNotNull(fire); assertEquals(IncidentType.FIRE, fire.getType()); assertEquals(IncidentStatus.PENDING, fire.getStatus()); assertTrue(fire.getId().startsWith("FIRE-")); assertNotNull(fire.getLocation()); assertNotNull(fire.getDescription()); |
| **Caso 3: Dos vehículos distintos en la misma ubicación disparan un accidente por colisión con gravedad HIGH.** | | |
| Definir vehicle1 = "P-01", vehicle2 = "A-01" y location = "Interseccion Calle 5 con Cra 10". | Incident accident = generator.checkCollisionAccident(vehicle1, vehicle2, location); | assertNotNull(accident); assertEquals(IncidentType.ACCIDENT, accident.getType()); assertEquals(Severity.HIGH, accident.getSeverity()); assertEquals(IncidentStatus.PENDING, accident.getStatus()); assertTrue(accident.getId().startsWith("ACC-")); assertTrue(accident.getDescription().contains("colision")); assertEquals("Interseccion Calle 5 con Cra 10", accident.getLocation()); |
| **Caso 4: No se genera colisión si los IDs de los vehículos son iguales o nulos.** | | |
| Instanciar generator = new IncidentGenerator(). | Incident same = generator.checkCollisionAccident("P-01", "P-01", "Calle 5"); Incident nullOne = generator.checkCollisionAccident(null, "A-01", "Calle 5"); | assertNull(same); assertNull(nullOne); |
| **Caso 5: Superar el umbral máximo de vehículos en una zona genera un accidente por congestión crítica.** | | |
| Definir zone = "Zona Comercial" y vehicleCount = 7 (mayor al umbral CRITICAL_CONGESTION_THRESHOLD = 5). | Incident accident = generator.checkCongestionAccident(zone, vehicleCount); | assertNotNull(accident); assertEquals(IncidentType.ACCIDENT, accident.getType()); assertEquals(Severity.MEDIUM, accident.getSeverity()); assertTrue(accident.getDescription().contains("congestion critica")); assertEquals("Zona Comercial", accident.getLocation()); |
| **Caso 6: No se genera accidente por congestión si la cantidad de vehículos está dentro del límite permitido.** | | |
| Definir zone = "Zona Comercial" y vehicleCount = 3 (menor o igual al umbral). | Incident accident = generator.checkCongestionAccident(zone, vehicleCount); | assertNull(accident); |
| **Caso 7: Un vehículo detenido durante ciclos que igualan o superan el umbral dispara un accidente.** | | |
| Definir vehicleId = "F-01", location = "Avenida Panamericana" y stoppedCycles = 4 (mayor o igual a STOPPED_CYCLES_THRESHOLD = 3). | Incident accident = generator.checkStoppedVehicleAccident(vehicleId, location, stoppedCycles); | assertNotNull(accident); assertEquals(IncidentType.ACCIDENT, accident.getType()); assertTrue(accident.getDescription().contains("F-01")); assertEquals("Avenida Panamericana", accident.getLocation()); |
| **Caso 8: No se genera accidente si el vehículo ha estado detenido menos ciclos que el umbral.** | | |
| Definir vehicleId = "F-01", location = "Avenida Panamericana" y stoppedCycles = 2 (menor al umbral). | Incident accident = generator.checkStoppedVehicleAccident(vehicleId, location, stoppedCycles); | assertNull(accident); |
| **Caso 9: Múltiples incidentes generados tienen IDs secuenciales y únicos.** | | |
| Instanciar generator = new IncidentGenerator(). | Incident theft1 = generator.generateRandomTheft(); Incident theft2 = generator.generateRandomTheft(); Incident fire1 = generator.generateRandomFire(); | assertNotEquals(theft1.getId(), theft2.getId()); assertEquals("THEFT-001", theft1.getId()); assertEquals("THEFT-002", theft2.getId()); assertEquals("FIRE-001", fire1.getId()); |
