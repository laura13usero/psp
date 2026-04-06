public class ServidorMaestro {
    public static final int PUERTO_TABERNA = 5000;
    public static final int PUERTO_MERCADO = 5001;
    public static final int PUERTO_PORTON = 5002;

    // === ESTADO DE LOS LUGARES (para el ataque del Dragón) ===
    public static volatile boolean mercadoDestruido = false;
    public static volatile boolean portonDestruido = false;
    public static volatile boolean tabernaDestruida = false;
    public static volatile boolean simulacionTerminada = false;

    // === OBJETO MONITOR PARA LA TABERNA ===
    // Se crea una ÚNICA instancia que se compartirá entre todos los hilos de la taberna.
    public static ControlTaberna controlTaberna = new ControlTaberna();

    public static void main(String[] args) {
        System.out.println("==============================================");
        System.out.println("  SERVIDORES DE ROEDALIA - MAESTRO");
        System.out.println("==============================================");

        // Iniciar servidor de la Taberna, pasándole el objeto de control
        Thread hiloServidorTaberna = new Thread(new HiloServidorTaberna(PUERTO_TABERNA, controlTaberna));
        hiloServidorTaberna.start();

        // Iniciar servidor del Mercado
        Thread hiloServidorMercado = new Thread(new HiloServidorMercado(PUERTO_MERCADO));
        hiloServidorMercado.start();

        // Iniciar servidor del Portón Norte
        Thread hiloServidorPorton = new Thread(new HiloServidorPorton(PUERTO_PORTON));
        hiloServidorPorton.start();


        System.out.println("[MAESTRO] Todos los servidores y amenazas están activos.");
        System.out.println("[MAESTRO] Esperando conexiones de los clientes...\n");
    }
}