import java.io.*;
import java.net.Socket;
import java.util.Random;

/**
 * HILO CABALLERO - Companero de Lance en el Porton Norte (identico al original).
 * 2 acciones (50/50): vigilancia o hablar con Lance.
 * Comunicacion con Lance via buzon compartido (lockLance + wait/notifyAll).
 * Comunicacion con Porton via SOCKET.
 */
public class HiloCaballero extends Thread {
    private int numeroCab;
    private String nombreCab;
    private Random random = new Random();
    private boolean herido = false;
    private long tiempoHerida = 0;

    public HiloCaballero(int numero) {
        this.numeroCab = numero;
        this.nombreCab = "Caballero" + numero;
    }

    @Override
    public void run() {
        System.out.println("[" + nombreCab + "] Comienza la simulacion.");
        while (!ClienteMaestro.simulacionTerminada) {
            if (herido) {
                long transcurrido = System.currentTimeMillis() - tiempoHerida;
                if (transcurrido < 30000) {
                    System.out.println("[" + nombreCab + "] Herido, recuperandose...");
                    try { Thread.sleep(5000); } catch (InterruptedException e) { }
                    continue;
                } else {
                    herido = false;
                    System.out.println("[" + nombreCab + "] Recuperado!");
                }
            }

            if (random.nextInt(2) == 0) {
                realizarVigilancia();
            } else {
                hablarConLance();
            }
        }
        System.out.println("[" + nombreCab + "] Simulacion terminada.");
    }

    private void realizarVigilancia() {
        String[] lugares = {"Porton Norte", "muralla", "torres"};
        String lugar = lugares[random.nextInt(lugares.length)];
        System.out.println("[" + nombreCab + "] Vigila: " + lugar);

        if (lugar.equals("Porton Norte")) {
            try {
                Socket skPorton = new Socket(ClienteMaestro.HOST, ClienteMaestro.PUERTO_PORTON);
                DataOutputStream salida = new DataOutputStream(skPorton.getOutputStream());
                DataInputStream entrada = new DataInputStream(skPorton.getInputStream());
                salida.writeUTF("VIGILAR_LUGAR");
                salida.writeUTF(nombreCab);
                salida.writeUTF(lugar);
                entrada.readUTF();
                salida.writeUTF("DESCONECTAR");
                entrada.readUTF();
                skPorton.close();
            } catch (IOException e) {
                System.out.println("[" + nombreCab + "] No pudo conectar al Porton.");
            }
        }

        try { Thread.sleep(6000); } catch (InterruptedException e) { }
    }

    private void hablarConLance() {
        System.out.println("[" + nombreCab + "] Quiere hablar con Lance...");
        String mensaje;
        if (random.nextInt(100) < 25) {
            mensaje = "OFENSA_ELISABETHA";
        } else {
            mensaje = "CONFIDENCIA";
        }

        synchronized (ClienteMaestro.lockLance) {
            long inicio = System.currentTimeMillis();
            ClienteMaestro.caballeroQuePide = nombreCab;
            ClienteMaestro.mensajeCaballero = mensaje;
            ClienteMaestro.hayPeticionCaballero = true;

            while (System.currentTimeMillis() - inicio < 25000 && ClienteMaestro.hayPeticionCaballero) {
                try { ClienteMaestro.lockLance.wait(2000); } catch (InterruptedException e) { }
            }

            boolean atendido = !ClienteMaestro.hayPeticionCaballero;
            if (!atendido) {
                ClienteMaestro.hayPeticionCaballero = false;
                ClienteMaestro.caballeroQuePide = null;
                ClienteMaestro.mensajeCaballero = null;
                System.out.println("[" + nombreCab + "] Lance no lo atendio (timeout).");
            } else {
                System.out.println("[" + nombreCab + "] Lance lo atendio.");
                if (mensaje.equals("OFENSA_ELISABETHA")) {
                    System.out.println("[" + nombreCab + "] Lance lo ha retado a duelo!");
                    if (random.nextInt(100) < 20) {
                        herido = true;
                        tiempoHerida = System.currentTimeMillis();
                        System.out.println("[" + nombreCab + "] Herido gravemente! 30s de recuperacion.");
                    }
                }
            }
        }
    }
}

