import java.util.Random;

/**
 * HILO DAMA - Dama del Lazo Perfumado (identica al original).
 * 2 acciones (50/50): realizar labores o confesar a Elisabetha.
 * Comunicacion con Elisabetha via buzon compartido (lockElisabetha + wait/notifyAll).
 */
public class HiloDama extends Thread {
    private int numeroDama;
    private String nombreDama;
    private Random random = new Random();

    public HiloDama(int numero) {
        this.numeroDama = numero;
        this.nombreDama = "Dama" + numero;
    }

    @Override
    public void run() {
        System.out.println("[" + nombreDama + "] Comienza la simulacion.");
        while (!ClienteMaestro.simulacionTerminada) {
            if (random.nextInt(2) == 0) {
                realizarLabores();
            } else {
                confesarAElisabetha();
            }
        }
        System.out.println("[" + nombreDama + "] Simulacion terminada.");
    }

    private void realizarLabores() {
        String[] labores = {"montar a caballo", "practicar esgrima", "enterarse de rumores"};
        String labor = labores[random.nextInt(labores.length)];
        System.out.println("[" + nombreDama + "] Realiza labor: " + labor);
        try { Thread.sleep(5000); } catch (InterruptedException e) { }
    }

    private void confesarAElisabetha() {
        System.out.println("[" + nombreDama + "] Quiere hablar con Elisabetha...");

        String mensaje;
        if (random.nextBoolean()) {
            mensaje = "CONFIDENCIA";
        } else {
            mensaje = random.nextBoolean() ? "RUMOR_LANCE" : "INVITACION_BAILE";
        }

        synchronized (ClienteMaestro.lockElisabetha) {
            long inicio = System.currentTimeMillis();
            ClienteMaestro.damaQuePide = nombreDama;
            ClienteMaestro.mensajeDama = mensaje;
            ClienteMaestro.hayPeticionDama = true;

            while (System.currentTimeMillis() - inicio < 20000 && ClienteMaestro.hayPeticionDama) {
                try { ClienteMaestro.lockElisabetha.wait(2000); } catch (InterruptedException e) { }
            }

            boolean atendida = !ClienteMaestro.hayPeticionDama;
            if (!atendida) {
                ClienteMaestro.hayPeticionDama = false;
                ClienteMaestro.damaQuePide = null;
                ClienteMaestro.mensajeDama = null;
                System.out.println("[" + nombreDama + "] Elisabetha no la atendio (timeout).");
            } else {
                System.out.println("[" + nombreDama + "] Elisabetha la atendio. Conto: " + mensaje);
            }
        }
    }
}

