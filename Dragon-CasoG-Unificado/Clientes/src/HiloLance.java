import java.io.*;
import java.net.Socket;
import java.util.Random;

/**
 * HILO LANCE - CASO G: Dragon Local Synchronized.
 *
 * MODIFICACIONES respecto al original:
 *   1. NUEVO ATRIBUTO: nivelValor (Escenario 7 de la guia)
 *      - Sube al ganar duelos: +7 valor
 *      - Baja al perder: -3 valor
 *      - Lance necesita valor >= 30 para poder luchar contra el dragon
 *   2. NUEVA ACCION: enfrentarDragon() - cuando dragonAtacando==true Y valor>=30
 *      - Usa synchronized(lockDragon) para lucha cuerpo a cuerpo
 *      - 50/50: victoria (+50 chispa, dragonDerrotado=true) o derrota (-20 chispa)
 */
public class HiloLance extends Thread {
    private int nivelChispa = 0;
    private int nivelValor = 0;      // NUEVO: atributo valor (Escenario 7)
    private boolean conoceAElisabetha = false;
    private boolean chispa100 = false;
    private Random random = new Random();

    @Override
    public void run() {
        System.out.println("[LANCE] Comienza. Chispa: " + nivelChispa + " Valor: " + nivelValor);
        while (!ClienteMaestro.simulacionTerminada) {
            // NUEVO: Si el dragon ataca y Lance tiene suficiente valor, enfrentarlo
            if (ClienteMaestro.dragonAtacando && !ClienteMaestro.dragonDerrotado
                    && conoceAElisabetha && nivelValor >= 30) {
                enfrentarDragon();
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
    // NUEVO: Enfrentar al Dragon (synchronized directo, SIN sockets)
    // =========================================================================
    /**
     * Lance lucha contra el dragon usando synchronized(lockDragon).
     * PATRON: Exclusion mutua sobre lockDragon para asegurar que solo
     * un hilo a la vez lucha contra el dragon.
     *
     * "En el climax de esta gesta, ambos se batiran en duelo contra la bestia.
     *  El resultado es incierto: +50 chispa (victoria) o -20 chispa (malherido)"
     */
    private void enfrentarDragon() {
        System.out.println("[LANCE] *** El dragon esta atacando! Lance lo enfrenta! ***");
        System.out.println("[LANCE] Valor: " + nivelValor + " (necesario >= 30)");

        // synchronized(lockDragon): solo un hilo a la vez lucha contra el dragon
        synchronized (ClienteMaestro.lockDragon) {
            if (ClienteMaestro.dragonDerrotado) {
                System.out.println("[LANCE] El dragon ya fue derrotado.");
                return;
            }

            System.out.println("[LANCE] *** SE BATE EN DUELO CONTRA LA BESTIA ***");
            try { Thread.sleep(5000); } catch (InterruptedException e) { }

            if (random.nextBoolean()) {
                // Victoria: +50 chispa, dragon derrotado
                nivelChispa += 50;
                nivelValor += 20;
                ClienteMaestro.dragonDerrotado = true;
                ClienteMaestro.dragonAtacando = false;
                System.out.println("[LANCE] *** VICTORIA! Abate al dragon! +50 chispa! ***");
                System.out.println("[LANCE] Chispa: " + nivelChispa + " Valor: " + nivelValor);
            } else {
                // Derrota parcial: -20 chispa
                if (!chispa100) { nivelChispa = Math.max(0, nivelChispa - 20); }
                nivelValor = Math.max(0, nivelValor - 10);
                System.out.println("[LANCE] Malherido! -20 chispa. Chispa: " + nivelChispa);
                System.out.println("[LANCE] Valor: " + nivelValor + " (bajo por la derrota)");
            }

            ClienteMaestro.lockDragon.notifyAll();
        }
    }

    // =========================================================================
    // Metodos base (identicos al original, con +valor en duelos)
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
        try { Thread.sleep(3000); } catch (InterruptedException e) { }
    }

    // MODIFICADO: ahora sube nivelValor al ganar
    private void realizarDueloConCompanero(String nc) {
        try { Thread.sleep(5000); } catch (InterruptedException e) { }
        if (random.nextInt(100) < 20) {
            if (!chispa100) nivelChispa = Math.max(0, nivelChispa - 5);
            nivelValor = Math.max(0, nivelValor - 3);  // NUEVO: baja valor
            System.out.println("[LANCE] Vence pero dana a " + nc + ". Chispa:" + nivelChispa + " Valor:" + nivelValor);
        } else {
            nivelChispa = conoceAElisabetha ? nivelChispa + 7 : Math.min(50, nivelChispa + 7);
            nivelValor += 7;  // NUEVO: sube valor
            System.out.println("[LANCE] Vence sin dano. Chispa:" + nivelChispa + " Valor:" + nivelValor);
        }
    }

    private void realizarGuardia() {
        if (random.nextBoolean()) vigilarPorton(); else vigilarTaberna();
    }

    private void vigilarPorton() {
        try {
            Socket sk = new Socket(ClienteMaestro.HOST, ClienteMaestro.PUERTO_PORTON);
            DataOutputStream sal = new DataOutputStream(sk.getOutputStream());
            DataInputStream ent = new DataInputStream(sk.getInputStream());
            sal.writeUTF("VIGILAR_PORTON"); sal.writeUTF("Lance");
            ent.readUTF(); ent.readUTF(); ent.readUTF();
            sal.writeUTF("DESCONECTAR"); ent.readUTF(); sk.close();
            try { Thread.sleep(5000); } catch (InterruptedException e) { }
        } catch (IOException e) { }
    }

    private void vigilarTaberna() {
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
        } catch (IOException e) { }
    }

    private void encuentroConElisabetha(DataOutputStream sal, DataInputStream ent) throws IOException {
        sal.writeUTF("YA_SE_CONOCEN"); boolean ya = ent.readBoolean();
        if (!ya) { sal.writeUTF("SE_CONOCEN"); ent.readUTF(); nivelChispa = 75; conoceAElisabetha = true;
            System.out.println("[LANCE] *** LA CHISPA HA NACIDO *** Chispa: 75");
        } else if (!conoceAElisabetha) { nivelChispa = 75; conoceAElisabetha = true;
        } else { nivelChispa += 10; }
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
        } catch (IOException e) { }
    }

    private void procesarAlquimista() {
        if ("POCION".equals(ClienteMaestro.tipoAccionAlquimistaL) && random.nextInt(100) < 20 && !chispa100)
            nivelChispa = Math.max(0, nivelChispa - 20);
        else if ("AMENAZA".equals(ClienteMaestro.tipoAccionAlquimistaL) && random.nextInt(100) < 20 && !chispa100)
            nivelChispa = Math.max(0, nivelChispa - 30);
        ClienteMaestro.hayPeticionAlquimistaL = false; ClienteMaestro.mensajeAlquimistaL = null;
        ClienteMaestro.tipoAccionAlquimistaL = null; ClienteMaestro.lockLance.notifyAll();
    }
}

