import java.io.*;
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
            skCliente = skServidor.accept(); //Bloqueante hasta que haya una  petición
            System.out.println("Atendiendo al cliente");
            InputStream aux = skCliente.getInputStream();
            DataInputStream flujo_entrada = new DataInputStream(aux);
            OutputStream aux2 = skCliente.getOutputStream();
            DataOutputStream flujo_salida = new DataOutputStream(aux2);
            int lectura = flujo_entrada.readInt();
            System.out.println("El cliente ha pedido la operación "+lectura);
            switch (lectura) {
                case 1:
                    int operando1 = flujo_entrada.readInt();
                    int operando2 = flujo_entrada.readInt();
                    int suma = operando1 + operando2;
                    flujo_salida.writeInt(suma);
                    break;
                case 2:
                    System.out.println("Operacion raiz solicitada");
                    break;
                case 4: //Desconexión
                    System.out.println("Operacion desconexión solicitada");
                    int port = skCliente.getLocalPort();
                    System.out.println("Desconecto el socket del cliente del puerto: "+port);
                    skCliente.close();
                    break;

                default:
                    System.out.println("Operacion no reconocida");
                    break;
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }



    }
}