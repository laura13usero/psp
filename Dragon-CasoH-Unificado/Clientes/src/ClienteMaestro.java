/**
 * CLIENTE MAESTRO - CASO H: Dragon Cliente-Servidor.
 * NO hay HiloDragon aqui: el dragon vive en el proceso servidor.
 * Los clientes solo atacan al dragon via socket (como Caso F).
 */
public class ClienteMaestro {
    public static final String HOST = "localhost";
    public static final int PUERTO_TABERNA = 5000;
    public static final int PUERTO_MERCADO = 5001;
    public static final int PUERTO_PORTON  = 5002;
    public static final int PUERTO_DRAGON  = 5003;

    public static final Object lockElisabetha = new Object();
    public static String damaQuePide = null;
    public static String mensajeDama = null;
    public static boolean hayPeticionDama = false;
    public static boolean hayPeticionAlquimistaE = false;
    public static String mensajeAlquimistaE = null;
    public static int chispaElisabetha = 0;

    public static final Object lockLance = new Object();
    public static String caballeroQuePide = null;
    public static String mensajeCaballero = null;
    public static boolean hayPeticionCaballero = false;
    public static boolean hayPeticionAlquimistaL = false;
    public static String mensajeAlquimistaL = null;
    public static String tipoAccionAlquimistaL = null;
    public static int chispaLance = 0;

    // Dragon flags
    public static volatile boolean dragonDerrotado = false;
    public static volatile boolean dragonAtacando = false;

    public static volatile boolean simulacionTerminada = false;

    public static void main(String[] args) {
        System.out.println("=== CLIENTES ROEDALIA - CASO H: DRAGON CLIENTE-SERVIDOR ===");
        System.out.println("[MAESTRO] El dragon vive en el servidor (5003).");
        System.out.println("[MAESTRO] Lance lo ataca por socket, y a la vez el dragon ataca lugares.");

        HiloElisabetha elisabetha = new HiloElisabetha(); elisabetha.start();
        HiloLance lance = new HiloLance(); lance.start();

        // NO hay HiloDragon: el dragon esta en el servidor (doble rol)
        // Pero lanzamos un vigia que consulta al dragon por socket
        // para saber si esta vivo y avisar a las Damas de evacuar.
        HiloVigiaDragon vigia = new HiloVigiaDragon();
        vigia.setDaemon(true);
        vigia.start();

        for (int i = 0; i < 4; i++) { new HiloDama(i + 1).start(); }
        for (int i = 0; i < 4; i++) { new HiloCaballero(i + 1).start(); }
        for (int i = 0; i < 2; i++) { new HiloAlquimista(i + 1).start(); }

        try { elisabetha.join(); lance.join(); } catch (InterruptedException e) { }
        simulacionTerminada = true;
        System.out.println("\n=== FINAL FELIZ ===");
        System.exit(0);
    }
}


