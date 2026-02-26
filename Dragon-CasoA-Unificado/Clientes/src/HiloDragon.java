import java.io.*;
import java.net.Socket;
import java.util.Random;

/**
 * NUEVO: HILO DRAGON CARMESI - El antagonista.
 * extends Thread (misma base que todos los demas personajes).
 *
 * COMPORTAMIENTO (segun el enunciado):
 * "Nuestro dragon morara en paz hasta el dia en que, por capricho del destino,
 *  irrumpa en el mercado para secuestrar a Elisabetha"
 *
 * El dragon duerme un tiempo aleatorio (15-30 seg) y luego:
 *   1. Ataca el mercado via socket (envia ATAQUE_DRAGON al servidor mercado)
 *   2. Secuestra a Elisabetha via socket (envia SECUESTRAR al servidor cubil)
 *   3. Marca la variable compartida elisabethaSecuestrada = true
 *   4. Espera a que Lance la rescate y ambos lo derroten
 *   5. Si sobrevive, vuelve a dormir y puede atacar de nuevo
 *
 * COMUNICACION:
 *   - Con Mercado: via SOCKET (DataInputStream/DataOutputStream)
 *   - Con Cubil: via SOCKET
 *   - Con Elisabetha/Lance: via VARIABLE COMPARTIDA (lockSecuestro)
 */
public class HiloDragon extends Thread {
    private Random random = new Random();
    private int vidaDragon = 100; // Puntos de vida del dragon

    @Override
    public void run() {
        System.out.println("[DRAGON] El Dragon Carmesi mora en paz en las montanas...");

        while (!ClienteMaestro.simulacionTerminada && vidaDragon > 0) {
            // El dragon duerme entre 15 y 30 segundos antes de atacar
            int tiempoSueno = 15000 + random.nextInt(16000);
            System.out.println("[DRAGON] El dragon duerme " + (tiempoSueno/1000) + " segundos...");
            try { Thread.sleep(tiempoSueno); } catch (InterruptedException e) { }

            // Comprobar si la simulacion termino mientras dormia
            if (ClienteMaestro.simulacionTerminada) break;

            // *** PASO 1: Atacar el mercado via socket ***
            System.out.println("[DRAGON] *** EL DRAGON CARMESI DESPIERTA! ***");
            System.out.println("[DRAGON] El dragon irrumpe en el mercado para secuestrar a Elisabetha!");
            atacarMercado();

            if (ClienteMaestro.simulacionTerminada) break;

            // *** PASO 2: Secuestrar a Elisabetha via socket al cubil ***
            secuestrarElisabetha();

            if (ClienteMaestro.simulacionTerminada) break;

            // *** PASO 3: Esperar a que se resuelva el duelo ***
            System.out.println("[DRAGON] El dragon espera en su cubil con Elisabetha cautiva...");
            // Esperar a que Lance la rescate (el dragon espera pasivamente)
            while (ClienteMaestro.elisabethaSecuestrada && !ClienteMaestro.simulacionTerminada) {
                try { Thread.sleep(3000); } catch (InterruptedException e) { }
            }

            // Si la simulacion sigue, el dragon puede haber sido derrotado o no
            if (!ClienteMaestro.simulacionTerminada) {
                System.out.println("[DRAGON] El dragon se retira a las montanas a recuperarse...");
                try { Thread.sleep(10000); } catch (InterruptedException e) { }
            }
        }

        System.out.println("[DRAGON] El Dragon Carmesi abandona Roedalia.");
    }

    /**
     * Atacar el mercado: se conecta por socket al servidor del Mercado
     * y envia el comando ATAQUE_DRAGON para quemar el mercado.
     * Patron identico a como Elisabetha visita el mercado.
     */
    private void atacarMercado() {
        try {
            // Crear socket al servidor del mercado (misma IP y puerto)
            Socket skMercado = new Socket(ClienteMaestro.HOST, ClienteMaestro.PUERTO_MERCADO);
            DataOutputStream salida = new DataOutputStream(skMercado.getOutputStream());
            DataInputStream entrada = new DataInputStream(skMercado.getInputStream());

            // Enviar comando de ataque (el servidor lo procesa en HiloMercado)
            salida.writeUTF("ATAQUE_DRAGON");
            String respuesta = entrada.readUTF(); // "ATAQUE_OK"
            System.out.println("[DRAGON] Mercado atacado: " + respuesta);

            // Desconectar del mercado
            salida.writeUTF("DESCONECTAR");
            entrada.readUTF();
            skMercado.close();
        } catch (IOException e) {
            System.out.println("[DRAGON] No pudo atacar el mercado: " + e.getMessage());
        }
    }

    /**
     * Secuestrar a Elisabetha: se conecta por socket al servidor del Cubil
     * y envia el comando SECUESTRAR. Luego marca la variable compartida.
     */
    private void secuestrarElisabetha() {
        try {
            // Conectar al cubil via socket
            Socket skCubil = new Socket(ClienteMaestro.HOST, ClienteMaestro.PUERTO_CUBIL);
            DataOutputStream salida = new DataOutputStream(skCubil.getOutputStream());
            DataInputStream entrada = new DataInputStream(skCubil.getInputStream());

            // Enviar comando de secuestro
            salida.writeUTF("SECUESTRAR");
            entrada.readUTF(); // "OK"

            // Desconectar del cubil
            salida.writeUTF("DESCONECTAR");
            entrada.readUTF();
            skCubil.close();

            // Marcar variable compartida para que Elisabetha lo vea
            // synchronized para exclusion mutua al escribir
            synchronized (ClienteMaestro.lockSecuestro) {
                ClienteMaestro.elisabethaSecuestrada = true;
                ClienteMaestro.lockSecuestro.notifyAll(); // Despertar hilos esperando
            }
            System.out.println("[DRAGON] Elisabetha ha sido secuestrada y llevada al cubil!");

        } catch (IOException e) {
            System.out.println("[DRAGON] No pudo secuestrar a Elisabetha: " + e.getMessage());
        }
    }
}

