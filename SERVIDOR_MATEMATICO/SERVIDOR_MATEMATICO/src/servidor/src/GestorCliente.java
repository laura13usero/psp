package servidor.src;

import java.io.*;
import java.net.Socket;
import java.util.concurrent.Semaphore;

/**
 * Hilo trabajador para manejar las peticiones individuales de clientes.
 */
public class GestorCliente extends Thread {

    private final Socket conexion;
    private final Semaphore semaforo;

    public GestorCliente(Socket conexion, Semaphore semaforo) {
        this.conexion = conexion;
        this.semaforo = semaforo;
    }

    @Override
    public void run() {
        try (DataInputStream entrada = new DataInputStream(conexion.getInputStream());
                DataOutputStream salida = new DataOutputStream(conexion.getOutputStream())) {

            boolean ejecutando = true;
            System.out.println("[Hilo " + this.getName() + "] Atención al cliente iniciada.");

            while (ejecutando) {
                try {
                    int opcionMenu = entrada.readInt();
                    double resultado;

                    switch (opcionMenu) {
                        case 1: // Suma
                            resultado = calcularSuma(entrada);
                            salida.writeDouble(resultado);
                            break;
                        case 2: // Raiz
                            resultado = calcularRaiz(entrada);
                            salida.writeDouble(resultado);
                            break;
                        case 3: // Serie
                            resultado = calcularSerie(entrada);
                            salida.writeDouble(resultado);
                            break;
                        case 4: // Salir
                            ejecutando = false;
                            System.out.println("[Hilo " + this.getName() + "] El cliente solicitó desconexión.");
                            break;
                        default:
                            System.out.println("[Hilo " + this.getName() + "] Opción desconocida recibida.");
                            break;
                    }
                } catch (EOFException e) {
                    ejecutando = false;
                    System.out.println("[Hilo " + this.getName() + "] Cliente desconectado inesperadamente.");
                }
            }

        } catch (IOException e) {
            System.err.println("[Hilo " + this.getName() + "] Error de E/S: " + e.getMessage());
        } finally {
            cerrarConexion();
            semaforo.release();
            System.out.println("[-] Hueco de cliente liberado. Disponibles: " + semaforo.availablePermits());
        }
    }

    private double calcularSuma(DataInputStream in) throws IOException {
        double a = in.readDouble();
        double b = in.readDouble();
        return a + b;
    }

    private double calcularRaiz(DataInputStream in) throws IOException {
        double valor = in.readDouble();
        return Math.sqrt(valor);
    }

    private double calcularSerie(DataInputStream in) throws IOException {
        int cantidad = in.readInt();
        double ultimo = 0;
        double previo = 0;

        for (int i = 0; i < cantidad; i++) {
            previo = ultimo;
            ultimo = in.readDouble();
        }
        // Suponiendo progresión aritmética: siguiente = ultimo + (diferencia)
        double diferencia = ultimo - previo;
        return ultimo + diferencia;
    }

    private void cerrarConexion() {
        try {
            if (conexion != null && !conexion.isClosed()) {
                conexion.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}