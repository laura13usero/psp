import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * SERVIDOR MAESTRO - CASO H: Dragon Cliente-Servidor.
 * Arranca 4 servidores. El ServidorDragon recibe los puertos de los
 * demas servidores para poder atacarlos como CLIENTE.
 */
public class ServidorMaestro {
    public static final int PUERTO_TABERNA = 5000;
    public static final int PUERTO_MERCADO = 5001;
    public static final int PUERTO_PORTON  = 5002;
    public static final int PUERTO_DRAGON  = 5003;

    public static void main(String[] args) {
        System.out.println("=== SERVIDORES ROEDALIA - CASO H: DRAGON CLIENTE-SERVIDOR ===");

        ControlTaberna controlTaberna = new ControlTaberna();
        ControlDragon controlDragon = new ControlDragon();

        // Arrancar servidores normales
        new Thread(new HiloServidorTaberna(PUERTO_TABERNA, controlTaberna)).start();
        new Thread(new HiloServidorMercado(PUERTO_MERCADO)).start();
        new Thread(new HiloServidorPorton(PUERTO_PORTON)).start();

        // Arrancar servidor del dragon con DOBLE ROL:
        // Le pasamos los puertos de los demas servidores para que pueda
        // atacarlos como CLIENTE desde dentro de su proceso servidor.
        new Thread(new HiloServidorDragon(
            PUERTO_DRAGON, controlDragon,
            PUERTO_TABERNA, PUERTO_MERCADO, PUERTO_PORTON  // Para la parte CLIENTE
        )).start();

        System.out.println("[MAESTRO] Taberna:" + PUERTO_TABERNA
            + " Mercado:" + PUERTO_MERCADO
            + " Porton:" + PUERTO_PORTON
            + " Dragon:" + PUERTO_DRAGON);
        System.out.println("[MAESTRO] *** Dragon tiene DOBLE ROL: Servidor (5003) + Cliente (ataca 5000/5001/5002) ***");
    }
}

