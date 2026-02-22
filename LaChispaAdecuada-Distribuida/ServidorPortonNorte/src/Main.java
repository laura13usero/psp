import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        System.out.println("==============================================");
        System.out.println("  PORTON NORTE DE ROEDALIA - SERVIDOR");
        System.out.println("==============================================");
        System.out.print("Puerto del Porton Norte (por defecto 5002): ");
        String input = sc.nextLine().trim();
        int puerto = input.isEmpty() ? 5002 : Integer.parseInt(input);

        try {
            ServerSocket skServidor = new ServerSocket(puerto);
            System.out.println("[PORTON] Servidor iniciado en el puerto " + puerto);
            System.out.println("[PORTON] Esperando guardias...");

            while (true) {
                Socket skCliente = skServidor.accept();
                System.out.println("[PORTON] Nuevo guardian conectado: " + skCliente.getInetAddress());
                HiloPorton hilo = new HiloPorton(skCliente);
                hilo.start();
            }
        } catch (IOException e) {
            System.out.println("[PORTON] Error: " + e.getMessage());
        }
    }
}

