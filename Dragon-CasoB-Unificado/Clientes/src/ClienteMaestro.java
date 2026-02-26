/**
 * CLIENTE MAESTRO - CASO B: Dragon como Servidor Independiente.
 * Igual que el original PERO con PUERTO_DRAGON para que Lance y Elisabetha
 * se conecten al servidor del dragon y luchen JUNTOS.
 *
 * NUEVA VARIABLE: dragonAparecido (volatile) - se activa aleatoriamente
 * para que Lance y Elisabetha decidan ir a luchar contra el dragon.
 */
public class ClienteMaestro {
    public static final String HOST = "localhost";
    public static final int PUERTO_TABERNA = 5000;
    public static final int PUERTO_MERCADO = 5001;
    public static final int PUERTO_PORTON  = 5002;
    public static final int PUERTO_DRAGON  = 5003; // NUEVO: servidor del dragon

    // === BUZON Elisabetha <-> Damas/Alquimistas (identico al original) ===
    public static final Object lockElisabetha = new Object();
    public static String damaQuePide = null;
    public static String mensajeDama = null;
    public static boolean hayPeticionDama = false;
    public static boolean hayPeticionAlquimistaE = false;
    public static String mensajeAlquimistaE = null;
    public static int chispaElisabetha = 0;

    // === BUZON Lance <-> Caballeros/Alquimistas (identico al original) ===
    public static final Object lockLance = new Object();
    public static String caballeroQuePide = null;
    public static String mensajeCaballero = null;
    public static boolean hayPeticionCaballero = false;
    public static boolean hayPeticionAlquimistaL = false;
    public static String mensajeAlquimistaL = null;
    public static String tipoAccionAlquimistaL = null;
    public static int chispaLance = 0;

    // NUEVO: Flag que indica que el dragon ha aparecido y hay que ir a luchar
    // volatile = todos los hilos lo ven inmediatamente
    public static volatile boolean dragonAparecido = false;

    public static volatile boolean simulacionTerminada = false;

    public static void main(String[] args) {
        System.out.println("=== CLIENTES DE ROEDALIA - CASO B: DRAGON SERVIDOR ===");

        HiloElisabetha elisabetha = new HiloElisabetha();
        elisabetha.start();

        HiloLance lance = new HiloLance();
        lance.start();

        // Hilo que hace aparecer al dragon periodicamente
        // Es un hilo anonimo que cada 20-40 seg activa dragonAparecido
        Thread activadorDragon = new Thread(new Runnable() {
            @Override
            public void run() {
                java.util.Random rnd = new java.util.Random();
                while (!simulacionTerminada) {
                    try { Thread.sleep(20000 + rnd.nextInt(20000)); } catch (InterruptedException e) { }
                    if (!simulacionTerminada) {
                        dragonAparecido = true;
                        System.out.println("[DRAGON] *** EL DRAGON CARMESI APARECE EN ROEDALIA! ***");
                        // Esperar a que ambos vayan a luchar y lo reseteen
                        try { Thread.sleep(15000); } catch (InterruptedException e) { }
                        dragonAparecido = false; // Si nadie fue, el dragon se retira
                    }
                }
            }
        });
        activadorDragon.setDaemon(true); // Daemon: muere cuando termina main
        activadorDragon.start();

        // 4 Damas, 4 Caballeros, 2 Alquimistas (identico al original)
        for (int i = 0; i < 4; i++) { new HiloDama(i + 1).start(); }
        for (int i = 0; i < 4; i++) { new HiloCaballero(i + 1).start(); }
        for (int i = 0; i < 2; i++) { new HiloAlquimista(i + 1).start(); }

        System.out.println("[MAESTRO] Todos los personajes iniciados!");

        try { elisabetha.join(); lance.join(); } catch (InterruptedException e) { }
        simulacionTerminada = true;

        System.out.println("\n=== FINAL DEL CANTAR - Felices y comen perdices! ===");
        System.exit(0);
    }
}

