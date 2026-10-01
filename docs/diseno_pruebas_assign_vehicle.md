# Diseño de pruebas unitarias

**Requerimiento funcional / Historia de usuario:** RF-7 - Asignar vehículos a incidentes: permitir la asignación de vehículos disponibles, validando su compatibilidad con el tipo de incidente y evitando asignaciones no permitidas.

**Nombre del método:** assignVehicle(Vehicle vehicle, Incident incident): void

**Objetivo de la Prueba:** Verificar que un vehículo compatible y disponible se asigne correctamente a un incidente pendiente (actualizando el estado de ambos y enlazándolos), y que la asignación sea rechazada con una excepción cuando el vehículo es incompatible, no está disponible, o el incidente no está pendiente.

| Arrange (Inicialización) | Act (Acción) | Assert (Verificar) |
|---|---|---|
| **Caso 1: Asignar un vehículo compatible y disponible a un incidente pendiente lo asigna correctamente.** | | |
| Crear incident1 (tipo THEFT, estado PENDING) y patrol1 (tipo PATROL, estado AVAILABLE). | manager.assignVehicle(patrol1, incident1); | assertEquals(IncidentStatus.IN_PROGRESS, incident1.getStatus()); assertEquals(VehicleStatus.EN_ROUTE, patrol1.getStatus()); assertEquals(patrol1, incident1.getAssignedVehicle()); |
| **Caso 2: Asignar un vehículo incompatible con el tipo de incidente lanza una excepción.** | | |
| Crear incident1 (tipo FIRE, estado PENDING) y ambulance1 (tipo AMBULANCE, estado AVAILABLE). | manager.assignVehicle(ambulance1, incident1); | assertThrows(VehicleAssignmentException.class, () -> manager.assignVehicle(ambulance1, incident1)); assertEquals(IncidentStatus.PENDING, incident1.getStatus()); |
| **Caso 3: Asignar un vehículo no disponible lanza una excepción.** | | |
| Crear incident1 (tipo ACCIDENT, estado PENDING) y ambulance1 (tipo AMBULANCE, estado OUT_OF_SERVICE). | manager.assignVehicle(ambulance1, incident1); | assertThrows(VehicleAssignmentException.class, () -> manager.assignVehicle(ambulance1, incident1)); assertEquals(IncidentStatus.PENDING, incident1.getStatus()); |
| **Caso 4: Asignar un vehículo a un incidente que ya no está pendiente lanza una excepción.** | | |
| Crear incident1 (tipo THEFT, estado IN_PROGRESS) y patrol1 (tipo PATROL, estado AVAILABLE). | manager.assignVehicle(patrol1, incident1); | assertThrows(VehicleAssignmentException.class, () -> manager.assignVehicle(patrol1, incident1)); assertEquals(VehicleStatus.AVAILABLE, patrol1.getStatus()); |