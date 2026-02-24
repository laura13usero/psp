import java.io.*;
import java.net.Socket;

/**
 * Hilo que atiende a UN cliente del Servidor Dragon.
 *
 * PROTOCOLO:
 *   ATACAR + dano(int) -> contraataque(int) + vidaRestante(int) + vivo(boolean)
 *   CURAR_DRAGON + curacion(int) -> vidaRestante(int)
 *   CONSULTAR_VIDA -> vidaRestante(int) + fase(int) + vivo(boolean)
 *   DESCONECTAR -> ADIOS
 *
 * PATRON: Mismo switch que HiloMercado/HiloTaberna pero con comandos de combate.
 */
public class HiloDragonServidor extends Thread {
    private Socket skCliente;
    private ControlDragon control;

    public HiloDragonServidor(Socket sk, ControlDragon ctrl) {
        this.skCliente = sk; this.control = ctrl;
    }

    @Override
    public void run() {
        try {
            DataInputStream entrada = new DataInputStream(skCliente.getInputStream());
            DataOutputStream salida = new DataOutputStream(skCliente.getOutputStream());
            boolean continuar = true;

            while (continuar) {
                String cmd = entrada.readUTF();
                switch (cmd) {
                    // Lance o Caballeros atacan al dragon
                    case "ATACAR":
                        int dano = entrada.readInt();
                        String atacante = entrada.readUTF();
                        System.out.println("[DRAGON-SRV] " + atacante + " ataca con " + dano + " de dano!");

                        int contraataque = control.recibirAtaque(dano);
                        salida.writeInt(contraataque);           // Dano del contraataque
                        salida.writeInt(control.getVidaDragon()); // Vida restante
                        salida.writeBoolean(control.dragonVivo()); // Sigue vivo?
                        break;

                    // Alquimistas curan al dragon (PATRON ANTAGONICO)
                    case "CURAR_DRAGON":
                        int curacion = entrada.readInt();
                        String alquimista = entrada.readUTF();
                        System.out.println("[DRAGON-SRV] " + alquimista + " cura al dragon +" + curacion + "!");

                        control.curar(curacion);
                        salida.writeInt(control.getVidaDragon()); // Vida despues de curar
                        break;

                    // Consultar estado del dragon
                    case "CONSULTAR_VIDA":
                        salida.writeInt(control.getVidaDragon());
                        salida.writeInt(control.getFase());
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
            System.out.println("[DRAGON-SRV] Desconexion");
        }
    }
}

