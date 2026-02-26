import java.io.*;
import java.net.Socket;
import java.util.Random;

/**
 * HILO LANCE - CASO B: Dragon como Servidor Independiente.
 *
 * MODIFICACION: Se anade accion "lucharContraDragon" cuando dragonAparecido==true.
 * Lance se conecta al ServidorDragon (puerto 5003) y envia LUCHAR + "LANCE".
 * El servidor usa BARRERA: si Elisabetha ya esta, ambos luchan juntos.
 * Si no, Lance espera con wait() hasta que ella llegue.
 */
public class HiloLance extends Thread {
    private int nivelChispa = 0;
    private boolean conoceAElisabetha = false;
    private boolean chispa100 = false;
    private Random random = new Random();

    @Override
    public void run() {
        System.out.println("[LANCE] Comienza la simulacion. Chispa: " + nivelChispa);
        while (!ClienteMaestro.simulacionTerminada) {
            // NUEVO: Si el dragon aparecio y ya conoce a Elisabetha, ir a luchar
            if (ClienteMaestro.dragonAparecido && conoceAElisabetha) {
                lucharContraDragon();
                continue;
            }

            synchronized (ClienteMaestro.lockLance) {
                if (ClienteMaestro.hayPeticionAlquimistaL) { procesarAlquimista(); }
            }
            if (chispa100) {
                System.out.println("[LANCE] Chispa 100! Espera a Elisabetha...");
                esperarElisabethaEnTaberna();
                break;
            }

            int accion = random.nextInt(3);
            switch (accion) {
                case 0: hablarConCompaneros(); break;
                case 1: desafiarDuelo(); break;
                case 2: realizarGuardia(); break;
            }

            synchronized (ClienteMaestro.lockLance) { ClienteMaestro.chispaLance = nivelChispa; }
            if (nivelChispa >= 100 && conoceAElisabetha) { nivelChispa = 100; chispa100 = true; }
        }
        System.out.println("[LANCE] *** SIMULACION TERMINADA ***");
    }

    // =========================================================================
    // NUEVO: Luchar contra el Dragon (BARRERA con Elisabetha)
    // =========================================================================
    private void lucharContraDragon() {
        System.out.println("[LANCE] *** El Dragon aparecio! Cabalga a enfrentarlo junto a Elisabetha! ***");
        try {
            Socket skDragon = new Socket(ClienteMaestro.HOST, ClienteMaestro.PUERTO_DRAGON);
            DataOutputStream salida = new DataOutputStream(skDragon.getOutputStream());
            DataInputStream entrada = new DataInputStream(skDragon.getInputStream());

            salida.writeUTF("LUCHAR");
            salida.writeUTF("LANCE");

            System.out.println("[LANCE] Esperando a Elisabetha para luchar juntos...");
            boolean fueVictoria = entrada.readBoolean();
            String resultado = entrada.readUTF();
            int cambioChispa = entrada.readInt();

            if (fueVictoria) {
                nivelChispa += 50;
                System.out.println("[LANCE] *** VICTORIA! +50 chispa! Cabeza del dragon como trofeo! ***");
                System.out.println("[LANCE] Chispa: " + nivelChispa);
            } else {
                if (!chispa100) { nivelChispa = Math.max(0, nivelChispa - 20); }
                System.out.println("[LANCE] Victoriosos pero MALHERIDOS. -20 chispa: " + nivelChispa);
            }

            salida.writeUTF("DESCONECTAR"); entrada.readUTF();
            skDragon.close();
            ClienteMaestro.dragonAparecido = false;

        } catch (IOException e) {
            System.out.println("[LANCE] No pudo llegar al dragon: " + e.getMessage());
        }
    }

    // =========================================================================
    // Metodos identicos al original
    // =========================================================================
    private void hablarConCompaneros() {
        System.out.println("[LANCE] Habla con companeros...");
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
                    if ("OFENSA_ELISABETHA".equals(msg)) { realizarDueloConCompanero(nc); }
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
        System.out.println("[LANCE] Entrena con su espada.");
        try { Thread.sleep(3000); } catch (InterruptedException e) { }
    }

    private void realizarDueloConCompanero(String nc) {
        System.out.println("[LANCE] Duelo con " + nc + "!");
        try { Thread.sleep(5000); } catch (InterruptedException e) { }
        if (random.nextInt(100) < 20) {
            if (!chispa100) { nivelChispa = Math.max(0, nivelChispa - 5); }
            System.out.println("[LANCE] Vence pero dana a " + nc + ". Chispa: " + nivelChispa);
        } else {
            nivelChispa = conoceAElisabetha ? nivelChispa + 7 : Math.min(50, nivelChispa + 7);
            System.out.println("[LANCE] Vence sin dano. Chispa: " + nivelChispa);
        }
    }

    private void realizarGuardia() {
        if (random.nextBoolean()) { vigilarPorton(); } else { vigilarTaberna(); }
    }

    private void vigilarPorton() {
        System.out.println("[LANCE] Vigila el Porton Norte...");
        try {
            Socket sk = new Socket(ClienteMaestro.HOST, ClienteMaestro.PUERTO_PORTON);
            DataOutputStream sal = new DataOutputStream(sk.getOutputStream());
            DataInputStream ent = new DataInputStream(sk.getInputStream());
            sal.writeUTF("VIGILAR_PORTON"); sal.writeUTF("Lance");
            ent.readUTF(); ent.readUTF(); ent.readUTF();
            sal.writeUTF("DESCONECTAR"); ent.readUTF(); sk.close();
            try { Thread.sleep(5000); } catch (InterruptedException e) { }
        } catch (IOException e) { System.out.println("[LANCE] Porton inaccesible."); }
    }

    private void vigilarTaberna() {
        System.out.println("[LANCE] Vigila la Taberna...");
        try {
            Socket sk = new Socket(ClienteMaestro.HOST, ClienteMaestro.PUERTO_TABERNA);
            DataOutputStream sal = new DataOutputStream(sk.getOutputStream());
            DataInputStream ent = new DataInputStream(sk.getInputStream());
            sal.writeUTF("ENTRAR"); sal.writeUTF("LANCE"); ent.readUTF();
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
            sal.writeUTF("ESPERAR_AL_OTRO"); sal.writeUTF("LANCE"); ent.readUTF();
            System.out.println("[LANCE] *** FINAL FELIZ ***");
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

