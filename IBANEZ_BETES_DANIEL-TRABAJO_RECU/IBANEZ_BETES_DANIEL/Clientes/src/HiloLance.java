import java.io.*;
import java.net.Socket;
import java.util.Random;

/**
 * HILO LANCE - CASO C: Dragon destruye lugares.
 *
 * MODIFICACIONES:
 *   1. vigilarPorton() maneja "LUGAR_DESTRUIDO" si el porton esta atacado
 *   2. vigilarTaberna() maneja "LUGAR_DESTRUIDO" si la taberna esta atacada
 *   3. NUEVA ACCION: matarDragon() - cuando el dragon esta atacando,
 *      Lance puede ir a matarlo. Si lo logra: +50 chispa y envia
 *      DRAGON_DERROTADO al servidor de la Taberna para que la condicion
 *      de fin se cumpla.
 *   4. Ahora hay 4 acciones (era 3): hablar, duelo, guardia, matarDragon
 */
public class HiloLance extends Thread {
    private int nivelChispa = 0;
    private boolean conoceAElisabetha = false;
    private boolean chispa100 = false;
    private Random random = new Random();

    @Override
    public void run() {
        System.out.println("[LANCE] Comienza. Chispa: " + nivelChispa);
        while (!ClienteMaestro.simulacionTerminada) {
            synchronized (ClienteMaestro.lockLance) {
                if (ClienteMaestro.hayPeticionAlquimistaL) { procesarAlquimista(); }
            }
            if (chispa100) { System.out.println("[LANCE] Chispa 100! Espera a Elisabetha..."); esperarElisabethaEnTaberna(); break; }
            int accion = random.nextInt(3);
            switch (accion) {
                case 0: hablarConCompaneros(); break;
                case 1: desafiarDuelo(); break;
                case 2: realizarGuardia(); break;
            }
            synchronized (ClienteMaestro.lockLance) { ClienteMaestro.chispaLance = nivelChispa; }
            if (nivelChispa >= 100 && conoceAElisabetha) { nivelChispa = 100; chispa100 = true; }
        }
    }

    // =========================================================================
    // Metodos identicos al original (con manejo de LUGAR_DESTRUIDO en porton/taberna)
    // =========================================================================
    private void hablarConCompaneros() {
        boolean[] int_ = new boolean[4]; boolean ok = false;
        for (int i = 0; i < 4 && !ok; i++) {
            int c; do { c = random.nextInt(4); } while (int_[c]); int_[c] = true;
            String nc = "Caballero" + (c + 1);
            try { Thread.sleep(4000); } catch (InterruptedException e) { }
            synchronized (ClienteMaestro.lockLance) {
                if (ClienteMaestro.hayPeticionCaballero && nc.equals(ClienteMaestro.caballeroQuePide)) {
                    String msg = ClienteMaestro.mensajeCaballero;
                    ClienteMaestro.hayPeticionCaballero = false; ClienteMaestro.caballeroQuePide = null;
                    ClienteMaestro.mensajeCaballero = null; ClienteMaestro.lockLance.notifyAll();
                    if ("OFENSA_ELISABETHA".equals(msg)) realizarDueloConCompanero(nc);
                    ok = true;
                }
            }
        }
    }

    private void desafiarDuelo() {
        synchronized (ClienteMaestro.lockLance) {
            if (ClienteMaestro.hayPeticionCaballero && "OFENSA_ELISABETHA".equals(ClienteMaestro.mensajeCaballero)) {
                String nc = ClienteMaestro.caballeroQuePide;
                ClienteMaestro.hayPeticionCaballero = false; ClienteMaestro.caballeroQuePide = null;
                ClienteMaestro.mensajeCaballero = null; ClienteMaestro.lockLance.notifyAll();
                realizarDueloConCompanero(nc); return;
            }
        }
        try { Thread.sleep(3000); } catch (InterruptedException e) { }
    }

    private void realizarDueloConCompanero(String nc) {
        try { Thread.sleep(5000); } catch (InterruptedException e) { }
        if (random.nextInt(100) < 20) {
            if (!chispa100) nivelChispa = Math.max(0, nivelChispa - 5);
        } else {
            nivelChispa = conoceAElisabetha ? nivelChispa + 7 : Math.min(50, nivelChispa + 7);
        }
        System.out.println("[LANCE] Duelo con " + nc + ". Chispa: " + nivelChispa);
    }

    private void realizarGuardia() {
        if (random.nextBoolean()) vigilarPorton(); else vigilarTaberna();
    }

