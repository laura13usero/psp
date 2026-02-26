import java.io.IOException;import java.net.ServerSocket;import java.net.Socket;
public class HiloServidorTaberna implements Runnable {
    private int puerto; private ControlTaberna control;
    public HiloServidorTaberna(int p, ControlTaberna c){this.puerto=p;this.control=c;}
    @Override public void run(){try{ServerSocket sk=new ServerSocket(puerto);System.out.println("[TABERNA] Servidor en "+puerto);while(true){new HiloTaberna(sk.accept(),control).start();}}catch(IOException e){}}
}

