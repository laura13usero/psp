import java.io.*;import java.net.Socket;import java.util.Random;
public class HiloMercado extends Thread {
    private Socket skCliente;
    private static final String[] PROD={"queso","pan recien horneado","especias del lejano oriente","telas para vestidos","jugo de grosella","repelente de gatos","brillantes collares de ratona","cucharas de boj tamano raton"};
    public HiloMercado(Socket s){this.skCliente=s;}
    @Override public void run(){try{DataInputStream ent=new DataInputStream(skCliente.getInputStream());DataOutputStream sal=new DataOutputStream(skCliente.getOutputStream());boolean c=true;while(c){String cmd=ent.readUTF();switch(cmd){case"VISITAR_MERCADO":String n=ent.readUTF();Random r=new Random();String[]of=new String[5];boolean[]u=new boolean[PROD.length];for(int i=0;i<5;i++){int x;do{x=r.nextInt(PROD.length);}while(u[x]);u[x]=true;of[i]=PROD[x];}sal.writeInt(5);for(int i=0;i<5;i++)sal.writeUTF(of[i]);int el=ent.readInt();sal.writeUTF("Gracias por comprar "+of[Math.max(0,Math.min(el,4))]+", "+n+"!");break;case"DESCONECTAR":c=false;sal.writeUTF("ADIOS");break;default:sal.writeUTF("?");break;}}skCliente.close();}catch(IOException e){}}
}

