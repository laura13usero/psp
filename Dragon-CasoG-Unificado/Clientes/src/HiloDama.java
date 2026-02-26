import java.io.*;
import java.net.Socket;
import java.util.Random;

/**
 * HILO DAMA - CASO G: Dragon Local Synchronized.
 *
 * MODIFICACION: Cuando dragonAtacando == true, la Dama deja de intentar
 * hablar con Elisabetha y EVACUA por el Porton Norte (Escenario 10).
 *
 * PATRON: Cambio dinamico de comportamiento segun flag volatile.
 * La Dama se conecta por socket al Porton Norte y envia "EVACUAR".
 */
public class HiloDama extends Thread {
    private String nombreDama;
    private Random random = new Random();
    public HiloDama(int n) { this.nombreDama = "Dama" + n; }

    @Override
    public void run() {
        while (!ClienteMaestro.simulacionTerminada) {
            // NUEVO: Si el dragon esta atacando, evacuar en vez de comportamiento normal
            if (ClienteMaestro.dragonAtacando) {
                evacuar();
                continue; // Volver al inicio del bucle
            }

            // Comportamiento normal (identico al original)
            if (random.nextInt(2) == 0) {
                String[] l = {"montar a caballo","practicar esgrima","enterarse de rumores"};
                System.out.println("[" + nombreDama + "] " + l[random.nextInt(l.length)]);
                try { Thread.sleep(5000); } catch (InterruptedException e) { }
            } else {
                confesarAElisabetha();
            }
        }
    }

    /**
     * NUEVO: Evacuar por el Porton Norte.
     * Se conecta por SOCKET al servidor del Porton y envia "EVACUAR".
     * Patron identico a como un caballero vigila el porton.
     */
    private void evacuar() {
        System.out.println("[" + nombreDama + "] *** EL DRAGON ATACA! Evacua por el Porton! ***");
        try {
            Socket skPorton = new Socket(ClienteMaestro.HOST, ClienteMaestro.PUERTO_PORTON);
            DataOutputStream salida = new DataOutputStream(skPorton.getOutputStream());
            DataInputStream entrada = new DataInputStream(skPorton.getInputStream());

            // Enviar comando EVACUAR al servidor del Porton
            salida.writeUTF("EVACUAR");
            salida.writeUTF(nombreDama);
            String respuesta = entrada.readUTF();
            System.out.println("[" + nombreDama + "] Porton dice: " + respuesta);

            salida.writeUTF("DESCONECTAR");
            entrada.readUTF();
            skPorton.close();

            // Esperar fuera de la ciudad hasta que pase el peligro
            try { Thread.sleep(5000); } catch (InterruptedException e) { }

        } catch (IOException e) {
            System.out.println("[" + nombreDama + "] No puede llegar al Porton! Huye por los tejados!");
            try { Thread.sleep(3000); } catch (InterruptedException e2) { }
        }
    }

    // Confesar a Elisabetha (identico al original)
    private void confesarAElisabetha() {
        String msg = random.nextBoolean() ? "CONFIDENCIA" : (random.nextBoolean() ? "RUMOR_LANCE" : "INVITACION_BAILE");
        synchronized (ClienteMaestro.lockElisabetha) {
            long t0 = System.currentTimeMillis();
            ClienteMaestro.damaQuePide = nombreDama; ClienteMaestro.mensajeDama = msg;
            ClienteMaestro.hayPeticionDama = true;
            while (System.currentTimeMillis()-t0 < 20000 && ClienteMaestro.hayPeticionDama) {
                try { ClienteMaestro.lockElisabetha.wait(2000); } catch (InterruptedException e) { }
            }
            if (ClienteMaestro.hayPeticionDama) {
                ClienteMaestro.hayPeticionDama = false; ClienteMaestro.damaQuePide = null;
                ClienteMaestro.mensajeDama = null;
            }
        }
    }
}

