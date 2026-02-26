/**
 * CLIENTE MAESTRO - CASO A: DRAGON (Secuestro + Rescate + Duelo).
 * Crea TODOS los personajes como hilos y los lanza, INCLUYENDO el Dragon Carmesi.
 *
 * NUEVAS VARIABLES respecto al original:
 *   - PUERTO_CUBIL: puerto del cubil del dragon (5003)
 *   - elisabethaSecuestrada: flag volatile que indica si Elisabetha fue secuestrada
 *     (volatile = todos los hilos ven el cambio inmediatamente)
 *   - lockSecuestro: objeto monitor para sincronizar el secuestro
 *
 * Las demas variables (lockElisabetha, lockLance, buzones de damas/caballeros/alquimistas)
 * son IDENTICAS al proyecto original.
 */
public class ClienteMaestro {
    // Conexion al servidor (localhost porque es version unificada)
    public static final String HOST = "localhost";
    public static final int PUERTO_TABERNA = 5000;
    public static final int PUERTO_MERCADO = 5001;
    public static final int PUERTO_PORTON  = 5002;
    public static final int PUERTO_CUBIL   = 5003; // NUEVO: cubil del dragon

    // === BUZON COMPARTIDO: Elisabetha <-> Damas/Alquimistas (identico al original) ===
    public static final Object lockElisabetha = new Object();
    public static String damaQuePide = null;
    public static String mensajeDama = null;
    public static boolean hayPeticionDama = false;
    public static boolean hayPeticionAlquimistaE = false;
    public static String mensajeAlquimistaE = null;
    public static int chispaElisabetha = 0;

    // === BUZON COMPARTIDO: Lance <-> Caballeros/Alquimistas (identico al original) ===
    public static final Object lockLance = new Object();
    public static String caballeroQuePide = null;
    public static String mensajeCaballero = null;
    public static boolean hayPeticionCaballero = false;
    public static boolean hayPeticionAlquimistaL = false;
    public static String mensajeAlquimistaL = null;
    public static String tipoAccionAlquimistaL = null;
    public static int chispaLance = 0;

    // === NUEVO: Variables para el Dragon Carmesi ===
    // volatile: cambios visibles inmediatamente en todos los hilos
    public static volatile boolean elisabethaSecuestrada = false;
    // Objeto monitor para coordinar el evento de secuestro entre hilos
    public static final Object lockSecuestro = new Object();

    // Flag de fin de simulacion (volatile, identico al original)
    public static volatile boolean simulacionTerminada = false;

    public static void main(String[] args) {
        System.out.println("==============================================");
        System.out.println("  CLIENTES DE ROEDALIA - CASO A: DRAGON");
        System.out.println("  Secuestro + Rescate + Duelo");
        System.out.println("==============================================");

        // Crear los protagonistas
        HiloElisabetha elisabetha = new HiloElisabetha();
        elisabetha.start();

        HiloLance lance = new HiloLance();
        lance.start();

        // NUEVO: Crear el Dragon Carmesi
        // El dragon es un hilo mas que ataca periodicamente
        HiloDragon dragon = new HiloDragon();
        dragon.start();

        // Crear 4 Damas (identico al original)
        HiloDama[] damas = new HiloDama[4];
        for (int i = 0; i < 4; i++) {
            damas[i] = new HiloDama(i + 1);
            damas[i].start();
        }

        // Crear 4 Caballeros (identico al original)
        HiloCaballero[] caballeros = new HiloCaballero[4];
        for (int i = 0; i < 4; i++) {
            caballeros[i] = new HiloCaballero(i + 1);
            caballeros[i].start();
        }

        // Crear 2 Alquimistas (identico al original)
        HiloAlquimista[] alquimistas = new HiloAlquimista[2];
        for (int i = 0; i < 2; i++) {
            alquimistas[i] = new HiloAlquimista(i + 1);
            alquimistas[i].start();
        }

        System.out.println("[MAESTRO] Todos los personajes + Dragon iniciados!");

        // join(): esperar a que Elisabetha y Lance terminen
        try {
            elisabetha.join();
            lance.join();
        } catch (InterruptedException e) {
            System.out.println("[MAESTRO] Interrupcion: " + e.getMessage());
        }

        // Marcar fin para que los hilos secundarios paren
        simulacionTerminada = true;

        System.out.println("\n==============================================");
        System.out.println("  *** FINAL DEL CANTAR DE ROEDALIA ***");
        System.out.println("  Elisabetha y Lance fueron felices");
        System.out.println("  y comieron perdices!");
        System.out.println("==============================================");

        System.exit(0);
    }
}

