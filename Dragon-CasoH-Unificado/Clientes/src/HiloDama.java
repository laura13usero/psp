import java.io.*;
import java.net.Socket;
import java.util.Random;

/** HILO DAMA - CASO H: Evacua por Porton cuando dragonAtacando. */
public class HiloDama extends Thread {
    private String nombreDama; private Random random = new Random();
    public HiloDama(int n) { this.nombreDama = "Dama" + n; }
    @Override
    public void run() {
        while (!ClienteMaestro.simulacionTerminada) {
            if (ClienteMaestro.dragonAtacando) { evacuar(); continue; }
            if (random.nextInt(2) == 0) {
                try { Thread.sleep(5000); } catch (InterruptedException e) { }
            } else { confesarAElisabetha(); }
        }
    }
    private void evacuar() {
        System.out.println("[" + nombreDama + "] *** DRAGON! Evacua por el Porton! ***");
        try {
            Socket sk = new Socket(ClienteMaestro.HOST, ClienteMaestro.PUERTO_PORTON);
            DataOutputStream sal = new DataOutputStream(sk.getOutputStream());
            DataInputStream ent = new DataInputStream(sk.getInputStream());
            sal.writeUTF("EVACUAR"); sal.writeUTF(nombreDama); ent.readUTF();
            sal.writeUTF("DESCONECTAR"); ent.readUTF(); sk.close();
            try { Thread.sleep(5000); } catch (InterruptedException e) { }
        } catch (IOException e) { try { Thread.sleep(3000); } catch (InterruptedException e2) { } }
    }
    private void confesarAElisabetha() {
        String msg = random.nextBoolean() ? "CONFIDENCIA" : (random.nextBoolean() ? "RUMOR_LANCE" : "INVITACION_BAILE");
        synchronized (ClienteMaestro.lockElisabetha) {
            long t0 = System.currentTimeMillis(); ClienteMaestro.damaQuePide = nombreDama; ClienteMaestro.mensajeDama = msg; ClienteMaestro.hayPeticionDama = true;
            while (System.currentTimeMillis()-t0 < 20000 && ClienteMaestro.hayPeticionDama) { try { ClienteMaestro.lockElisabetha.wait(2000); } catch (InterruptedException e) { } }
            if (ClienteMaestro.hayPeticionDama) { ClienteMaestro.hayPeticionDama = false; ClienteMaestro.damaQuePide = null; ClienteMaestro.mensajeDama = null; }
        }
    }
}

