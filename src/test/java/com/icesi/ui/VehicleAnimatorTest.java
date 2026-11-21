package com.icesi.ui;

import com.icesi.controller.GameController;
import com.icesi.model.CityMap;
import com.icesi.model.Incident;
import com.icesi.model.Operator;
import com.icesi.model.Vehicle;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

// Pruebas del viaje de los vehiculos por las calles (cuando llegan y cuando van en camino).
class VehicleAnimatorTest {

    private GameController createController() {
        return new GameController(new CityMap(), new Operator(8, 8),
                GameController.createInitialVehicles(), new Random(5));
    }

    // Caso 1: Al empezar cada vehiculo esta en su estacion y no se esta moviendo.
    @Test
    void vehicleStartsAtItsStation() {
        // Arrange
        GameController controller = createController();
        VehicleAnimator animator = new VehicleAnimator(controller);
        Vehicle vehicle = controller.getVehicles().get(0);

        // Act
        animator.update(0);

        // Assert
        int[] station = controller.getCellOf(vehicle);
        assertArrayEquals(new double[]{station[0], station[1]}, animator.positionOf(vehicle));
        assertFalse(animator.isMoving(vehicle));
    }

    // Caso 2: Recien asignado el vehiculo todavia no ha llegado al incidente.
    @Test
    void justAssignedVehicleHasNotArrived() {
        // Arrange
        GameController controller = createController();
        VehicleAnimator animator = new VehicleAnimator(controller);
        animator.update(0);
        Incident incident = controller.spawnRandomIncident();
        Vehicle vehicle = controller.proposeVehicle(incident);

        // Act
        controller.assignVehicle(vehicle.getId(), incident.getId());
        animator.update(0.01);

        // Assert
        assertFalse(animator.hasArrived(vehicle));
    }

    // Caso 3: Despues de suficiente tiempo el vehiculo llega a la celda del incidente.
    @Test
    void vehicleArrivesAfterEnoughTime() {
        // Arrange
        GameController controller = createController();
        VehicleAnimator animator = new VehicleAnimator(controller);
        animator.update(0);
        Incident incident = controller.spawnRandomIncident();
        Vehicle vehicle = controller.proposeVehicle(incident);
        controller.assignVehicle(vehicle.getId(), incident.getId());
        animator.update(0.01);

        // Act
        animator.update(1000);

        // Assert
        int[] target = controller.getCellOf(incident);
        assertTrue(animator.hasArrived(vehicle));
        assertArrayEquals(new double[]{target[0], target[1]}, animator.positionOf(vehicle));
    }
}
