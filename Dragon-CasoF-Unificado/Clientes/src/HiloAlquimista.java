import java.io.*;
import java.net.Socket;
import java.util.Random;

/**
 * HILO ALQUIMISTA - CASO F: MODIFICADO para curar al Dragon.
 *
 * NUEVA ACCION: curarDragon() (20% de probabilidad).
 * El alquimista se conecta al ServidorDragon (puerto 5003) y envia
 * CURAR_DRAGON + curacion(int). Esto SUBE la vida del dragon.
 *
 * PATRON ANTAGONICO: Mientras Lance ataca al dragon quitandole vida,
 * los alquimistas lo curan subiendole vida. Ambos compiten por el
 * recurso "vidaDragon" en ControlDragon (synchronized).
 *
 * Probabilidades MODIFICADAS:
 *   50% estudiar calderos (era 60%)
 *   15% visitar Elisabetha (era 20%)
 *   15% visitar Lance (era 20%)
 *   20% curar al Dragon (NUEVO)
 */
public class HiloAlquimista extends Thread {
    private String nombreAlq;
    private Random random = new Random();
    private int pocionesE = 0, pocionesL = 0;
    private int pocionesDragon = 0; // NUEVO: pociones para curar al dragon

    public HiloAlquimista(int n) { this.nombreAlq = "Alquimista" + n; }

    @Override
    public void run() {
        estudiarCalderos();
        while (!ClienteMaestro.simulacionTerminada) {
            int a = random.nextInt(100);
            // MODIFICADO: nuevas probabilidades
            if (a < 50) {
                estudiarCalderos();       // 50%
            } else if (a < 65) {
                visitarElisabetha();       // 15%
            } else if (a < 80) {
                visitarLance();            // 15%
            } else {
                curarDragon();             // 20% - NUEVA ACCION
            }
        }
    }

    // MODIFICADO: Estudiar calderos ahora tambien puede crear pocion para el dragon
    private void estudiarCalderos() {
        System.out.println("[" + nombreAlq + "] Estudia calderos...");
        try { Thread.sleep(30000); } catch (InterruptedException e) { }
        int r = random.nextInt(100);
        if (r < 25) {
            pocionesE++;
            System.out.println("[" + nombreAlq + "] Crea pocion anti-Elisabetha. Total: " + pocionesE);
        } else if (r < 50) {
            pocionesL++;
            System.out.println("[" + nombreAlq + "] Crea pocion anti-Lance. Total: " + pocionesL);
        } else if (r < 70) {
            pocionesDragon++; // NUEVO: pocion para curar al dragon
            System.out.println("[" + nombreAlq + "] Crea pocion CURATIVA para el Dragon! Total: " + pocionesDragon);
        } else {
            System.out.println("[" + nombreAlq + "] Fracasa en la pocion.");
        }
    }

    // =========================================================================
    // NUEVA ACCION: Curar al Dragon via socket
    // =========================================================================
    /**
     * El alquimista se conecta al ServidorDragon y envia CURAR_DRAGON + curacion.
     * Necesita al menos 1 pocion de curacion en su alacena.
     *
     * Patron: mismo socket que Lance usa para ATACAR, pero con comando CURAR_DRAGON.
     * ControlDragon.curar() (synchronized) suma vida al dragon.
     * Mientras, Lance puede estar atacando en otra conexion -> COMPETENCIA.
     */
    private void curarDragon() {
        if (ClienteMaestro.dragonDerrotado) {
            System.out.println("[" + nombreAlq + "] El dragon ya murio. No puede curarlo.");
            try { Thread.sleep(3000); } catch (InterruptedException e) { }
            return;
        }

        if (pocionesDragon <= 0) {
            System.out.println("[" + nombreAlq + "] No tiene pociones curativas. Se lamenta.");
            try { Thread.sleep(3000); } catch (InterruptedException e) { }
            return;
        }

        System.out.println("[" + nombreAlq + "] *** Va a curar al Dragon! ***");
        try {
            Socket sk = new Socket(ClienteMaestro.HOST, ClienteMaestro.PUERTO_DRAGON);
            DataOutputStream sal = new DataOutputStream(sk.getOutputStream());
            DataInputStream ent = new DataInputStream(sk.getInputStream());

            int curacion = 30 + random.nextInt(21); // Cura entre 30-50 puntos
            sal.writeUTF("CURAR_DRAGON");
            sal.writeInt(curacion);
            sal.writeUTF(nombreAlq);

            int vidaRestante = ent.readInt();
            pocionesDragon--;
            System.out.println("[" + nombreAlq + "] Cura al Dragon +" + curacion
                + ". Vida Dragon: " + vidaRestante + ". Pociones restantes: " + pocionesDragon);

            sal.writeUTF("DESCONECTAR"); ent.readUTF(); sk.close();
            try { Thread.sleep(5000); } catch (InterruptedException e) { }

        } catch (IOException e) {
            System.out.println("[" + nombreAlq + "] No pudo llegar al dragon.");
        }
    }

