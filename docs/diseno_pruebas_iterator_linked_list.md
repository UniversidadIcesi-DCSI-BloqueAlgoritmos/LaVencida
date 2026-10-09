# Diseño de pruebas unitarias

**Estructura de datos:** LinkedList<T> (lista enlazada propia del equipo) implementando Iterable<T>.

**Operaciones probadas:** iterator(): Iterator<T>, hasNext(): boolean, next(): T

**Objetivo de la Prueba:** Verificar que el iterador recorre todos los elementos de la lista en el orden en que fueron agregados, que hasNext indica correctamente cuándo no quedan más elementos, que next lanza una excepción cuando ya no hay elementos y que la lista se puede recorrer con un ciclo for-each.

| Arrange (Inicialización) | Act (Acción) | Assert (Verificar) |
|---|---|---|
| **Caso 1: Recorrer una lista con varios elementos los entrega en el orden en que fueron agregados.** | | |
| Crear una lista list y agregar "A", "B" y "C" con addLast. | Iterator<String> iterator = list.iterator(); | assertTrue(iterator.hasNext()); assertEquals("A", iterator.next()); assertEquals("B", iterator.next()); assertEquals("C", iterator.next()); assertFalse(iterator.hasNext()); |
| **Caso 2: Iterar una lista vacía indica que no hay elementos.** | | |
| Crear una lista vacía list. | Iterator<String> iterator = list.iterator(); | assertFalse(iterator.hasNext()); |
| **Caso 3: Pedir el siguiente elemento cuando ya no quedan más lanza una excepción.** | | |
| Crear una lista list con "A" y obtener su iterador iterator. Llamar iterator.next() una vez para consumir el único elemento. | iterator.next(); | assertThrows(NoSuchElementException.class, () -> iterator.next()); |
| **Caso 4: Recorrer la lista con un ciclo for-each visita todos los elementos.** | | |
| Crear una lista list de enteros con 1, 2 y 3, y definir int sum = 0. | for (int value : list) { sum += value; } | assertEquals(6, sum); |