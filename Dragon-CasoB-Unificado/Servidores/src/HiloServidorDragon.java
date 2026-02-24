import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * NUEVO: ServerSocket del Dragon (puerto 5003).
 * Patron identico a HiloServidorTaberna: accept() en bucle + hilo por cliente.
 * Recibe ControlDragon (monitor) para coordinar el combate.
 */
public class HiloServidorDragon implements Runnable {
    private int puerto;
    private ControlDragon control;

    public HiloServidorDragon(int puerto, ControlDragon control) {
        this.puerto = puerto;
        this.control = control;
    }

    @Override
    public void run() {
        try {
            ServerSocket skServidor = new ServerSocket(puerto);
            System.out.println("[DRAGON-SRV] Servidor del Dragon en puerto " + puerto);
            while (true) {
                Socket skCliente = skServidor.accept();
                HiloDragonServidor hilo = new HiloDragonServidor(skCliente, control);
                hilo.start();
            }
        } catch (IOException e) {
            System.out.println("[DRAGON-SRV] Error: " + e.getMessage());
        }
    }
}

