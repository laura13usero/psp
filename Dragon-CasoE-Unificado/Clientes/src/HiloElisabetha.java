import java.io.*;
import java.net.Socket;
import java.util.Random;

/** HILO ELISABETHA - CASO E: maneja MERCADO_ATACADO y LUGAR_DESTRUIDO. */
public class HiloElisabetha extends Thread {
    private int nivelChispa = 0;
    private boolean conoceALance = false;
    private boolean chispa100 = false;
    private Random random = new Random();

    @Override
    public void run() {
        while (!ClienteMaestro.simulacionTerminada) {
            synchronized (ClienteMaestro.lockElisabetha) {
                if (ClienteMaestro.hayPeticionAlquimistaE) { procesarAlquimista(); }
            }
            if (chispa100) { esperarLanceEnTaberna(); break; }
            int accion = random.nextInt(4);
            switch (accion) {
                case 0: atenderDamas(); break;
                case 1: asistirBaile(); break;
                case 2: leerPergaminos(); break;
                case 3: escaparseALugar(); break;
            }
            synchronized (ClienteMaestro.lockElisabetha) { ClienteMaestro.chispaElisabetha = nivelChispa; }
            if (nivelChispa >= 100 && conoceALance) { nivelChispa = 100; chispa100 = true; }
        }
    }

    private void atenderDamas() {
        boolean[] int_ = new boolean[4]; boolean ok = false;
        for (int i = 0; i < 4 && !ok; i++) {
            int d; do { d = random.nextInt(4); } while (int_[d]); int_[d] = true;
            try { Thread.sleep(4000); } catch (InterruptedException e) { }
            synchronized (ClienteMaestro.lockElisabetha) {
                if (ClienteMaestro.hayPeticionDama && ("Dama"+(d+1)).equals(ClienteMaestro.damaQuePide)) {
                    String msg = ClienteMaestro.mensajeDama;
                    ClienteMaestro.hayPeticionDama = false; ClienteMaestro.damaQuePide = null;
                    ClienteMaestro.mensajeDama = null; ClienteMaestro.lockElisabetha.notifyAll();
                    if ("RUMOR_LANCE".equals(msg) && !chispa100) nivelChispa = Math.max(0, nivelChispa - 5);
                    else if ("INVITACION_BAILE".equals(msg) && random.nextInt(100) < 20) asistirBaile();
                    ok = true;
                }
            }
        }
    }

    private void asistirBaile() {
        try { Thread.sleep(5000); } catch (InterruptedException e) { }
        if (!chispa100) nivelChispa = Math.max(0, nivelChispa - 5);
    }

    private void leerPergaminos() {
        try { Thread.sleep(5000); } catch (InterruptedException e) { }
        if (chispa100) return;
        if (random.nextBoolean()) nivelChispa = Math.max(0, nivelChispa - 7);
        else nivelChispa = conoceALance ? nivelChispa + 5 : Math.min(30, nivelChispa + 5);
    }

    private void escaparseALugar() {
        if (random.nextBoolean()) visitarMercado(); else visitarTaberna();
    }

    private void visitarMercado() {
        try {
            Socket sk = new Socket(ClienteMaestro.HOST, ClienteMaestro.PUERTO_MERCADO);
            DataOutputStream sal = new DataOutputStream(sk.getOutputStream());
            DataInputStream ent = new DataInputStream(sk.getInputStream());
            sal.writeUTF("VISITAR_MERCADO"); sal.writeUTF("Elisabetha");
            String estado = ent.readUTF();
            if (estado.equals("MERCADO_ATACADO")) {
                System.out.println("[ELISABETHA] Mercado en llamas! Huye!");
            } else {
                int n = ent.readInt(); String[] p = new String[n];
                for (int i = 0; i < n; i++) p[i] = ent.readUTF();
                sal.writeInt(random.nextInt(n)); ent.readUTF();
            }
            sal.writeUTF("DESCONECTAR"); ent.readUTF(); sk.close();
            try { Thread.sleep(5000); } catch (InterruptedException e) { }
        } catch (IOException e) { }
    }

    private void visitarTaberna() {
        try {
            Socket sk = new Socket(ClienteMaestro.HOST, ClienteMaestro.PUERTO_TABERNA);
            DataOutputStream sal = new DataOutputStream(sk.getOutputStream());
            DataInputStream ent = new DataInputStream(sk.getInputStream());
            sal.writeUTF("ENTRAR"); sal.writeUTF("ELISABETHA");
            String resp = ent.readUTF();
            if (resp.equals("LUGAR_DESTRUIDO")) {
                sal.writeUTF("DESCONECTAR"); ent.readUTF(); sk.close(); return;
            }
            sal.writeUTF("CONSULTAR_LANCE"); boolean esta = ent.readBoolean();
            long t0 = System.currentTimeMillis();
            if (esta) { encuentroConLance(sal, ent); }
            else { while (System.currentTimeMillis()-t0<5000) { try{Thread.sleep(1000);}catch(InterruptedException e){}sal.writeUTF("CONSULTAR_LANCE");esta=ent.readBoolean();if(esta){encuentroConLance(sal,ent);break;}}}
            sal.writeUTF("SALIR_TABERNA"); sal.writeUTF("ELISABETHA"); ent.readUTF();
            sal.writeUTF("DESCONECTAR"); ent.readUTF(); sk.close();
        } catch (IOException e) { }
    }

    private void encuentroConLance(DataOutputStream sal, DataInputStream ent) throws IOException {
        sal.writeUTF("YA_SE_CONOCEN"); boolean ya = ent.readBoolean();
        if (!ya) { sal.writeUTF("SE_CONOCEN"); ent.readUTF(); nivelChispa = 75; conoceALance = true; }
        else if (!conoceALance) { nivelChispa = 75; conoceALance = true; }
        else { nivelChispa += 10; }
    }

    private void esperarLanceEnTaberna() {
        try {
            Socket sk = new Socket(ClienteMaestro.HOST, ClienteMaestro.PUERTO_TABERNA);
            DataOutputStream sal = new DataOutputStream(sk.getOutputStream());
            DataInputStream ent = new DataInputStream(sk.getInputStream());
            sal.writeUTF("REGISTRAR_CHISPA_100"); sal.writeUTF("ELISABETHA"); ent.readUTF();
            sal.writeUTF("ESPERAR_AL_OTRO"); sal.writeUTF("ELISABETHA"); ent.readUTF();
            sal.writeUTF("DESCONECTAR"); ent.readUTF(); sk.close();
        } catch (IOException e) { }
    }

    private void procesarAlquimista() {
        if ("POCION".equals(ClienteMaestro.mensajeAlquimistaE) && random.nextInt(100) < 30 && !chispa100)
            nivelChispa = Math.max(0, nivelChispa - 20);
        ClienteMaestro.hayPeticionAlquimistaE = false; ClienteMaestro.mensajeAlquimistaE = null;
        ClienteMaestro.lockElisabetha.notifyAll();
    }
}

