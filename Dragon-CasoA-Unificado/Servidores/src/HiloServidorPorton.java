import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Hilo del ServerSocket del Porton Norte (identico al original).
 */
public class HiloServidorPorton implements Runnable {
    private int puerto;

    public HiloServidorPorton(int puerto) {
        this.puerto = puerto;
    }

    @Override
    public void run() {
        try {
            ServerSocket skServidor = new ServerSocket(puerto);
            System.out.println("[PORTON] Servidor iniciado en puerto " + puerto);
            while (true) {
                Socket skCliente = skServidor.accept();
                HiloPorton hilo = new HiloPorton(skCliente);
                hilo.start();
            }
        } catch (IOException e) {
            System.out.println("[PORTON] Error: " + e.getMessage());
        }
    }
}

