import java.io.*;
import java.net.Socket;
import java.util.Random;

/**
 * HILO ELISABETHA - CASO B: Dragon como Servidor Independiente.
 *
 * MODIFICACION: Se anade una 5a accion "lucharContraDragon" que solo se
 * activa cuando dragonAparecido == true. Elisabetha se conecta al
 * ServidorDragon (puerto 5003) y envia LUCHAR + "ELISABETHA".
 * El servidor usa una BARRERA: Elisabetha espera con wait() hasta que
 * Lance tambien se conecte. Entonces ambos luchan juntos.
 * Resultado: +50 chispa (victoria) o -20 chispa (malheridos).
 */
public class HiloElisabetha extends Thread {
    private int nivelChispa = 0;
    private boolean conoceALance = false;
    private boolean chispa100 = false;
    private Random random = new Random();

    @Override
    public void run() {
        System.out.println("[ELISABETHA] Comienza la simulacion. Chispa: " + nivelChispa);
        while (!ClienteMaestro.simulacionTerminada) {
            // NUEVO: Si el dragon aparecio y ya conoce a Lance, ir a luchar
            if (ClienteMaestro.dragonAparecido && conoceALance) {
                lucharContraDragon();
                continue; // Volver al inicio del bucle
            }

            synchronized (ClienteMaestro.lockElisabetha) {
                if (ClienteMaestro.hayPeticionAlquimistaE) { procesarAlquimista(); }
            }
            if (chispa100) {
                System.out.println("[ELISABETHA] Chispa 100! Espera a Lance...");
                esperarLanceEnTaberna();
                break;
            }

            int accion = random.nextInt(4);
            switch (accion) {
                case 0: atenderDamas(); break;
                case 1: asistirBaile(); break;
                case 2: leerPergaminos(); break;
                case 3: escaparseALugar(); break;
            }

            synchronized (ClienteMaestro.lockElisabetha) {
                ClienteMaestro.chispaElisabetha = nivelChispa;
            }
            if (nivelChispa >= 100 && conoceALance) { nivelChispa = 100; chispa100 = true; }
        }
        System.out.println("[ELISABETHA] *** SIMULACION TERMINADA ***");
    }

    // =========================================================================
    // NUEVO: Luchar contra el Dragon (se conecta al ServidorDragon via socket)
    // =========================================================================
    /**
     * Elisabetha se conecta al ServidorDragon (puerto 5003) y envia LUCHAR.
     * El servidor ejecuta ControlDragon.llegarABatalla("ELISABETHA") que es
     * una BARRERA: si Lance no ha llegado aun, Elisabetha queda BLOQUEADA
     * en wait() hasta que Lance tambien envie LUCHAR.
     * Cuando ambos estan, la batalla se resuelve (50/50).
     */
    private void lucharContraDragon() {
        System.out.println("[ELISABETHA] *** El Dragon ha aparecido! Va a luchar junto a Lance! ***");
        try {
            // Conectar al ServidorDragon via socket (mismo patron que visitar mercado)
            Socket skDragon = new Socket(ClienteMaestro.HOST, ClienteMaestro.PUERTO_DRAGON);
            DataOutputStream salida = new DataOutputStream(skDragon.getOutputStream());
            DataInputStream entrada = new DataInputStream(skDragon.getInputStream());

            // Enviar LUCHAR + nombre -> BARRERA en el servidor
            salida.writeUTF("LUCHAR");
            salida.writeUTF("ELISABETHA");

            // --- BLOQUEO: espera aqui hasta que Lance tambien llegue ---
            System.out.println("[ELISABETHA] Esperando a que Lance llegue para luchar juntos...");
            boolean fueVictoria = entrada.readBoolean(); // Se desbloquea cuando ambos luchan
            String resultado = entrada.readUTF();        // "DRAGON_DERROTADO" o "MALHERIDOS"
            int cambioChispa = entrada.readInt();        // +50 o -20

            if (fueVictoria) {
                nivelChispa += 50;
                System.out.println("[ELISABETHA] *** VICTORIA contra el Dragon! +50 chispa! ***");
                System.out.println("[ELISABETHA] Regresan con la cabeza como trofeo. Chispa: " + nivelChispa);
            } else {
                if (!chispa100) { nivelChispa = Math.max(0, nivelChispa - 20); }
                System.out.println("[ELISABETHA] Victoriosos pero MALHERIDOS. -20 chispa: " + nivelChispa);
            }

            salida.writeUTF("DESCONECTAR");
            entrada.readUTF();
            skDragon.close();

            // Resetear flag del dragon
            ClienteMaestro.dragonAparecido = false;

        } catch (IOException e) {
            System.out.println("[ELISABETHA] No pudo llegar al dragon: " + e.getMessage());
        }
    }

    // =========================================================================
    // Metodos identicos al original (sin cambios)
    // =========================================================================
    private void atenderDamas() {
        System.out.println("[ELISABETHA] Atiende a sus Damas del Lazo...");
        boolean[] int_ = new boolean[4]; boolean ok = false;
        for (int i = 0; i < 4 && !ok; i++) {
            int d; do { d = random.nextInt(4); } while (int_[d]); int_[d] = true;
            String nd = "Dama" + (d + 1);
            try { Thread.sleep(4000); } catch (InterruptedException e) { }
            synchronized (ClienteMaestro.lockElisabetha) {
                if (ClienteMaestro.hayPeticionDama && ClienteMaestro.damaQuePide != null
                        && ClienteMaestro.damaQuePide.equals(nd)) {
                    String msg = ClienteMaestro.mensajeDama;
                    ClienteMaestro.hayPeticionDama = false; ClienteMaestro.damaQuePide = null;
                    ClienteMaestro.mensajeDama = null; ClienteMaestro.lockElisabetha.notifyAll();
                    if ("RUMOR_LANCE".equals(msg) && !chispa100) { nivelChispa = Math.max(0, nivelChispa - 5); }
                    else if ("INVITACION_BAILE".equals(msg) && random.nextInt(100) < 20) { asistirBaile(); }
                    ok = true;
                }
            }
        }
    }

