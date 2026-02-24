import java.io.*;
import java.net.Socket;
import java.util.Random;

/**
 * NUEVO: HILO DRAGON - CASO C: Dragon que destruye los servidores existentes.
 *
 * El dragon es un hilo que periodicamente se conecta via socket a los servidores
 * (Mercado, Porton, Taberna) y envia ATAQUE_DRAGON para destruirlos temporalmente.
 *
 * Cada 20-40 segundos elige un lugar aleatorio para atacar.
 * Cuando ataca, el servidor marca destruido=true durante 20 segundos.
 * Los clientes que intenten usar ese servidor reciben "LUGAR_DESTRUIDO".
 */
public class HiloDragon extends Thread {
    private Random random = new Random();

    @Override
    public void run() {
        System.out.println("[DRAGON] El Dragon Carmesi mora en las montanas...");

        // El dragon espera un poco antes de empezar a atacar
        try { Thread.sleep(25000); } catch (InterruptedException e) { }

        while (!ClienteMaestro.simulacionTerminada && !ClienteMaestro.dragonDerrotado) {
            // Elegir lugar aleatorio para atacar: 0=Mercado, 1=Porton, 2=Taberna
            int lugar = random.nextInt(3);

            ClienteMaestro.dragonAtacando = true;
            System.out.println("[DRAGON] *** EL DRAGON CARMESI DESPIERTA Y ATACA! ***");

            switch (lugar) {
                case 0: atacarLugar(ClienteMaestro.PUERTO_MERCADO, "MERCADO"); break;
                case 1: atacarLugar(ClienteMaestro.PUERTO_PORTON, "PORTON"); break;
                case 2: atacarLugar(ClienteMaestro.PUERTO_TABERNA, "TABERNA"); break;
            }

            // El dragon permanece atacando 10 segundos antes de retirarse
            try { Thread.sleep(10000); } catch (InterruptedException e) { }
            ClienteMaestro.dragonAtacando = false;

            if (ClienteMaestro.dragonDerrotado) break;

            // Dormir antes de volver a atacar (20-40 seg)
            int sueno = 20000 + random.nextInt(20000);
            System.out.println("[DRAGON] Se retira a las montanas. Volvera en " + (sueno/1000) + "s...");
            try { Thread.sleep(sueno); } catch (InterruptedException e) { }
        }

        System.out.println("[DRAGON] El Dragon Carmesi ha sido derrotado o ha huido.");
    }

    /**
     * Atacar un lugar: conectar por socket y enviar ATAQUE_DRAGON.
     * Patron identico a como un personaje visita el mercado/porton.
     */
    private void atacarLugar(int puerto, String nombreLugar) {
        try {
            Socket sk = new Socket(ClienteMaestro.HOST, puerto);
            DataOutputStream salida = new DataOutputStream(sk.getOutputStream());
            DataInputStream entrada = new DataInputStream(sk.getInputStream());

            // Enviar ataque (el servidor procesara este comando en su switch)
            salida.writeUTF("ATAQUE_DRAGON");
            String respuesta = entrada.readUTF(); // "LUGAR_DESTRUIDO"
            System.out.println("[DRAGON] Ataca " + nombreLugar + "! Respuesta: " + respuesta);

            salida.writeUTF("DESCONECTAR");
            entrada.readUTF();
            sk.close();
        } catch (IOException e) {
            System.out.println("[DRAGON] No pudo atacar " + nombreLugar + ": " + e.getMessage());
        }
    }
}

