# Diseño de pruebas unitarias

**Requerimiento funcional / Historia de usuario:** RF-8 - Gestionar la atención de incidentes: permitir iniciar y finalizar la atención de un incidente, actualizar su estado y liberar el vehículo correspondiente cuando finalice la atención.

**Nombre del método:** finishAttention(Incident incident): void

**Objetivo de la Prueba:** Verificar que finalizar la atención de un incidente "En proceso" lo marca como "Resuelto" y libera el vehículo asignado (queda "Disponible"), y que intentar finalizar un incidente que no está "En proceso" es rechazado mediante la excepción de dominio correspondiente, sin alterar el estado del vehículo.

| Arrange (Inicialización) | Act (Acción) | Assert (Verificar) |
|---|---|---|
| **Caso 1: Finalizar la atención de un incidente "En proceso" lo marca como resuelto y libera el vehículo asignado.** | | |
| Crear patrol1 (estado EN_ROUTE) e incident1 (tipo THEFT, estado IN_PROGRESS) con patrol1 como vehículo asignado. | manager.finishAttention(incident1); | assertEquals(IncidentStatus.RESOLVED, incident1.getStatus()); assertEquals(VehicleStatus.AVAILABLE, patrol1.getStatus()); |
| **Caso 2: Finalizar un incidente que aún no ha sido atendido ("Pendiente") lanza una excepción.** | | |
| Crear incident1 (tipo THEFT, estado PENDING), sin vehículo asignado. | manager.finishAttention(incident1); | assertThrows(IncidentStateException.class, () -> manager.finishAttention(incident1)); assertEquals(IncidentStatus.PENDING, incident1.getStatus()); |
| **Caso 3: Finalizar un incidente que ya se encuentra "Resuelto" lanza una excepción y no vuelve a modificar el vehículo.** | | |
| Crear ambulance1 (estado AVAILABLE) e incident1 (tipo ACCIDENT, estado RESOLVED), con el vehículo ya liberado previamente. | manager.finishAttention(incident1); | assertThrows(IncidentStateException.class, () -> manager.finishAttention(incident1)); assertEquals(VehicleStatus.AVAILABLE, ambulance1.getStatus()); |