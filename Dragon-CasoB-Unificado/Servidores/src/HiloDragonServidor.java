import java.io.*;
import java.net.Socket;

/**
 * NUEVO: Hilo que atiende a UN cliente conectado al Servidor del Dragon.
 * Protocolo basado en comandos (readUTF/writeUTF):
 *
 *   LUCHAR + nombre     -> El personaje llega a luchar. Usa ControlDragon.llegarABatalla()
 *                          que bloquea con wait() hasta que ambos esten. Resultado 50/50.
 *   CONSULTAR_DRAGON    -> Devuelve la vida actual del dragon
 *   DESCONECTAR         -> Cierra la conexion
 *
 * El comando LUCHAR es el CLAVE: usa la BARRERA del ControlDragon para que
 * ambos protagonistas (Elisabetha y Lance) luchen juntos.
 */
public class HiloDragonServidor extends Thread {
    private Socket skCliente;
    private ControlDragon control;

    public HiloDragonServidor(Socket skCliente, ControlDragon control) {
        this.skCliente = skCliente;
        this.control = control;
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
                    case "LUCHAR":
                        // Leer el nombre del personaje que quiere luchar
                        String personaje = entrada.readUTF();
                        System.out.println("[DRAGON-SRV] " + personaje + " quiere luchar!");

                        // Llegar a la batalla (BARRERA: espera al otro con wait)
                        // Este metodo BLOQUEA hasta que ambos esten presentes
                        control.llegarABatalla(personaje);

                        // Cuando llega aqui, la batalla ya se resolvio
                        // Enviar resultado al cliente
                        boolean fueVictoria = control.fueVictoria();
                        salida.writeBoolean(fueVictoria);

                        if (fueVictoria) {
                            salida.writeUTF("DRAGON_DERROTADO");
                            salida.writeInt(50); // chispa ganada
                        } else {
                            salida.writeUTF("MALHERIDOS");
                            salida.writeInt(-20); // chispa perdida
                        }
                        break;

                    case "CONSULTAR_DRAGON":
                        // Informar de la vida actual del dragon
                        salida.writeInt(control.getVidaDragon());
                        salida.writeBoolean(control.dragonVivo());
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
            System.out.println("[DRAGON-SRV] Cliente desconectado: " + e.getMessage());
        }
    }
}

