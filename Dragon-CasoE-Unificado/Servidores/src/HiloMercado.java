import java.io.*;
import java.net.Socket;
import java.util.Random;

/**
 * Hilo Mercado - CASO E: PATRON NUEVO = SERVIDOR ACTUA COMO CLIENTE.
 *
 * Cuando recibe ATAQUE_DRAGON, ADEMAS de marcar destruido=true, el servidor
 * ABRE UN SOCKET COMO CLIENTE hacia el Porton Norte (puerto 5002) y envia
 * el comando "SOCORRO" + "MERCADO" para avisar del ataque.
 *
 * Esto demuestra COMUNICACION SERVIDOR -> SERVIDOR: un ServerSocket que
 * normalmente solo recibe conexiones, ahora se conecta a OTRO ServerSocket
 * como si fuera un cliente. Patron que NO aparece en ningun otro caso.
 */
public class HiloMercado extends Thread {
    private Socket skCliente;
    private static final String[] PRODUCTOS = {
        "queso", "pan recien horneado", "especias del lejano oriente",
        "telas para vestidos", "jugo de grosella", "repelente de gatos",
        "brillantes collares de ratona", "cucharas de boj tamano raton"
    };

    // Flag volatile de destruccion (como en Caso C)
    public static volatile boolean destruido = false;

    // Puerto del Porton para enviar socorro (comunicacion servidor->servidor)
    private static final int PUERTO_PORTON = 5002;

    public HiloMercado(Socket sk) { this.skCliente = sk; }

    @Override
    public void run() {
        try {
            DataInputStream entrada = new DataInputStream(skCliente.getInputStream());
            DataOutputStream salida = new DataOutputStream(skCliente.getOutputStream());
            boolean c = true;
            while (c) {
                String cmd = entrada.readUTF();
                switch (cmd) {
                    case "VISITAR_MERCADO":
                        String nom = entrada.readUTF();
                        if (destruido) {
                            salida.writeUTF("MERCADO_ATACADO");
                        } else {
                            salida.writeUTF("MERCADO_OK");
                            Random r = new Random();
                            String[] of = new String[5];
                            boolean[] u = new boolean[PRODUCTOS.length];
                            for (int i = 0; i < 5; i++) {
                                int x; do { x = r.nextInt(PRODUCTOS.length); } while (u[x]);
                                u[x] = true; of[i] = PRODUCTOS[x];
                            }
                            salida.writeInt(5);
                            for (int i = 0; i < 5; i++) salida.writeUTF(of[i]);
                            int el = entrada.readInt();
                            salida.writeUTF("Gracias por comprar " + of[Math.max(0, Math.min(el, 4))] + ", " + nom + "!");
                        }
                        break;

                    // ============================================================
                    // CLAVE DEL CASO E: SERVIDOR ACTUA COMO CLIENTE
                    // ============================================================
                    case "ATAQUE_DRAGON":
                        destruido = true;
                        System.out.println("[MERCADO] *** DRAGON ATACA EL MERCADO! ***");
                        salida.writeUTF("LUGAR_DESTRUIDO");

                        // *** NUEVO: SOCORRO SERVIDOR -> SERVIDOR ***
                        // El Mercado (que es un servidor) abre un Socket COMO CLIENTE
                        // hacia el Porton Norte (otro servidor) para pedir socorro.
                        // Patron: un servidor que se conecta a otro servidor.
                        enviarSocorro("MERCADO");

                        // Reconstruccion tras 20 seg
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try { Thread.sleep(20000); } catch (InterruptedException e) { }
                                destruido = false;
                                System.out.println("[MERCADO] Mercado reconstruido.");
                            }
                        }).start();
                        break;

                    case "DESCONECTAR":
                        c = false;
                        salida.writeUTF("ADIOS");
                        break;

                    default:
                        salida.writeUTF("COMANDO_DESCONOCIDO");
                        break;
                }
            }
            skCliente.close();
        } catch (IOException e) {
            System.out.println("[MERCADO] Desconexion");
        }
    }

    /**
     * NUEVO: Enviar mensaje de SOCORRO al Porton Norte.
     *
     * El Mercado (servidor) se conecta al Porton (otro servidor) COMO CLIENTE.
     * Abre un Socket, envia "SOCORRO" + nombre del lugar atacado, y cierra.
     *
     * PATRON: Servidor actuando como cliente de otro servidor.
     * En clase siempre vimos clientes -> servidor. Aqui servidor -> servidor.
     *
     * @param lugarAtacado nombre del lugar que esta siendo atacado
     */
    private void enviarSocorro(String lugarAtacado) {
        try {
            // El Mercado ABRE UN SOCKET como cliente hacia el Porton
            Socket skSocorro = new Socket("localhost", PUERTO_PORTON);
            DataOutputStream salidaSocorro = new DataOutputStream(skSocorro.getOutputStream());
            DataInputStream entradaSocorro = new DataInputStream(skSocorro.getInputStream());

            // Enviar comando SOCORRO + nombre del lugar
            salidaSocorro.writeUTF("SOCORRO");
            salidaSocorro.writeUTF(lugarAtacado);
            String respuesta = entradaSocorro.readUTF();

            System.out.println("[MERCADO -> PORTON] Socorro enviado! Respuesta: " + respuesta);

            salidaSocorro.writeUTF("DESCONECTAR");
            entradaSocorro.readUTF();
            skSocorro.close();
        } catch (IOException e) {
            System.out.println("[MERCADO] No pudo enviar socorro al Porton: " + e.getMessage());
        }
    }
}

