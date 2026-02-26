import java.util.Random;

/**
 * HILO ALQUIMISTA - Raton alquimista malvado (identico al original).
 * 60% estudiar calderos, 20% visitar Elisabetha, 20% visitar Lance.
 * Comunicacion via buzon compartido (lockElisabetha / lockLance + wait/notifyAll).
 */
public class HiloAlquimista extends Thread {
    private int numeroAlq;
    private String nombreAlq;
    private Random random = new Random();
    private int pocionesElisabetha = 0;
    private int pocionesLance = 0;

    public HiloAlquimista(int numero) {
        this.numeroAlq = numero;
        this.nombreAlq = "Alquimista" + numero;
    }

    @Override
    public void run() {
        System.out.println("[" + nombreAlq + "] Comienza. Estudiando calderos...");
        estudiarCalderos();

        while (!ClienteMaestro.simulacionTerminada) {
            int accion = random.nextInt(100);
            if (accion < 60) {
                estudiarCalderos();
            } else if (accion < 80) {
                visitarElisabetha();
            } else {
                visitarLance();
            }
        }
        System.out.println("[" + nombreAlq + "] Simulacion terminada.");
    }

    private void estudiarCalderos() {
        System.out.println("[" + nombreAlq + "] Estudia calderos...");
        try { Thread.sleep(30000); } catch (InterruptedException e) { }

        int resultado = random.nextInt(100);
        if (resultado < 30) {
            pocionesElisabetha++;
            System.out.println("[" + nombreAlq + "] Pocion para Elisabetha! E=" + pocionesElisabetha + " L=" + pocionesLance);
        } else if (resultado < 60) {
            pocionesLance++;
            System.out.println("[" + nombreAlq + "] Pocion para Lance! E=" + pocionesElisabetha + " L=" + pocionesLance);
        } else {
            System.out.println("[" + nombreAlq + "] Fracasa! Pocion explota.");
        }
    }

    private void visitarElisabetha() {
        if (pocionesElisabetha <= 0) {
            System.out.println("[" + nombreAlq + "] Sin pociones para Elisabetha! Se lamenta!");
            try { Thread.sleep(3000); } catch (InterruptedException e) { }
            return;
        }

        System.out.println("[" + nombreAlq + "] Visita a Elisabetha con 'tonico de belleza'...");
        try { Thread.sleep(5000); } catch (InterruptedException e) { }

        synchronized (ClienteMaestro.lockElisabetha) {
            ClienteMaestro.hayPeticionAlquimistaE = true;
            ClienteMaestro.mensajeAlquimistaE = "POCION";

            long inicio = System.currentTimeMillis();
            while (ClienteMaestro.hayPeticionAlquimistaE && System.currentTimeMillis() - inicio < 10000) {
                try { ClienteMaestro.lockElisabetha.wait(2000); } catch (InterruptedException e) { }
            }

            pocionesElisabetha--;
            if (!ClienteMaestro.hayPeticionAlquimistaE) {
                System.out.println("[" + nombreAlq + "] Elisabetha atendida.");
            } else {
                ClienteMaestro.hayPeticionAlquimistaE = false;
                ClienteMaestro.mensajeAlquimistaE = null;
                System.out.println("[" + nombreAlq + "] No pudo afectar a Elisabetha.");
            }
        }
    }

    private void visitarLance() {
        int tipoAccion = random.nextInt(100);
        if (tipoAccion < 80) {
            if (pocionesLance <= 0) {
                System.out.println("[" + nombreAlq + "] Sin pociones para Lance! Se lamenta!");
                try { Thread.sleep(3000); } catch (InterruptedException e) { }
                return;
            }

            System.out.println("[" + nombreAlq + "] Visita a Lance con pocion...");
            try { Thread.sleep(7000); } catch (InterruptedException e) { }

            synchronized (ClienteMaestro.lockLance) {
                ClienteMaestro.hayPeticionAlquimistaL = true;
                ClienteMaestro.tipoAccionAlquimistaL = "POCION";
                ClienteMaestro.mensajeAlquimistaL = "POCION";

                long inicio = System.currentTimeMillis();
                while (ClienteMaestro.hayPeticionAlquimistaL && System.currentTimeMillis() - inicio < 15000) {
                    try { ClienteMaestro.lockLance.wait(2000); } catch (InterruptedException e) { }
                }

                pocionesLance--;
                if (!ClienteMaestro.hayPeticionAlquimistaL) {
                    System.out.println("[" + nombreAlq + "] Lance atendido.");
                } else {
                    ClienteMaestro.hayPeticionAlquimistaL = false;
                    ClienteMaestro.mensajeAlquimistaL = null;
                    ClienteMaestro.tipoAccionAlquimistaL = null;
                    System.out.println("[" + nombreAlq + "] No pudo enganar a Lance.");
                }
            }
        } else {
            System.out.println("[" + nombreAlq + "] Amenaza a Lance con el Frente Norte...");
            try { Thread.sleep(7000); } catch (InterruptedException e) { }

            synchronized (ClienteMaestro.lockLance) {
                ClienteMaestro.hayPeticionAlquimistaL = true;
                ClienteMaestro.tipoAccionAlquimistaL = "AMENAZA";
                ClienteMaestro.mensajeAlquimistaL = "AMENAZA";

                long inicio = System.currentTimeMillis();
                while (ClienteMaestro.hayPeticionAlquimistaL && System.currentTimeMillis() - inicio < 15000) {
                    try { ClienteMaestro.lockLance.wait(2000); } catch (InterruptedException e) { }
                }

                if (!ClienteMaestro.hayPeticionAlquimistaL) {
                    System.out.println("[" + nombreAlq + "] Amenaza a Lance completada.");
                } else {
                    ClienteMaestro.hayPeticionAlquimistaL = false;
                    ClienteMaestro.mensajeAlquimistaL = null;
                    ClienteMaestro.tipoAccionAlquimistaL = null;
                    System.out.println("[" + nombreAlq + "] Lance no se deja amedrentar.");
                }
            }
        }
    }
}

