import java.io.*;import java.net.Socket;
public class HiloTaberna extends Thread {
    private Socket skCliente; private ControlTaberna control;
    public HiloTaberna(Socket s,ControlTaberna c){this.skCliente=s;this.control=c;}
    @Override public void run(){try{DataInputStream ent=new DataInputStream(skCliente.getInputStream());DataOutputStream sal=new DataOutputStream(skCliente.getOutputStream());boolean c=true;while(c){String cmd=ent.readUTF();switch(cmd){case"ENTRAR":String pe=ent.readUTF();control.entrar(pe);sal.writeUTF("OK");break;case"SALIR_TABERNA":String ps=ent.readUTF();control.salir(ps);sal.writeUTF("OK");break;case"CONSULTAR_ELISABETHA":sal.writeBoolean(control.estaElisabetha());break;case"CONSULTAR_LANCE":sal.writeBoolean(control.estaLance());break;case"YA_SE_CONOCEN":sal.writeBoolean(control.yaSeConocen());break;case"SE_CONOCEN":control.seConocen();sal.writeUTF("CHISPA_GENERADA");break;case"REGISTRAR_CHISPA_100":String p1=ent.readUTF();control.registrarChispa100(p1);sal.writeUTF("OK");break;case"ESPERAR_AL_OTRO":String pw=ent.readUTF();control.esperarAlOtro(pw);sal.writeUTF("FINAL_FELIZ");break;case"DESCONECTAR":c=false;sal.writeUTF("ADIOS");break;default:sal.writeUTF("?");break;}}skCliente.close();}catch(IOException e){}}
}

