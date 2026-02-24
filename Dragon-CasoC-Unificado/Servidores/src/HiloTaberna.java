import java.io.*;
import java.net.Socket;

/**
 * Hilo Taberna - CASO C MODIFICADO: anade comandos ATAQUE_DRAGON y DRAGON_DERROTADO.
 * ATAQUE_DRAGON: el dragon ataca la taberna, se destruye temporalmente.
 * DRAGON_DERROTADO: Lance notifica que el dragon ha sido derrotado.
 */
public class HiloTaberna extends Thread {
    private Socket skCliente;
    private ControlTaberna control;

    // NUEVO: flag volatile que indica si la taberna esta destruida por el dragon
    public static volatile boolean destruida = false;

    public HiloTaberna(Socket skCliente, ControlTaberna control) {
        this.skCliente = skCliente; this.control = control;
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
                    case "ENTRAR":
                        String pe = entrada.readUTF();
                        // NUEVO: comprobar si la taberna esta destruida
                        if (destruida) {
                            salida.writeUTF("LUGAR_DESTRUIDO");
                            System.out.println("[TABERNA] " + pe + " llega pero la taberna esta en llamas!");
                        } else {
                            control.entrar(pe);
                            salida.writeUTF("OK");
                        }
                        break;
                    case "SALIR_TABERNA":
                        String ps = entrada.readUTF(); control.salir(ps); salida.writeUTF("OK"); break;
                    case "CONSULTAR_ELISABETHA": salida.writeBoolean(control.estaElisabetha()); break;
                    case "CONSULTAR_LANCE": salida.writeBoolean(control.estaLance()); break;
                    case "YA_SE_CONOCEN": salida.writeBoolean(control.yaSeConocen()); break;
                    case "SE_CONOCEN": control.seConocen(); salida.writeUTF("CHISPA_GENERADA"); break;
                    case "REGISTRAR_CHISPA_100":
                        String p1 = entrada.readUTF(); control.registrarChispa100(p1); salida.writeUTF("OK"); break;
                    case "ESPERAR_AL_OTRO":
                        String pw = entrada.readUTF(); control.esperarAlOtro(pw); salida.writeUTF("FINAL_FELIZ"); break;

                    // NUEVO: El dragon ataca la taberna
                    case "ATAQUE_DRAGON":
                        destruida = true;
                        System.out.println("[TABERNA] *** DRAGON ATACA LA TABERNA! En llamas! ***");
                        salida.writeUTF("LUGAR_DESTRUIDO");
                        // Reconstruccion automatica tras 20 seg
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try { Thread.sleep(20000); } catch (InterruptedException e) { }
                                destruida = false;
                                System.out.println("[TABERNA] Taberna reconstruida.");
                            }
                        }).start();
                        break;

                    // NUEVO: Lance notifica que ha derrotado al dragon
                    case "DRAGON_DERROTADO":
                        control.dragonDerrotado();
                        salida.writeUTF("OK");
                        break;

                    case "CONSULTAR_DRAGON_DERROTADO":
                        salida.writeBoolean(control.isDragonDerrotado());
                        break;

                    case "DESCONECTAR": continuar = false; salida.writeUTF("ADIOS"); break;
                    default: salida.writeUTF("COMANDO_DESCONOCIDO"); break;
                }
            }
            skCliente.close();
        } catch (IOException e) { System.out.println("[TABERNA] Desconexion: " + e.getMessage()); }
    }
}

