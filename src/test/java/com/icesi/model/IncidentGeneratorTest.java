package com.icesi.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class IncidentGeneratorTest {

    private IncidentGenerator generator;

    @BeforeEach
    void setUp() {
        generator = new IncidentGenerator();
    }

    // RF4
    // Caso 1: Generar robo aleatorio crea un incidente de tipo THEFT en estado PENDING con ID valido.
    @Test
    void generateRandomTheftCreatesTheftIncident() {
        Incident theft = generator.generateRandomTheft();

        assertNotNull(theft);
        assertEquals(IncidentType.THEFT, theft.getType());
        assertEquals(IncidentStatus.PENDING, theft.getStatus());
        assertTrue(theft.getId().startsWith("THEFT-"));
        assertNotNull(theft.getLocation());
        assertNotNull(theft.getDescription());
    }

    // RF4
    // Caso 2: Generar incendio aleatorio crea un incidente de tipo FIRE en estado PENDING con ID valido.
    @Test
    void generateRandomFireCreatesFireIncident() {
        Incident fire = generator.generateRandomFire();

        assertNotNull(fire);
        assertEquals(IncidentType.FIRE, fire.getType());
        assertEquals(IncidentStatus.PENDING, fire.getStatus());
        assertTrue(fire.getId().startsWith("FIRE-"));
        assertNotNull(fire.getLocation());
        assertNotNull(fire.getDescription());
    }

    // RF4 - Criterio 1 de Accidente: Colision por dos vehiculos en la misma posicion.
    // Caso 3: Dos vehiculos distintos en la misma ubicacion disparan un accidente con gravedad HIGH.
    @Test
    void checkCollisionAccidentGeneratesAccidentWhenTwoVehiclesShareLocation() {
        Incident accident = generator.checkCollisionAccident("P-01", "A-01", "Interseccion Calle 5 con Cra 10");

        assertNotNull(accident);
        assertEquals(IncidentType.ACCIDENT, accident.getType());
        assertEquals(Severity.HIGH, accident.getSeverity());
        assertEquals(IncidentStatus.PENDING, accident.getStatus());
        assertTrue(accident.getId().startsWith("ACC-"));
        assertTrue(accident.getDescription().contains("colision"));
        assertEquals("Interseccion Calle 5 con Cra 10", accident.getLocation());
    }

    // Caso 4: No se genera colision si los IDs de los vehiculos son iguales o nulos.
    @Test
    void checkCollisionAccidentReturnsNullWhenInvalidOrSameVehicle() {
        Incident same = generator.checkCollisionAccident("P-01", "P-01", "Calle 5");
        Incident nullOne = generator.checkCollisionAccident(null, "A-01", "Calle 5");

        assertNull(same);
        assertNull(nullOne);
    }

    // RF4 - Criterio 2 de Accidente: Congestion critica en una zona.
    // Caso 5: Superar el umbral maximo de vehiculos en una zona genera un accidente de transito.
    @Test
    void checkCongestionAccidentGeneratesAccidentWhenThresholdExceeded() {
        Incident accident = generator.checkCongestionAccident("Zona Comercial", 7);

        assertNotNull(accident);
        assertEquals(IncidentType.ACCIDENT, accident.getType());
        assertEquals(Severity.MEDIUM, accident.getSeverity());
        assertTrue(accident.getDescription().contains("congestion critica"));
        assertEquals("Zona Comercial", accident.getLocation());
    }

    // Caso 6: No se genera accidente si la cantidad de vehiculos esta dentro del limite permitido.
    @Test
    void checkCongestionAccidentReturnsNullWhenUnderThreshold() {
        Incident accident = generator.checkCongestionAccident("Zona Comercial", 3);
        assertNull(accident);
    }

    // RF4 - Criterio 3 de Accidente: Vehiculo detenido prolongadamente en una via.
    // Caso 7: Un vehiculo detenido durante ciclos que superan el umbral dispara un accidente.
    @Test
    void checkStoppedVehicleAccidentGeneratesAccidentWhenThresholdExceeded() {
        Incident accident = generator.checkStoppedVehicleAccident("F-01", "Avenida Panamericana", 4);

        assertNotNull(accident);
        assertEquals(IncidentType.ACCIDENT, accident.getType());
        assertTrue(accident.getDescription().contains("F-01"));
        assertEquals("Avenida Panamericana", accident.getLocation());
    }

    // Caso 8: No se genera accidente si el vehiculo ha estado detenido menos ciclos que el umbral.
    @Test
    void checkStoppedVehicleAccidentReturnsNullWhenUnderThreshold() {
        Incident accident = generator.checkStoppedVehicleAccident("F-01", "Avenida Panamericana", 2);
        assertNull(accident);
    }

    // Caso 9: Multiples incidentes generados tienen IDs secuenciales y unicos.
    @Test
    void generatedIncidentsHaveUniqueSequentialIds() {
        Incident theft1 = generator.generateRandomTheft();
        Incident theft2 = generator.generateRandomTheft();
        Incident fire1 = generator.generateRandomFire();

        assertNotEquals(theft1.getId(), theft2.getId());
        assertEquals("THEFT-001", theft1.getId());
        assertEquals("THEFT-002", theft2.getId());
        assertEquals("FIRE-001", fire1.getId());
    }
}
