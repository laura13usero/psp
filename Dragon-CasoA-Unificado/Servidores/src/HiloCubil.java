import java.io.*;
import java.net.Socket;

/**
 * NUEVO: Hilo que atiende a UN cliente conectado al Cubil del Dragon.
 * Protocolo basado en comandos por texto (readUTF/writeUTF), identico
 * al patron de HiloTaberna/HiloMercado/HiloPorton.
 *
 * PROTOCOLO DEL CUBIL:
 *   SECUESTRAR           -> El dragon anuncia que ha secuestrado a Elisabetha
 *   ESPERAR_RESCATE      -> Elisabetha queda BLOQUEADA con wait() hasta rescate
 *   RESCATAR             -> Lance lucha y libera a Elisabetha con notifyAll()
 *   CONSULTAR_SECUESTRO  -> Devuelve boolean: esta Elisabetha secuestrada?
 *   CONSULTAR_RESULTADO  -> Devuelve boolean: fue victoria?
 *   DESCONECTAR          -> Cierra la conexion
 */
public class HiloCubil extends Thread {
    private Socket skCliente;       // Socket del cliente conectado
    private ControlCubil control;   // Monitor compartido del cubil

    public HiloCubil(Socket skCliente, ControlCubil control) {
        this.skCliente = skCliente;
        this.control = control;
    }

    @Override
    public void run() {
        try {
            // Crear flujos de entrada/salida (patron identico a HiloTaberna)
            DataInputStream entrada = new DataInputStream(skCliente.getInputStream());
            DataOutputStream salida = new DataOutputStream(skCliente.getOutputStream());

            boolean continuar = true;
            while (continuar) {
                // Leer comando del cliente
                String comando = entrada.readUTF();

                switch (comando) {
                    case "SECUESTRAR":
                        // El dragon anuncia el secuestro de Elisabetha
                        control.secuestrar();
                        salida.writeUTF("OK");
                        break;

                    case "ESPERAR_RESCATE":
                        // Elisabetha llega al cubil y QUEDA BLOQUEADA
                        // control.esperarRescate() hace wait() internamente
                        // Este hilo se duerme hasta que Lance haga notifyAll()
                        control.esperarRescate();
                        // Cuando despierta, la batalla ya se resolvio
                        salida.writeBoolean(control.fueVictoria());
                        break;

                    case "RESCATAR":
                        // Lance llega al cubil para rescatar a Elisabetha
                        // control.rescatar() hace el duelo y notifyAll()
                        boolean huboRescate = control.rescatar();
                        salida.writeBoolean(huboRescate);
                        if (huboRescate) {
                            salida.writeBoolean(control.fueVictoria());
                        }
                        break;

                    case "CONSULTAR_SECUESTRO":
                        // Cualquiera puede preguntar si hay secuestro activo
                        salida.writeBoolean(control.estaSecuestrada());
                        break;

                    case "CONSULTAR_RESULTADO":
                        salida.writeBoolean(control.fueVictoria());
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
            System.out.println("[CUBIL] Cliente desconectado: " + e.getMessage());
        }
    }
}

