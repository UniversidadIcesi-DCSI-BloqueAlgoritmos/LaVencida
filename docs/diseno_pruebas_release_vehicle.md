# Diseño de pruebas unitarias

**Requerimiento funcional / Historia de usuario:** RF-6 - Gestionar vehículos de atención: registrar y consultar los vehículos, su tipo, ubicación y estado, así como controlar los cambios de estado producidos durante la atención de un incidente.

**Nombre del método:** releaseVehicle(Vehicle vehicle): void

**Objetivo de la Prueba:** Verificar que liberar un vehículo que está atendiendo un incidente "En proceso" lo deja "Disponible" y devuelve el incidente a "Pendiente" sin vehículo asignado (para que se le pueda asignar otro), y que intentar liberar un vehículo que ya está disponible o que no está atendiendo ningún incidente es rechazado mediante la excepción de dominio correspondiente, sin alterar su estado.

| Arrange (Inicialización) | Act (Acción) | Assert (Verificar) |
|---|---|---|
| **Caso 1: Liberar un vehículo que atiende un incidente "En proceso" lo deja disponible y el incidente vuelve a quedar pendiente.** | | |
| Crear incident1 (tipo THEFT, estado PENDING) y registrarlo. Crear patrol1 (tipo PATROL) y asignarlo a incident1 (incident1 queda IN_PROGRESS y patrol1 EN_ROUTE). | manager.releaseVehicle(patrol1); | assertEquals(VehicleStatus.AVAILABLE, patrol1.getStatus()); assertEquals(IncidentStatus.PENDING, incident1.getStatus()); assertNull(incident1.getAssignedVehicle()); |
| **Caso 2: Después de liberar un vehículo, el incidente puede recibir otro vehículo compatible.** | | |
| Crear incident1 (tipo ACCIDENT, estado PENDING) y registrarlo. Crear patrol1 (tipo PATROL) y ambulance1 (tipo AMBULANCE). Asignar patrol1 a incident1 y luego liberar patrol1. | manager.assignVehicle(ambulance1, incident1); | assertEquals(IncidentStatus.IN_PROGRESS, incident1.getStatus()); assertEquals(ambulance1, incident1.getAssignedVehicle()); assertEquals(VehicleStatus.AVAILABLE, patrol1.getStatus()); |
| **Caso 3: Liberar un vehículo que ya está "Disponible" lanza una excepción.** | | |
| Crear ambulance1 (tipo AMBULANCE, estado AVAILABLE), sin incidente asignado. | manager.releaseVehicle(ambulance1); | assertThrows(VehicleReleaseException.class, () -> manager.releaseVehicle(ambulance1)); assertEquals(VehicleStatus.AVAILABLE, ambulance1.getStatus()); |
| **Caso 4: Liberar un vehículo que no está atendiendo ningún incidente lanza una excepción y no cambia su estado.** | | |
| Crear fireTruck1 (tipo FIRE_TRUCK, estado OUT_OF_SERVICE), sin incidente asignado. | manager.releaseVehicle(fireTruck1); | assertThrows(VehicleReleaseException.class, () -> manager.releaseVehicle(fireTruck1)); assertEquals(VehicleStatus.OUT_OF_SERVICE, fireTruck1.getStatus()); |