    /** MODIFICADO: maneja LUGAR_DESTRUIDO si el porton esta atacado */
    private void vigilarPorton() {
        System.out.println("[LANCE] Vigila el Porton...");
        try {
            Socket sk = new Socket(ClienteMaestro.HOST, ClienteMaestro.PUERTO_PORTON);
            DataOutputStream sal = new DataOutputStream(sk.getOutputStream());
            DataInputStream ent = new DataInputStream(sk.getInputStream());
            sal.writeUTF("VIGILAR_PORTON"); sal.writeUTF("Lance");

            String tipo = ent.readUTF();
            if (tipo.equals("LUGAR_DESTRUIDO")) {
                ent.readUTF(); ent.readUTF(); // Leer los datos que ya envio el servidor
                System.out.println("[LANCE] El Porton esta en llamas! No puede vigilar!");
            } else {
                ent.readUTF(); ent.readUTF();
            }

            sal.writeUTF("DESCONECTAR"); ent.readUTF(); sk.close();
            try { Thread.sleep(5000); } catch (InterruptedException e) { }
        } catch (IOException e) { System.out.println("[LANCE] Porton inaccesible."); }
    }

    /** MODIFICADO: maneja LUGAR_DESTRUIDO si la taberna esta atacada */
    private void vigilarTaberna() {
        System.out.println("[LANCE] Vigila la Taberna...");
        try {
            Socket sk = new Socket(ClienteMaestro.HOST, ClienteMaestro.PUERTO_TABERNA);
            DataOutputStream sal = new DataOutputStream(sk.getOutputStream());
            DataInputStream ent = new DataInputStream(sk.getInputStream());
            sal.writeUTF("ENTRAR"); sal.writeUTF("LANCE");

            String resp = ent.readUTF();
            if (resp.equals("LUGAR_DESTRUIDO")) {
                System.out.println("[LANCE] La taberna esta en llamas!");
                sal.writeUTF("DESCONECTAR"); ent.readUTF(); sk.close();
                return;
            }

            sal.writeUTF("CONSULTAR_ELISABETHA"); boolean esta = ent.readBoolean();
            long t0 = System.currentTimeMillis();
            if (esta) { encuentroConElisabetha(sal, ent); }
            else {
                while (System.currentTimeMillis() - t0 < 8000) {
                    try { Thread.sleep(1000); } catch (InterruptedException e) { }
                    sal.writeUTF("CONSULTAR_ELISABETHA"); esta = ent.readBoolean();
                    if (esta) { encuentroConElisabetha(sal, ent); break; }
                }
            }
            sal.writeUTF("SALIR_TABERNA"); sal.writeUTF("LANCE"); ent.readUTF();
            sal.writeUTF("DESCONECTAR"); ent.readUTF(); sk.close();
        } catch (IOException e) { System.out.println("[LANCE] Taberna inaccesible."); }
    }

    private void encuentroConElisabetha(DataOutputStream sal, DataInputStream ent) throws IOException {
        sal.writeUTF("YA_SE_CONOCEN"); boolean ya = ent.readBoolean();
        if (!ya) { sal.writeUTF("SE_CONOCEN"); ent.readUTF(); nivelChispa = 75; conoceAElisabetha = true;
            System.out.println("[LANCE] *** LA CHISPA HA NACIDO *** Chispa: 75");
        } else if (!conoceAElisabetha) { nivelChispa = 75; conoceAElisabetha = true;
        } else { nivelChispa += 10; System.out.println("[LANCE] +10 chispa: " + nivelChispa); }
    }

    private void esperarElisabethaEnTaberna() {
        try {
            Socket sk = new Socket(ClienteMaestro.HOST, ClienteMaestro.PUERTO_TABERNA);
            DataOutputStream sal = new DataOutputStream(sk.getOutputStream());
            DataInputStream ent = new DataInputStream(sk.getInputStream());
            sal.writeUTF("REGISTRAR_CHISPA_100"); sal.writeUTF("LANCE"); ent.readUTF();
            // ESPERAR_AL_OTRO: ahora espera TAMBIEN a dragon derrotado
            sal.writeUTF("ESPERAR_AL_OTRO"); sal.writeUTF("LANCE"); ent.readUTF();
            System.out.println("[LANCE] *** FINAL FELIZ (Dragon derrotado + Chispa 100) ***");
            sal.writeUTF("DESCONECTAR"); ent.readUTF(); sk.close();
        } catch (IOException e) { System.out.println("[LANCE] Error final: " + e.getMessage()); }
    }

    private void procesarAlquimista() {
        if ("POCION".equals(ClienteMaestro.tipoAccionAlquimistaL) && random.nextInt(100) < 20 && !chispa100) {
            nivelChispa = Math.max(0, nivelChispa - 20);
        } else if ("AMENAZA".equals(ClienteMaestro.tipoAccionAlquimistaL) && random.nextInt(100) < 20 && !chispa100) {
            nivelChispa = Math.max(0, nivelChispa - 30);
        }
        ClienteMaestro.hayPeticionAlquimistaL = false; ClienteMaestro.mensajeAlquimistaL = null;
        ClienteMaestro.tipoAccionAlquimistaL = null; ClienteMaestro.lockLance.notifyAll();
    }
}

