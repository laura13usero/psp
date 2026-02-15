import java.io.DataOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.io.IOException;

// Press Shift twice to open the Search Everywhere dialog and type `show whitespaces`,
// then press Enter. You can now see whitespace characters in your code.
public class Main {
    public static void main(String[] args) {
        try {
            System.out.println("Cliente intentando conectar");
            Socket skCliente = new Socket("192.168.104.57", 5000);
            System.out.println("Cliente conectado !!!");
            OutputStream aux = skCliente.getOutputStream();
            DataOutputStream flujo_salida = new DataOutputStream(aux);
            System.out.println("Voy a pedir la operación 7");
            flujo_salida.writeInt(7);
            System.out.println("Operación 7 pedida !!!");
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }

        }
}