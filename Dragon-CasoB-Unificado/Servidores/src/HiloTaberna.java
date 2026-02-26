import java.io.*;
import java.net.Socket;

/** Hilo que atiende a UN cliente en la Taberna (identico al original). */
public class HiloTaberna extends Thread {
    private Socket skCliente;
    private ControlTaberna control;
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
                    case "ENTRAR": String pe = entrada.readUTF(); control.entrar(pe); salida.writeUTF("OK"); break;
                    case "SALIR_TABERNA": String ps = entrada.readUTF(); control.salir(ps); salida.writeUTF("OK"); break;
                    case "CONSULTAR_ELISABETHA": salida.writeBoolean(control.estaElisabetha()); break;
                    case "CONSULTAR_LANCE": salida.writeBoolean(control.estaLance()); break;
                    case "YA_SE_CONOCEN": salida.writeBoolean(control.yaSeConocen()); break;
                    case "SE_CONOCEN": control.seConocen(); salida.writeUTF("CHISPA_GENERADA"); break;
                    case "REGISTRAR_CHISPA_100": String p1 = entrada.readUTF(); control.registrarChispa100(p1); salida.writeUTF("OK"); break;
                    case "ESPERAR_AL_OTRO": String pw = entrada.readUTF(); control.esperarAlOtro(pw); salida.writeUTF("FINAL_FELIZ"); break;
                    case "DESCONECTAR": continuar = false; salida.writeUTF("ADIOS"); break;
                    default: salida.writeUTF("COMANDO_DESCONOCIDO"); break;
                }
            }
            skCliente.close();
        } catch (IOException e) { System.out.println("[TABERNA] Desconexion: " + e.getMessage()); }
    }
}

