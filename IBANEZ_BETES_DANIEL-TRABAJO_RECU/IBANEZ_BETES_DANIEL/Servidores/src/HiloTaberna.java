import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HiloTaberna extends Thread {
    private Socket skCliente;
    private ControlTaberna control;

    public static volatile boolean destruida = false;
    private static final List<Socket> clientesActivos = Collections.synchronizedList(new ArrayList<Socket>());

    public HiloTaberna(Socket skCliente, ControlTaberna control) {
        this.skCliente = skCliente; this.control = control;
    }

    @Override
    public void run() {
        clientesActivos.add(skCliente);
        try {
            DataInputStream entrada = new DataInputStream(skCliente.getInputStream());
            DataOutputStream salida = new DataOutputStream(skCliente.getOutputStream());
            boolean continuar = true;
            while (continuar) {
                String cmd = entrada.readUTF();

                if (destruida && !"ATAQUE_DRAGON".equals(cmd) && !"DESCONECTAR".equals(cmd)) {
                    salida.writeUTF("LUGAR_DESTRUIDO");
                    continuar = false;
                    break;
                }

                switch (cmd) {
                    case "ENTRAR":
                        String pe = entrada.readUTF();
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
                        String pw = entrada.readUTF();
                        if (destruida) {
                            salida.writeUTF("LUGAR_DESTRUIDO");
                        } else {
                            boolean finalFeliz = control.esperarAlOtro(pw);
                            salida.writeUTF(finalFeliz ? "FINAL_FELIZ" : "LUGAR_DESTRUIDO");
                        }
                        break;

                    case "ATAQUE_DRAGON":
                        if (!destruida) {
                            destruida = true;
                            control.marcarDestruidaYExpulsar();
                            System.out.println("[TABERNA] *** DRAGON ATACA LA TABERNA! Reducida a cenizas! ***");
                            expulsarClientesActivos(skCliente);
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    try { Thread.sleep(20000); } catch (InterruptedException e) { }
                                    destruida = false;
                                    control.marcarReconstruida();
                                    System.out.println("[TABERNA] Taberna reconstruida y operativa.");
                                }
                            }).start();
                        }
                        salida.writeUTF("LUGAR_DESTRUIDO");
                        break;

                    case "DESCONECTAR": continuar = false; salida.writeUTF("ADIOS"); break;
                    default: salida.writeUTF("COMANDO_DESCONOCIDO"); break;
                }
            }
        } catch (IOException e) {
            System.out.println("[TABERNA] Desconexion: " + e.getMessage());
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
