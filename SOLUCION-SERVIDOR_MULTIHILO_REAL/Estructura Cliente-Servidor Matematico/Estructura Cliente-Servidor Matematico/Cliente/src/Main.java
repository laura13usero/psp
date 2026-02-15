import java.io.*;
import java.net.Socket;

// Press Shift twice to open the Search Everywhere dialog and type `show whitespaces`,
// then press Enter. You can now see whitespace characters in your code.
public class Main {
    public static void main(String[] args) {
        try {
            int resultado;
            System.out.println("Cliente intentando conectar");
            //Socket skCliente = new Socket("192.168.104.53", 5000);
            //Socket skCliente = new Socket("127.0.0.1", 5000);
            Socket skCliente = new Socket("localhost", 5000);
            System.out.println("Cliente conectado !!!");

            OutputStream aux = skCliente.getOutputStream();
            DataOutputStream flujo_salida = new DataOutputStream(aux);
            InputStream aux2 = skCliente.getInputStream();
            DataInputStream flujo_entrada = new DataInputStream(aux2);
            /*System.out.println("Voy a pedir la operación 4");
            flujo_salida.writeInt(4);*/

            System.out.println("Voy a pedir la operación 1");
            flujo_salida.writeInt(1);
            System.out.println("Operación 1 pedida !!!");
            flujo_salida.writeInt(10);
            System.out.println("Operando 1 el 10 !!!");
            flujo_salida.writeInt(17);
            System.out.println("Operando 2 el 17 !!!");
            System.out.println("Esperando respuesta del servidor");
            resultado = flujo_entrada.readInt();
            System.out.println("El resultado es: "+resultado);
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }

        }
}