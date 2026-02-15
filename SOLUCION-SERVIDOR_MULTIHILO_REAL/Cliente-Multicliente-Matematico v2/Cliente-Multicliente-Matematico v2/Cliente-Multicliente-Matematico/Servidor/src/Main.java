import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

// Press Shift twice to open the Search Everywhere dialog and type `show whitespaces`,
// then press Enter. You can now see whitespace characters in your code.
public class Main {

    // crear tercera opcion
    // que pueda atender a varios usuarios

    public static void main(String[] args) {

        ServidorHilo[] hilos = new ServidorHilo[4];
        ServerSocket skServidor = null;

        try {
            skServidor = new ServerSocket(5000); //puertos apartir del 5000 son reserbados a programadores
            System.out.println("servidor iniciado en el puerto 5000");
        } catch (IOException e){
            System.out.println("error en el socket al iniciar");
            System.out.println(e.getMessage());
        }

        while (true){
            try {

                for (int i = 0; i < hilos.length; i++) {
                    if (hilos[i] != null && !hilos[i].isAlive()) {
                        System.out.println("Hilo " + i + " ha terminado. hilo liberado.");
                        hilos[i] = null;
                    }
                }

                int HiloDisponible = -1;
                for (int i = 0; i < hilos.length; i++) {
                    if (hilos[i] == null) {
                        HiloDisponible = i;
                        break;
                    }
                }

                if (HiloDisponible == -1) {
                    System.out.println("Servidor lleno (4/4 clientes). Esperando...");
                    Thread.sleep(1000); // Un segundoooooooo !!!
                }
                else {
                    Socket skCliente; // esperamos un cliente que la peticion se guardara en skCliente
                    System.out.println("Esperando al cliente en hilo: " + HiloDisponible);

                    skCliente = skServidor.accept(); // aceptamos la conexion con el cliente
                    System.out.println("Atendiendo al cliente en hilo: " + HiloDisponible);

                    ServidorHilo servidorHilo = new ServidorHilo(skCliente, HiloDisponible);
                    hilos[HiloDisponible] = servidorHilo;
                    hilos[HiloDisponible].start();

                    System.out.println("Hilo " + HiloDisponible + " iniciado - Estado: " + hilos[HiloDisponible].getState());
                }

            } catch (IOException e) {
                System.out.println(e.getMessage());
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

    }
}