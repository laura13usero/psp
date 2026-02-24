import java.io.*;
import java.net.Socket;
import java.util.Random;

/**
 * HILO DRAGON - CASO E: Ataca servidores existentes (como Caso C).
 * Cuando ataca Mercado o Taberna, esos servidores envian SOCORRO al Porton
 * (comunicacion servidor->servidor, ver HiloMercado y HiloTaberna).
 */
public class HiloDragon extends Thread {
    private Random random = new Random();

    @Override
    public void run() {
        System.out.println("[DRAGON] El Dragon Carmesi mora en las montanas...");
        try { Thread.sleep(25000); } catch (InterruptedException e) { }

        while (!ClienteMaestro.simulacionTerminada && !ClienteMaestro.dragonDerrotado) {
            int lugar = random.nextInt(3);
            ClienteMaestro.dragonAtacando = true;
            System.out.println("[DRAGON] *** EL DRAGON CARMESI ATACA! ***");

            switch (lugar) {
                case 0: atacarLugar(ClienteMaestro.PUERTO_MERCADO, "MERCADO"); break;
                case 1: atacarLugar(ClienteMaestro.PUERTO_PORTON, "PORTON"); break;
                case 2: atacarLugar(ClienteMaestro.PUERTO_TABERNA, "TABERNA"); break;
            }

            try { Thread.sleep(10000); } catch (InterruptedException e) { }
            ClienteMaestro.dragonAtacando = false;

            if (ClienteMaestro.dragonDerrotado) break;

            int sueno = 20000 + random.nextInt(20000);
            System.out.println("[DRAGON] Se retira " + (sueno / 1000) + " seg...");
            try { Thread.sleep(sueno); } catch (InterruptedException e) { }
        }
        System.out.println("[DRAGON] Fin del Dragon.");
    }

    private void atacarLugar(int puerto, String nombre) {
        try {
            Socket sk = new Socket(ClienteMaestro.HOST, puerto);
            DataOutputStream sal = new DataOutputStream(sk.getOutputStream());
            DataInputStream ent = new DataInputStream(sk.getInputStream());
            sal.writeUTF("ATAQUE_DRAGON");
            ent.readUTF();
            System.out.println("[DRAGON] Ataca " + nombre + "!");
            sal.writeUTF("DESCONECTAR"); ent.readUTF(); sk.close();
        } catch (IOException e) {
            System.out.println("[DRAGON] No pudo atacar " + nombre);
        }
    }
}

