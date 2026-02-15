import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;

// Press Shift twice to open the Search Everywhere dialog and type `show whitespaces`,
// then press Enter. You can now see whitespace characters in your code.
public class Main {
    public static void main(String[] args) {

        try {
            Socket skCliente;
            System.out.println("Creando el server socket");
            ServerSocket skServidor = new ServerSocket(5000);
            System.out.println("Esperando al cliente");
            skCliente = skServidor.accept();
            System.out.println("Atendiendo al cliente");
            InputStream aux = skCliente.getInputStream();
            DataInputStream flujo_entrada = new DataInputStream(aux);
            int lectura = flujo_entrada.readInt();
            System.out.println("El cliente ha pedido la operación "+lectura);
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }



    }
}