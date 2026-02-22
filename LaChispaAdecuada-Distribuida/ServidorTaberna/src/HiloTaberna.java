import java.io.*;
import java.net.Socket;

public class HiloTaberna extends Thread {
    private Socket skCliente;
    private ControlTaberna control;

    public HiloTaberna(Socket skCliente, ControlTaberna control) {
        this.skCliente = skCliente;
        this.control = control;
    }

    @Override
    public void run() {
        try {
            DataInputStream entrada = new DataInputStream(skCliente.getInputStream());
            DataOutputStream salida = new DataOutputStream(skCliente.getOutputStream());

            boolean continuar = true;
            while (continuar) {
                String comando = entrada.readUTF();

                switch (comando) {
                    case "ENTRAR":
                        String personajeEntrar = entrada.readUTF();
                        control.entrar(personajeEntrar);
                        salida.writeUTF("OK");
                        break;

                    case "SALIR_TABERNA":
                        String personajeSalir = entrada.readUTF();
                        control.salir(personajeSalir);
                        salida.writeUTF("OK");
                        break;

                    case "CONSULTAR_ELISABETHA":
                        salida.writeBoolean(control.estaElisabetha());
                        break;

                    case "CONSULTAR_LANCE":
                        salida.writeBoolean(control.estaLance());
                        break;

                    case "YA_SE_CONOCEN":
                        salida.writeBoolean(control.yaSeConocen());
                        break;

                    case "SE_CONOCEN":
                        control.seConocen();
                        salida.writeUTF("CHISPA_GENERADA");
                        break;

                    case "REGISTRAR_CHISPA_100":
                        String personaje100 = entrada.readUTF();
                        control.registrarChispa100(personaje100);
                        salida.writeUTF("OK");
                        break;

                    case "CONSULTAR_CHISPA_100_ELISABETHA":
                        salida.writeBoolean(control.elisabethaChispa100());
                        break;

                    case "CONSULTAR_CHISPA_100_LANCE":
                        salida.writeBoolean(control.lanceChispa100());
                        break;

                    case "CONSULTAR_AMBOS_100":
                        salida.writeBoolean(control.ambosChispa100());
                        break;

                    case "ESPERAR_AL_OTRO":
                        String personajeEspera = entrada.readUTF();
                        control.esperarAlOtro(personajeEspera);
                        salida.writeUTF("FINAL_FELIZ");
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
            System.out.println("[TABERNA] Cliente desconectado: " + e.getMessage());
        }
    }
}

