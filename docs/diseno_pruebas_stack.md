# Diseño de pruebas unitarias

**Estructura de datos:** Stack<T> (pila propia implementada sobre la LinkedList<T> del equipo).

**Operaciones probadas:** push(T), pop(): T, peek(): T, isEmpty(): boolean, size(): int

**Objetivo de la Prueba:** Verificar que la pila guarda los elementos en orden LIFO (el último en entrar es el primero en salir), que peek consulta el tope sin sacarlo, que el tamaño se actualiza con cada operación y que pop y peek sobre una pila vacía lanzan EmptyStructureException.

| Arrange (Inicialización) | Act (Acción) | Assert (Verificar) |
|---|---|---|
| **Caso 1: Apilar elementos deja en el tope el último elemento apilado.** | | |
| Crear una pila vacía stack. | stack.push("A"); stack.push("B"); | assertEquals("B", stack.peek()); assertEquals(2, stack.size()); assertFalse(stack.isEmpty()); |
| **Caso 2: Desapilar retorna los elementos en orden inverso al que entraron (LIFO).** | | |
| Crear una pila stack y apilar "A", "B" y "C". | String first = stack.pop(); String second = stack.pop(); | assertEquals("C", first); assertEquals("B", second); assertEquals(1, stack.size()); assertEquals("A", stack.peek()); |
| **Caso 3: Consultar el tope con peek no saca el elemento de la pila.** | | |
| Crear una pila stack y apilar "A". | stack.peek(); stack.peek(); | assertEquals("A", stack.peek()); assertEquals(1, stack.size()); |
| **Caso 4: Desapilar el único elemento deja la pila vacía.** | | |
| Crear una pila stack y apilar "A". | stack.pop(); | assertTrue(stack.isEmpty()); assertEquals(0, stack.size()); |
| **Caso 5: Desapilar una pila vacía lanza una excepción.** | | |
| Crear una pila vacía stack. | stack.pop(); | assertThrows(EmptyStructureException.class, () -> stack.pop()); |
| **Caso 6: Consultar el tope de una pila vacía lanza una excepción.** | | |
| Crear una pila vacía stack. | stack.peek(); | assertThrows(EmptyStructureException.class, () -> stack.peek()); |
