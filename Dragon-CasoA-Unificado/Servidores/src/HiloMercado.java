import java.io.*;
import java.net.Socket;
import java.util.Random;

/**
 * Hilo que atiende a UN cliente en el Mercado.
 * MODIFICADO respecto al original: se anade el comando ATAQUE_DRAGON
 * que el HiloDragon enviara cuando decida irrumpir en el mercado.
 *
 * PROTOCOLO DEL MERCADO:
 *   VISITAR_MERCADO + nombre -> ofrecer 5 productos, cliente elige 1
 *   ATAQUE_DRAGON            -> NUEVO: el dragon ataca el mercado
 *   DESCONECTAR              -> cierra conexion
 */
public class HiloMercado extends Thread {
    private Socket skCliente;

    // Productos disponibles en el mercado (identico al original)
    private static final String[] TODOS_LOS_PRODUCTOS = {
        "queso", "pan recien horneado", "especias del lejano oriente",
        "telas para vestidos", "jugo de grosella", "repelente de gatos",
        "brillantes collares de ratona", "cucharas de boj tamano raton"
    };

    // NUEVO: flag volatile que indica si el dragon esta atacando el mercado
    // volatile = todos los hilos ven el cambio inmediatamente
    public static volatile boolean dragonAtacando = false;

    public HiloMercado(Socket skCliente) {
        this.skCliente = skCliente;
    }

    @Override
    public void run() {
        try {
            DataInputStream entrada = new DataInputStream(skCliente.getInputStream());
            DataOutputStream salida = new DataOutputStream(skCliente.getOutputStream());

            boolean continuar = true;
            while (continuar) {
                String comando = entrada.readUTF();

                switch (comando) {
                    case "VISITAR_MERCADO":
                        String nombreCliente = entrada.readUTF();
                        System.out.println("[MERCADO] " + nombreCliente + " ha llegado al mercado.");

                        // NUEVO: Si el dragon esta atacando, no se puede comprar
                        if (dragonAtacando) {
                            salida.writeUTF("MERCADO_ATACADO");
                            System.out.println("[MERCADO] " + nombreCliente + " huye! El dragon esta atacando!");
                        } else {
                            salida.writeUTF("MERCADO_OK");
                            atenderCliente(nombreCliente, salida, entrada);
                        }
                        break;

                    // NUEVO: El dragon irrumpe en el mercado para secuestrar a Elisabetha
                    case "ATAQUE_DRAGON":
                        dragonAtacando = true;
                        System.out.println("[MERCADO] *** EL DRAGON CARMESI IRRUMPE EN EL MERCADO! ***");
                        System.out.println("[MERCADO] El mercado arde en llamas!");
                        salida.writeUTF("ATAQUE_OK");

                        // El mercado queda destruido 20 segundos, luego se reconstruye
                        // Se lanza un hilo anonimo para la reconstruccion (patron visto en clase)
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try { Thread.sleep(20000); } catch (InterruptedException e) { }
                                dragonAtacando = false;
                                System.out.println("[MERCADO] El mercado ha sido reconstruido.");
                            }
                        }).start();
                        break;

                    case "DESCONECTAR":
                        continuar = false;
                        salida.writeUTF("ADIOS");
                        break;

                    default:
                        salida.writeUTF("COMANDO_DESCONOCIDO");
                        break;
                }
            }
            skCliente.close();
        } catch (IOException e) {
            System.out.println("[MERCADO] Cliente desconectado: " + e.getMessage());
        }
    }

    // Atencion normal al cliente (identica al original)
    private void atenderCliente(String nombre, DataOutputStream salida, DataInputStream entrada) throws IOException {
        Random random = new Random();
        String[] oferta = new String[5];
        boolean[] usados = new boolean[TODOS_LOS_PRODUCTOS.length];
        for (int i = 0; i < 5; i++) {
            int idx;
            do { idx = random.nextInt(TODOS_LOS_PRODUCTOS.length); } while (usados[idx]);
            usados[idx] = true;
            oferta[i] = TODOS_LOS_PRODUCTOS[idx];
        }
        salida.writeInt(5);
        for (int i = 0; i < 5; i++) {
            salida.writeUTF(oferta[i]);
        }
        int eleccion = entrada.readInt();
        if (eleccion >= 0 && eleccion < 5) {
            System.out.println("[MERCADO] " + nombre + " compro: " + oferta[eleccion]);
            salida.writeUTF("Gracias por comprar " + oferta[eleccion] + ", " + nombre + "!");
        } else {
            salida.writeUTF("Eleccion no valida.");
        }
    }
}

