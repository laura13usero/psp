import java.io.*;import java.net.Socket;import java.util.Random;
public class HiloPorton extends Thread {
    private Socket skCliente;
    private static final String[] R_CIUDAD={"Raton Pedro","Ratona Maria","Raton Juan","Ratona Ana"};
    private static final String[] R_FORA={"Raton del Norte","Ratona del Este","Raton del Sur","Ratona del Oeste"};
    private static final String[] PROD={"queso fermentado","telas finas","especias","herramientas","queso sin fermentar","leche cruda","pan artesano","frutas secas","miel"};
    public HiloPorton(Socket sk){this.skCliente=sk;}
    @Override public void run(){try{DataInputStream ent=new DataInputStream(skCliente.getInputStream());DataOutputStream sal=new DataOutputStream(skCliente.getOutputStream());boolean c=true;while(c){String cmd=ent.readUTF();switch(cmd){case"VIGILAR_PORTON":String g=ent.readUTF();Random r=new Random();if(r.nextBoolean()){String rt=R_CIUDAD[r.nextInt(R_CIUDAD.length)];sal.writeUTF("LOCAL");sal.writeUTF(rt);sal.writeUTF("Bienvenido "+rt);}else{String rt=R_FORA[r.nextInt(R_FORA.length)];String p=PROD[r.nextInt(PROD.length)];if(p.equals("queso sin fermentar")||p.equals("leche cruda")){sal.writeUTF("RECHAZADA");sal.writeUTF(rt);sal.writeUTF("Denegado: "+p);}else{sal.writeUTF("PERMITIDA");sal.writeUTF(rt);sal.writeUTF("Puede pasar con "+p);}}break;case"VIGILAR_LUGAR":ent.readUTF();ent.readUTF();sal.writeUTF("OK");break;case"DESCONECTAR":c=false;sal.writeUTF("ADIOS");break;default:sal.writeUTF("?");break;}}skCliente.close();}catch(IOException e){}}
}

