import java.io.*;
import java.net.Socket;
import java.util.Random;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

public class HiloPorton extends Thread {
    public static final List<Socket> clientesActivos = Collections.synchronizedList(new ArrayList<>());
    private Socket skCliente;

    private static final String[] TIPOS_CARRETA = {"de heno", "de barriles", "de mercaderias", "del recaudador"};
    private static final String[] RATONES_A_BORDO = {"un raton con sombrero", "un raton con espada", "un raton juglar", "ningun raton sospechoso"};

    public HiloPorton(Socket skCliente) {
        this.skCliente = skCliente;
    }

    @Override
    public void run() {
        try {
            DataInputStream entrada = new DataInputStream(skCliente.getInputStream());
            DataOutputStream salida = new DataOutputStream(skCliente.getOutputStream());

            // === COMPROBAR SI EL LUGAR ESTÁ DESTRUIDO ===
            if (ServidorMaestro.portonDestruido) {
                System.out.println("[PORTON] Un cliente intenta acceder, pero el lugar está en ruinas. Conexión rechazada.");
                salida.writeUTF("LUGAR_DESTRUIDO");
                skCliente.close();
                return;
            }
            salida.writeUTF("CONEXION_OK");
            clientesActivos.add(skCliente);
            // =============================================

            boolean continuar = true;
            while (continuar) {
                String comando = entrada.readUTF();

                switch (comando) {
                    case "VIGILAR_PORTON":
                        String nombreCliente = entrada.readUTF();
                        System.out.println("[PORTON] " + nombreCliente + " se une a la guardia.");
                        realizarInspeccion(nombreCliente, salida);
                        break;

                    case "DESCONECTAR":
                        continuar = false;
                        salida.writeUTF("ADIOS");
                        System.out.println("[PORTON] Cliente desconectado.");
                        break;
                        
                    case "DESTRUIR_LUGAR":
                        ServidorMaestro.portonDestruido = true;
                        System.out.println("\n[PORTON] ¡ÉXITO! ¡El Portón Norte ha sido reducido a cenizas por el Dragón!");
                        
                        synchronized(clientesActivos) {
                            for (Socket sk : clientesActivos) {
                                if (sk != skCliente) {
                                    try { sk.close(); } catch (IOException ignore) {}
                                }
                            }
                            clientesActivos.clear();
                        }
                        
                        new Thread(() -> {
                            try { Thread.sleep(20000); } catch (InterruptedException ignore) {}
                            ServidorMaestro.portonDestruido = false;
                            System.out.println("[RECONSTRUCCION] ¡El Portón Norte ha sido reconstruido y la guardia se reanuda!");
                        }).start();
                        
                        continuar = false;
                        break;

                    default:
                        salida.writeUTF("COMANDO_DESCONOCIDO");
                        break;
                }
            }

            skCliente.close();
        } catch (IOException e) {
            // System.out.println("[PORTON] Cliente desconectado: " + e.getMessage());
        } finally {
            clientesActivos.remove(skCliente);
        }
    }

    private void realizarInspeccion(String nombre, DataOutputStream salida) throws IOException {
        Random random = new Random();
        String carreta = TIPOS_CARRETA[random.nextInt(TIPOS_CARRETA.length)];
        String raton = RATONES_A_BORDO[random.nextInt(RATONES_A_BORDO.length)];

        salida.writeUTF("Una carreta " + carreta);
        salida.writeUTF("A bordo viaja " + raton);
        salida.writeUTF("El Capitan de la Guardia te agradece, " + nombre + ". Sin novedad en el porton.");
    }
}