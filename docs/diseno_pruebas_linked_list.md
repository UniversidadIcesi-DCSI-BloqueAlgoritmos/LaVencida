# Diseño de pruebas unitarias

**Estructura de datos:** LinkedList<T> (lista enlazada simple propia del equipo).

**Operaciones probadas:** addFirst(T), addLast(T), remove(int): T, contains(T): boolean, sort(Comparator<T>): void, binarySearch(T, Comparator<T>): int

**Objetivo de la Prueba:** Verificar que la lista agrega y elimina elementos manteniendo el orden y las referencias de cabeza y cola, que contains encuentra los elementos existentes, que sort (ordenamiento burbuja) deja los elementos en orden ascendente según el comparador sin perder elementos y conservando el orden de los iguales, y que binarySearch encuentra la posición de un elemento en una lista ordenada o retorna -1 si no existe.

| Arrange (Inicialización) | Act (Acción) | Assert (Verificar) |
|---|---|---|
| **Caso 1: addFirst y addLast agregan al inicio y al final respectivamente.** | | |
| Crear una lista vacía list. | list.addLast(2); list.addFirst(1); list.addLast(3); | assertEquals(1, list.get(0)); assertEquals(2, list.get(1)); assertEquals(3, list.get(2)); assertEquals(3, list.size()); |
| **Caso 2: Eliminar un elemento del medio conserva el orden de los demás.** | | |
| Crear list con 10, 20, 30. | int removed = list.remove(1); | assertEquals(20, removed); assertEquals(2, list.size()); assertEquals(10, list.get(0)); assertEquals(30, list.get(1)); |
| **Caso 3: Eliminar el último elemento actualiza la cola y se puede seguir agregando al final.** | | |
| Crear list con 10, 20, 30. | list.remove(2); list.addLast(40); | assertEquals(3, list.size()); assertEquals(20, list.get(1)); assertEquals(40, list.get(2)); |
| **Caso 4: Eliminar con un índice fuera de rango lanza una excepción.** | | |
| Crear list con 10. | list.remove(5); | assertThrows(IndexOutOfBoundsException.class, () -> list.remove(5)); assertEquals(1, list.size()); |
| **Caso 5: contains retorna true si el elemento está y false si no.** | | |
| Crear list con 10, 20. | list.contains(20); list.contains(99); | assertTrue(list.contains(20)); assertFalse(list.contains(99)); |
| **Caso 6: Ordenar una lista desordenada la deja en orden ascendente.** | | |
| Crear list con 5, 3, 8, 1, 4. | list.sort(Comparator.naturalOrder()); | assertEquals(1, list.get(0)); assertEquals(3, list.get(1)); assertEquals(4, list.get(2)); assertEquals(5, list.get(3)); assertEquals(8, list.get(4)); |
| **Caso 7: Ordenar una lista vacía o de un solo elemento no lanza error.** | | |
| Crear una lista vacía emptyList y una lista singleList con 7. | emptyList.sort(Comparator.naturalOrder()); singleList.sort(Comparator.naturalOrder()); | assertEquals(0, emptyList.size()); assertEquals(7, singleList.get(0)); |
| **Caso 8: La búsqueda binaria encuentra la posición de un elemento en una lista ordenada.** | | |
| Crear list con 1, 3, 5, 7, 9. | int index = list.binarySearch(7, Comparator.naturalOrder()); | assertEquals(3, index); |
| **Caso 9: La búsqueda binaria retorna -1 si el elemento no está.** | | |
| Crear list con 1, 3, 5, 7, 9. | int index = list.binarySearch(4, Comparator.naturalOrder()); | assertEquals(-1, index); |
