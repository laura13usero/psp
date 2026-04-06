import java.io.*;
import java.net.Socket;
import java.util.Random;

/**
 * HILO CABALLERO - Companero de Lance en el Porton Norte.
 * Hay 4 instancias (Caballero1..4).
 *
 * 2 acciones (50/50):
 * 1. Vigilancia (6s): Porton Norte, muralla o torres
 * 2. Hablar con Lance: timeout 25s con wait()
 *    - 75% confidencia (sin efecto)
 *    - 25% ofensa sobre Elisabetha -> Lance lo reta a duelo
 *      Si herido gravemente (20%): 30 segundos de recuperacion
 *
 * COMUNICACION con Lance: buzon compartido (lockLance + wait/notifyAll)
 * COMUNICACION con Porton Norte: via SOCKET
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
            // Si esta herido, recuperarse 30 segundos
            if (herido) {
                long transcurrido = System.currentTimeMillis() - tiempoHerida;
                if (transcurrido < 30000) {
                    long restante = (30000 - transcurrido) / 1000;
                    System.out.println("[" + nombreCab + "] Herido, recuperandose... (" + restante + "s)");
                    try { Thread.sleep(5000); } catch (InterruptedException e) { }
                    continue;
                } else {
                    herido = false;
                    System.out.println("[" + nombreCab + "] Recuperado de sus heridas!");
                }
            }

            int accion = random.nextInt(2);

            if (accion == 0) {
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
                System.out.println("[" + nombreCab + "] No pudo conectar al Porton: " + e.getMessage());
            }
        }

        try { Thread.sleep(6000); } catch (InterruptedException e) { }
        System.out.println("[" + nombreCab + "] Termina vigilancia en: " + lugar);
    }

    private void hablarConLance() {
        System.out.println("[" + nombreCab + "] Quiere hablar con Lance...");

        // 25% ofensa, 75% confidencia
        String mensaje;
        if (random.nextInt(100) < 25) {
            mensaje = "OFENSA_ELISABETHA";
            System.out.println("[" + nombreCab + "] Va a decir algo ofensivo sobre Elisabetha...");
        } else {
            mensaje = "CONFIDENCIA";
            System.out.println("[" + nombreCab + "] Va a contar una confidencia...");
        }

        synchronized (ClienteMaestro.lockLance) {
            long inicio = System.currentTimeMillis();

            // CORRECCION: Esperar si el buzon ya esta ocupado por otro caballero
            while (ClienteMaestro.hayPeticionCaballero && System.currentTimeMillis() - inicio < 25000) {
                try {
                    ClienteMaestro.lockLance.wait(1000);
                } catch (InterruptedException e) { }
            }

            // Si tras esperar sigue ocupado, desistir
            if (ClienteMaestro.hayPeticionCaballero) {
                System.out.println("[" + nombreCab + "] Lance esta ocupado con otro caballero. Desiste.");
                return;
            }

            ClienteMaestro.caballeroQuePide = nombreCab;
            ClienteMaestro.mensajeCaballero = mensaje;
            ClienteMaestro.hayPeticionCaballero = true;
            ClienteMaestro.lockLance.notifyAll(); // Avisar que hay peticion

            while (System.currentTimeMillis() - inicio < 25000 && ClienteMaestro.hayPeticionCaballero) {
                try {
                    ClienteMaestro.lockLance.wait(2000);
                } catch (InterruptedException e) { }
            }

            boolean atendido = !ClienteMaestro.hayPeticionCaballero;
            if (!atendido) {
                // Limpiar buzon si nos vamos por timeout
                if (ClienteMaestro.caballeroQuePide != null && ClienteMaestro.caballeroQuePide.equals(nombreCab)) {
                    ClienteMaestro.hayPeticionCaballero = false;
                    ClienteMaestro.caballeroQuePide = null;
                    ClienteMaestro.mensajeCaballero = null;
                    ClienteMaestro.lockLance.notifyAll(); // Avisar a otros caballeros esperando
                }
                System.out.println("[" + nombreCab + "] Lance no lo atendio (timeout). Vuelve a labores.");
            } else {
                System.out.println("[" + nombreCab + "] Lance lo atendio.");
                if (mensaje.equals("OFENSA_ELISABETHA")) {
                    System.out.println("[" + nombreCab + "] Lance lo ha retado a duelo y ha sido derrotado!");
                    // Posibilidad de quedar herido
                    if (random.nextInt(100) < 20) {
                        herido = true;
                        tiempoHerida = System.currentTimeMillis();
                        System.out.println("[" + nombreCab + "] Herido gravemente! 30 segundos de recuperacion.");
                    }
                }
            }
        }
    }
}
