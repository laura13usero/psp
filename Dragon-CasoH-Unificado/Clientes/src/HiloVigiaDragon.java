import java.io.*;
import java.net.Socket;

/**
 * HILO VIGIA DRAGON - Hilo en Clientes que periodicamente consulta
 * al servidor del dragon si esta vivo y activo.
 *
 * Como el dragon vive en el proceso servidor (Caso H), los clientes
 * no tienen acceso directo a sus variables. Este vigia se conecta
 * por socket al ServidorDragon (5003) cada 5 seg para consultar la
 * vida del dragon. Si el dragon esta vivo, marca dragonAtacando = true
 * para que las Damas evacuen. Si muere, lo marca como derrotado.
 */
public class HiloVigiaDragon extends Thread {
    @Override
    public void run() {
        System.out.println("[VIGIA] Vigilando al Dragon...");

        // Esperar a que el dragon despierte
        try { Thread.sleep(25000); } catch (InterruptedException e) { }

        while (!ClienteMaestro.simulacionTerminada && !ClienteMaestro.dragonDerrotado) {
            try {
                Socket sk = new Socket(ClienteMaestro.HOST, ClienteMaestro.PUERTO_DRAGON);
                DataOutputStream sal = new DataOutputStream(sk.getOutputStream());
                DataInputStream ent = new DataInputStream(sk.getInputStream());

                sal.writeUTF("CONSULTAR_VIDA");
                int vida = ent.readInt();
                int fase = ent.readInt();
                boolean vivo = ent.readBoolean();

                // Si el dragon esta vivo, marcar que ataca (para que Damas evacuen)
                ClienteMaestro.dragonAtacando = vivo;

                if (!vivo) {
                    ClienteMaestro.dragonDerrotado = true;
                    System.out.println("[VIGIA] *** Dragon muerto! ***");
                }

                sal.writeUTF("DESCONECTAR");
                ent.readUTF();
                sk.close();
            } catch (IOException e) {
                // Si no puede conectar, el servidor del dragon podria estar caido
            }

            // Esperar 5 seg antes de volver a consultar
            try { Thread.sleep(5000); } catch (InterruptedException e) { }
        }

        ClienteMaestro.dragonAtacando = false;
        System.out.println("[VIGIA] Fin de la vigilancia.");
    }
}

