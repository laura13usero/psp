import java.io.*;
import java.net.Socket;
import java.util.Random;

/** HILO CABALLERO - identico al original. */
public class HiloCaballero extends Thread {
    private String nombreCab;
    private Random random = new Random();
    private boolean herido = false;
    private long tiempoHerida = 0;
    public HiloCaballero(int n) { this.nombreCab = "Caballero" + n; }

    @Override
    public void run() {
        while (!ClienteMaestro.simulacionTerminada) {
            if (herido) {
                if (System.currentTimeMillis() - tiempoHerida < 30000) {
                    try { Thread.sleep(5000); } catch (InterruptedException e) { } continue;
                } else { herido = false; }
            }
            if (random.nextInt(2) == 0) {
                String[] l = {"Porton Norte","muralla","torres"};
                String lu = l[random.nextInt(l.length)];
                if (lu.equals("Porton Norte")) {
                    try {
                        Socket sk = new Socket(ClienteMaestro.HOST, ClienteMaestro.PUERTO_PORTON);
                        DataOutputStream sal = new DataOutputStream(sk.getOutputStream());
                        DataInputStream ent = new DataInputStream(sk.getInputStream());
                        sal.writeUTF("VIGILAR_LUGAR"); sal.writeUTF(nombreCab); sal.writeUTF(lu); ent.readUTF();
                        sal.writeUTF("DESCONECTAR"); ent.readUTF(); sk.close();
                    } catch (IOException e) { }
                }
                try { Thread.sleep(6000); } catch (InterruptedException e) { }
            } else {
                String msg = random.nextInt(100) < 25 ? "OFENSA_ELISABETHA" : "CONFIDENCIA";
                synchronized (ClienteMaestro.lockLance) {
                    long t0 = System.currentTimeMillis();
                    ClienteMaestro.caballeroQuePide = nombreCab; ClienteMaestro.mensajeCaballero = msg; ClienteMaestro.hayPeticionCaballero = true;
                    while (System.currentTimeMillis()-t0 < 25000 && ClienteMaestro.hayPeticionCaballero) {
                        try { ClienteMaestro.lockLance.wait(2000); } catch (InterruptedException e) { }
                    }
                    boolean ok = !ClienteMaestro.hayPeticionCaballero;
                    if (!ok) { ClienteMaestro.hayPeticionCaballero=false; ClienteMaestro.caballeroQuePide=null; ClienteMaestro.mensajeCaballero=null; }
                    else if (msg.equals("OFENSA_ELISABETHA") && random.nextInt(100) < 20) { herido=true; tiempoHerida=System.currentTimeMillis(); }
                }
            }
        }
    }
}

