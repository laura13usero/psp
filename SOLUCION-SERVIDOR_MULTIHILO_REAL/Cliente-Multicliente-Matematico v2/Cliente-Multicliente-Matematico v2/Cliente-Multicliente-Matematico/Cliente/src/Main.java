import java.io.*;
import java.net.Socket;
import java.util.Scanner;

// Press Shift twice to open the Search Everywhere dialog and type `show whitespaces`,
// then press Enter. You can now see whitespace characters in your code.
public class Main {

    public static void main(String[] args) {
        Scanner leer = new Scanner(System.in);

        Socket skCliente = null;
        int lecturaInt;
        int scrituraInt;
        String lecturaString;
        String scrituraString;

        System.out.println("Cliente intentando conectar");

        try {
            //skCliente = new Socket("192.168.104.53", port 5000)
            skCliente = new Socket("localhost", 5000);
        } catch (IOException e){
            System.out.println("error en el socket");
            System.out.println(e.getMessage());
        }

        try {
            // Socket skCliente = new Socket("localhost", 5000);
            // Socker skCliente = new Socket("192.168.104.53", port 5000);
            System.out.println("Cliente conectado !!!");

            OutputStream post = skCliente.getOutputStream(); // obtener mensajes
            DataOutputStream flujo_salida = new DataOutputStream(post); // creamos clase para leer datos primitivos
            InputStream get = skCliente.getInputStream(); // obtener mensajes
            DataInputStream flujo_entrada = new DataInputStream(get); // creamos clase para leer datos primitivos

            boolean encendido = true;
            while (encendido){
                flujo_salida.writeInt(100000);
                lecturaString = flujo_entrada.readUTF();
                System.out.println(lecturaString);

                Boolean run = true;
                while (run){
                    lecturaString = flujo_entrada.readUTF();

                    if (lecturaString.equals("preguntar_al_usuario")){
                        lecturaString = flujo_entrada.readUTF();
                        System.out.println(lecturaString);
                        System.out.print("=> ");
                        int respuesta = leer.nextInt();
                        flujo_salida.writeInt(respuesta);
                        lecturaString = "";
                    }

                    if (lecturaString.equals("mostrar_al_usuario")){
                        lecturaString = flujo_entrada.readUTF();
                        System.out.println(lecturaString);
                        System.out.println();
                        lecturaString = "";
                    }

                    if (lecturaString.equals("restear_interacion")){
                        System.out.println("reset");
                        run = false;
                        lecturaString = "";
                    }

                    if (lecturaString.equals("desconectar_usuario")){
                        System.out.println("poweroff");
                        run = false;
                        lecturaString = "";
                        encendido = false;
                    }

                    if (lecturaString.equals("preguntar_al_usuario_UTF")){
                        lecturaString = flujo_entrada.readUTF();
                        System.out.println(lecturaString);
                        System.out.print("=> ");
                        String respuesta = leer.next();
                        flujo_salida.writeUTF(respuesta);
                        lecturaString = "";
                    }

                }
            }



            //System.out.println("escribe el numero del servicio");
            //int servicio = leer.nextInt();

//            if (servicio == 1) {
//                System.out.println("Pidiendo la operación 1");
//                flujo_salida.writeInt(1);
//                System.out.println("Escribe el primer numero");
//                int numero_sum_1 = leer.nextInt();
//                System.out.println("escribe el segundo numero a escribir");
//                int numero_sum_2 = leer.nextInt();
//                flujo_salida.writeInt(numero_sum_1);
//                flujo_salida.writeInt(numero_sum_2);
//                lectura = flujo_entrada.readInt();
//                System.out.println(lectura);
//
//            } else if (servicio == 2) {
//                System.out.println("Pidiendo la operación 2");
//                flujo_salida.writeInt(2);
//                System.out.print("numero a calcular: ");
//                int numero_sum_1 = leer.nextInt();
//                flujo_salida.writeInt(numero_sum_1);
//                lectura = flujo_entrada.readInt();
//                System.out.println("Numero calculado: " + lectura);
//
//            } else {
//                System.out.println("no se reconoce");
//            }

        } catch (IOException e) {
            System.out.println(e.getMessage());
        }

    }
}