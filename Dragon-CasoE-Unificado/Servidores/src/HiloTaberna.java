import java.io.*;
import java.net.Socket;

/**
 * Hilo Taberna - CASO E: +ATAQUE_DRAGON +destruida +DRAGON_DERROTADO.
 * Identico al Caso C pero con socorro al Porton cuando es atacada.
 */
public class HiloTaberna extends Thread {
    private Socket skCliente;
    private ControlTaberna control;
    public static volatile boolean destruida = false;
    private static final int PUERTO_PORTON = 5002;

    public HiloTaberna(Socket sk, ControlTaberna c) { this.skCliente = sk; this.control = c; }

    @Override
    public void run() {
        try {
            DataInputStream ent = new DataInputStream(skCliente.getInputStream());
            DataOutputStream sal = new DataOutputStream(skCliente.getOutputStream());
            boolean c = true;
            while (c) {
                String cmd = ent.readUTF();
                switch (cmd) {
                    case "ENTRAR":
                        String pe = ent.readUTF();
                        if (destruida) { sal.writeUTF("LUGAR_DESTRUIDO"); }
                        else { control.entrar(pe); sal.writeUTF("OK"); }
                        break;
                    case "SALIR_TABERNA": String ps = ent.readUTF(); control.salir(ps); sal.writeUTF("OK"); break;
                    case "CONSULTAR_ELISABETHA": sal.writeBoolean(control.estaElisabetha()); break;
                    case "CONSULTAR_LANCE": sal.writeBoolean(control.estaLance()); break;
                    case "YA_SE_CONOCEN": sal.writeBoolean(control.yaSeConocen()); break;
                    case "SE_CONOCEN": control.seConocen(); sal.writeUTF("CHISPA_GENERADA"); break;
                    case "REGISTRAR_CHISPA_100": String p1=ent.readUTF(); control.registrarChispa100(p1); sal.writeUTF("OK"); break;
                    case "ESPERAR_AL_OTRO": String pw=ent.readUTF(); control.esperarAlOtro(pw); sal.writeUTF("FINAL_FELIZ"); break;
                    case "DRAGON_DERROTADO": control.dragonDerrotado(); sal.writeUTF("OK"); break;

                    // Ataque del dragon + socorro al Porton (srv->srv)
                    case "ATAQUE_DRAGON":
                        destruida = true;
                        System.out.println("[TABERNA] *** DRAGON ATACA LA TABERNA! ***");
                        sal.writeUTF("LUGAR_DESTRUIDO");
                        enviarSocorro("TABERNA");
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try { Thread.sleep(20000); } catch (InterruptedException e) { }
                                destruida = false;
                                System.out.println("[TABERNA] Taberna reconstruida.");
                            }
                        }).start();
                        break;

                    case "DESCONECTAR": c = false; sal.writeUTF("ADIOS"); break;
                    default: sal.writeUTF("?"); break;
                }
            }
            skCliente.close();
        } catch (IOException e) { }
    }

    /** NUEVO: Socorro servidor->servidor (identico a HiloMercado) */
    private void enviarSocorro(String lugar) {
        try {
            Socket skSocorro = new Socket("localhost", PUERTO_PORTON);
            DataOutputStream salS = new DataOutputStream(skSocorro.getOutputStream());
            DataInputStream entS = new DataInputStream(skSocorro.getInputStream());
            salS.writeUTF("SOCORRO"); salS.writeUTF(lugar); entS.readUTF();
            System.out.println("[TABERNA -> PORTON] Socorro enviado!");
            salS.writeUTF("DESCONECTAR"); entS.readUTF(); skSocorro.close();
        } catch (IOException e) {
            System.out.println("[TABERNA] No pudo enviar socorro.");
        }
    }
}

