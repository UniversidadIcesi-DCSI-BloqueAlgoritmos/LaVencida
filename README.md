## Indicadores de calidad

Esta sección reporta el avance del proyecto a través de commits representativos, siguiendo el ciclo TDD (Red → Green). Las métricas se calculan sobre el estado de las pruebas unitarias en el momento de cada commit, considerando como alcance inicial los 6 métodos asignados a Daniel para la Entrega 2 de APO2.

Fórmulas:
- Densidad de errores-fallos = total de fallos / total de pruebas
- Confiabilidad = 1 - densidad de fallos
- Completitud = casos de prueba / total funcionalidades (6)

Iteración 1: 4ed0164 (esqueleto de registerIncident — aún sin pruebas)
Densidad de errores-fallos = N/A (0 pruebas aún)
Confiabilidad = N/A
Completitud = 0.0 (0/6)

<img width="1113" height="311" alt="image" src="https://github.com/user-attachments/assets/ba653f4b-da37-4600-af33-6e2c22bfb80a" />

Iteración 2: 6b4425e (pruebas unitarias de registerIncident en fase Red)
Densidad de errores-fallos = 1.0 (4/4)
Confiabilidad = 0.0
Completitud = 0.67 (4/6)

<img width="1140" height="325" alt="image" src="https://github.com/user-attachments/assets/1d2fd3a6-68f0-411c-b862-c268b35e559a" />
Iteración 3: 8b7a3ee (registerIncident implementado, pruebas en fase Green)
Densidad de errores-fallos = 0.0 (0/4)
Confiabilidad = 1.0
Completitud = 0.67 (4/6)
