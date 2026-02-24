import java.io.*;
import java.net.Socket;
import java.util.Random;

/**
 * HILO ATACANTE DRAGON - La PARTE CLIENTE del dragon.
 *
 * Este hilo vive DENTRO del proceso servidor del dragon y periodicamente
 * abre sockets COMO CLIENTE hacia los demas servidores (Mercado, Porton,
 * Taberna) para atacarlos y destruirlos temporalmente.
 *
 * PATRON: Servidor actuando como cliente. Desde el proceso que tiene
 * el ServerSocket del dragon (puerto 5003), este hilo crea nuevos
 * Sockets hacia otros puertos (5000, 5001, 5002) para enviar
 * "ATAQUE_DRAGON". Es comunicacion de SERVIDOR -> SERVIDOR.
 *
 * Se detiene cuando el dragon muere (ControlDragon.dragonVivo() == false).
 */
public class HiloAtacanteDragon implements Runnable {
    private ControlDragon control;
    private int puertoTaberna;
    private int puertoMercado;
    private int puertoPorton;
    private Random random = new Random();

    public HiloAtacanteDragon(ControlDragon control,
                              int puertoTaberna, int puertoMercado, int puertoPorton) {
        this.control = control;
        this.puertoTaberna = puertoTaberna;
        this.puertoMercado = puertoMercado;
        this.puertoPorton = puertoPorton;
    }

    @Override
    public void run() {
        System.out.println("[DRAGON-CLI] El dragon mora en paz...");

        // Esperar antes de empezar a atacar
        try { Thread.sleep(25000); } catch (InterruptedException e) { }

        while (control.dragonVivo()) {
            // Elegir un lugar aleatorio para atacar
            int objetivo = random.nextInt(3);
            int puerto;
            String nombre;

            switch (objetivo) {
                case 0:  puerto = puertoMercado; nombre = "MERCADO";  break;
                case 1:  puerto = puertoPorton;  nombre = "PORTON";   break;
                default: puerto = puertoTaberna;  nombre = "TABERNA";  break;
            }

            System.out.println("[DRAGON-CLI] *** ATACA " + nombre + "! (dragon como CLIENTE) ***");
            System.out.println("[DRAGON-CLI] Vida actual: " + control.getVidaDragon()
                + " (Fase " + control.getFase() + ")");

            atacarLugar(puerto, nombre);

            // Si sigue vivo, dormir antes de volver a atacar
            if (control.dragonVivo()) {
                int sueno = 20000 + random.nextInt(20000);
                System.out.println("[DRAGON-CLI] Se retira " + (sueno / 1000) + " seg...");
                try { Thread.sleep(sueno); } catch (InterruptedException e) { }
            }
        }

        System.out.println("[DRAGON-CLI] El dragon ha muerto. Deja de atacar lugares.");
    }

    /**
     * Abrir un socket COMO CLIENTE hacia un servidor y enviar ATAQUE_DRAGON.
     * Patron identico a como lo hace HiloDragon en los Casos C/E, pero
     * aqui se ejecuta desde DENTRO del proceso servidor del dragon.
     */
    private void atacarLugar(int puerto, String nombre) {
        try {
            // Abrir socket como CLIENTE hacia el servidor del lugar
            Socket sk = new Socket("localhost", puerto);
            DataOutputStream sal = new DataOutputStream(sk.getOutputStream());
            DataInputStream ent = new DataInputStream(sk.getInputStream());

            // Enviar comando de ataque
            sal.writeUTF("ATAQUE_DRAGON");
            String respuesta = ent.readUTF();
            System.out.println("[DRAGON-CLI] " + nombre + " atacado: " + respuesta);

            // Desconectar
            sal.writeUTF("DESCONECTAR");
            ent.readUTF();
            sk.close();
        } catch (IOException e) {
            System.out.println("[DRAGON-CLI] No pudo atacar " + nombre + ": " + e.getMessage());
        }
    }
}

