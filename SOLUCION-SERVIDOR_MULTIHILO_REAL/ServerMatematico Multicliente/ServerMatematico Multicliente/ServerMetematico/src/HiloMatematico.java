import java.io.*;
import java.net.Socket;
import java.net.SocketException;

public class HiloMatematico extends Thread{

    public DataInputStream in;
    public DataOutputStream out;

    public HiloMatematico(Socket cliente) throws IOException {

        InputStream is = cliente.getInputStream();
        in = new DataInputStream(is);
        System.out.println(in.readUTF());

        OutputStream aux = cliente.getOutputStream();
        out = new DataOutputStream(aux);
        out.writeUTF("Hola Cliente, soy el hilo del servidor!!");

    }

    @Override
    public void run(){

        Boolean continuar = false;

            do { // Cuando un cliente se conecta se queda con él hasta que el cliente sale
                try {
                    continuar = execute();
                } catch(SocketException se){
                    System.out.println("Un cliente se ha desconectado repentinamente");
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            } while(continuar);

            System.out.println("Cliente desconectado");

        try {
            out.close();
            in.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


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

}
