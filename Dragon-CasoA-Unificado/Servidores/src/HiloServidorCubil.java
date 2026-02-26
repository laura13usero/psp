import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * NUEVO: Hilo que ejecuta el ServerSocket del Cubil del Dragon (puerto 5003).
 * Patron identico a HiloServidorTaberna:
 *   1. Crear ServerSocket en el puerto
 *   2. Bucle infinito: accept() espera conexiones
 *   3. Por cada cliente: crear HiloCubil para atenderlo
 *
 * Recibe el ControlCubil (monitor) para pasarlo a cada HiloCubil.
 */
public class HiloServidorCubil implements Runnable {
    private int puerto;              // Puerto donde escucha (5003)
    private ControlCubil control;    // Monitor compartido del cubil

    public HiloServidorCubil(int puerto, ControlCubil control) {
        this.puerto = puerto;
        this.control = control;
    }

    @Override
    public void run() {
        try {
            // Crear el ServerSocket que acepta conexiones en el puerto
            ServerSocket skServidor = new ServerSocket(puerto);
            System.out.println("[CUBIL] Servidor del Cubil del Dragon en puerto " + puerto);

            while (true) {
                // accept() BLOQUEA hasta que un cliente se conecte
                Socket skCliente = skServidor.accept();
                // Crear un hilo para atender a ese cliente (Elisabetha o Lance)
                HiloCubil hilo = new HiloCubil(skCliente, control);
                hilo.start();
            }
        } catch (IOException e) {
            System.out.println("[CUBIL] Error: " + e.getMessage());
        }
    }
}

