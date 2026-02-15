import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

// Press Shift twice to open the Search Everywhere dialog and type `show whitespaces`,
// then press Enter. You can now see whitespace characters in your code.
public class ServidorHilo implements Runnable {

    private Socket skCliente;
    private int HiloNum;

    public ServidorHilo(Socket socket, int num){
        this.skCliente = socket;
        this.HiloNum = num;
    }

    @Override
    public void run() {

        try {

            int lecturaInt;
            int scrituraInt;
            String lecturaString;
            String scrituraString;

            boolean continuar = true;

            while (continuar) {
                try {
                    InputStream get = this.skCliente.getInputStream(); // obtener mensajes
                    DataInputStream flujo_entrada = new DataInputStream(get); // creamos clase para leer datos primitivos
                    OutputStream post = this.skCliente.getOutputStream(); // enviar mensajes
                    DataOutputStream flujo_salida = new DataOutputStream(post); // creamos clase para leer datos primitivos

                    boolean run = true;
                    while (run){

                        lecturaInt = flujo_entrada.readInt(); // speramos recibir los datos del cliente
                        System.out.println("Hilo: " + this.HiloNum  + " El cliente ha pedido la operación " + lecturaInt);

                        if(lecturaInt == 100000) {
                            flujo_salida.writeUTF(
                                    "--------------------------\n" +
                                            "opcion 1 (suma)\n" +
                                            "opcion 2 (raiz cuadrada)\n" +
                                            "opcion 3 (segir la serie)\n" +
                                            "opcion 4 (desconectar)\n" +
                                            "--------------------------\n"
                            );
                            flujo_salida.writeUTF("preguntar_al_usuario");
                            flujo_salida.writeUTF("Que operacion desea realizar");
                            lecturaInt = flujo_entrada.readInt();
                        }

                        if(lecturaInt == 999999){
                            lecturaInt = 0;
                            run = false;
                        }

                        if(lecturaInt == 4){
                            lecturaInt = 0;
                            run = false;
                            continuar = false;
                            int port = this.skCliente.getPort();
                            flujo_salida.writeUTF("desconectar_usuario puerto " + port);
                            System.out.println("Hilo: " + this.HiloNum  + " Cliente en puerto " + port + " desconectado. Terminando hilo.");
                        }

                        if (lecturaInt == 1){
                            flujo_salida.writeUTF("preguntar_al_usuario");
                            flujo_salida.writeUTF("primer numero a sumar");
                            int num1 = flujo_entrada.readInt(); // esperamos recibir dato
                            flujo_salida.writeUTF("preguntar_al_usuario");
                            flujo_salida.writeUTF("segundo numero a sumar");
                            int num2 = flujo_entrada.readInt();
                            int num3 = (num1 + num2);
                            flujo_salida.writeUTF("mostrar_al_usuario");
                            flujo_salida.writeUTF("Resultado: " + String.valueOf(num3)); // le escribimos al cliente el dato
                            flujo_salida.writeUTF("restear_interacion");
                            lecturaInt = 0;


                        } else if (lecturaInt == 2) {
                            flujo_salida.writeUTF("preguntar_al_usuario");
                            flujo_salida.writeUTF("numero para calcular la raiz cuadrada");
                            int num1 = flujo_entrada.readInt();
                            double raiz = Math.sqrt(num1);
                            flujo_salida.writeUTF("mostrar_al_usuario");
                            flujo_salida.writeUTF("Resultado: " + String.valueOf(raiz));
                            flujo_salida.writeUTF("restear_interacion");
                            lecturaInt = 0;

                        } else if (lecturaInt == 3) {
                            boolean validInput = false;
                            String respuesta = "";
                            while (!validInput) {
                                flujo_salida.writeUTF("preguntar_al_usuario_UTF");
                                flujo_salida.writeUTF("=== OPCION 3: Seguir la serie ===\n" +
                                        "Formato: servicio_siguiente,cantidad,num1,num2,num3,...\n" +
                                        "Ejemplo 1: 1,3,2,4,6\n" +
                                        "  - Servicio 1 (suma) se ejecutará después\n" +
                                        "  - 3 números en la serie: 2,4,6\n" +
                                        "  - Respuesta: 8 (serie incremental +2)\n" +
                                        "Ejemplo 2: 2,4,1,4,1,4\n" +
                                        "  - Servicio 2 (raiz cuadrada) se ejecutará después\n" +
                                        "  - 4 números en la serie: 1,4,1,4\n" +
                                        "  - Respuesta: 1 (patrón alternante)\n" +
                                        "Ejemplo 3: 4,4,3,6,12,24\n" +
                                        "  - Servicio 4 (desconectar) se ejecutará después\n" +
                                        "  - 4 números en la serie: 3,6,12,24\n" +
                                        "  - Respuesta: 48 (serie geométrica x2)\n" +
                                        "\nEl primer dígito: servicio siguiente (1-4)\n" +
                                        "El segundo dígito: cantidad de números en la serie\n" +
                                        "Ingrese su serie: ");
                                respuesta = flujo_entrada.readUTF();
                                respuesta = respuesta.replace(" ", "");

                                int contador = 0;
                                for (int i = 0; i < respuesta.length(); i++) {
                                    if (respuesta.charAt(i) == ',') {
                                        contador++;
                                    }
                                }

                                if (contador >= 2) {
                                    validInput = true;
                                } else {
                                    flujo_salida.writeUTF("mostrar_al_usuario");
                                    flujo_salida.writeUTF("Error: Debe ingresar al menos el servicio, la cantidad y un número separados por comas (servicio,cantidad,num1,...). Intente nuevamente.");
                                }
                            }

                            // Parsear los números de la respuesta
                            String[] numerosStr = respuesta.split(",");
                            int[] numeros = new int[numerosStr.length];

                            try {
                                for (int i = 0; i < numerosStr.length; i++) {
                                    numeros[i] = Integer.parseInt(numerosStr[i]);
                                }

                                // El primer número indica qué servicio se ejecutará después
                                int servicioSiguiente = numeros[0];
                                // El segundo número indica cuántos números hay en la serie
                                int cantidadNumeros = numeros[1];

                                // Validar que el servicio sea válido
                                if (servicioSiguiente < 1 || servicioSiguiente > 4) {
                                    flujo_salida.writeUTF("mostrar_al_usuario");
                                    flujo_salida.writeUTF("Error: El servicio debe estar entre 1 y 4.");
                                } else if (cantidadNumeros < 1) {
                                    flujo_salida.writeUTF("mostrar_al_usuario");
                                    flujo_salida.writeUTF("Error: La cantidad de números debe ser al menos 1.");
                                } else if (numerosStr.length < cantidadNumeros + 2) {
                                    flujo_salida.writeUTF("mostrar_al_usuario");
                                    flujo_salida.writeUTF("Error: Se esperaban " + cantidadNumeros + " números en la serie, pero solo se recibieron " + (numerosStr.length - 2) + ".");
                                } else {
                                    // Guardar los números de la serie en un vector (excluyendo los dos primeros dígitos)
                                    int[] serie = new int[cantidadNumeros];
                                    for (int i = 0; i < cantidadNumeros; i++) {
                                        serie[i] = numeros[i + 2];
                                    }

                                    // Calcular el patrón de la serie
                                    int siguienteNumero = 0;
                                    boolean patronEncontrado = false;

                                    // Intentar detectar serie aritmética (diferencia constante)
                                    if (cantidadNumeros >= 2) {
                                        int diferencia = serie[1] - serie[0];
                                        boolean esAritmetica = true;

                                        for (int i = 2; i < cantidadNumeros; i++) {
                                            if (serie[i] - serie[i - 1] != diferencia) {
                                                esAritmetica = false;
                                                break;
                                            }
                                        }

                                        if (esAritmetica) {
                                            siguienteNumero = serie[cantidadNumeros - 1] + diferencia;
                                            patronEncontrado = true;
                                        }
                                    }

                                    // Si no es aritmética, intentar detectar serie geométrica (multiplicación constante)
                                    if (!patronEncontrado && cantidadNumeros >= 2 && serie[0] != 0) {
                                        double razon = (double) serie[1] / serie[0];
                                        boolean esGeometrica = true;

                                        for (int i = 2; i < cantidadNumeros; i++) {
                                            if (serie[i - 1] == 0 || Math.abs(((double) serie[i] / serie[i - 1]) - razon) > 0.0001) {
                                                esGeometrica = false;
                                                break;
                                            }
                                        }

                                        if (esGeometrica) {
                                            siguienteNumero = (int) (serie[cantidadNumeros - 1] * razon);
                                            patronEncontrado = true;
                                        }
                                    }

                                    // Si no es geométrica, intentar detectar patrón alternante de 2 elementos
                                    if (!patronEncontrado && cantidadNumeros >= 4) {
                                        boolean esAlternante2 = true;

                                        for (int i = 2; i < cantidadNumeros; i++) {
                                            if (serie[i] != serie[i % 2]) {
                                                esAlternante2 = false;
                                                break;
                                            }
                                        }

                                        if (esAlternante2) {
                                            siguienteNumero = serie[cantidadNumeros % 2];
                                            patronEncontrado = true;
                                        }
                                    }

                                    // Si no es alternante de 2, intentar patrón alternante de 3 elementos
                                    if (!patronEncontrado && cantidadNumeros >= 6) {
                                        boolean esAlternante3 = true;

                                        for (int i = 3; i < cantidadNumeros; i++) {
                                            if (serie[i] != serie[i % 3]) {
                                                esAlternante3 = false;
                                                break;
                                            }
                                        }

                                        if (esAlternante3) {
                                            siguienteNumero = serie[cantidadNumeros % 3];
                                            patronEncontrado = true;
                                        }
                                    }

                                    // Si encontramos un patrón, enviar el resultado
                                    if (patronEncontrado) {
                                        flujo_salida.writeUTF("mostrar_al_usuario");
                                        flujo_salida.writeUTF("El siguiente número de la serie es: " + siguienteNumero);
                                    } else {
                                        flujo_salida.writeUTF("mostrar_al_usuario");
                                        flujo_salida.writeUTF("No se pudo detectar un patrón claro en la serie.");
                                    }

                                    // Mostrar mensaje sobre el servicio sugerido
                                    flujo_salida.writeUTF("mostrar_al_usuario");
                                    String nombreServicio = "";
                                    switch (servicioSiguiente) {
                                        case 1:
                                            nombreServicio = "Suma";
                                            break;
                                        case 2:
                                            nombreServicio = "Raíz cuadrada";
                                            break;
                                        case 3:
                                            nombreServicio = "Seguir la serie";
                                            break;
                                        case 4:
                                            nombreServicio = "Desconectar";
                                            break;
                                    }
                                    flujo_salida.writeUTF("Servicio sugerido a continuación: " + servicioSiguiente + " (" + nombreServicio + ")");
                                }

                            } catch (NumberFormatException e) {
                                flujo_salida.writeUTF("mostrar_al_usuario");
                                flujo_salida.writeUTF("Error: Los valores ingresados no son números válidos.");
                            }

                            flujo_salida.writeUTF("restear_interacion");
                            lecturaInt = 0;

                        } else {
                            flujo_salida.writeChars("Servicio no disponible");
                            lecturaInt = 0;
                            flujo_salida.writeUTF("restear_interacion");
                        }
                    }

                } catch (IOException e) {
                    System.out.println(e.getMessage());
                }
            }

        } finally {
            try {
                this.skCliente.close();
                System.out.println("Hilo: " + this.HiloNum  + " Socket cerrado y hilo terminado");
            } catch (IOException e) {
                System.out.println("Hilo: " + this.HiloNum  + " Error al cerrar socket: " + e.getMessage());
            }
        }

    }
}