import java.io.*;
import java.net.Socket;
import java.util.Random;

/** HILO LANCE - CASO E: maneja LUGAR_DESTRUIDO + matarDragon + DRAGON_DERROTADO. */
public class HiloLance extends Thread {
    private int nivelChispa = 0;
    private boolean conoceAElisabetha = false;
    private boolean chispa100 = false;
    private Random random = new Random();

    @Override
    public void run() {
        while (!ClienteMaestro.simulacionTerminada) {
            if (ClienteMaestro.dragonAtacando && conoceAElisabetha && !ClienteMaestro.dragonDerrotado) {
                matarDragon(); continue;
            }
            synchronized (ClienteMaestro.lockLance) {
                if (ClienteMaestro.hayPeticionAlquimistaL) procesarAlquimista();
            }
            if (chispa100) { esperarElisabethaEnTaberna(); break; }
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

    /** Matar al dragon (50/50), luego notificar DRAGON_DERROTADO a Taberna */
    private void matarDragon() {
        System.out.println("[LANCE] *** Enfrenta al Dragon! ***");
        try { Thread.sleep(5000); } catch (InterruptedException e) { }
        if (random.nextBoolean()) {
            nivelChispa += 50;
            ClienteMaestro.dragonDerrotado = true;
            ClienteMaestro.dragonAtacando = false;
            System.out.println("[LANCE] *** VICTORIA! +50 chispa! ***");
            // Notificar al servidor Taberna via socket
            try {
                Socket sk = new Socket(ClienteMaestro.HOST, ClienteMaestro.PUERTO_TABERNA);
                DataOutputStream sal = new DataOutputStream(sk.getOutputStream());
                DataInputStream ent = new DataInputStream(sk.getInputStream());
                sal.writeUTF("DRAGON_DERROTADO"); ent.readUTF();
                sal.writeUTF("DESCONECTAR"); ent.readUTF(); sk.close();
            } catch (IOException e) { }
        } else {
            if (!chispa100) nivelChispa = Math.max(0, nivelChispa - 20);
            System.out.println("[LANCE] Malherido. -20 chispa: " + nivelChispa);
        }
    }

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
            String tipo = ent.readUTF();
            if (tipo.equals("LUGAR_DESTRUIDO")) { ent.readUTF(); ent.readUTF(); }
            else { ent.readUTF(); ent.readUTF(); }
            sal.writeUTF("DESCONECTAR"); ent.readUTF(); sk.close();
            try { Thread.sleep(5000); } catch (InterruptedException e) { }
        } catch (IOException e) { }
    }

    private void vigilarTaberna() {
        try {
            Socket sk = new Socket(ClienteMaestro.HOST, ClienteMaestro.PUERTO_TABERNA);
            DataOutputStream sal = new DataOutputStream(sk.getOutputStream());
            DataInputStream ent = new DataInputStream(sk.getInputStream());
            sal.writeUTF("ENTRAR"); sal.writeUTF("LANCE");
            String resp = ent.readUTF();
            if (resp.equals("LUGAR_DESTRUIDO")) { sal.writeUTF("DESCONECTAR"); ent.readUTF(); sk.close(); return; }
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
        if (!ya) { sal.writeUTF("SE_CONOCEN"); ent.readUTF(); nivelChispa = 75; conoceAElisabetha = true; }
        else if (!conoceAElisabetha) { nivelChispa = 75; conoceAElisabetha = true; }
        else { nivelChispa += 10; }
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

