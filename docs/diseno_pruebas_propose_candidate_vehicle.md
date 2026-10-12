# Diseño de pruebas unitarias

**Requerimiento funcional / Historia de usuario:** RF-7 - Asignar vehículos a incidentes: validar que exista al menos un vehículo disponible del tipo requerido y proponer un vehículo candidato adecuado antes de realizar la asignación.

**Nombre del método:** proposeCandidateVehicle(Incident incident, LinkedList<Vehicle> vehicles): Vehicle

**Objetivo de la Prueba:** Verificar que el sistema proponga un vehículo disponible y compatible con el tipo de incidente (priorizando ambulancia sobre patrulla en caso de accidente de tránsito), y que lance VehicleAssignmentException cuando no exista ningún vehículo compatible disponible en la flota.

| Arrange (Inicialización) | Act (Acción) | Assert (Verificar) |
|---|---|---|
| **Caso 1: Propone el vehículo compatible disponible para un incendio o robo.** | | |
| Crear lista fleet con patrol (PATROL, AVAILABLE) y fireTruck (FIRE_TRUCK, AVAILABLE). Crear incidente fire (FIRE) e incidente theft (THEFT). | Vehicle fireCandidate = manager.proposeCandidateVehicle(fire, fleet); Vehicle theftCandidate = manager.proposeCandidateVehicle(theft, fleet); | assertEquals(fireTruck, fireCandidate); assertEquals(patrol, theftCandidate); |
| **Caso 2: Para un accidente, prioriza una ambulancia disponible aunque haya una patrulla antes en la lista.** | | |
| Crear lista fleet agregando primero patrol (PATROL, AVAILABLE) y luego ambulance (AMBULANCE, AVAILABLE). Crear incidente accident (ACCIDENT). | Vehicle candidate = manager.proposeCandidateVehicle(accident, fleet); | assertEquals(ambulance, candidate); |
| **Caso 3: Para un accidente, si no hay ambulancia disponible propone una patrulla disponible como apoyo.** | | |
| Crear lista fleet con patrol (PATROL, AVAILABLE) y ambulance (AMBULANCE, EN_ROUTE). Crear incidente accident (ACCIDENT). | Vehicle candidate = manager.proposeCandidateVehicle(accident, fleet); | assertEquals(patrol, candidate); |
| **Caso 4: Lanza VehicleAssignmentException si no hay ningún vehículo compatible disponible.** | | |
| Crear lista fleet únicamente con patrol (PATROL, AVAILABLE). Crear incidente fire (FIRE, requiere FIRE_TRUCK). | manager.proposeCandidateVehicle(fire, fleet); | assertThrows(VehicleAssignmentException.class, () -> manager.proposeCandidateVehicle(fire, fleet)); |
