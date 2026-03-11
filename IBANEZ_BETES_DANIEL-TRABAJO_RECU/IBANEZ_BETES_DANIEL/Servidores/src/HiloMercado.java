import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class HiloMercado extends Thread {
    private Socket skCliente;
    private static final String[] PRODUCTOS = {
        "queso","pan recien horneado","especias del lejano oriente",
        "telas para vestidos","jugo de grosella","repelente de gatos",
        "brillantes collares de ratona","cucharas de boj tamano raton"
    };

    public static volatile boolean destruido = false;
    private static final List<Socket> clientesActivos = Collections.synchronizedList(new ArrayList<Socket>());

    public HiloMercado(Socket sk) { this.skCliente = sk; }

    @Override
    public void run() {
        clientesActivos.add(skCliente);
        try {
            DataInputStream entrada = new DataInputStream(skCliente.getInputStream());
            DataOutputStream salida = new DataOutputStream(skCliente.getOutputStream());
            boolean c = true;
            while (c) {
                String cmd = entrada.readUTF();

                if (destruido && !"ATAQUE_DRAGON".equals(cmd) && !"DESCONECTAR".equals(cmd)) {
                    salida.writeUTF("LUGAR_DESTRUIDO");
                    c = false;
                    break;
                }

                switch (cmd) {
                    case "VISITAR_MERCADO":
                        String nom = entrada.readUTF();
                        if (destruido) {
                            salida.writeUTF("LUGAR_DESTRUIDO");
                            System.out.println("[MERCADO] " + nom + " huye! Mercado en llamas!");
                        } else {
                            salida.writeUTF("MERCADO_OK");
                            Random r = new Random();
                            String[] of = new String[5]; boolean[] u = new boolean[PRODUCTOS.length];
                            for (int i=0;i<5;i++){int x; do{x=r.nextInt(PRODUCTOS.length);}while(u[x]); u[x]=true; of[i]=PRODUCTOS[x];}
                            salida.writeInt(5);
                            for (int i=0;i<5;i++) salida.writeUTF(of[i]);
                            int el = entrada.readInt();
                            salida.writeUTF("Gracias por comprar " + of[Math.max(0,Math.min(el,4))] + ", " + nom + "!");
                        }
                        break;

                    case "ATAQUE_DRAGON":
                        if (!destruido) {
                            destruido = true;
                            System.out.println("[MERCADO] *** DRAGON ATACA EL MERCADO! Reducido a cenizas! ***");
                            expulsarClientesActivos(skCliente);
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    try { Thread.sleep(20000); } catch (InterruptedException e) { }
                                    destruido = false;
                                    System.out.println("[MERCADO] Mercado reconstruido y operativo.");
                                }
                            }).start();
                        }
                        salida.writeUTF("LUGAR_DESTRUIDO");
                        break;

                    case "DESCONECTAR": c = false; salida.writeUTF("ADIOS"); break;
                    default: salida.writeUTF("COMANDO_DESCONOCIDO"); break;
                }
            }
        } catch (IOException e) {
            System.out.println("[MERCADO] Desconexion");
        } finally {
            clientesActivos.remove(skCliente);
            try { skCliente.close(); } catch (IOException e) { }
        }
    }

    private static void expulsarClientesActivos(Socket atacante) {
        List<Socket> copia;
        synchronized (clientesActivos) {
            copia = new ArrayList<Socket>(clientesActivos);
        }
        for (Socket s : copia) {
            if (s == atacante || s.isClosed()) {
                continue;
            }
            try { s.close(); } catch (IOException e) { }
        }
    }
}
