import java.io.*;
import java.net.Socket;
import java.util.Random;

public class HiloPorton extends Thread {
    private Socket skCliente;
    private static final String[] R_CIUDAD = {"Raton Pedro","Ratona Maria","Raton Juan","Ratona Ana"};
    private static final String[] R_FORA = {"Raton del Norte","Ratona del Este","Raton del Sur","Ratona del Oeste"};
    private static final String[] PROD = {"queso fermentado","telas finas","especias","herramientas","queso sin fermentar","leche cruda","pan artesano","frutas secas","miel"};

    // NUEVO: flag volatile de destruccion
    public static volatile boolean destruido = false;

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
                        // NUEVO: comprobar si destruido
                        if (destruido) {
                            sal.writeUTF("LUGAR_DESTRUIDO");
                            sal.writeUTF(g);
                            sal.writeUTF("El Porton esta en llamas! No se puede vigilar!");
                            System.out.println("[PORTON] " + g + " huye! Porton en llamas!");
                        } else {
                            Random r = new Random();
                            if (r.nextBoolean()) {
                                String rt=R_CIUDAD[r.nextInt(R_CIUDAD.length)];
                                sal.writeUTF("LOCAL"); sal.writeUTF(rt); sal.writeUTF("Bienvenido "+rt);
                            } else {
                                String rt=R_FORA[r.nextInt(R_FORA.length)]; String p=PROD[r.nextInt(PROD.length)];
                                if(p.equals("queso sin fermentar")||p.equals("leche cruda")){
                                    sal.writeUTF("RECHAZADA"); sal.writeUTF(rt); sal.writeUTF("Denegado: "+p);
                                } else {
                                    sal.writeUTF("PERMITIDA"); sal.writeUTF(rt); sal.writeUTF("Puede pasar con "+p);
                                }
                            }
                        }
                        break;
                    case "VIGILAR_LUGAR": ent.readUTF(); ent.readUTF(); sal.writeUTF("OK"); break;

                    // NUEVO: Dragon ataca el porton
                    case "ATAQUE_DRAGON":
                        destruido = true;
                        System.out.println("[PORTON] *** DRAGON ATACA EL PORTON! En llamas! ***");
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

                    case "DESCONECTAR": c=false; sal.writeUTF("ADIOS"); break;
                    default: sal.writeUTF("?"); break;
                }
            }
            skCliente.close();
        } catch (IOException e) { System.out.println("[PORTON] Desconexion"); }
    }
}

