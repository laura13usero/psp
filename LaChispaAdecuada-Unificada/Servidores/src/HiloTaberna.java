import java.io.*;
import java.net.Socket;

/**
 * Hilo que atiende a UN cliente conectado a la taberna.
 * Protocolo basado en comandos por texto (readUTF/writeUTF):
 * - El cliente envia un comando (String)
 * - El servidor procesa y responde
 *
 * PROTOCOLO DE LA TABERNA:
 * ENTRAR + nombre        -> Marca presencia en la taberna
 * SALIR_TABERNA + nombre -> Marca salida de la taberna
 * CONSULTAR_LANCE        -> Devuelve boolean: esta Lance?
 * CONSULTAR_ELISABETHA   -> Devuelve boolean: esta Elisabetha?
 * YA_SE_CONOCEN          -> Devuelve boolean: ya se encontraron antes?
 * SE_CONOCEN             -> Marca primer encuentro (chispa sube a 75)
 * REGISTRAR_CHISPA_100   -> Marca que un personaje llego a chispa 100
 * ESPERAR_AL_OTRO        -> BLOQUEA con wait() hasta que ambos esten a 100
 * DESCONECTAR            -> Cierra la conexion
 */
public class HiloTaberna extends Thread {
    private Socket skCliente;        // Socket del cliente conectado
    private ControlTaberna control;  // Objeto compartido synchronized

    public HiloTaberna(Socket skCliente, ControlTaberna control) {
        this.skCliente = skCliente;
        this.control = control;
    }

    @Override
    public void run() {
        try {
            // Flujos de entrada/salida para comunicacion por socket
            DataInputStream entrada = new DataInputStream(skCliente.getInputStream());
            DataOutputStream salida = new DataOutputStream(skCliente.getOutputStream());

            boolean continuar = true;
            while (continuar) {
                // Leer comando del cliente (protocolo basado en texto)
                String comando = entrada.readUTF();

                switch (comando) {
                    case "ENTRAR":
                        // El personaje anuncia que esta en la taberna
                        String personajeEntrar = entrada.readUTF();
                        control.entrar(personajeEntrar); // synchronized internamente
                        salida.writeUTF("OK");
                        break;

                    case "SALIR_TABERNA":
                        // El personaje se va de la taberna
                        String personajeSalir = entrada.readUTF();
                        control.salir(personajeSalir);
                        salida.writeUTF("OK");
                        break;

                    case "CONSULTAR_ELISABETHA":
                        // Pregunta: esta Elisabetha en la taberna? -> devuelve boolean
                        salida.writeBoolean(control.estaElisabetha());
                        break;

                    case "CONSULTAR_LANCE":
                        salida.writeBoolean(control.estaLance());
                        break;

                    case "YA_SE_CONOCEN":
                        // Pregunta si es la PRIMERA vez que coinciden
                        salida.writeBoolean(control.yaSeConocen());
                        break;

                    case "SE_CONOCEN":
                        // Primera coincidencia -> chispa a 75 en ambos
                        control.seConocen();
                        salida.writeUTF("CHISPA_GENERADA");
                        break;

                    case "REGISTRAR_CHISPA_100":
                        // Un personaje llego a chispa 100 -> fase final
                        String personaje100 = entrada.readUTF();
                        control.registrarChispa100(personaje100);
                        salida.writeUTF("OK");
                        break;

                    case "CONSULTAR_CHISPA_100_ELISABETHA":
                        salida.writeBoolean(control.elisabethaChispa100());
                        break;

                    case "CONSULTAR_CHISPA_100_LANCE":
                        salida.writeBoolean(control.lanceChispa100());
                        break;

                    case "CONSULTAR_AMBOS_100":
                        salida.writeBoolean(control.ambosChispa100());
                        break;

                    case "ESPERAR_AL_OTRO":
                        // CLAVE: este metodo BLOQUEA el hilo con wait()
                        // hasta que el otro personaje tambien llegue a 100
                        String personajeEspera = entrada.readUTF();
                        control.esperarAlOtro(personajeEspera);
                        salida.writeUTF("FINAL_FELIZ");
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
            System.out.println("[TABERNA] Cliente desconectado: " + e.getMessage());
        }
    }
}
