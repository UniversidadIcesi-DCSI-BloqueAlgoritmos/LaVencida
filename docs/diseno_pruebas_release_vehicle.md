# Diseño de pruebas unitarias

**Requerimiento funcional / Historia de usuario:** RF-8 - Gestionar la atención de incidentes: permitir iniciar y finalizar la atención de un incidente, actualizar su estado y liberar el vehículo correspondiente cuando finalice la atención.

**Nombre del método:** releaseVehicle(Vehicle vehicle): void

**Objetivo de la Prueba:** Verificar que un vehículo que está en ruta o atendiendo un incidente queda "Disponible" al liberarlo, y que liberar un vehículo que ya está disponible o que está fuera de servicio es rechazado mediante la excepción de dominio correspondiente, sin alterar su estado.

| Arrange (Inicialización) | Act (Acción) | Assert (Verificar) |
|---|---|---|
| **Caso 1: Liberar un vehículo "En ruta" lo deja disponible.** | | |
| Crear patrol1 (tipo PATROL) y asignarle el estado EN_ROUTE. | manager.releaseVehicle(patrol1); | assertEquals(VehicleStatus.AVAILABLE, patrol1.getStatus()); |
| **Caso 2: Liberar un vehículo "Atendiendo" lo deja disponible.** | | |
| Crear ambulance1 (tipo AMBULANCE) y asignarle el estado ATTENDING. | manager.releaseVehicle(ambulance1); | assertEquals(VehicleStatus.AVAILABLE, ambulance1.getStatus()); |
| **Caso 3: Liberar un vehículo que ya está disponible lanza una excepción.** | | |
| Crear patrol1 (tipo PATROL, estado AVAILABLE por defecto). | manager.releaseVehicle(patrol1); | assertThrows(VehicleStateException.class, () -> manager.releaseVehicle(patrol1)); assertEquals(VehicleStatus.AVAILABLE, patrol1.getStatus()); |
| **Caso 4: Liberar un vehículo fuera de servicio lanza una excepción y no cambia su estado.** | | |
| Crear fireTruck1 (tipo FIRE_TRUCK) y asignarle el estado OUT_OF_SERVICE. | manager.releaseVehicle(fireTruck1); | assertThrows(VehicleStateException.class, () -> manager.releaseVehicle(fireTruck1)); assertEquals(VehicleStatus.OUT_OF_SERVICE, fireTruck1.getStatus()); |