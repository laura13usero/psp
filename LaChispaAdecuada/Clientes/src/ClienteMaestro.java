/**
 * CLIENTE MAESTRO - Punto de entrada del lado cliente.
 * Crea TODOS los personajes como hilos y los lanza.
 *
 * VARIABLES COMPARTIDAS: Las variables static sirven como "buzones"
 * para comunicacion entre hilos (Damas<->Elisabetha, Caballeros<->Lance).
 * Se protegen con locks (objetos para synchronized) y wait/notifyAll.
 *
 * volatile en simulacionTerminada: garantiza que todos los hilos vean
 * el cambio inmediatamente cuando se pone a true (visibilidad entre hilos).
 */
public class ClienteMaestro {
    public static final String HOST = "localhost";
    public static final int PUERTO_TABERNA = 5000;  // Debe coincidir con ServidorMaestro
    public static final int PUERTO_MERCADO = 5001;
    public static final int PUERTO_PORTON = 5002;

    // === BUZON COMPARTIDO: Elisabetha <-> Damas/Alquimistas ===
    // lockElisabetha: objeto usado como monitor para synchronized(lockElisabetha)
    public static final Object lockElisabetha = new Object();
    public static String damaQuePide = null;          // Nombre de la dama que quiere hablar
    public static String mensajeDama = null;           // Tipo de mensaje: CONFIDENCIA, RUMOR_LANCE, INVITACION_BAILE
    public static boolean hayPeticionDama = false;     // Flag: hay una dama esperando ser atendida?
    public static boolean hayPeticionAlquimistaE = false; // Flag: un alquimista quiere ver a Elisabetha?
    public static String mensajeAlquimistaE = null;    // Tipo: POCION
public static int chispaElisabetha = 0;            // Copia de la chispa para que alquimistas la consulten

    // === BUZON COMPARTIDO: Lance <-> Caballeros/Alquimistas ===
    public static final Object lockLance = new Object();
    public static String caballeroQuePide = null;
    public static String mensajeCaballero = null;       // CONFIDENCIA u OFENSA_ELISABETHA
    public static boolean hayPeticionCaballero = false;
    public static boolean hayPeticionAlquimistaL = false;
    public static String mensajeAlquimistaL = null;
    public static String tipoAccionAlquimistaL = null;  // POCION o AMENAZA
    public static int chispaLance = 0;

    // volatile: cambios visibles inmediatamente en todos los hilos
    public static volatile boolean simulacionTerminada = false;

    public static void main(String[] args) {
        System.out.println("==============================================");
        System.out.println("  CLIENTES DE ROEDALIA - MAESTRO");
        System.out.println("==============================================");
        System.out.println("[MAESTRO] Iniciando todos los personajes...\n");

        // Crear Elisabetha - Hilo especial, protagonista
        HiloElisabetha elisabetha = new HiloElisabetha();
        elisabetha.start();

        // Crear Lance - Hilo especial, protagonista
        HiloLance lance = new HiloLance();
        lance.start();

        // Crear 4 Damas del Lazo (Dama1, Dama2, Dama3, Dama4)
        HiloDama[] damas = new HiloDama[4];
        for (int i = 0; i < 4; i++) {
            damas[i] = new HiloDama(i + 1); // Recibe numero 1-4
            damas[i].start();
        }

        // Crear 4 Caballeros del Porton (Caballero1..4)
        HiloCaballero[] caballeros = new HiloCaballero[4];
        for (int i = 0; i < 4; i++) {
            caballeros[i] = new HiloCaballero(i + 1);
            caballeros[i].start();
        }

        // Crear 2 Alquimistas (Alquimista1, Alquimista2)
        HiloAlquimista[] alquimistas = new HiloAlquimista[2];
        for (int i = 0; i < 2; i++) {
            alquimistas[i] = new HiloAlquimista(i + 1);
            alquimistas[i].start();
        }

        // === INICIAR AL DRAGÓN DE CENIZA CARMESÍ ===
        HiloDragon dragon = new HiloDragon();
        dragon.start();

        System.out.println("[MAESTRO] Todos los personajes iniciados!");
        System.out.println("[MAESTRO] Esperando el final feliz del Cantar...\n");

        // join(): el hilo main ESPERA a que Elisabetha y Lance terminen
        // La simulacion acaba cuando ambos llegan a chispa 100
        try {
            elisabetha.join();
            lance.join();
        } catch (InterruptedException e) {
            System.out.println("[MAESTRO] Interrupcion: " + e.getMessage());
        }

        // Marcar fin para que los hilos secundarios (damas, caballeros, alquimistas) paren
        simulacionTerminada = true;

        System.out.println("\n==============================================");
        System.out.println("  *** FINAL DEL CANTAR DE ROEDALIA ***");
        System.out.println("  Elisabetha y Lance fueron felices");
        System.out.println("  y comieron perdices!");
        System.out.println("==============================================");

        // System.exit(0) fuerza la terminacion de hilos daemon pendientes
        System.exit(0);
    }
}
