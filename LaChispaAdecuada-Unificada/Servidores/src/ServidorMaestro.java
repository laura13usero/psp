import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * SERVIDOR MAESTRO - Punto de entrada del lado servidor.
 * Arranca los 3 servidores (Taberna, Mercado, Porton Norte) como hilos independientes.
 * Cada servidor abre su propio ServerSocket en un puerto distinto.
 */
public class ServidorMaestro {
    // Puertos fijos para cada servidor (a partir de 5000, reservados a programadores)
    public static final int PUERTO_TABERNA = 5000;
    public static final int PUERTO_MERCADO = 5001;
    public static final int PUERTO_PORTON = 5002;

    public static void main(String[] args) {
        System.out.println("==============================================");
        System.out.println("  SERVIDORES DE ROEDALIA - MAESTRO");
        System.out.println("==============================================");

        // ControlTaberna es el OBJETO COMPARTIDO que sincroniza el encuentro
        // entre Elisabetha y Lance. Usa synchronized + wait/notifyAll.
        ControlTaberna controlTaberna = new ControlTaberna();

        // Cada servidor se lanza como un hilo que internamente hace accept() en bucle
        // Patron: new Thread(Runnable).start()

        // Servidor Taberna: lugar clave donde se genera "la chispa adecuada"
        Thread hiloTaberna = new Thread(new HiloServidorTaberna(PUERTO_TABERNA, controlTaberna));
        hiloTaberna.start();

        // Servidor Mercado: vende productos aleatorios a los personajes
        Thread hiloMercado = new Thread(new HiloServidorMercado(PUERTO_MERCADO));
        hiloMercado.start();

        // Servidor Porton Norte: inspecciona carretas que entran a la ciudad
        Thread hiloPorton = new Thread(new HiloServidorPorton(PUERTO_PORTON));
        hiloPorton.start();

        System.out.println("[MAESTRO] Todos los servidores iniciados.");
        System.out.println("[MAESTRO] Taberna: puerto " + PUERTO_TABERNA);
        System.out.println("[MAESTRO] Mercado: puerto " + PUERTO_MERCADO);
        System.out.println("[MAESTRO] Porton Norte: puerto " + PUERTO_PORTON);
        System.out.println("[MAESTRO] Esperando conexiones de personajes...");
    }
}
