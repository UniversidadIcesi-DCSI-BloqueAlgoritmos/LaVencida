# Diseño de pruebas unitarias

**Requerimiento funcional / Historia de usuario:** RF-21 - Historial de movimientos del operador (Stack): el sistema debe registrar cada movimiento válido del operador (W, A, S, D o teclas de flecha) en una pila propia Stack<Movement>. El operador podrá deshacer su último movimiento, lo que desapila el movimiento y restaura la posición anterior. Deshacer sobre historial vacío debe lanzar EmptyStructureException.

**Nombre del método:** undoLastMovement(): Movement

**Objetivo de la Prueba:** Verificar que deshacer el último movimiento del operador restaura la posición que tenía antes de ese movimiento, respetando el orden LIFO de la pila (se deshace primero el movimiento más reciente), y que deshacer con el historial vacío lanza EmptyStructureException sin cambiar la posición del operador.

| Arrange (Inicialización) | Act (Acción) | Assert (Verificar) |
|---|---|---|
| **Caso 1: Deshacer después de un movimiento devuelve al operador a su posición anterior.** | | |
| Crear operator1 en la posición (5, 5). Mover operator1 hacia arriba (UP), queda en (4, 5). | operator1.undoLastMovement(); | assertEquals(5, operator1.getRow()); assertEquals(5, operator1.getColumn()); assertEquals(0, operator1.getMovementCount()); |
| **Caso 2: Con varios movimientos, deshacer revierte solo el último (orden LIFO).** | | |
| Crear operator1 en (5, 5). Mover RIGHT (queda en (5, 6)) y luego DOWN (queda en (6, 6)). | Movement undone = operator1.undoLastMovement(); | assertEquals(Direction.DOWN, undone.getDirection()); assertEquals(5, operator1.getRow()); assertEquals(6, operator1.getColumn()); assertEquals(1, operator1.getMovementCount()); |
| **Caso 3: Deshacer todos los movimientos devuelve al operador a la posición inicial.** | | |
| Crear operator1 en (2, 3). Mover LEFT, UP y LEFT. | operator1.undoLastMovement(); operator1.undoLastMovement(); operator1.undoLastMovement(); | assertEquals(2, operator1.getRow()); assertEquals(3, operator1.getColumn()); assertEquals(0, operator1.getMovementCount()); |
| **Caso 4: Deshacer con el historial vacío lanza una excepción y no cambia la posición.** | | |
| Crear operator1 en (5, 5), sin movimientos. | operator1.undoLastMovement(); | assertThrows(EmptyStructureException.class, () -> operator1.undoLastMovement()); assertEquals(5, operator1.getRow()); assertEquals(5, operator1.getColumn()); |
