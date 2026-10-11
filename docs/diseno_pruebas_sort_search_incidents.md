# Diseño de pruebas unitarias

**Requerimiento funcional / Historia de usuario:** RF-3 - Gestionar incidentes (consultar) y RF-5 - Gestionar la prioridad de los incidentes: organizar los incidentes activos según su gravedad y aplicar un criterio de desempate cuando tengan la misma prioridad.

**Nombre de los métodos:** getIncidentsSortedByPriority(): LinkedList<Incident> y findIncidentById(String id): Incident

**Objetivo de la Prueba:** Verificar que el sistema retorna los incidentes ordenados de mayor a menor gravedad (Alta > Media > Baja), desempatando por el más antiguo, sin alterar el orden original de registro; y que la búsqueda binaria por ID retorna el incidente correcto o lanza IncidentNotFoundException si el ID no existe.

| Arrange (Inicialización) | Act (Acción) | Assert (Verificar) |
|---|---|---|
| **Caso 1: Los incidentes se retornan ordenados de mayor a menor gravedad.** | | |
| Registrar incident1 (LOW), incident2 (HIGH) e incident3 (MEDIUM). | LinkedList<Incident> sorted = manager.getIncidentsSortedByPriority(); | assertEquals(incident2, sorted.get(0)); assertEquals(incident3, sorted.get(1)); assertEquals(incident1, sorted.get(2)); |
| **Caso 2: Con la misma gravedad, el más antiguo queda primero y el orden original no cambia.** | | |
| Registrar incident1 (MEDIUM), incident2 (HIGH) e incident3 (HIGH), en ese orden. | LinkedList<Incident> sorted = manager.getIncidentsSortedByPriority(); | assertEquals(incident2, sorted.get(0)); assertEquals(incident3, sorted.get(1)); assertEquals(incident1, manager.getIncidentAt(0)); |
| **Caso 3: Buscar por ID un incidente registrado lo retorna.** | | |
| Registrar incident1 (INC003), incident2 (INC001) e incident3 (INC002). | Incident result = manager.findIncidentById("INC002"); | assertEquals(incident3, result); |
| **Caso 4: Buscar por ID un incidente que no existe lanza una excepción.** | | |
| Registrar incident1 (INC001). | manager.findIncidentById("INC999"); | assertThrows(IncidentNotFoundException.class, () -> manager.findIncidentById("INC999")); |
