import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Hilo que ejecuta el ServerSocket de la Taberna.
 * Patron clasico de servidor multihilo:
 * 1. Crear ServerSocket en un puerto
 * 2. Bucle infinito: accept() espera a que un cliente conecte
 * 3. Por cada cliente, crear un nuevo hilo (HiloTaberna) para atenderlo
 *
 * Esto permite que multiples personajes se conecten a la taberna simultaneamente.
 */
public class HiloServidorTaberna implements Runnable {
    private int puerto;
    private ControlTaberna control;

    public HiloServidorTaberna(int puerto, ControlTaberna control) {
        this.puerto = puerto;
        this.control = control;
    }

    @Override
    public void run() {
        try {
            ServerSocket skServidor = new ServerSocket(puerto);
            System.out.println("[TABERNA] Servidor iniciado en puerto " + puerto);

            while (true) {
                Socket skCliente = skServidor.accept();
                HiloTaberna hilo = new HiloTaberna(skCliente, control);
                hilo.start();
            }
        } catch (IOException e) {
            System.out.println("[TABERNA] Error: " + e.getMessage());
        }
    }
}
