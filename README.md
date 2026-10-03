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

Iteración 2: 6b4425e (pruebas unitarias de registerIncident en fase Red)
Densidad de errores-fallos = 1.0 (4/4)
Confiabilidad = 0.0
Completitud = 0.67 (4/6)
<img width="1113" height="311" alt="image" src="https://github.com/user-attachments/assets/1f9ca79d-7893-4c20-bfb2-851475020be5" />

Iteración 3: 8b7a3ee (registerIncident implementado, pruebas en fase Green)
Densidad de errores-fallos = 0.0 (0/4)
Confiabilidad = 1.0
Completitud = 0.67 (4/6)
<img width="1140" height="325" alt="image" src="https://github.com/user-attachments/assets/f0a3a754-a3c3-4348-8caf-999bf571feda" />

Iteración 4: 55f3074 (pruebas de assignVehicle en fase Red)
Densidad de errores-fallos = 0.33 (4/12)
Confiabilidad = 0.67
Completitud = 2.0 (12/6)
<img width="1237" height="308" alt="image" src="https://github.com/user-attachments/assets/c58db2ab-d75e-42b6-bf05-6e386e22d4ec" />


Iteración 5: 9ae2a55 (assignVehicle implementado, pruebas en fase Green)
Densidad de errores-fallos = 0.0 (0/12)
Confiabilidad = 1.0
Completitud = 2.0 (12/6)
<img width="995" height="340" alt="image" src="https://github.com/user-attachments/assets/654c0972-56c1-4017-8590-34e183eb1642" />

