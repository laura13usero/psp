import java.io.*;
import java.net.Socket;
import java.util.Random;

public class HiloMercado extends Thread {
    private Socket skCliente;

    private static final String[] TODOS_LOS_PRODUCTOS = {
        "queso", "pan recien horneado", "especias del lejano oriente",
        "telas para vestidos", "jugo de grosella", "repelente de gatos",
        "brillantes collares de ratona", "cucharas de boj tamano raton"
    };

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
                        atenderCliente(nombreCliente, salida, entrada);
                        break;

                    case "DESCONECTAR":
                        continuar = false;
                        salida.writeUTF("ADIOS");
                        System.out.println("[MERCADO] Cliente desconectado.");
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

    private void atenderCliente(String nombre, DataOutputStream salida, DataInputStream entrada) throws IOException {
        Random random = new Random();

        String[] oferta = new String[5];
        boolean[] usados = new boolean[TODOS_LOS_PRODUCTOS.length];
        for (int i = 0; i < 5; i++) {
            int idx;
            do {
                idx = random.nextInt(TODOS_LOS_PRODUCTOS.length);
            } while (usados[idx]);
            usados[idx] = true;
            oferta[i] = TODOS_LOS_PRODUCTOS[idx];
        }

        salida.writeInt(5);
        for (int i = 0; i < 5; i++) {
            salida.writeUTF(oferta[i]);
        }

        System.out.println("[MERCADO] Ofreciendo 5 productos a " + nombre);

        int eleccion = entrada.readInt();
        if (eleccion >= 0 && eleccion < 5) {
            System.out.println("[MERCADO] " + nombre + " ha comprado: " + oferta[eleccion]);
            salida.writeUTF("Gracias por comprar " + oferta[eleccion] + ", " + nombre + "!");
        } else {
            salida.writeUTF("Eleccion no valida. Vuelve pronto!");
        }
    }
}

