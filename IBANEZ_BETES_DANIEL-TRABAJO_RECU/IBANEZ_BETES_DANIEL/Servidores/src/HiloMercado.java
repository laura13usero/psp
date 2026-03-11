import java.io.*;
import java.net.Socket;
import java.util.Random;

public class HiloMercado extends Thread {
    private Socket skCliente;
    private static final String[] PRODUCTOS = {
        "queso","pan recien horneado","especias del lejano oriente",
        "telas para vestidos","jugo de grosella","repelente de gatos",
        "brillantes collares de ratona","cucharas de boj tamano raton"
    };

    // NUEVO: flag volatile de destruccion
    public static volatile boolean destruido = false;

    public HiloMercado(Socket sk) { this.skCliente = sk; }

    @Override
    public void run() {
        try {
            DataInputStream entrada = new DataInputStream(skCliente.getInputStream());
            DataOutputStream salida = new DataOutputStream(skCliente.getOutputStream());
            boolean c = true;
            while (c) {
                String cmd = entrada.readUTF();
                switch (cmd) {
                    case "VISITAR_MERCADO":
                        String nom = entrada.readUTF();
                        // NUEVO: comprobar si destruido
                        if (destruido) {
                            salida.writeUTF("MERCADO_ATACADO");
                            System.out.println("[MERCADO] " + nom + " huye! Mercado en llamas!");
                        } else {
                            salida.writeUTF("MERCADO_OK");
                            // Atencion normal
                            Random r = new Random();
                            String[] of = new String[5]; boolean[] u = new boolean[PRODUCTOS.length];
                            for (int i=0;i<5;i++){int x; do{x=r.nextInt(PRODUCTOS.length);}while(u[x]); u[x]=true; of[i]=PRODUCTOS[x];}
                            salida.writeInt(5);
                            for (int i=0;i<5;i++) salida.writeUTF(of[i]);
                            int el = entrada.readInt();
                            salida.writeUTF("Gracias por comprar " + of[Math.max(0,Math.min(el,4))] + ", " + nom + "!");
                        }
                        break;

                    // NUEVO: Dragon ataca el mercado
                    case "ATAQUE_DRAGON":
                        destruido = true;
                        System.out.println("[MERCADO] *** DRAGON ATACA EL MERCADO! En llamas! ***");
                        salida.writeUTF("LUGAR_DESTRUIDO");
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try { Thread.sleep(20000); } catch (InterruptedException e) { }
                                destruido = false;
                                System.out.println("[MERCADO] Mercado reconstruido.");
                            }
                        }).start();
                        break;

                    case "DESCONECTAR": c = false; salida.writeUTF("ADIOS"); break;
                    default: salida.writeUTF("COMANDO_DESCONOCIDO"); break;
                }
            }
            skCliente.close();
        } catch (IOException e) { System.out.println("[MERCADO] Desconexion"); }
    }
}

