# Diseño de pruebas unitarias

**Requerimiento funcional / Historia de usuario:** RF-5 - Gestionar la prioridad de los incidentes: organizar los incidentes activos según su gravedad y aplicar un criterio de desempate cuando tengan la misma prioridad. El sistema deberá permitir consultar y atender el incidente de mayor prioridad.

**Nombre del método:** getHighestPriorityIncident(): Incident

**Objetivo de la Prueba:** Verificar que el sistema retorne el incidente de mayor prioridad según gravedad (Alta > Media > Baja), aplicando como criterio de desempate la fecha de generación (más antiguo primero), y que se lance una excepción cuando no hay incidentes registrados.

| Arrange (Inicialización) | Act (Acción) | Assert (Verificar) |
|---|---|---|
| **Caso 1: Con un único incidente registrado, se retorna ese mismo incidente.** | | |
| Crear un IncidentManager vacío y registrar incident1 con gravedad HIGH. | Ejecutar manager.getHighestPriorityIncident() | assertEquals(incident1, resultado); |
| **Caso 2: Con incidentes de distinta gravedad, se retorna el de mayor gravedad.** | | |
| Registrar incident1 (gravedad LOW), incident2 (gravedad HIGH) e incident3 (gravedad MEDIUM), en ese orden. | Ejecutar manager.getHighestPriorityIncident() | assertEquals(incident2, resultado); |
| **Caso 3: Con dos incidentes de la misma gravedad, se retorna el más antiguo (registrado primero).** | | |
| Registrar incident1 (gravedad HIGH) y luego incident2 (gravedad HIGH). | Ejecutar manager.getHighestPriorityIncident() | assertEquals(incident1, resultado); |
| **Caso 4: Consultar la prioridad sin incidentes registrados lanza una excepción.** | | |
| Crear un IncidentManager vacío. | Ejecutar manager.getHighestPriorityIncident() | assertThrows(NoActiveIncidentsException.class, () -> manager.getHighestPriorityIncident()); |