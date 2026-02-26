import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class HiloServidorMercado implements Runnable {
    private int puerto;
    public HiloServidorMercado(int puerto) { this.puerto = puerto; }
    @Override
    public void run() {
        try {
            ServerSocket sk = new ServerSocket(puerto);
            System.out.println("[MERCADO] Servidor en puerto " + puerto);
            while (true) { new HiloMercado(sk.accept()).start(); }
        } catch (IOException e) { System.out.println("[MERCADO] Error: " + e.getMessage()); }
    }
}

