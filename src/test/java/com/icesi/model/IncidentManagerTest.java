package com.icesi.model;

import com.icesi.exceptions.DuplicateIncidentException;
import com.icesi.exceptions.IncidentNotFoundException;
import com.icesi.exceptions.IncidentStateException;
import com.icesi.exceptions.NoActiveIncidentsException;
import com.icesi.exceptions.VehicleAssignmentException;
import com.icesi.exceptions.VehicleReleaseException;
import com.icesi.structures.LinkedList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class IncidentManagerTest {

    private IncidentManager manager;

    @BeforeEach
    void setUp() {
        manager = new IncidentManager();
    }

    // RF3
    // Caso 1: Registrar un incidente válido lo agrega correctamente al sistema.
    @Test
    void registerValidIncidentAddsItToTheSystem() {
        // Arrange
        Incident incident1 = new Incident("INC001", IncidentType.ACCIDENT, "Zona Norte", Severity.HIGH);

        // Act
        manager.registerIncident(incident1);

        // Assert
        assertEquals(1, manager.getIncidentCount());
        assertEquals(incident1, manager.getIncidentAt(0));
    }

    // Caso 2: Registrar dos incidentes distintos conserva el orden de llegada.
    @Test
    void registerTwoDifferentIncidentsKeepsArrivalOrder() {
        // Arrange
        Incident incident1 = new Incident("INC001", IncidentType.ACCIDENT, "Zona Norte", Severity.HIGH);
        Incident incident2 = new Incident("INC002", IncidentType.THEFT, "Zona Sur", Severity.MEDIUM);

        // Act
        manager.registerIncident(incident1);
        manager.registerIncident(incident2);

        // Assert
        assertEquals(2, manager.getIncidentCount());
        assertEquals(incident1, manager.getIncidentAt(0));
        assertEquals(incident2, manager.getIncidentAt(1));
    }

    // Caso 3: Registrar un incidente con un ID ya existente lanza una excepción.
    @Test
    void registerIncidentWithExistingIdThrowsException() {
        // Arrange
        Incident incident1 = new Incident("INC001", IncidentType.ACCIDENT, "Zona Norte", Severity.HIGH);
        Incident incident1Duplicado = new Incident("INC001", IncidentType.THEFT, "Zona Sur", Severity.LOW);
        manager.registerIncident(incident1);

        // Act & Assert
        assertThrows(DuplicateIncidentException.class,
                () -> manager.registerIncident(incident1Duplicado));
        assertEquals(1, manager.getIncidentCount());
    }

    // Caso 4: Registrar un incidente nulo lanza una excepción.
    @Test
    void registerNullIncidentThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class,
                () -> manager.registerIncident(null));
        assertEquals(0, manager.getIncidentCount());
    }

    // RF5
    // Caso 1: Con un único incidente registrado, se retorna ese mismo incidente.
    @Test
    void getHighestPriorityIncidentWithSingleIncidentReturnsIt() {
        // Arrange
        Incident incident1 = new Incident("INC001", IncidentType.ACCIDENT, "Zona Norte", Severity.HIGH);
        manager.registerIncident(incident1);

        // Act
        Incident result = manager.getHighestPriorityIncident();

        // Assert
        assertEquals(incident1, result);
    }

    // Caso 2: Con incidentes de distinta gravedad, se retorna el de mayor gravedad.
    @Test
    void getHighestPriorityIncidentReturnsMostSevereOne() {
        // Arrange
        Incident incident1 = new Incident("INC001", IncidentType.THEFT, "Zona Norte", Severity.LOW);
        Incident incident2 = new Incident("INC002", IncidentType.ACCIDENT, "Zona Sur", Severity.HIGH);
        Incident incident3 = new Incident("INC003", IncidentType.FIRE, "Zona Este", Severity.MEDIUM);
        manager.registerIncident(incident1);
        manager.registerIncident(incident2);
        manager.registerIncident(incident3);

        // Act
        Incident result = manager.getHighestPriorityIncident();

        // Assert
        assertEquals(incident2, result);
    }

    // Caso 3: Con dos incidentes de la misma gravedad, se retorna el más antiguo (registrado primero).
    @Test
    void getHighestPriorityIncidentBreaksTiesByOldestFirst() {
        // Arrange
        Incident incident1 = new Incident("INC001", IncidentType.ACCIDENT, "Zona Norte", Severity.HIGH);
        Incident incident2 = new Incident("INC002", IncidentType.ACCIDENT, "Zona Sur", Severity.HIGH);
        manager.registerIncident(incident1);
        manager.registerIncident(incident2);

        // Act
        Incident result = manager.getHighestPriorityIncident();

        // Assert
        assertEquals(incident1, result);
    }

    // Caso 4: Consultar la prioridad sin incidentes registrados lanza una excepción.
    @Test
    void getHighestPriorityIncidentWithNoIncidentsThrowsException() {
        // Act & Assert
        assertThrows(NoActiveIncidentsException.class,
                () -> manager.getHighestPriorityIncident());
    }

    // RF7
    // Caso 1: Asignar un vehículo compatible y disponible a un incidente pendiente lo asigna correctamente.
    @Test
    void assignCompatibleAndAvailableVehicleToPendingIncidentAssignsIt() {
        // Arrange
        Incident incident1 = new Incident("INC001", IncidentType.THEFT, "Zona Norte", Severity.MEDIUM);
        Vehicle patrol1 = new Vehicle("VEH001", VehicleType.PATROL);

        // Act
        manager.assignVehicle(patrol1, incident1);

        // Assert
        assertEquals(IncidentStatus.IN_PROGRESS, incident1.getStatus());
        assertEquals(VehicleStatus.EN_ROUTE, patrol1.getStatus());
        assertEquals(patrol1, incident1.getAssignedVehicle());
    }

    // Caso 2: Asignar un vehículo incompatible con el tipo de incidente lanza una excepción.
    @Test
    void assignIncompatibleVehicleThrowsException() {
        // Arrange
        Incident incident1 = new Incident("INC001", IncidentType.FIRE, "Zona Norte", Severity.HIGH);
        Vehicle ambulance1 = new Vehicle("VEH001", VehicleType.AMBULANCE);

        // Act & Assert
        assertThrows(VehicleAssignmentException.class,
                () -> manager.assignVehicle(ambulance1, incident1));
        assertEquals(IncidentStatus.PENDING, incident1.getStatus());
    }

    // Caso 3: Asignar un vehículo no disponible lanza una excepción.
    @Test
    void assignUnavailableVehicleThrowsException() {
        // Arrange
        Incident incident1 = new Incident("INC001", IncidentType.ACCIDENT, "Zona Norte", Severity.HIGH);
        Vehicle ambulance1 = new Vehicle("VEH001", VehicleType.AMBULANCE);
        ambulance1.setStatus(VehicleStatus.OUT_OF_SERVICE);

        // Act & Assert
        assertThrows(VehicleAssignmentException.class,
                () -> manager.assignVehicle(ambulance1, incident1));
        assertEquals(IncidentStatus.PENDING, incident1.getStatus());
    }

    // Caso 4: Asignar un vehículo a un incidente que ya no está pendiente lanza una excepción.
    @Test
    void assignVehicleToNonPendingIncidentThrowsException() {
        // Arrange
        Incident incident1 = new Incident("INC001", IncidentType.THEFT, "Zona Norte", Severity.MEDIUM);
        incident1.setStatus(IncidentStatus.IN_PROGRESS);
        Vehicle patrol1 = new Vehicle("VEH001", VehicleType.PATROL);

        // Act & Assert
        assertThrows(VehicleAssignmentException.class,
                () -> manager.assignVehicle(patrol1, incident1));
        assertEquals(VehicleStatus.AVAILABLE, patrol1.getStatus());
    }
    //RF8
    // Caso 1: Finalizar la atención de un incidente "En proceso" lo marca como resuelto y libera el vehículo asignado.
    @Test
    void finishAttentionOfInProgressIncidentResolvesItAndFreesVehicle() {
        // Arrange
        Incident incident1 = new Incident("INC001", IncidentType.THEFT, "Zona Norte", Severity.MEDIUM);
        Vehicle patrol1 = new Vehicle("VEH001", VehicleType.PATROL);
        manager.assignVehicle(patrol1, incident1);

        // Act
        manager.finishAttention(incident1);

        // Assert
        assertEquals(IncidentStatus.RESOLVED, incident1.getStatus());
        assertEquals(VehicleStatus.AVAILABLE, patrol1.getStatus());
    }

    // Caso 2: Finalizar un incidente que aún no ha sido atendido ("Pendiente") lanza una excepción.
    @Test
    void finishAttentionOfPendingIncidentThrowsException() {
        // Arrange
        Incident incident1 = new Incident("INC001", IncidentType.THEFT, "Zona Norte", Severity.MEDIUM);

        // Act & Assert
        assertThrows(IncidentStateException.class,
                () -> manager.finishAttention(incident1));
        assertEquals(IncidentStatus.PENDING, incident1.getStatus());
    }

    // Caso 3: Finalizar un incidente que ya se encuentra "Resuelto" lanza una excepción y no vuelve a modificar el vehículo.
    @Test
    void finishAttentionOfResolvedIncidentThrowsExceptionAndDoesNotChangeVehicle() {
        // Arrange
        Incident incident1 = new Incident("INC001", IncidentType.ACCIDENT, "Zona Norte", Severity.HIGH);
        Vehicle ambulance1 = new Vehicle("VEH001", VehicleType.AMBULANCE);
        manager.assignVehicle(ambulance1, incident1);
        manager.finishAttention(incident1);

        // Act & Assert
        assertThrows(IncidentStateException.class,
                () -> manager.finishAttention(incident1));
        assertEquals(VehicleStatus.AVAILABLE, ambulance1.getStatus());
    }

    // RF6
    // Caso 1: Liberar un vehículo que atiende un incidente "En proceso" lo deja disponible y el incidente vuelve a quedar pendiente.
    @Test
    void releaseVehicleOfInProgressIncidentMakesItAvailableAndIncidentPending() {
        // Arrange
        Incident incident1 = new Incident("INC001", IncidentType.THEFT, "Zona Norte", Severity.MEDIUM);
        Vehicle patrol1 = new Vehicle("VEH001", VehicleType.PATROL);
        manager.registerIncident(incident1);
        manager.assignVehicle(patrol1, incident1);

        // Act
        manager.releaseVehicle(patrol1);

        // Assert
        assertEquals(VehicleStatus.AVAILABLE, patrol1.getStatus());
        assertEquals(IncidentStatus.PENDING, incident1.getStatus());
        assertNull(incident1.getAssignedVehicle());
    }

    // Caso 2: Después de liberar un vehículo, el incidente puede recibir otro vehículo compatible.
    @Test
    void releasedIncidentCanReceiveAnotherVehicle() {
        // Arrange
        Incident incident1 = new Incident("INC001", IncidentType.ACCIDENT, "Zona Norte", Severity.HIGH);
        Vehicle patrol1 = new Vehicle("VEH001", VehicleType.PATROL);
        Vehicle ambulance1 = new Vehicle("VEH002", VehicleType.AMBULANCE);
        manager.registerIncident(incident1);
        manager.assignVehicle(patrol1, incident1);
        manager.releaseVehicle(patrol1);

        // Act
        manager.assignVehicle(ambulance1, incident1);

        // Assert
        assertEquals(IncidentStatus.IN_PROGRESS, incident1.getStatus());
        assertEquals(ambulance1, incident1.getAssignedVehicle());
        assertEquals(VehicleStatus.AVAILABLE, patrol1.getStatus());
    }

    // Caso 3: Liberar un vehículo que ya está "Disponible" lanza una excepción.
    @Test
    void releaseAvailableVehicleThrowsException() {
        // Arrange
        Vehicle ambulance1 = new Vehicle("VEH001", VehicleType.AMBULANCE);

        // Act & Assert
        assertThrows(VehicleReleaseException.class,
                () -> manager.releaseVehicle(ambulance1));
        assertEquals(VehicleStatus.AVAILABLE, ambulance1.getStatus());
    }

    // Caso 4: Liberar un vehículo que no está atendiendo ningún incidente lanza una excepción y no cambia su estado.
    @Test
    void releaseVehicleWithoutIncidentThrowsExceptionAndKeepsStatus() {
        // Arrange
        Vehicle fireTruck1 = new Vehicle("VEH001", VehicleType.FIRE_TRUCK);
        fireTruck1.setStatus(VehicleStatus.OUT_OF_SERVICE);

        // Act & Assert
        assertThrows(VehicleReleaseException.class,
                () -> manager.releaseVehicle(fireTruck1));
        assertEquals(VehicleStatus.OUT_OF_SERVICE, fireTruck1.getStatus());
    }

    // RF5 - ordenamiento
    // Caso 1: Los incidentes se retornan ordenados de mayor a menor gravedad.
    @Test
    void getIncidentsSortedByPriorityOrdersFromHighToLow() {
        // Arrange
        Incident incident1 = new Incident("INC001", IncidentType.THEFT, "Zona Norte", Severity.LOW);
        Incident incident2 = new Incident("INC002", IncidentType.ACCIDENT, "Zona Sur", Severity.HIGH);
        Incident incident3 = new Incident("INC003", IncidentType.FIRE, "Zona Este", Severity.MEDIUM);
        manager.registerIncident(incident1);
        manager.registerIncident(incident2);
        manager.registerIncident(incident3);

        // Act
        LinkedList<Incident> sorted = manager.getIncidentsSortedByPriority();

        // Assert
        assertEquals(incident2, sorted.get(0));
        assertEquals(incident3, sorted.get(1));
        assertEquals(incident1, sorted.get(2));
    }

    // Caso 2: Con la misma gravedad, el más antiguo queda primero y el orden original no cambia.
    @Test
    void getIncidentsSortedByPriorityBreaksTiesAndKeepsOriginalOrder() {
        // Arrange
        Incident incident1 = new Incident("INC001", IncidentType.THEFT, "Zona Norte", Severity.MEDIUM);
        Incident incident2 = new Incident("INC002", IncidentType.ACCIDENT, "Zona Sur", Severity.HIGH);
        Incident incident3 = new Incident("INC003", IncidentType.FIRE, "Zona Este", Severity.HIGH);
        manager.registerIncident(incident1);
        manager.registerIncident(incident2);
        manager.registerIncident(incident3);

        // Act
        LinkedList<Incident> sorted = manager.getIncidentsSortedByPriority();

        // Assert
        assertEquals(incident2, sorted.get(0));
        assertEquals(incident3, sorted.get(1));
        assertEquals(incident1, manager.getIncidentAt(0));
    }

    // RF3 - búsqueda binaria
    // Caso 3: Buscar por ID un incidente registrado lo retorna.
    @Test
    void findIncidentByIdReturnsTheRightIncident() {
        // Arrange
        Incident incident1 = new Incident("INC003", IncidentType.THEFT, "Zona Norte", Severity.LOW);
        Incident incident2 = new Incident("INC001", IncidentType.ACCIDENT, "Zona Sur", Severity.HIGH);
        Incident incident3 = new Incident("INC002", IncidentType.FIRE, "Zona Este", Severity.MEDIUM);
        manager.registerIncident(incident1);
        manager.registerIncident(incident2);
        manager.registerIncident(incident3);

        // Act
        Incident result = manager.findIncidentById("INC002");

        // Assert
        assertEquals(incident3, result);
    }

    // Caso 4: Buscar por ID un incidente que no existe lanza una excepción.
    @Test
    void findIncidentByIdThatDoesNotExistThrowsException() {
        // Arrange
        Incident incident1 = new Incident("INC001", IncidentType.THEFT, "Zona Norte", Severity.LOW);
        manager.registerIncident(incident1);

        // Act & Assert
        assertThrows(IncidentNotFoundException.class,
                () -> manager.findIncidentById("INC999"));
    }

    // RF14 - Sistema de puntuacion del operador
    // Caso 1: calculateScore otorga el puntaje base segun la gravedad (100 alta, 70 media, 40 baja).
    @Test
    void calculateScoreReturnsBasePointsAccordingToSeverity() {
        LocalDateTime base = LocalDateTime.of(2026, 10, 9, 10, 0, 0);
        LocalDateTime resolvedLate = base.plusMinutes(20);

        Incident high = new Incident("I-H", IncidentType.ACCIDENT, "Centro", Severity.HIGH, base);
        Incident med = new Incident("I-M", IncidentType.THEFT, "Norte", Severity.MEDIUM, base);
        Incident low = new Incident("I-L", IncidentType.FIRE, "Sur", Severity.LOW, base);

        assertEquals(100, manager.calculateScore(high, resolvedLate));
        assertEquals(70, manager.calculateScore(med, resolvedLate));
        assertEquals(40, manager.calculateScore(low, resolvedLate));
    }

    // Caso 2: calculateScore suma bonificacion de 20 puntos si se atiende dentro del tiempo limite.
    @Test
    void calculateScoreIncludesBonusWhenWithinTimeLimit() {
        LocalDateTime base = LocalDateTime.of(2026, 10, 9, 10, 0, 0);
        LocalDateTime resolvedOnTime = base.plusMinutes(2); // dentro de los 5 min de HIGH

        Incident high = new Incident("I-H", IncidentType.ACCIDENT, "Centro", Severity.HIGH, base);

        assertEquals(120, manager.calculateScore(high, resolvedOnTime));
    }

    // Caso 3: calculateScore no suma bonificacion si se supera el tiempo limite.
    @Test
    void calculateScoreExcludesBonusWhenExceedingTimeLimit() {
        LocalDateTime base = LocalDateTime.of(2026, 10, 9, 10, 0, 0);
        LocalDateTime resolvedLate = base.plusMinutes(6); // supera los 5 min de HIGH

        Incident high = new Incident("I-H", IncidentType.ACCIDENT, "Centro", Severity.HIGH, base);

        assertEquals(100, manager.calculateScore(high, resolvedLate));
    }

    // Caso 4: finishAttention con Operador calcula puntos y actualiza el puntaje del operador.
    @Test
    void finishAttentionWithOperatorUpdatesScore() {
        LocalDateTime base = LocalDateTime.of(2026, 10, 9, 10, 0, 0);
        LocalDateTime resolved = base.plusMinutes(2);

        Incident incident = new Incident("I-01", IncidentType.THEFT, "Centro", Severity.MEDIUM, base);
        Vehicle vehicle = new Vehicle("V-01", VehicleType.PATROL);
        Operator operator = new Operator(0, 0);

        manager.registerIncident(incident);
        manager.assignVehicle(vehicle, incident);

        int points = manager.finishAttention(incident, operator, resolved);

        assertEquals(90, points); // 70 base + 20 bonificacion
        assertEquals(90, operator.getScore());
        assertEquals(IncidentStatus.RESOLVED, incident.getStatus());
        assertEquals(VehicleStatus.AVAILABLE, vehicle.getStatus());
        assertEquals(resolved, incident.getResolvedAt());
    }

    // Caso 5: finishAttention con Operador acumula el puntaje tras resolver multiples incidentes.
    @Test
    void finishAttentionAccumulatesScoreForMultipleIncidents() {
        LocalDateTime base = LocalDateTime.of(2026, 10, 9, 10, 0, 0);
        Operator operator = new Operator(0, 0);

        Incident incident1 = new Incident("I-01", IncidentType.THEFT, "Centro", Severity.HIGH, base);
        Vehicle vehicle1 = new Vehicle("V-01", VehicleType.PATROL);
        manager.registerIncident(incident1);
        manager.assignVehicle(vehicle1, incident1);
        manager.finishAttention(incident1, operator, base.plusMinutes(2)); // 100 + 20 = 120

        Incident incident2 = new Incident("I-02", IncidentType.ACCIDENT, "Sur", Severity.LOW, base);
        Vehicle vehicle2 = new Vehicle("V-02", VehicleType.AMBULANCE);
        manager.registerIncident(incident2);
        manager.assignVehicle(vehicle2, incident2);
        manager.finishAttention(incident2, operator, base.plusMinutes(20)); // 40 base (sin bono)

        assertEquals(160, operator.getScore());
    }

    // RF13 - Indicadores en tiempo real
    // Caso 1: getActiveIncidentCount cuenta solo incidentes en estado PENDING o IN_PROGRESS, ignorando los RESOLVED.
    @Test
    void getActiveIncidentCountCountsOnlyActiveOnes() {
        Incident inc1 = new Incident("I-01", IncidentType.ACCIDENT, "Centro", Severity.HIGH);
        Incident inc2 = new Incident("I-02", IncidentType.THEFT, "Norte", Severity.MEDIUM);
        Incident inc3 = new Incident("I-03", IncidentType.FIRE, "Sur", Severity.LOW);
        Vehicle patrol = new Vehicle("P-01", VehicleType.PATROL);
        Vehicle fireTruck = new Vehicle("F-01", VehicleType.FIRE_TRUCK);

        manager.registerIncident(inc1);
        manager.registerIncident(inc2);
        manager.registerIncident(inc3);

        manager.assignVehicle(patrol, inc2); // inc2 queda IN_PROGRESS
        manager.assignVehicle(fireTruck, inc3);
        manager.finishAttention(inc3); // inc3 queda RESOLVED

        assertEquals(2, manager.getActiveIncidentCount());
    }

    // Caso 2: Los contadores por tipo separan correctamente accidentes, robos e incendios activos.
    @Test
    void getActiveCountsByTypeClassifyAccidentsTheftsAndFires() {
        manager.registerIncident(new Incident("A-01", IncidentType.ACCIDENT, "Centro", Severity.HIGH));
        manager.registerIncident(new Incident("A-02", IncidentType.ACCIDENT, "Norte", Severity.LOW));
        manager.registerIncident(new Incident("T-01", IncidentType.THEFT, "Sur", Severity.MEDIUM));
        Incident resolvedFire = new Incident("F-01", IncidentType.FIRE, "Oeste", Severity.HIGH);
        Incident activeFire = new Incident("F-02", IncidentType.FIRE, "Este", Severity.MEDIUM);
        manager.registerIncident(resolvedFire);
        manager.registerIncident(activeFire);

        Vehicle ft = new Vehicle("FT-1", VehicleType.FIRE_TRUCK);
        manager.assignVehicle(ft, resolvedFire);
        manager.finishAttention(resolvedFire); // F-01 resuelto, no debe contarse como activo

        assertEquals(2, manager.getActiveAccidentCount());
        assertEquals(1, manager.getActiveTheftCount());
        assertEquals(1, manager.getActiveFireCount());
    }

    // Caso 3: countAvailableVehicles cuenta unicamente los vehiculos en estado AVAILABLE.
    @Test
    void countAvailableVehiclesCountsOnlyAvailableOnes() {
        LinkedList<Vehicle> fleet = new LinkedList<>();
        Vehicle v1 = new Vehicle("P-01", VehicleType.PATROL);
        Vehicle v2 = new Vehicle("A-01", VehicleType.AMBULANCE);
        Vehicle v3 = new Vehicle("F-01", VehicleType.FIRE_TRUCK);
        v2.setStatus(VehicleStatus.EN_ROUTE);

        fleet.addLast(v1);
        fleet.addLast(v2);
        fleet.addLast(v3);

        assertEquals(2, manager.countAvailableVehicles(fleet));
    }
}
