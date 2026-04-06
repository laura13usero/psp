import java.io.*;
import java.net.Socket;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

public class HiloTaberna extends Thread {
    public static final List<Socket> clientesActivos = Collections.synchronizedList(new ArrayList<>());
    private Socket skCliente;
    private ControlTaberna control; // Instancia del monitor compartido

    public HiloTaberna(Socket skCliente, ControlTaberna control) {
        this.skCliente = skCliente;
        this.control = control;
    }

    @Override
    public void run() {
        try (DataInputStream entrada = new DataInputStream(skCliente.getInputStream());
             DataOutputStream salida = new DataOutputStream(skCliente.getOutputStream())) {

            if (ServidorMaestro.tabernaDestruida) {
                System.out.println("[TABERNA] Un cliente intenta acceder, pero el lugar está en ruinas. Conexión rechazada.");
                salida.writeUTF("LUGAR_DESTRUIDO");
                skCliente.close();
                return;
            }
            salida.writeUTF("CONEXION_OK");
            clientesActivos.add(skCliente);

            boolean continuar = true;
            while (continuar) {
                String comando = entrada.readUTF();
                String personaje;

                switch (comando) {
                    case "ENTRAR":
                        personaje = entrada.readUTF();
                        control.entrar(personaje); // Llamada correcta
                        salida.writeUTF("BIENVENIDO");
                        break;
                    case "SALIR_TABERNA":
                        personaje = entrada.readUTF();
                        control.salir(personaje); // Llamada correcta
                        salida.writeUTF("ADIOS");
                        break;
                    case "CONSULTAR_LANCE":
                        salida.writeBoolean(control.estaLance()); // Llamada correcta
                        break;
                    case "CONSULTAR_ELISABETHA":
                        salida.writeBoolean(control.estaElisabetha()); // Llamada correcta
                        break;
                    case "YA_SE_CONOCEN":
                        salida.writeBoolean(control.yaSeConocen()); // Llamada correcta
                        break;
                    case "SE_CONOCEN":
                        control.seConocen(); // Llamada correcta
                        salida.writeUTF("OK");
                        break;
                    case "REGISTRAR_CHISPA_100":
                        personaje = entrada.readUTF();
                        control.registrarChispa100(personaje); // Llamada correcta
                        salida.writeUTF("REGISTRADO");
                        break;
                    case "ESPERAR_AL_OTRO":
                        personaje = entrada.readUTF();
                        // La lógica de espera se maneja en el monitor, que ya tiene la salida
                        control.esperarAlOtro(personaje); 
                        // Una vez que esperarAlOtro termina, significa que ambos han llegado.
                        // Enviamos la confirmación final al cliente.
                        salida.writeUTF("FINAL_FELIZ");
                        break;
                    case "DESCONECTAR":
                        continuar = false;
                        salida.writeUTF("ADIOS");
                        break;
                        
                    case "DESTRUIR_LUGAR":
                        ServidorMaestro.tabernaDestruida = true;
                        System.out.println("\n[TABERNA] ¡ÉXITO! ¡La Taberna 'El Descanso del Guerrero' ha sido reducida a cenizas por el Dragón!");
                        
                        synchronized(clientesActivos) {
                            for (Socket sk : clientesActivos) {
                                if (sk != skCliente) {
                                    try { sk.close(); } catch (IOException ignore) {}
                                }
                            }
                            clientesActivos.clear();
                        }
                        
                        new Thread(() -> {
                            try { Thread.sleep(20000); } catch (InterruptedException ignore) {}
                            ServidorMaestro.tabernaDestruida = false;
                            System.out.println("[RECONSTRUCCION] ¡La Taberna 'El Descanso del Guerrero' ha sido reconstruida y vuelve a servir hidromiel!");
                        }).start();
                        
                        continuar = false;
                        break;
                        
                    default:
                        salida.writeUTF("COMANDO_DESCONOCIDO");
                        break;
                }
            }
        } catch (IOException e) {
            // System.out.println("[TABERNA] Cliente desconectado: " + e.getMessage());
        } finally {
            clientesActivos.remove(skCliente);
            try {
                skCliente.close();
            } catch (IOException e) {
                // Silencioso
            }
        }
    }
}