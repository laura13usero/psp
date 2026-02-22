import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        System.out.println("==============================================");
        System.out.println("  TABERNA 'EL DESCANSO DEL GUERRERO' - SERVIDOR");
        System.out.println("==============================================");
        System.out.print("Puerto de la Taberna (por defecto 5000): ");
        String input = sc.nextLine().trim();
        int puerto = input.isEmpty() ? 5000 : Integer.parseInt(input);

        ControlTaberna controlTaberna = new ControlTaberna();

        try {
            ServerSocket skServidor = new ServerSocket(puerto);
            System.out.println("[TABERNA] Servidor iniciado en el puerto " + puerto);
            System.out.println("[TABERNA] Esperando personajes...");

            while (true) {
                Socket skCliente = skServidor.accept();
                System.out.println("[TABERNA] Nuevo personaje conectado: " + skCliente.getInetAddress());
                HiloTaberna hilo = new HiloTaberna(skCliente, controlTaberna);
                hilo.start();
            }
        } catch (IOException e) {
            System.out.println("[TABERNA] Error: " + e.getMessage());
        }
    }
}

