import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Random;

/**
 * SERVIDOR DRAGON - CASO H: CLIENTE-SERVIDOR A LA VEZ.
 *
 * PATRON NUEVO: Este Runnable hace DOS cosas simultaneamente:
 *
 *   1. PARTE SERVIDOR: Abre un ServerSocket en puerto 5003 y acepta
 *      conexiones de Lance/Caballeros que quieren atacar al dragon.
 *      Por cada conexion lanza un HiloDragonServidor (como siempre).
 *
 *   2. PARTE CLIENTE: Lanza un hilo separado (HiloAtacanteDragon) que
 *      periodicamente ABRE SOCKETS como cliente hacia Mercado(5001),
 *      Porton(5002) y Taberna(5000) para destruirlos.
 *
 * ESTO DEMUESTRA que un mismo proceso puede ser SERVIDOR (acepta
 * conexiones entrantes) y CLIENTE (abre conexiones salientes) al
 * mismo tiempo. Es la combinacion de los patrones de Caso C/E
 * (dragon como cliente) y Caso B/F (dragon como servidor).
 *
 * Cuando el dragon muere (ControlDragon.vidaDragon <= 0), el hilo
 * atacante tambien se detiene.
 */
public class HiloServidorDragon implements Runnable {
    private int puerto;
    private ControlDragon control;

    // Puertos de los servidores que el dragon atacara como CLIENTE
    private int puertoTaberna;
    private int puertoMercado;
    private int puertoPorton;

    public HiloServidorDragon(int puerto, ControlDragon control,
                              int puertoTaberna, int puertoMercado, int puertoPorton) {
        this.puerto = puerto;
        this.control = control;
        this.puertoTaberna = puertoTaberna;
        this.puertoMercado = puertoMercado;
        this.puertoPorton = puertoPorton;
    }

    @Override
    public void run() {
        // =====================================================================
        // PARTE CLIENTE: Lanzar hilo que ataca servidores existentes
        // =====================================================================
        // El dragon, ADEMAS de ser servidor, lanza un hilo que abre sockets
        // como cliente para atacar los demas servidores periodicamente.
        Thread hiloAtacante = new Thread(new HiloAtacanteDragon(
            control, puertoTaberna, puertoMercado, puertoPorton));
        hiloAtacante.setDaemon(true); // Se detiene cuando el servidor pare
        hiloAtacante.start();
        System.out.println("[DRAGON-SRV] Hilo atacante lanzado (PARTE CLIENTE).");

        // =====================================================================
        // PARTE SERVIDOR: Aceptar conexiones de quien quiera atacar al dragon
        // =====================================================================
        try {
            ServerSocket skServidor = new ServerSocket(puerto);
            System.out.println("[DRAGON-SRV] Servidor Dragon en puerto " + puerto);
            System.out.println("[DRAGON-SRV] *** DOBLE ROL: Servidor (acepta ataques) + Cliente (ataca lugares) ***");

            while (true) {
                Socket skCliente = skServidor.accept();
                System.out.println("[DRAGON-SRV] Nuevo atacante conectado.");
                new HiloDragonServidor(skCliente, control).start();
            }
        } catch (IOException e) {
            System.out.println("[DRAGON-SRV] Error: " + e.getMessage());
        }
    }
}

