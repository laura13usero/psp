import java.io.IOException;import java.net.ServerSocket;import java.net.Socket;
public class HiloServidorPorton implements Runnable {
    private int puerto;
    public HiloServidorPorton(int p){this.puerto=p;}
    @Override public void run(){try{ServerSocket sk=new ServerSocket(puerto);System.out.println("[PORTON] Servidor en "+puerto);while(true){new HiloPorton(sk.accept()).start();}}catch(IOException e){}}
}

