import java.io.*;
import java.net.Socket;
import java.util.Random;

/**
 * Hilo que atiende a UN cliente en el Porton Norte (identico al original).
 */
public class HiloPorton extends Thread {
    private Socket skCliente;

    private static final String[] RATONES_CIUDAD = {
        "Raton Mercader Pedro", "Ratona Costurera Maria",
        "Raton Panadero Juan", "Ratona Herrera Ana"
    };
    private static final String[] RATONES_FORANEOS = {
        "Raton viajero del Norte", "Ratona comerciante del Este",
        "Raton errante del Sur", "Ratona exploradora del Oeste"
    };
    private static final String[] PRODUCTOS_CARRETA = {
        "queso fermentado", "telas finas", "especias",
        "herramientas", "queso sin fermentar", "leche cruda",
        "pan artesano", "frutas secas", "miel"
    };

    public HiloPorton(Socket skCliente) {
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
                    case "VIGILAR_PORTON":
                        String nombreGuardia = entrada.readUTF();
                        System.out.println("[PORTON] " + nombreGuardia + " comienza turno de guardia.");
                        inspeccionarCarreta(nombreGuardia, salida);
                        break;
                    case "VIGILAR_LUGAR":
                        String nombreCaballero = entrada.readUTF();
                        String lugar = entrada.readUTF();
                        System.out.println("[PORTON] " + nombreCaballero + " vigila: " + lugar);
                        salida.writeUTF("VIGILANCIA_OK");
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
            System.out.println("[PORTON] Cliente desconectado: " + e.getMessage());
        }
    }

    private void inspeccionarCarreta(String guardia, DataOutputStream salida) throws IOException {
        Random random = new Random();
        boolean esDeLaCiudad = random.nextBoolean();
        if (esDeLaCiudad) {
            String raton = RATONES_CIUDAD[random.nextInt(RATONES_CIUDAD.length)];
            salida.writeUTF("CARRETA_LOCAL");
            salida.writeUTF(raton);
            salida.writeUTF("Adelante " + raton + ", bienvenido a Roedalia!");
        } else {
            String raton = RATONES_FORANEOS[random.nextInt(RATONES_FORANEOS.length)];
            String producto = PRODUCTOS_CARRETA[random.nextInt(PRODUCTOS_CARRETA.length)];
            if (producto.equals("queso sin fermentar") || producto.equals("leche cruda")) {
                salida.writeUTF("CARRETA_RECHAZADA");
                salida.writeUTF(raton);
                salida.writeUTF("No puede entrar con " + producto + ". Acceso denegado.");
            } else {
                salida.writeUTF("CARRETA_PERMITIDA");
                salida.writeUTF(raton);
                salida.writeUTF("Puede pasar con su " + producto + ". Bienvenido.");
            }
        }
    }
}