    // =========================================================================
    // Metodos originales (identicos)
    // =========================================================================
    private void visitarElisabetha() {
        if (pocionesE <= 0) { try { Thread.sleep(3000); } catch (InterruptedException e) { } return; }
        try { Thread.sleep(5000); } catch (InterruptedException e) { }
        synchronized (ClienteMaestro.lockElisabetha) {
            ClienteMaestro.hayPeticionAlquimistaE = true; ClienteMaestro.mensajeAlquimistaE = "POCION";
            long t0 = System.currentTimeMillis();
            while (ClienteMaestro.hayPeticionAlquimistaE && System.currentTimeMillis()-t0 < 10000) {
                try { ClienteMaestro.lockElisabetha.wait(2000); } catch (InterruptedException e) { }
            }
            pocionesE--;
            if (ClienteMaestro.hayPeticionAlquimistaE) { ClienteMaestro.hayPeticionAlquimistaE = false; ClienteMaestro.mensajeAlquimistaE = null; }
        }
    }

    private void visitarLance() {
        if (random.nextInt(100) < 80) {
            if (pocionesL <= 0) { try { Thread.sleep(3000); } catch (InterruptedException e) { } return; }
            try { Thread.sleep(7000); } catch (InterruptedException e) { }
            synchronized (ClienteMaestro.lockLance) {
                ClienteMaestro.hayPeticionAlquimistaL = true; ClienteMaestro.tipoAccionAlquimistaL = "POCION"; ClienteMaestro.mensajeAlquimistaL = "POCION";
                long t0 = System.currentTimeMillis();
                while (ClienteMaestro.hayPeticionAlquimistaL && System.currentTimeMillis()-t0 < 15000) { try { ClienteMaestro.lockLance.wait(2000); } catch (InterruptedException e) { } }
                pocionesL--;
                if (ClienteMaestro.hayPeticionAlquimistaL) { ClienteMaestro.hayPeticionAlquimistaL = false; ClienteMaestro.mensajeAlquimistaL = null; ClienteMaestro.tipoAccionAlquimistaL = null; }
            }
        } else {
            try { Thread.sleep(7000); } catch (InterruptedException e) { }
            synchronized (ClienteMaestro.lockLance) {
                ClienteMaestro.hayPeticionAlquimistaL = true; ClienteMaestro.tipoAccionAlquimistaL = "AMENAZA"; ClienteMaestro.mensajeAlquimistaL = "AMENAZA";
                long t0 = System.currentTimeMillis();
                while (ClienteMaestro.hayPeticionAlquimistaL && System.currentTimeMillis()-t0 < 15000) { try { ClienteMaestro.lockLance.wait(2000); } catch (InterruptedException e) { } }
                if (ClienteMaestro.hayPeticionAlquimistaL) { ClienteMaestro.hayPeticionAlquimistaL = false; ClienteMaestro.mensajeAlquimistaL = null; ClienteMaestro.tipoAccionAlquimistaL = null; }
            }
        }
    }
}

