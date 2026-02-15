import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class Cliente {

    static final String HOST = "localhost";
    static final int PUERTO = 5000;

    public DataOutputStream out;
    public DataInputStream in;
    public Scanner sc;

    public Cliente() {

        sc = new Scanner(System.in);

        try {
            Socket skCliente = new Socket(HOST, PUERTO);
            OutputStream oS = skCliente.getOutputStream();
            out = new DataOutputStream(oS);
            out.writeUTF("Buenas!!!! Te saluda el cliente");
            InputStream aux = skCliente.getInputStream();
            in = new DataInputStream(aux);
            System.out.println(in.readUTF());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    public boolean execute() throws IOException {

        System.out.println("Introduzca el numero de operación a ejecutar: ");
        System.out.println("1 - SUMA");
        System.out.println("2 - RAIZ_CUADRADA");
        System.out.println("3 - SERIE");
        System.out.println("4 - SALIR");

        Integer action = sc.nextInt();

        switch (action){
            case 1:
                suma();
                break;
            case 2:
                raizCuadrada();
                break;
            case 3:
                serie();
                break;
            case 4:
                out.writeUTF("SALIR");
                System.out.println("Saliendo del programa...");
                return false;
        }

        return true;

    }

    private void suma() throws IOException {

        System.out.println("Números a sumar:");
        System.out.print("a: ");
        Double a = sc.nextDouble();
        System.out.print("b: ");
        Double b = sc.nextDouble();

        out.writeUTF("SUMA");
        out.writeDouble(a);
        out.writeDouble(b);
        System.out.println(in.readUTF());
    }

    private void raizCuadrada() throws IOException {

        System.out.print("Número del que quiere obtener la raíz cuadrada: ");
        Double a = sc.nextDouble();

        out.writeUTF("RAIZ_CUADRADA");
        out.writeDouble(a);
        System.out.println(in.readUTF());
    }

    private void serie() throws IOException {

        out.writeUTF("SERIE");
        System.out.print("Número de números en la serie: ");
        Integer total = sc.nextInt();
        out.writeInt(total);

        Integer temp;

        System.out.println("Introduzca los número de la serie: ");
        for (int i = 0; i < total; i++) {
            System.out.print(i + ":");
            temp = sc.nextInt();
            out.writeInt(temp);
        }

        System.out.println(in.readUTF());

    }

}
