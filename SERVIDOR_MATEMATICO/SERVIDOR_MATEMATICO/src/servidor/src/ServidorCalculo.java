package servidor.src;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.Semaphore;

/**
 * Servidor que acepta múltiples clientes para operaciones matemáticas.
 * Limita las conexiones actuales a 4.
 */
public class ServidorCalculo {
    private static final int PUERTO_SERVIDOR = 5000;
    private static final int MAX_CLIENTES = 4;
    private static final Semaphore SEMAFORO_LIMITE = new Semaphore(MAX_CLIENTES);

    public static void main(String[] args) {
        new ServidorCalculo().iniciarServidor();
    }

    public void iniciarServidor() {
        System.out.println(">>> Servidor de Cálculo Online (Puerto " + PUERTO_SERVIDOR + ") <<<");
        System.out.println(">>> Esperando conexiones... Máx clientes: " + MAX_CLIENTES);

        try (ServerSocket socketServidor = new ServerSocket(PUERTO_SERVIDOR)) {
            while (true) {
                // Mostrar huecos disponibles
                int huecosLibres = SEMAFORO_LIMITE.availablePermits();
                System.out.println("[INFO] En espera... (Huecos libres: " + huecosLibres + "/" + MAX_CLIENTES + ")");

                Socket socketCliente = socketServidor.accept();

                if (SEMAFORO_LIMITE.tryAcquire()) {
                    System.out.println("[+] Nuevo Cliente Aceptado: " + socketCliente.getInetAddress());
                    GestorCliente trabajador = new GestorCliente(socketCliente, SEMAFORO_LIMITE);
                    trabajador.start();
                } else {
                    System.out.println("[-] Servidor Lleno. Rechazando conexión de: " + socketCliente.getInetAddress());
                    socketCliente.close();
                }
            }
        } catch (IOException e) {
            System.err.println("[ERROR] Excepción del servidor: " + e.getMessage());
        }
    }
}