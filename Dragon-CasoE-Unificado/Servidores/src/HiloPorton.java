import java.io.*;
import java.net.Socket;
import java.util.Random;

/**
 * Hilo Porton - CASO E: recibe SOCORRO de otros servidores + EVACUAR de Damas.
 *
 * NUEVOS COMANDOS:
 *   SOCORRO + nombre_lugar: recibido de OTRO SERVIDOR (comunicacion srv->srv)
 *     El Porton registra la alerta y la imprime.
 *   EVACUAR + nombre_persona: recibido de las Damas que huyen del dragon.
 */
public class HiloPorton extends Thread {
    private Socket skCliente;
    private static final String[] R_CIUDAD = {"Raton Pedro","Ratona Maria","Raton Juan","Ratona Ana"};
    private static final String[] R_FORA = {"Raton del Norte","Ratona del Este","Raton del Sur","Ratona del Oeste"};
    private static final String[] PROD = {"queso fermentado","telas finas","especias","herramientas",
        "queso sin fermentar","leche cruda","pan artesano","frutas secas","miel"};

    public static volatile boolean destruido = false;

    // Contador de evacuaciones recibidas
    public static volatile int evacuaciones = 0;

    public HiloPorton(Socket sk) { this.skCliente = sk; }

    @Override
    public void run() {
        try {
            DataInputStream ent = new DataInputStream(skCliente.getInputStream());
            DataOutputStream sal = new DataOutputStream(skCliente.getOutputStream());
            boolean c = true;
            while (c) {
                String cmd = ent.readUTF();
                switch (cmd) {
                    case "VIGILAR_PORTON":
                        String g = ent.readUTF();
                        if (destruido) {
                            sal.writeUTF("LUGAR_DESTRUIDO"); sal.writeUTF(g);
                            sal.writeUTF("Porton en llamas!");
                        } else {
                            Random r = new Random();
                            if (r.nextBoolean()) {
                                String rt = R_CIUDAD[r.nextInt(R_CIUDAD.length)];
                                sal.writeUTF("LOCAL"); sal.writeUTF(rt); sal.writeUTF("Bienvenido " + rt);
                            } else {
                                String rt = R_FORA[r.nextInt(R_FORA.length)];
                                String p = PROD[r.nextInt(PROD.length)];
                                if (p.equals("queso sin fermentar") || p.equals("leche cruda")) {
                                    sal.writeUTF("RECHAZADA"); sal.writeUTF(rt); sal.writeUTF("Denegado: " + p);
                                } else {
                                    sal.writeUTF("PERMITIDA"); sal.writeUTF(rt); sal.writeUTF("Puede pasar con " + p);
                                }
                            }
                        }
                        break;

                    case "VIGILAR_LUGAR":
                        ent.readUTF(); ent.readUTF(); sal.writeUTF("OK"); break;

                    // ============================================================
                    // NUEVO: SOCORRO de otro servidor (comunicacion srv -> srv)
                    // ============================================================
                    case "SOCORRO":
                        String lugarAtacado = ent.readUTF();
                        System.out.println("[PORTON] *** SOCORRO! El " + lugarAtacado + " esta siendo atacado por el Dragon! ***");
                        System.out.println("[PORTON] Abriendo puertas para evacuacion de emergencia!");
                        sal.writeUTF("SOCORRO_RECIBIDO");
                        break;

                    // NUEVO: Evacuacion de personas (Escenario 10)
                    case "EVACUAR":
                        String nombreEvacuado = ent.readUTF();
                        evacuaciones++;
                        System.out.println("[PORTON] *** EVACUACION #" + evacuaciones + ": " + nombreEvacuado + " sale de Roedalia! ***");
                        sal.writeUTF("Evacuacion OK. " + nombreEvacuado + " esta a salvo.");
                        break;

                    // Ataque directo del dragon al Porton
                    case "ATAQUE_DRAGON":
                        destruido = true;
                        System.out.println("[PORTON] *** DRAGON ATACA EL PORTON! ***");
                        sal.writeUTF("LUGAR_DESTRUIDO");
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try { Thread.sleep(20000); } catch (InterruptedException e) { }
                                destruido = false;
                                System.out.println("[PORTON] Porton reconstruido.");
                            }
                        }).start();
                        break;

                    case "DESCONECTAR":
                        c = false; sal.writeUTF("ADIOS"); break;

                    default:
                        sal.writeUTF("COMANDO_DESCONOCIDO"); break;
                }
            }
            skCliente.close();
        } catch (IOException e) {
            System.out.println("[PORTON] Desconexion");
        }
    }
}

