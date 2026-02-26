/**
 * CLIENTE MAESTRO - CASO G: Dragon Local Synchronized.
 *
 * NUEVAS VARIABLES respecto al original:
 *   - dragonAtacando: volatile boolean que indica si el dragon esta atacando
 *     (las Damas lo comprueban para decidir si evacuar)
 *   - dragonDerrotado: volatile boolean (Lance lo pone a true cuando lo derrota)
 *   - lockDragon: Object para sincronizar la lucha contra el dragon
 *
 * El dragon NO tiene socket ni servidor propio. Accede directamente a
 * lockElisabetha y lockLance con synchronized para bloquear a los personajes.
 */
public class ClienteMaestro {
    public static final String HOST = "localhost";
    public static final int PUERTO_TABERNA = 5000;
    public static final int PUERTO_MERCADO = 5001;
    public static final int PUERTO_PORTON  = 5002;
    // NO HAY PUERTO_DRAGON: el dragon es un hilo local sin sockets

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

    // === NUEVO: Variables del Dragon (sin sockets, solo variables compartidas) ===
    // volatile: todos los hilos ven el cambio inmediatamente
    public static volatile boolean dragonAtacando = false;    // Dragon esta atacando?
    public static volatile boolean dragonDerrotado = false;   // Lance mato al dragon?
    // Lock para coordinar la lucha cuerpo a cuerpo contra el dragon
    public static final Object lockDragon = new Object();

    public static volatile boolean simulacionTerminada = false;

    public static void main(String[] args) {
        System.out.println("=== CLIENTES DE ROEDALIA - CASO G: DRAGON LOCAL SYNCHRONIZED ===");
        System.out.println("[MAESTRO] El dragon NO usa sockets. Usa synchronized directo.");

        HiloElisabetha elisabetha = new HiloElisabetha();
        elisabetha.start();

        HiloLance lance = new HiloLance();
        lance.start();

        // NUEVO: Dragon como hilo LOCAL (accede a lockElisabetha/lockLance directamente)
        HiloDragon dragon = new HiloDragon();
        dragon.start();

        for (int i = 0; i < 4; i++) { new HiloDama(i + 1).start(); }
        for (int i = 0; i < 4; i++) { new HiloCaballero(i + 1).start(); }
        for (int i = 0; i < 2; i++) { new HiloAlquimista(i + 1).start(); }

        System.out.println("[MAESTRO] Todos los personajes + Dragon Local iniciados!");

        try { elisabetha.join(); lance.join(); } catch (InterruptedException e) { }
        simulacionTerminada = true;

        System.out.println("\n=== FINAL - Felices y comen perdices! ===");
        System.exit(0);
    }
}

