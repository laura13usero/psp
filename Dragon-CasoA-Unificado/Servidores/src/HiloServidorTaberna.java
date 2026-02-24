import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Hilo del ServerSocket de la Taberna (identico al original).
 * Patron: ServerSocket + accept() en bucle + HiloTaberna por cliente.
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

