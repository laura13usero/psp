import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ServidorMaestro {
    public static final int PUERTO_TABERNA = 5000;
    public static final int PUERTO_MERCADO = 5001;
    public static final int PUERTO_PORTON  = 5002;
    public static final int PUERTO_DRAGON  = 5003; // NUEVO

    public static void main(String[] args) {
        System.out.println("=== SERVIDORES ROEDALIA - CASO F: ALQUIMISTAS CURAN DRAGON ===");
        ControlTaberna controlTaberna = new ControlTaberna();
        ControlDragon controlDragon = new ControlDragon(); // NUEVO

        new Thread(new HiloServidorTaberna(PUERTO_TABERNA, controlTaberna)).start();
        new Thread(new HiloServidorMercado(PUERTO_MERCADO)).start();
        new Thread(new HiloServidorPorton(PUERTO_PORTON)).start();
        new Thread(new HiloServidorDragon(PUERTO_DRAGON, controlDragon)).start(); // NUEVO
        System.out.println("[MAESTRO] Taberna:"+PUERTO_TABERNA+" Mercado:"+PUERTO_MERCADO
            +" Porton:"+PUERTO_PORTON+" Dragon:"+PUERTO_DRAGON);
    }
}

