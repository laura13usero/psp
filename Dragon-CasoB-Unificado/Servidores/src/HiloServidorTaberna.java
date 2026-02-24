import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class HiloServidorTaberna implements Runnable {
    private int puerto;
    private ControlTaberna control;
    public HiloServidorTaberna(int puerto, ControlTaberna control) {
        this.puerto = puerto; this.control = control;
    }
    @Override
    public void run() {
        try {
            ServerSocket skServidor = new ServerSocket(puerto);
            System.out.println("[TABERNA] Servidor en puerto " + puerto);
            while (true) { Socket sk = skServidor.accept(); new HiloTaberna(sk, control).start(); }
        } catch (IOException e) { System.out.println("[TABERNA] Error: " + e.getMessage()); }
    }
}

