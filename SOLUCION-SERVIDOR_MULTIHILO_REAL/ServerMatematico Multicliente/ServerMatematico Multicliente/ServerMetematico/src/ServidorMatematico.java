import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class ServidorMatematico {

    static final int PUERTO = 5000;

    public DataInputStream in;
    public DataOutputStream out;

    ServerSocket skServidor;

    public ServidorMatematico() {
        try {
            skServidor = new ServerSocket(PUERTO);
            System.out.println("Escucho en el puerto: " + PUERTO);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void conectar() throws IOException {
        Socket mi_cliente = skServidor.accept();

        InputStream is = mi_cliente.getInputStream();
        in = new DataInputStream(is);
        System.out.println(in.readUTF());

        OutputStream aux = mi_cliente.getOutputStream();
        out = new DataOutputStream(aux);
        out.writeUTF("Hola Cliente, soy el servidor!!");
    }

    public boolean execute() throws IOException {

        String action = in.readUTF();

        switch(action){
            case "SUMA":
                suma();
                break;
            case "RAIZ_CUADRADA":
                raizCuadrada();
                break;
            case "SERIE":
                serie();
                break;
            case "SALIR":
                return false;
        }

        return true;

    }

    public void suma() throws IOException {
        Double a = in.readDouble();
        Double b = in.readDouble();
        Double response = a + b;

        String responseTXT = "La suma de " + a + " y " + b +" es " + response;
        System.out.println(responseTXT);
        out.writeUTF(responseTXT);
    }

    public void raizCuadrada() throws IOException{
        Double a = in.readDouble();
        Double response = Math.sqrt(a);

        String responseTXT = "La raíz cuadrada de " + a + " es " + response;
        System.out.println(responseTXT);
        out.writeUTF(responseTXT);
    }

    private void serie() throws IOException {
        Integer cantidad = in.readInt();

        for (int i = 0; i < cantidad - 2; i++) {
            in.readInt();
        }

        Integer penultimo = in.readInt();
        Integer ultimo = in.readInt();

        Integer response = ultimo + (ultimo - penultimo);

        String responseTXT = "El siguiente número de la serie es " + response;
        System.out.println(responseTXT);
        out.writeUTF(responseTXT);
    }

    public static void main(String[] args) throws IOException {
        ServidorMatematico servidorM = new ServidorMatematico();

        Boolean continuar;

        while (true) { // El servidor está siempre levantado

            servidorM.conectar();

            do { // Cuando un cliente se conecta se queda con él hasta que el cliente sale
                continuar = servidorM.execute();
            } while(continuar);

            System.out.println("Cliente desconectado");

            servidorM.out.close();
            servidorM.in.close();
        }
    }
}
