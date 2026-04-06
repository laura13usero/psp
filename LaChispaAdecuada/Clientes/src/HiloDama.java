import java.util.Random;

/**
 * HILO DAMA - Dama del Lazo Perfumado.
 * Hay 4 instancias (Dama1..Dama4), cada una con su nombre propio.
 *
 * 2 acciones con misma probabilidad (50/50):
 * 1. Realizar labores (5s): montar a caballo, esgrima, enterarse de rumores
 * 2. Confesar a Elisabetha: intenta contactar durante 20s (timeout con wait)
 *    - 50% confidencia (sin efecto)
 *    - 25% rumor sobre Lance (chispa Elisabetha -5)
 *    - 25% invitacion a baile (80% Elisabetha lo esquiva)
 *
 * COMUNICACION con Elisabetha: via buzon compartido en ClienteMaestro
 * La dama escribe en el buzon (damaQuePide, mensajeDama, hayPeticionDama)
 * y espera con wait() a que Elisabetha lo lea y haga notifyAll().
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
            int accion = random.nextInt(2);

            if (accion == 0) {
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
        System.out.println("[" + nombreDama + "] Termina labor: " + labor);
    }

    private void confesarAElisabetha() {
        System.out.println("[" + nombreDama + "] Quiere hablar con Elisabetha...");

        // Decidir que contar: 50% confidencia, 50% rumor/invitacion
        String mensaje;
        if (random.nextBoolean()) {
            mensaje = "CONFIDENCIA";
        } else {
            if (random.nextBoolean()) {
                mensaje = "RUMOR_LANCE";
            } else {
                mensaje = "INVITACION_BAILE";
            }
        }

        synchronized (ClienteMaestro.lockElisabetha) {
            // La dama intenta contactar durante 20 segundos
            long inicio = System.currentTimeMillis();

            // CORRECCION: Esperar si el buzon ya esta ocupado por otra dama
            while (ClienteMaestro.hayPeticionDama && System.currentTimeMillis() - inicio < 20000) {
                try {
                    ClienteMaestro.lockElisabetha.wait(1000);
                } catch (InterruptedException e) { }
            }

            // Si tras esperar sigue ocupado, desistir
            if (ClienteMaestro.hayPeticionDama) {
                System.out.println("[" + nombreDama + "] Elisabetha esta ocupada con otra dama. Desiste.");
                return;
            }

            ClienteMaestro.damaQuePide = nombreDama;
            ClienteMaestro.mensajeDama = mensaje;
            ClienteMaestro.hayPeticionDama = true;
            ClienteMaestro.lockElisabetha.notifyAll(); // Avisar que hay peticion

            while (System.currentTimeMillis() - inicio < 20000 && ClienteMaestro.hayPeticionDama) {
                try {
                    ClienteMaestro.lockElisabetha.wait(2000);
                } catch (InterruptedException e) { }
            }

            boolean atendida = !ClienteMaestro.hayPeticionDama;
            if (!atendida) {
                // Limpiar buzon si nos vamos por timeout
                if (ClienteMaestro.damaQuePide != null && ClienteMaestro.damaQuePide.equals(nombreDama)) {
                    ClienteMaestro.hayPeticionDama = false;
                    ClienteMaestro.damaQuePide = null;
                    ClienteMaestro.mensajeDama = null;
                    ClienteMaestro.lockElisabetha.notifyAll(); // Avisar a otras damas esperando
                }
                System.out.println("[" + nombreDama + "] Elisabetha no la atendio (timeout). Vuelve a labores.");
            } else {
                System.out.println("[" + nombreDama + "] Elisabetha la atendio. Le conto: " + mensaje);
            }
        }
    }
}
