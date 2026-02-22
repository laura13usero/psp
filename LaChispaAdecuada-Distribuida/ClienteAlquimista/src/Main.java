import java.io.*;
import java.net.Socket;
import java.util.Random;
import java.util.Scanner;

public class Main {
    static int pocionesElisabetha = 0;
    static int pocionesLance = 0;

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        System.out.println("==============================================");
        System.out.println("  ALQUIMISTA DEL REINO - ROEDALIA");
        System.out.println("==============================================");

        System.out.print("Numero de este alquimista (1-2): ");
        int numeroAlq = sc.nextInt();
        sc.nextLine();
        String nombreAlq = "Alquimista" + numeroAlq;

        System.out.print("IP de Elisabetha (localhost): ");
        String input = sc.nextLine().trim();
        String hostElisabetha = input.isEmpty() ? "localhost" : input;

        System.out.print("Puerto de Elisabetha (6000): ");
        input = sc.nextLine().trim();
        int puertoElisabetha = input.isEmpty() ? 6000 : Integer.parseInt(input);

        System.out.print("IP de Lance (localhost): ");
        input = sc.nextLine().trim();
        String hostLance = input.isEmpty() ? "localhost" : input;

        System.out.print("Puerto de Lance (6001): ");
        input = sc.nextLine().trim();
        int puertoLance = input.isEmpty() ? 6001 : Integer.parseInt(input);

        Random random = new Random();
        System.out.println("[" + nombreAlq + "] Comienza la simulacion. Estudiando calderos...");

        // Siempre comienzan estudiando sus calderos
        estudiarCalderos(nombreAlq, random);

        while (true) {
            // Probabilidades: 60% estudiar, 20% Elisabetha, 20% Lance
            int accion = random.nextInt(100);

            if (accion < 60) {
                estudiarCalderos(nombreAlq, random);
            } else if (accion < 80) {
                visitarElisabetha(nombreAlq, hostElisabetha, puertoElisabetha, random);
            } else {
                visitarLance(nombreAlq, hostLance, puertoLance, random);
            }
        }
    }

    static void estudiarCalderos(String nombreAlq, Random random) {
        System.out.println("[" + nombreAlq + "] Estudia sus calderos de pociones magicas...");
        try { Thread.sleep(30000); } catch (InterruptedException e) { }

        // 30% pocion para Elisabetha, 30% pocion para Lance, 40% fracaso
        int resultado = random.nextInt(100);
        if (resultado < 30) {
            pocionesElisabetha++;
            System.out.println("[" + nombreAlq + "] Ha creado una pocion para Elisabetha! Alacena: " + pocionesElisabetha + " pociones para Elisabetha, " + pocionesLance + " para Lance");
        } else if (resultado < 60) {
            pocionesLance++;
            System.out.println("[" + nombreAlq + "] Ha creado una excusa/pocion para Lance! Alacena: " + pocionesElisabetha + " pociones para Elisabetha, " + pocionesLance + " para Lance");
        } else {
            System.out.println("[" + nombreAlq + "] Fracasa en el intento! La pocion explota. Alacena sin cambios.");
        }
    }

    static void visitarElisabetha(String nombreAlq, String host, int puerto, Random random) {
        if (pocionesElisabetha <= 0) {
            System.out.println("[" + nombreAlq + "] Quiere visitar a Elisabetha pero NO tiene pociones! Se lamenta dando grandes voces!");
            try { Thread.sleep(3000); } catch (InterruptedException e) { }
            return;
        }

        System.out.println("[" + nombreAlq + "] Se dirige a visitar a Elisabetha con un 'tonico de belleza'...");
        try { Thread.sleep(5000); } catch (InterruptedException e) { }

        try {
            Socket skElisabetha = new Socket(host, puerto);
            DataOutputStream salida = new DataOutputStream(skElisabetha.getOutputStream());
            DataInputStream entrada = new DataInputStream(skElisabetha.getInputStream());

            salida.writeUTF("ALQUIMISTA");
            salida.writeUTF(nombreAlq);
            salida.writeUTF("POCION");

            boolean atendido = entrada.readBoolean();
            int chispaActual = entrada.readInt();

            pocionesElisabetha--;

            if (atendido) {
                System.out.println("[" + nombreAlq + "] Elisabetha fue atendida. Chispa actual de Elisabetha: " + chispaActual);
            } else {
                System.out.println("[" + nombreAlq + "] No pudo afectar a Elisabetha. Se lamenta dando grandes voces!");
            }

            skElisabetha.close();
        } catch (IOException e) {
            System.out.println("[" + nombreAlq + "] No pudo contactar con Elisabetha: " + e.getMessage());
            try { Thread.sleep(3000); } catch (InterruptedException ex) { }
        }
    }

    static void visitarLance(String nombreAlq, String host, int puerto, Random random) {
        // Decidir accion: 80% pocion, 20% amenaza
        int tipoAccion = random.nextInt(100);

        if (tipoAccion < 80) {
            // Intenta usar pocion
            if (pocionesLance <= 0) {
                System.out.println("[" + nombreAlq + "] Quiere usar una pocion contra Lance pero NO tiene! Se lamenta dando grandes voces!");
                try { Thread.sleep(3000); } catch (InterruptedException e) { }
                return;
            }

            System.out.println("[" + nombreAlq + "] Se dirige a visitar a Lance con una pocion...");
            try { Thread.sleep(7000); } catch (InterruptedException e) { }

            try {
                Socket skLance = new Socket(host, puerto);
                DataOutputStream salida = new DataOutputStream(skLance.getOutputStream());
                DataInputStream entrada = new DataInputStream(skLance.getInputStream());

                salida.writeUTF("ALQUIMISTA");
                salida.writeUTF(nombreAlq);
                salida.writeUTF("POCION");

                boolean atendido = entrada.readBoolean();
                int chispaActual = entrada.readInt();

                pocionesLance--;

                if (atendido) {
                    System.out.println("[" + nombreAlq + "] Lance fue atendido. Chispa actual de Lance: " + chispaActual);
                } else {
                    System.out.println("[" + nombreAlq + "] No pudo enganar a Lance. Se lamenta dando grandes voces!");
                }

                skLance.close();
            } catch (IOException e) {
                System.out.println("[" + nombreAlq + "] No pudo contactar con Lance: " + e.getMessage());
                try { Thread.sleep(3000); } catch (InterruptedException ex) { }
            }
        } else {
            // Amenaza al Frente Norte (no necesita pocion)
            System.out.println("[" + nombreAlq + "] Se dirige a amenazar a Lance con enviarlo al Frente Norte...");
            try { Thread.sleep(7000); } catch (InterruptedException e) { }

            try {
                Socket skLance = new Socket(host, puerto);
                DataOutputStream salida = new DataOutputStream(skLance.getOutputStream());
                DataInputStream entrada = new DataInputStream(skLance.getInputStream());

                salida.writeUTF("ALQUIMISTA");
                salida.writeUTF(nombreAlq);
                salida.writeUTF("AMENAZA");

                boolean atendido = entrada.readBoolean();
                int chispaActual = entrada.readInt();

                if (atendido) {
                    System.out.println("[" + nombreAlq + "] Amenaza a Lance. Chispa actual de Lance: " + chispaActual);
                } else {
                    System.out.println("[" + nombreAlq + "] Lance no se deja amedrentar. Se lamenta dando grandes voces!");
                }

                skLance.close();
            } catch (IOException e) {
                System.out.println("[" + nombreAlq + "] No pudo contactar con Lance: " + e.getMessage());
                try { Thread.sleep(3000); } catch (InterruptedException ex) { }
            }
        }
    }
}

