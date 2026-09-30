# Diseño de pruebas unitarias

**Requerimiento funcional / Historia de usuario:** RF-3 - Gestionar incidentes: registrar, consultar y actualizar incidentes de tipo accidente, robo e incendio, incluyendo su identificación, ubicación, gravedad, fecha y hora de generación, descripción, estado y vehículo asignado.

**Nombre del método:** registerIncident(Incident incident): void

**Objetivo de la Prueba:** Verificar que un incidente válido se registre correctamente en el sistema, que dos incidentes con el mismo ID no puedan coexistir, y que no se acepten incidentes nulos.

| Arrange (Inicialización) | Act (Acción) | Assert (Verificar) |
|---|---|---|
| **Caso 1: Registrar un incidente válido lo agrega correctamente al sistema.** | | |
| Crear un IncidentManager vacío y un incidente incident1 con id "INC001", tipo ACCIDENT, ubicación "Zona Norte" y gravedad HIGH. | Ejecutar manager.registerIncident(incident1); | assertEquals(1, manager.getIncidentCount()); assertEquals(incident1, manager.getIncidentAt(0)); |
| **Caso 2: Registrar dos incidentes distintos conserva el orden de llegada.** | | |
| Crear un IncidentManager vacío, registrar incident1 (id "INC001") y luego incident2 (id "INC002", tipo THEFT). | Ejecutar manager.registerIncident(incident1); manager.registerIncident(incident2); | assertEquals(2, manager.getIncidentCount()); assertEquals(incident1, manager.getIncidentAt(0)); assertEquals(incident2, manager.getIncidentAt(1)); |
| **Caso 3: Registrar un incidente con un ID ya existente lanza una excepción.** | | |
| Crear un IncidentManager y registrar incident1 con id "INC001". Crear incident1Duplicado con el mismo id "INC001". | Ejecutar manager.registerIncident(incident1Duplicado) | assertThrows(DuplicateIncidentException.class, () -> manager.registerIncident(incident1Duplicado)); assertEquals(1, manager.getIncidentCount()); |
| **Caso 4: Registrar un incidente nulo lanza una excepción.** | | |
| Crear un IncidentManager vacío. | Ejecutar manager.registerIncident(null) | assertThrows(IllegalArgumentException.class, () -> manager.registerIncident(null)); assertEquals(0, manager.getIncidentCount()); |

