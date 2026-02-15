package cliente.src;

import java.io.*;
import java.net.*;
import java.util.Scanner;

/**
 * Aplicación cliente para el Servidor de Cálculo.
 */
public class ClienteCalculo {

    private static final String HOST_SERVIDOR = "localhost";
    private static final int PUERTO_SERVIDOR = 5000;

    public static void main(String[] args) {
        Scanner lectorInput = new Scanner(System.in);
        System.out.println("==========================================");
        System.out.println("      CLIENTE DE CÁLCULO - ALUMNO B       ");
        System.out.println("==========================================");

        try (Socket socket = new Socket(HOST_SERVIDOR, PUERTO_SERVIDOR);
                DataOutputStream salida = new DataOutputStream(socket.getOutputStream());
                DataInputStream entrada = new DataInputStream(socket.getInputStream())) {

            System.out.println("[INFO] Conectado al entorno de ejecución.");

            boolean sesionActiva = true;
            while (sesionActiva) {
                mostrarMenu();
                if (lectorInput.hasNextInt()) {
                    int eleccion = lectorInput.nextInt();
                    salida.writeInt(eleccion);

                    switch (eleccion) {
                        case 1: // Suma
                            manejarSuma(lectorInput, salida, entrada);
                            break;
                        case 2: // Raiz
                            manejarRaiz(lectorInput, salida, entrada);
                            break;
                        case 3: // Serie
                            manejarSerie(lectorInput, salida, entrada);
                            break;
                        case 4: // Salir
                            sesionActiva = false;
                            System.out.println("[INFO] Terminando sesión. Adiós.");
                            break;
                        default:
                            System.out.println("[WARN] Opción inválida seleccionada.");
                    }
                } else {
                    lectorInput.next(); // Consumir entrada inválida
                    System.out.println("[WARN] Por favor, introduzca un número válido.");
                }
            }

        } catch (ConnectException e) {
            System.err.println("[ERROR] Servidor no disponible o lleno. Inténtelo más tarde.");
        } catch (EOFException e) {
            System.err.println("[ERROR] Conexión terminada por el servidor.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void mostrarMenu() {
        System.out.println("\n--- Menú de Operaciones ---");
        System.out.println("1. Suma");
        System.out.println("2. Raíz Cuadrada");
        System.out.println("3. Serie Aritmética (Siguiente término)");
        System.out.println("4. Desconectar");
        System.out.print("Seleccione operación > ");
    }

    private static void manejarSuma(Scanner sc, DataOutputStream salida, DataInputStream entrada) throws IOException {
        System.out.print("Introduzca primer número: ");
        salida.writeDouble(sc.nextDouble());
        System.out.print("Introduzca segundo número: ");
        salida.writeDouble(sc.nextDouble());

        double resultado = entrada.readDouble();
        System.out.println(">> Resultado: " + resultado);
    }

    private static void manejarRaiz(Scanner sc, DataOutputStream salida, DataInputStream entrada) throws IOException {
        System.out.print("Introduzca número: ");
        salida.writeDouble(sc.nextDouble());

        double resultado = entrada.readDouble();
        System.out.println(">> Resultado: " + resultado);
    }

    private static void manejarSerie(Scanner sc, DataOutputStream salida, DataInputStream entrada) throws IOException {
        System.out.print("¿Cuántos números tiene la serie?: ");
        int n = sc.nextInt();
        salida.writeInt(n);

        for (int i = 0; i < n; i++) {
            System.out.print("Término " + (i + 1) + ": ");
            salida.writeDouble(sc.nextDouble());
        }

        double siguienteTermino = entrada.readDouble();
        System.out.println(">> El siguiente término en la progresión es: " + siguienteTermino);
    }
}