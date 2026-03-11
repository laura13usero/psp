public class ClienteMaestro {
    public static final String HOST = "localhost";
    public static final int PUERTO_TABERNA = 5000;
    public static final int PUERTO_MERCADO = 5001;
    public static final int PUERTO_PORTON  = 5002;

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

    public static volatile boolean dragonAtacando = false;

    // Version 2.0: Ratones Caballero
    public static volatile boolean ratonesDanados = false;

    public static volatile boolean simulacionTerminada = false;

    public static void main(String[] args) {
        System.out.println("=== REINO DE ROEDALIA ===");

        HiloElisabetha elisabetha = new HiloElisabetha();
        elisabetha.start();

        HiloLance lance = new HiloLance();
        lance.start();

        HiloDragon dragon = new HiloDragon();
        dragon.start();

        for (int i = 0; i < 4; i++) { new HiloDama(i + 1).start(); }
        for (int i = 0; i < 4; i++) { new HiloCaballero(i + 1).start(); }
        for (int i = 0; i < 2; i++) { new HiloAlquimista(i + 1).start(); }

        System.out.println("[MAESTRO] Todos los personajes iniciados!");

        try { elisabetha.join(); lance.join(); } catch (InterruptedException e) { }
        simulacionTerminada = true;

        System.out.println("\n=== FINAL - Sufrieron al dragón en el camino pero acabaron felices bebiendo cerveza en la taberna!!! ===");
        System.exit(0);
    }
}

