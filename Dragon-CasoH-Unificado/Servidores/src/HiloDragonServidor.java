import java.io.*;
import java.net.Socket;

/**
 * Hilo que atiende a UN cliente del Servidor Dragon (PARTE SERVIDOR).
 * Protocolo identico al Caso F: ATACAR, CONSULTAR_VIDA, DESCONECTAR.
 * Recibe ataques de Lance/Caballeros y devuelve contraataques.
 */
public class HiloDragonServidor extends Thread {
    private Socket skCliente;
    private ControlDragon control;

    public HiloDragonServidor(Socket sk, ControlDragon ctrl) {
        this.skCliente = sk;
        this.control = ctrl;
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
                        System.out.println("[DRAGON-SRV] " + atacante + " ataca con " + dano + "!");
                        int contraataque = control.recibirAtaque(dano);
                        salida.writeInt(contraataque);
                        salida.writeInt(control.getVidaDragon());
                        salida.writeBoolean(control.dragonVivo());
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
            System.out.println("[DRAGON-SRV] Desconexion de cliente.");
        }
    }
}

