import java.io.IOException;
import java.net.Socket;

public class Main {
    public static void main(String[] args) throws IOException {
        ServidorMatematico servidorM = new ServidorMatematico();

        while (true) { // El servidor está siempre levantado

            Socket cliente = servidorM.skServidor.accept();

            HiloMatematico hiloMatematico = new HiloMatematico(cliente);

            hiloMatematico.start();

            System.out.println("Cliente atendido!");
        }
    }
}