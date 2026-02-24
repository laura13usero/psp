import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/** SERVIDOR MAESTRO - CASO G: solo 3 puertos (sin servidor del dragon). */
public class ServidorMaestro {
    public static final int PUERTO_TABERNA = 5000;
    public static final int PUERTO_MERCADO = 5001;
    public static final int PUERTO_PORTON  = 5002;
    public static void main(String[] args) {
        System.out.println("=== SERVIDORES ROEDALIA - CASO G: DRAGON LOCAL ===");
        ControlTaberna controlTaberna = new ControlTaberna();
        new Thread(new HiloServidorTaberna(PUERTO_TABERNA, controlTaberna)).start();
        new Thread(new HiloServidorMercado(PUERTO_MERCADO)).start();
        new Thread(new HiloServidorPorton(PUERTO_PORTON)).start();
        System.out.println("[MAESTRO] Taberna:"+PUERTO_TABERNA+" Mercado:"+PUERTO_MERCADO+" Porton:"+PUERTO_PORTON);
    }
}