    private void asistirBaile() {
        System.out.println("[ELISABETHA] Asiste a un baile...");
        try { Thread.sleep(5000); } catch (InterruptedException e) { }
        if (!chispa100) { nivelChispa = Math.max(0, nivelChispa - 5); }
        System.out.println("[ELISABETHA] Baile aburrido. Chispa: " + nivelChispa);
    }

    private void leerPergaminos() {
        System.out.println("[ELISABETHA] Lee pergaminos...");
        try { Thread.sleep(5000); } catch (InterruptedException e) { }
        if (chispa100) return;
        if (random.nextBoolean()) { nivelChispa = Math.max(0, nivelChispa - 7); }
        else { nivelChispa = conoceALance ? nivelChispa + 5 : Math.min(30, nivelChispa + 5); }
        System.out.println("[ELISABETHA] Pergaminos. Chispa: " + nivelChispa);
    }

    private void escaparseALugar() {
        if (random.nextBoolean()) { visitarMercado(); } else { visitarTaberna(); }
    }

    private void visitarMercado() {
        System.out.println("[ELISABETHA] Va al Mercado...");
        try {
            Socket sk = new Socket(ClienteMaestro.HOST, ClienteMaestro.PUERTO_MERCADO);
            DataOutputStream sal = new DataOutputStream(sk.getOutputStream());
            DataInputStream ent = new DataInputStream(sk.getInputStream());
            sal.writeUTF("VISITAR_MERCADO"); sal.writeUTF("Elisabetha");
            int n = ent.readInt(); String[] p = new String[n];
            for (int i = 0; i < n; i++) p[i] = ent.readUTF();
            sal.writeInt(random.nextInt(n));
            System.out.println("[ELISABETHA] Compra: " + p[0]); ent.readUTF();
            sal.writeUTF("DESCONECTAR"); ent.readUTF(); sk.close();
            try { Thread.sleep(5000); } catch (InterruptedException e) { }
        } catch (IOException e) { System.out.println("[ELISABETHA] Mercado inaccesible."); }
    }

    private void visitarTaberna() {
        System.out.println("[ELISABETHA] Va a la Taberna...");
        try {
            Socket sk = new Socket(ClienteMaestro.HOST, ClienteMaestro.PUERTO_TABERNA);
            DataOutputStream sal = new DataOutputStream(sk.getOutputStream());
            DataInputStream ent = new DataInputStream(sk.getInputStream());
            sal.writeUTF("ENTRAR"); sal.writeUTF("ELISABETHA"); ent.readUTF();
            sal.writeUTF("CONSULTAR_LANCE"); boolean esta = ent.readBoolean();
            long t0 = System.currentTimeMillis();
            if (esta) { encuentroConLance(sal, ent); }
            else {
                while (System.currentTimeMillis() - t0 < 5000) {
                    try { Thread.sleep(1000); } catch (InterruptedException e) { }
                    sal.writeUTF("CONSULTAR_LANCE"); esta = ent.readBoolean();
                    if (esta) { encuentroConLance(sal, ent); break; }
                }
            }
            sal.writeUTF("SALIR_TABERNA"); sal.writeUTF("ELISABETHA"); ent.readUTF();
            sal.writeUTF("DESCONECTAR"); ent.readUTF(); sk.close();
        } catch (IOException e) { System.out.println("[ELISABETHA] Taberna inaccesible."); }
    }

    private void encuentroConLance(DataOutputStream sal, DataInputStream ent) throws IOException {
        sal.writeUTF("YA_SE_CONOCEN"); boolean ya = ent.readBoolean();
        if (!ya) { sal.writeUTF("SE_CONOCEN"); ent.readUTF(); nivelChispa = 75; conoceALance = true;
            System.out.println("[ELISABETHA] *** LA CHISPA HA NACIDO *** Chispa: 75");
        } else if (!conoceALance) { nivelChispa = 75; conoceALance = true;
        } else { nivelChispa += 10; System.out.println("[ELISABETHA] +10 chispa: " + nivelChispa); }
    }

    private void esperarLanceEnTaberna() {
        try {
            Socket sk = new Socket(ClienteMaestro.HOST, ClienteMaestro.PUERTO_TABERNA);
            DataOutputStream sal = new DataOutputStream(sk.getOutputStream());
            DataInputStream ent = new DataInputStream(sk.getInputStream());
            sal.writeUTF("REGISTRAR_CHISPA_100"); sal.writeUTF("ELISABETHA"); ent.readUTF();
            sal.writeUTF("ESPERAR_AL_OTRO"); sal.writeUTF("ELISABETHA"); ent.readUTF();
            System.out.println("[ELISABETHA] *** FINAL FELIZ ***");
            sal.writeUTF("DESCONECTAR"); ent.readUTF(); sk.close();
        } catch (IOException e) { System.out.println("[ELISABETHA] Error final: " + e.getMessage()); }
    }

    private void procesarAlquimista() {
        if ("POCION".equals(ClienteMaestro.mensajeAlquimistaE) && random.nextInt(100) < 30 && !chispa100) {
            nivelChispa = Math.max(0, nivelChispa - 20);
            System.out.println("[ELISABETHA] Engañada por alquimista! Chispa: " + nivelChispa);
        }
        ClienteMaestro.hayPeticionAlquimistaE = false; ClienteMaestro.mensajeAlquimistaE = null;
        ClienteMaestro.lockElisabetha.notifyAll();
    }
}

