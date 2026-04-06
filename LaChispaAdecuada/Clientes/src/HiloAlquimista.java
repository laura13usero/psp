import java.util.Random;

/**
 * HILO ALQUIMISTA - Raton alquimista malvado del reino.
 * Hay 2 instancias (Alquimista1, Alquimista2).
 *
 * SIEMPRE comienzan estudiando calderos. Luego 3 acciones:
 * - 60% estudiar calderos (30s): 30% pocion para E, 30% pocion para L, 40% fallo
 * - 20% visitar Elisabetha (5s): necesita pocion, 30% exito -> chispa -20
 * - 20% visitar Lance (7s):
 *     80% pocion (necesita pocion, 20% exito -> chispa -20)
 *     20% amenaza Frente Norte (sin pocion, 20% exito -> chispa -30)
 *
 * ALACENA: contadores internos pocionesElisabetha y pocionesLance.
 * Solo puede visitar si tiene al menos 1 pocion disponible (excepto amenaza).
 *
 * COMUNICACION: buzon compartido (lockElisabetha / lockLance + wait/notifyAll)
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
        System.out.println("[" + nombreAlq + "] Comienza la simulacion. Estudiando calderos...");

        // Siempre comienzan estudiando
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
        System.out.println("[" + nombreAlq + "] Estudia sus calderos de pociones magicas...");
        try { Thread.sleep(30000); } catch (InterruptedException e) { }

        int resultado = random.nextInt(100);
        if (resultado < 30) {
            pocionesElisabetha++;
            System.out.println("[" + nombreAlq + "] Pocion para Elisabetha creada! Alacena: E=" + pocionesElisabetha + " L=" + pocionesLance);
        } else if (resultado < 60) {
            pocionesLance++;
            System.out.println("[" + nombreAlq + "] Excusa/pocion para Lance creada! Alacena: E=" + pocionesElisabetha + " L=" + pocionesLance);
        } else {
            System.out.println("[" + nombreAlq + "] Fracasa! La pocion explota. Alacena sin cambios.");
        }
    }

    private void visitarElisabetha() {
        if (pocionesElisabetha <= 0) {
            System.out.println("[" + nombreAlq + "] Quiere visitar a Elisabetha pero NO tiene pociones! Se lamenta dando grandes voces!");
            try { Thread.sleep(3000); } catch (InterruptedException e) { }
            return;
        }

        System.out.println("[" + nombreAlq + "] Se dirige a visitar a Elisabetha con un 'tonico de belleza'...");
        try { Thread.sleep(5000); } catch (InterruptedException e) { }

        synchronized (ClienteMaestro.lockElisabetha) {
            ClienteMaestro.hayPeticionAlquimistaE = true;
            ClienteMaestro.mensajeAlquimistaE = "POCION";
            System.out.println("[" + nombreAlq + "] Visita a Elisabetha...");

            long inicio = System.currentTimeMillis();
            while (ClienteMaestro.hayPeticionAlquimistaE && System.currentTimeMillis() - inicio < 10000) {
                try {
                    ClienteMaestro.lockElisabetha.wait(2000);
                } catch (InterruptedException e) { }
            }

            pocionesElisabetha--;

            if (!ClienteMaestro.hayPeticionAlquimistaE) {
                System.out.println("[" + nombreAlq + "] Elisabetha atendida. Chispa actual: " + ClienteMaestro.chispaElisabetha);
            } else {
                ClienteMaestro.hayPeticionAlquimistaE = false;
                ClienteMaestro.mensajeAlquimistaE = null;
                System.out.println("[" + nombreAlq + "] No pudo afectar a Elisabetha. Se lamenta dando grandes voces!");
            }
        }
    }

    private void visitarLance() {
        // 80% pocion, 20% amenaza
        int tipoAccion = random.nextInt(100);

        if (tipoAccion < 80) {
            // Intenta usar pocion
            if (pocionesLance <= 0) {
                System.out.println("[" + nombreAlq + "] Quiere usar pocion contra Lance pero NO tiene! Se lamenta dando grandes voces!");
                try { Thread.sleep(3000); } catch (InterruptedException e) { }
                return;
            }

            System.out.println("[" + nombreAlq + "] Se dirige a visitar a Lance con una pocion...");
            try { Thread.sleep(7000); } catch (InterruptedException e) { }

            synchronized (ClienteMaestro.lockLance) {
                ClienteMaestro.hayPeticionAlquimistaL = true;
                ClienteMaestro.tipoAccionAlquimistaL = "POCION";
                ClienteMaestro.mensajeAlquimistaL = "POCION";

                long inicio = System.currentTimeMillis();
                while (ClienteMaestro.hayPeticionAlquimistaL && System.currentTimeMillis() - inicio < 15000) {
                    try {
                        ClienteMaestro.lockLance.wait(2000);
                    } catch (InterruptedException e) { }
                }

                pocionesLance--;

                if (!ClienteMaestro.hayPeticionAlquimistaL) {
                    System.out.println("[" + nombreAlq + "] Lance atendido. Chispa actual: " + ClienteMaestro.chispaLance);
                } else {
                    ClienteMaestro.hayPeticionAlquimistaL = false;
                    ClienteMaestro.mensajeAlquimistaL = null;
                    ClienteMaestro.tipoAccionAlquimistaL = null;
                    System.out.println("[" + nombreAlq + "] No pudo enganar a Lance. Se lamenta dando grandes voces!");
                }
            }
        } else {
            // Amenaza al Frente Norte (no necesita pocion)
            System.out.println("[" + nombreAlq + "] Se dirige a amenazar a Lance con enviarlo al Frente Norte...");
            try { Thread.sleep(7000); } catch (InterruptedException e) { }

            synchronized (ClienteMaestro.lockLance) {
                ClienteMaestro.hayPeticionAlquimistaL = true;
                ClienteMaestro.tipoAccionAlquimistaL = "AMENAZA";
                ClienteMaestro.mensajeAlquimistaL = "AMENAZA";

                long inicio = System.currentTimeMillis();
                while (ClienteMaestro.hayPeticionAlquimistaL && System.currentTimeMillis() - inicio < 15000) {
                    try {
                        ClienteMaestro.lockLance.wait(2000);
                    } catch (InterruptedException e) { }
                }

                if (!ClienteMaestro.hayPeticionAlquimistaL) {
                    System.out.println("[" + nombreAlq + "] Amenaza a Lance. Chispa actual: " + ClienteMaestro.chispaLance);
                } else {
                    ClienteMaestro.hayPeticionAlquimistaL = false;
                    ClienteMaestro.mensajeAlquimistaL = null;
                    ClienteMaestro.tipoAccionAlquimistaL = null;
                    System.out.println("[" + nombreAlq + "] Lance no se deja amedrentar. Se lamenta dando grandes voces!");
                }
            }
        }
    }
}
