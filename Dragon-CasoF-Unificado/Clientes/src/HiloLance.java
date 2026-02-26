import java.io.*;
import java.net.Socket;
import java.util.Random;

/**
 * HILO LANCE - CASO F: +atacarDragon() via socket al ServidorDragon.
 *
 * NUEVA ACCION: atacarDragon() - Lance se conecta al puerto 5003 y envia
 * ATACAR + dano(int). Recibe contraataque(int) + vidaRestante(int) + vivo(boolean).
 * Si el dragon muere: +50 chispa. Si contraataque: -chispa segun fase.
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
                if (ClienteMaestro.hayPeticionAlquimistaL) procesarAlquimista();
            }
            if (chispa100) { esperarElisabethaEnTaberna(); break; }

            // 4 acciones: hablar, duelo, guardia, atacar dragon
            int accion = random.nextInt(4); // MODIFICADO: era 3, ahora 4
            switch (accion) {
                case 0: hablarConCompaneros(); break;
                case 1: desafiarDuelo(); break;
                case 2: realizarGuardia(); break;
                case 3: atacarDragon(); break; // NUEVA ACCION
            }

            synchronized (ClienteMaestro.lockLance) { ClienteMaestro.chispaLance = nivelChispa; }
            if (nivelChispa >= 100 && conoceAElisabetha) { nivelChispa = 100; chispa100 = true; }
        }
    }

    // =========================================================================
    // NUEVA ACCION: Atacar al Dragon via socket (Productor-Consumidor Antagonico)
    // =========================================================================
    /**
     * Lance se conecta al ServidorDragon (5003) y envia ATACAR + dano.
     * El servidor procesa el dano en ControlDragon.recibirAtaque() (synchronized)
     * y devuelve el contraataque.
     *
     * MIENTRAS Lance ataca, un Alquimista puede estar curando al dragon
     * en otra conexion. Ambos compiten por el recurso "vidaDragon".
     *
     * Si el dragon muere: +50 chispa, dragonDerrotado = true.
     * Si contraataque > 0: Lance pierde esa cantidad de chispa.
     */
    private void atacarDragon() {
        if (ClienteMaestro.dragonDerrotado) {
            System.out.println("[LANCE] El dragon ya esta muerto.");
            try { Thread.sleep(3000); } catch (InterruptedException e) { }
            return;
        }

        System.out.println("[LANCE] *** Va a atacar al Dragon! ***");
        try {
            Socket sk = new Socket(ClienteMaestro.HOST, ClienteMaestro.PUERTO_DRAGON);
            DataOutputStream sal = new DataOutputStream(sk.getOutputStream());
            DataInputStream ent = new DataInputStream(sk.getInputStream());

            // Primero consultar si sigue vivo
            sal.writeUTF("CONSULTAR_VIDA");
            int vidaActual = ent.readInt();
            int fase = ent.readInt();
            boolean vivo = ent.readBoolean();

            if (!vivo) {
                System.out.println("[LANCE] Dragon ya derrotado!");
                ClienteMaestro.dragonDerrotado = true;
                sal.writeUTF("DESCONECTAR"); ent.readUTF(); sk.close();
                return;
            }

            System.out.println("[LANCE] Dragon: vida=" + vidaActual + " fase=" + fase);

            // Atacar con dano aleatorio 10-30
            int dano = 10 + random.nextInt(21);
            sal.writeUTF("ATACAR");
            sal.writeInt(dano);
            sal.writeUTF("Lance");

            // Recibir resultado
            int contraataque = ent.readInt();
            int vidaRestante = ent.readInt();
            boolean sigueVivo = ent.readBoolean();

            if (!sigueVivo) {
                // Dragon muerto: +50 chispa
                nivelChispa += 50;
                ClienteMaestro.dragonDerrotado = true;
                System.out.println("[LANCE] *** DRAGON DERROTADO! +50 chispa! Chispa: " + nivelChispa + " ***");
            } else if (contraataque > 0) {
                // Contraataque: pierde chispa segun la fase del dragon
                if (!chispa100) { nivelChispa = Math.max(0, nivelChispa - contraataque); }
                System.out.println("[LANCE] Ataca con " + dano + ", recibe contraataque -" + contraataque
                    + " chispa. Dragon vida: " + vidaRestante + ". Chispa: " + nivelChispa);
            }

            sal.writeUTF("DESCONECTAR"); ent.readUTF(); sk.close();
            try { Thread.sleep(5000); } catch (InterruptedException e) { }

        } catch (IOException e) {
            System.out.println("[LANCE] No pudo llegar al dragon.");
        }
    }

    // =========================================================================
    // Metodos base (identicos al original)
    // =========================================================================
    private void hablarConCompaneros() {
        boolean[] int_ = new boolean[4]; boolean ok = false;
        for (int i = 0; i < 4 && !ok; i++) {
            int c; do { c = random.nextInt(4); } while (int_[c]); int_[c] = true;
            try { Thread.sleep(4000); } catch (InterruptedException e) { }
            synchronized (ClienteMaestro.lockLance) {
                if (ClienteMaestro.hayPeticionCaballero && ("Caballero"+(c+1)).equals(ClienteMaestro.caballeroQuePide)) {
                    String msg = ClienteMaestro.mensajeCaballero;
                    ClienteMaestro.hayPeticionCaballero = false; ClienteMaestro.caballeroQuePide = null;
                    ClienteMaestro.mensajeCaballero = null; ClienteMaestro.lockLance.notifyAll();
                    if ("OFENSA_ELISABETHA".equals(msg)) realizarDueloConCompanero("Caballero"+(c+1));
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
            if (esta) encuentroConElisabetha(sal, ent);
            else { while (System.currentTimeMillis()-t0<8000) { try{Thread.sleep(1000);}catch(InterruptedException e){}sal.writeUTF("CONSULTAR_ELISABETHA");esta=ent.readBoolean();if(esta){encuentroConElisabetha(sal,ent);break;}}}
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

