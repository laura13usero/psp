import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class HiloServidorMercado implements Runnable {
    private int puerto;

    public HiloServidorMercado(int puerto) {
        this.puerto = puerto;
    }

    @Override
    public void run() {
        try {
            ServerSocket skServidor = new ServerSocket(puerto);
            System.out.println("[MERCADO] Servidor iniciado en puerto " + puerto);

            while (true) {
                Socket skCliente = skServidor.accept();
                HiloMercado hilo = new HiloMercado(skCliente);
                hilo.start();
            }
        } catch (IOException e) {
            System.out.println("[MERCADO] Error: " + e.getMessage());
        }
    }
}

