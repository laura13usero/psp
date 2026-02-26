import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * SERVIDOR MAESTRO - CASO B: Dragon como Servidor Independiente.
 * Arranca 4 servidores: Taberna(5000), Mercado(5001), Porton(5002), Dragon(5003).
 */
public class ServidorMaestro {
    public static final int PUERTO_TABERNA = 5000;
    public static final int PUERTO_MERCADO = 5001;
    public static final int PUERTO_PORTON  = 5002;
    public static final int PUERTO_DRAGON  = 5003; // NUEVO: servidor del dragon

    public static void main(String[] args) {
        System.out.println("=== SERVIDORES DE ROEDALIA - CASO B: DRAGON SERVIDOR ===");

        ControlTaberna controlTaberna = new ControlTaberna();
        // NUEVO: Monitor del dragon para coordinar el combate conjunto
        ControlDragon controlDragon = new ControlDragon();

        new Thread(new HiloServidorTaberna(PUERTO_TABERNA, controlTaberna)).start();
        new Thread(new HiloServidorMercado(PUERTO_MERCADO)).start();
        new Thread(new HiloServidorPorton(PUERTO_PORTON)).start();
        // NUEVO: Servidor del Dragon donde Lance y Elisabetha se conectan para luchar
        new Thread(new HiloServidorDragon(PUERTO_DRAGON, controlDragon)).start();

        System.out.println("[MAESTRO] Taberna:" + PUERTO_TABERNA + " Mercado:" + PUERTO_MERCADO);
        System.out.println("[MAESTRO] Porton:" + PUERTO_PORTON + " Dragon:" + PUERTO_DRAGON);
    }
}

