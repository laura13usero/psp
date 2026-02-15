v1.0 ------------------------------------------------------------------
Crear un servidor que atienda a un único cliente de manera que ofrezca tres servicios matemáticos:

Los servicios serán:

1 - Suma de dos números que el cliente proporcionará al servidor
2 - Raíz cuadrada de un número que el cliente proporcionará al servidor

El servidor una vez que conecte el cliente esperará una petición de servicio, la servirá y acto seguido desconectará del cliente.

v2.0 -----------------------------------------------------

Añadiremos la opción 3 al menú y la posibilidad de pedir más de una operación matemática al servidor. Además el cliente pide por teclado al usuario a través de un menú la opción que quiere pedir al servidor y los números asociados a cada operación.

3 - Seguir la serie, el cliente proporcionará una serie de números siendo el primero de ellos el número de números de la serie y el servidor calculará el siguiente número que correspondería a la serie, por ejemplo, el cliente pide "3", "4", "1", "2", "3", "4", está indicando que quiere el servicio 3, va a proporcionar 4 números y los números de la serie son 1, 2, 3 y 4, de modo que el servidor debería responder "5".

Opcional 1:  El cliente podrá pedir tantas operaciones matemáticas como desee hasta que pida desconectar, de modo que el servicio 4 - Será la desconexión.

Opcional 2:  El servidor podrá servir a tantos clientes como lleguen en modo secuencial, es decir, uno detrás de otro como en el ejemplo de las cajeras del supermercado.





Modificamos el código del servidor monocliente del ejercicio anterior de nuestro servidor matemático para que el nuevo servidor sea capaz de atender a un número indeterminado de clientes de manera simultánea a través de hilos de modo que:
Creamos un bucle en el servidor para poder atender a más de un cliente (un bucle tipo while(1))
El servidor principal siga utilizando el puerto 5000 para aceptar clientes
Cada cliente que acepto sea atendido por un hilo privado solo para ese cliente, de modo que el hilo reciba en el constructor el socket del cliente que hemos preparado a partir del accept del servidor principal
A partir de este punto el hilo de atención al cliente será completamente independiente y no necesitará ya contactar con el servidor principal de ningún modo.
El servidor principal vuelve a atender el puerto 5000 para dar atención a un nuevo cliente.
v 2.0 (Opcional) Limitación de clientes para que no me tiren el servidor los jóvenes estudiantes de DAM
El servidor principal limitará la atención a 4 clientes simultáneos.