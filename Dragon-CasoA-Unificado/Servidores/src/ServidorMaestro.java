import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * SERVIDOR MAESTRO - CASO A: DRAGON (Secuestro + Rescate + Duelo).
 * Arranca 4 servidores: Taberna(5000), Mercado(5001), Porton(5002), Cubil(5003).
 * El patron es el mismo que el proyecto original: cada servidor es un Runnable
 * con ServerSocket.accept() en bucle que lanza un Thread por cada cliente.
 */
public class ServidorMaestro {
    // Puertos fijos - deben coincidir con ClienteMaestro
    public static final int PUERTO_TABERNA = 5000;
    public static final int PUERTO_MERCADO = 5001;
    public static final int PUERTO_PORTON  = 5002;
    public static final int PUERTO_CUBIL   = 5003; // NUEVO: cubil del dragon

    public static void main(String[] args) {
        System.out.println("=== SERVIDORES DE ROEDALIA - CASO A: DRAGON ===");

        // Monitor de la taberna (sincroniza encuentro Elisabetha-Lance)
        ControlTaberna controlTaberna = new ControlTaberna();
        // NUEVO: Monitor del cubil (Elisabetha hace wait, Lance hace notifyAll)
        ControlCubil controlCubil = new ControlCubil();

        // Arrancar los 4 servidores (patron: new Thread(Runnable).start())
        new Thread(new HiloServidorTaberna(PUERTO_TABERNA, controlTaberna)).start();
        new Thread(new HiloServidorMercado(PUERTO_MERCADO)).start();
        new Thread(new HiloServidorPorton(PUERTO_PORTON)).start();
        new Thread(new HiloServidorCubil(PUERTO_CUBIL, controlCubil)).start(); // NUEVO

        System.out.println("[MAESTRO] Taberna:" + PUERTO_TABERNA + " Mercado:" + PUERTO_MERCADO);
        System.out.println("[MAESTRO] Porton:" + PUERTO_PORTON + " Cubil:" + PUERTO_CUBIL);
        System.out.println("[MAESTRO] Esperando conexiones...");
    }
}

