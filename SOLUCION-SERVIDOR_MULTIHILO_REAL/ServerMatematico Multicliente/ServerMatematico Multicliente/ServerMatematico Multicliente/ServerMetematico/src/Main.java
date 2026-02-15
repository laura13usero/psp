import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Main {
    public static void main(String[] args) throws IOException {
        int PUERTO = 5000;

        ServerSocket skServidor;

        try {
            skServidor = new ServerSocket(PUERTO);
            System.out.println("Escucho en el puerto: " + PUERTO);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        while (true) { // El servidor está siempre levantado

            Socket cliente = skServidor.accept();

            HiloMatematico hiloMatematico = new HiloMatematico(cliente);

            hiloMatematico.start();

            System.out.println("Cliente está siendo atendido por el hilo !!!");
        }
    }
}