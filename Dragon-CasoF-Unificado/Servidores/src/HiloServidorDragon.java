import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/** Servidor del Dragon - acepta conexiones de atacantes Y alquimistas. */
public class HiloServidorDragon implements Runnable {
    private int puerto;
    private ControlDragon control;
    public HiloServidorDragon(int puerto, ControlDragon control) {
        this.puerto = puerto; this.control = control;
    }
    @Override
    public void run() {
        try {
            ServerSocket sk = new ServerSocket(puerto);
            System.out.println("[DRAGON-SRV] Servidor Dragon en puerto " + puerto);
            while (true) {
                Socket skCliente = sk.accept();
                new HiloDragonServidor(skCliente, control).start();
            }
        } catch (IOException e) {
            System.out.println("[DRAGON-SRV] Error: " + e.getMessage());
        }
    }
}

