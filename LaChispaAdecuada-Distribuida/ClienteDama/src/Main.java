import java.io.*;
import java.net.Socket;
import java.util.Random;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        System.out.println("==============================================");
        System.out.println("  DAMA DEL LAZO PERFUMADO - ROEDALIA");
        System.out.println("==============================================");

        System.out.print("Numero de esta dama (1-4): ");
        int numeroDama = sc.nextInt();
        sc.nextLine();
        String nombreDama = "Dama" + numeroDama;

        System.out.print("IP de Elisabetha (localhost): ");
        String input = sc.nextLine().trim();
        String hostElisabetha = input.isEmpty() ? "localhost" : input;

        System.out.print("Puerto de Elisabetha (6000): ");
        input = sc.nextLine().trim();
        int puertoElisabetha = input.isEmpty() ? 6000 : Integer.parseInt(input);

        Random random = new Random();
        System.out.println("[" + nombreDama + "] Comienza la simulacion.");

        while (true) {
            // Elegir accion aleatoria (2 opciones: labores o confesar a Elisabetha)
            int accion = random.nextInt(2);

            if (accion == 0) {
                realizarLabores(nombreDama, random);
            } else {
                confesarAElisabetha(nombreDama, hostElisabetha, puertoElisabetha, random);
            }
        }
    }

    static void realizarLabores(String nombreDama, Random random) {
        String[] labores = {"montar a caballo", "practicar esgrima", "enterarse de rumores"};
        String labor = labores[random.nextInt(labores.length)];
        System.out.println("[" + nombreDama + "] Realiza labor: " + labor);
        try { Thread.sleep(5000); } catch (InterruptedException e) { }
        System.out.println("[" + nombreDama + "] Termina labor: " + labor);
    }

    static void confesarAElisabetha(String nombreDama, String host, int puerto, Random random) {
        System.out.println("[" + nombreDama + "] Quiere hablar con Elisabetha...");

        // Decidir que contarle: 50% confidencia, 50% accion (rumor o invitacion baile)
        String mensaje;
        int tipoMensaje = random.nextInt(2);
        if (tipoMensaje == 0) {
            mensaje = "CONFIDENCIA";
        } else {
            // De la parte mala: puede ser rumor infundado o invitacion al baile
            if (random.nextBoolean()) {
                mensaje = "RUMOR_LANCE";
            } else {
                mensaje = "INVITACION_BAILE";
            }
        }

        try {
            Socket skElisabetha = new Socket(host, puerto);
            DataOutputStream salida = new DataOutputStream(skElisabetha.getOutputStream());
            DataInputStream entrada = new DataInputStream(skElisabetha.getInputStream());

            salida.writeUTF("DAMA");
            salida.writeUTF(nombreDama);
            salida.writeUTF(mensaje);

            boolean atendida = entrada.readBoolean();

            if (atendida) {
                System.out.println("[" + nombreDama + "] Elisabetha la atendio. Le conto: " + mensaje);
            } else {
                System.out.println("[" + nombreDama + "] Elisabetha no la atendio (timeout 20s). Vuelve a sus labores.");
            }

            skElisabetha.close();
        } catch (IOException e) {
            System.out.println("[" + nombreDama + "] No pudo contactar con Elisabetha: " + e.getMessage());
            try { Thread.sleep(3000); } catch (InterruptedException ex) { }
        }
    }
}

