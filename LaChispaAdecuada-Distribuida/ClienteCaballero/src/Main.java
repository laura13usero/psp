import java.io.*;
import java.net.Socket;
import java.util.Random;
import java.util.Scanner;

public class Main {
    static boolean herido = false;
    static long tiempoHerida = 0;

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        System.out.println("==============================================");
        System.out.println("  CABALLERO DEL PORTON NORTE - ROEDALIA");
        System.out.println("==============================================");

        System.out.print("Numero de este caballero (1-4): ");
        int numeroCab = sc.nextInt();
        sc.nextLine();
        String nombreCab = "Caballero" + numeroCab;

        System.out.print("IP de Lance (localhost): ");
        String input = sc.nextLine().trim();
        String hostLance = input.isEmpty() ? "localhost" : input;

        System.out.print("Puerto de Lance (6001): ");
        input = sc.nextLine().trim();
        int puertoLance = input.isEmpty() ? 6001 : Integer.parseInt(input);

        System.out.print("IP del Porton Norte (localhost): ");
        input = sc.nextLine().trim();
        String hostPorton = input.isEmpty() ? "localhost" : input;

        System.out.print("Puerto del Porton Norte (5002): ");
        input = sc.nextLine().trim();
        int puertoPorton = input.isEmpty() ? 5002 : Integer.parseInt(input);

        Random random = new Random();
        System.out.println("[" + nombreCab + "] Comienza la simulacion.");

        while (true) {
            // Si esta herido, recuperarse 30 segundos
            if (herido) {
                long transcurrido = System.currentTimeMillis() - tiempoHerida;
                if (transcurrido < 30000) {
                    long restante = (30000 - transcurrido) / 1000;
                    System.out.println("[" + nombreCab + "] Esta herido, recuperandose... (" + restante + "s restantes)");
                    try { Thread.sleep(5000); } catch (InterruptedException e) { }
                    continue;
                } else {
                    herido = false;
                    System.out.println("[" + nombreCab + "] Se ha recuperado de sus heridas!");
                }
            }

            // Elegir accion aleatoria
            int accion = random.nextInt(2);

            if (accion == 0) {
                realizarVigilancia(nombreCab, hostPorton, puertoPorton, random);
            } else {
                hablarConLance(nombreCab, hostLance, puertoLance, random);
            }
        }
    }

    static void realizarVigilancia(String nombreCab, String hostPorton, int puertoPorton, Random random) {
        String[] lugares = {"Porton Norte", "muralla", "torres"};
        String lugar = lugares[random.nextInt(lugares.length)];
        System.out.println("[" + nombreCab + "] Realiza vigilancia en: " + lugar);

        if (lugar.equals("Porton Norte")) {
            try {
                Socket skPorton = new Socket(hostPorton, puertoPorton);
                DataOutputStream salida = new DataOutputStream(skPorton.getOutputStream());
                DataInputStream entrada = new DataInputStream(skPorton.getInputStream());

                salida.writeUTF("VIGILAR_LUGAR");
                salida.writeUTF(nombreCab);
                salida.writeUTF(lugar);
                entrada.readUTF(); // VIGILANCIA_OK

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

    static void hablarConLance(String nombreCab, String hostLance, int puertoLance, Random random) {
        System.out.println("[" + nombreCab + "] Quiere hablar con Lance...");

        // 25% ofensa, 75% confidencia
        String mensaje;
        if (random.nextInt(100) < 25) {
            mensaje = "OFENSA_ELISABETHA";
            System.out.println("[" + nombreCab + "] Va a decir algo ofensivo sobre Elisabetha...");
        } else {
            mensaje = "CONFIDENCIA";
            System.out.println("[" + nombreCab + "] Va a contar una confidencia personal...");
        }

        try {
            Socket skLance = new Socket(hostLance, puertoLance);
            DataOutputStream salida = new DataOutputStream(skLance.getOutputStream());
            DataInputStream entrada = new DataInputStream(skLance.getInputStream());

            salida.writeUTF("CABALLERO");
            salida.writeUTF(nombreCab);
            salida.writeUTF(mensaje);

            boolean atendido = entrada.readBoolean();

            if (atendido) {
                System.out.println("[" + nombreCab + "] Lance lo atendio.");
                if (mensaje.equals("OFENSA_ELISABETHA")) {
                    boolean huboDuelo = entrada.readBoolean();
                    if (huboDuelo) {
                        System.out.println("[" + nombreCab + "] Lance lo ha retado a duelo y ha sido derrotado!");
                        // 20% de quedar herido gravemente (calculado en Lance)
                        if (random.nextInt(100) < 20) {
                            herido = true;
                            tiempoHerida = System.currentTimeMillis();
                            System.out.println("[" + nombreCab + "] Ha sido herido gravemente! 30 segundos de recuperacion.");
                        }
                    }
                }
            } else {
                System.out.println("[" + nombreCab + "] Lance no lo atendio (timeout 25s). Vuelve a sus labores.");
            }

            skLance.close();
        } catch (IOException e) {
            System.out.println("[" + nombreCab + "] No pudo contactar con Lance: " + e.getMessage());
            try { Thread.sleep(3000); } catch (InterruptedException ex) { }
        }
    }
}

