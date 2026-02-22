import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        System.out.println("==============================================");
        System.out.println("  MERCADO DE ROEDALIA - SERVIDOR");
        System.out.println("==============================================");
        System.out.print("Puerto del Mercado (por defecto 5001): ");
        String input = sc.nextLine().trim();
        int puerto = input.isEmpty() ? 5001 : Integer.parseInt(input);

        try {
            ServerSocket skServidor = new ServerSocket(puerto);
            System.out.println("[MERCADO] Servidor iniciado en el puerto " + puerto);
            System.out.println("[MERCADO] Esperando clientes...");

            while (true) {
                Socket skCliente = skServidor.accept();
                System.out.println("[MERCADO] Nuevo cliente conectado: " + skCliente.getInetAddress());
                HiloMercado hilo = new HiloMercado(skCliente);
                hilo.start();
            }
        } catch (IOException e) {
            System.out.println("[MERCADO] Error: " + e.getMessage());
        }
    }
}

