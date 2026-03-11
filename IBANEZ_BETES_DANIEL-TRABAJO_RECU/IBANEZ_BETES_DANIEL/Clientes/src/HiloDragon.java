import java.io.*;
import java.net.Socket;
import java.util.Random;

public class HiloDragon extends Thread {
    private Random random = new Random();

    @Override
    public void run() {
        System.out.println("[DRAGON] El Dragon Carmesi mora en las montanas...");



        while (!ClienteMaestro.simulacionTerminada) {
            // Elegir lugar aleatorio para atacar: 0=Mercado y porton, 1=Mercado y taberna, 2=porton y taberna
            int lugar = random.nextInt(3);

            ClienteMaestro.dragonAtacando = true;
            System.out.println("[DRAGON] *** EL DRAGON CARMESI DESPIERTA Y ATACA! ***");

            switch (lugar) {
                case 0:

                    int a = random.nextInt(100);
                    if (a < 80) { atacarLugar(ClienteMaestro.PUERTO_MERCADO, "MERCADO"); }
                    else if (a > 80) { System.out.println("El Mercado no ha sido destruido."); };

                    int c = random.nextInt(100);
                    if (c < 60) { atacarLugar(ClienteMaestro.PUERTO_PORTON, "PORTON"); }
                    else if (c > 60) { System.out.println("El Porton no ha sido destruido."); };

                    break;

                case 1:

                    int b = random.nextInt(100);
                    if (b < 80) { atacarLugar(ClienteMaestro.PUERTO_MERCADO, "MERCADO"); }
                    else if (b > 80) { System.out.println("El Mercado no ha sido destruido."); };

                    int e = random.nextInt(100);
                    if (e < 30) { atacarLugar(ClienteMaestro.PUERTO_TABERNA, "TABERNA"); }
                    else if (e > 30) { System.out.println("La Taberna no ha sido destruida."); }

                    break;

                case 2:

                    int d = random.nextInt(100);
                    if (d < 60) { atacarLugar(ClienteMaestro.PUERTO_PORTON, "PORTON"); }
                    else if (d > 60) { System.out.println("El Porton no ha sido destruido."); };

                    int f = random.nextInt(100);
                    if (f < 30) { atacarLugar(ClienteMaestro.PUERTO_TABERNA, "TABERNA"); }
                    else if (f > 30) { System.out.println("La Taberna no ha sido destruida."); }

                    break;
            }

            // El dragon permanece atacando 6 segundos antes de retirarse
            try { Thread.sleep(6000); } catch (InterruptedException e) { }
            ClienteMaestro.dragonAtacando = false;


            // Dormir antes de volver a atacar (120 seg)
            int sueno = 120000;
            System.out.println("[DRAGON] Se retira a las montanas. Despertará en " + (sueno/1000) + "segundos.");
            try { Thread.sleep(sueno); } catch (InterruptedException e) { }
        }

    }

    private void atacarLugar(int puerto, String nombreLugar) {
        try {
            Socket sk = new Socket(ClienteMaestro.HOST, puerto);
            DataOutputStream salida = new DataOutputStream(sk.getOutputStream());
            DataInputStream entrada = new DataInputStream(sk.getInputStream());

            // enviar ataque (el servidor procesara este comando en su switch)
            salida.writeUTF("ATAQUE_DRAGON");
            String respuesta = entrada.readUTF(); // "LUGAR_DESTRUIDO"
            System.out.println("[DRAGON] Ataca " + nombreLugar + "! Respuesta: " + respuesta);

            salida.writeUTF("DESCONECTAR");
            entrada.readUTF();
            sk.close();
        } catch (IOException e) {
            System.out.println("[DRAGON] No pudo atacar " + nombreLugar + ": " + e.getMessage());
        }
    }
}

