import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class HiloDragon extends Thread {
    private final Random random = new Random();

    @Override
    public void run() {
        System.out.println("[DRAGON] El Dragón de Ceniza Carmesí duerme en las montañas...");

        while (!ClienteMaestro.simulacionTerminada) {
            try {
                // El Dragón espera 120 segundos antes de cada ciclo de actividad
                System.out.println("[DRAGON] ...zzZzZz... el mal ancestral descansa ...");
                Thread.sleep(120 * 1000);
            } catch (InterruptedException e) {
                break; // Si se interrumpe el cliente, el dragón también para.
            }

            // 25% de probabilidad de aparecer
            if (random.nextInt(100) < 25) {
                realizarAtaque();
            } else {
                System.out.println("[DRAGON] El reino permanece en paz. El Dragón no ha despertado.");
            }
        }
        System.out.println("[DRAGON] El Dragón siente el final del Cantar y se retira para siempre.");
    }

    private void realizarAtaque() {
        System.out.println("\n============================================================");
        System.out.println("¡EL CIELO SE OSCURECE! ¡EL DRAGÓN DE CENIZA CARMESÍ DESCIENDE!");
        System.out.println("============================================================\n");

        // Elige 2 lugares distintos para atacar
        List<String> lugares = new ArrayList<>();
        lugares.add("Mercado");
        lugares.add("Porton");
        lugares.add("Taberna");
        Collections.shuffle(lugares);

        String lugar1 = lugares.get(0);
        String lugar2 = lugares.get(1);

        // Ataca al primer lugar
        atacarLugar(lugar1);

        // Ataca al segundo lugar
        atacarLugar(lugar2);

        System.out.println("\n[DRAGON] Satisfecho con la destrucción, el Dragón regresa a su guarida en las montañas.\n");
    }

    private void atacarLugar(String lugar) {
        System.out.println("[DRAGON] El Dragón fija su mirada llameante en: " + getNombreCompleto(lugar));
        try {
            Thread.sleep(6000); // Cada ataque tarda 6 segundos
        } catch (InterruptedException e) {
            return;
        }

        boolean exito = false;
        int puertoDian = 0;
        switch (lugar) {
            case "Mercado":
                if (random.nextInt(100) < 80) exito = true;
                puertoDian = ClienteMaestro.PUERTO_MERCADO;
                break;
            case "Porton":
                if (random.nextInt(100) < 60) exito = true;
                puertoDian = ClienteMaestro.PUERTO_PORTON;
                break;
            case "Taberna":
                if (random.nextInt(100) < 30) exito = true;
                puertoDian = ClienteMaestro.PUERTO_TABERNA;
                break;
        }

        if (exito) {
            System.out.println("[DRAGON] ¡ÉXITO! ¡" + getNombreCompleto(lugar) + " ha sido reducido a cenizas!");
            enviarDestruccionAServidor(lugar, puertoDian);
        } else {
            System.out.println("[DRAGON] ¡FRACASO! ¡" + getNombreCompleto(lugar) + " resiste el embate del Dragón!");
        }
    }

    private void enviarDestruccionAServidor(String lugar, int puerto) {
        try (Socket skLugar = new Socket(ClienteMaestro.HOST, puerto);
             DataOutputStream salida = new DataOutputStream(skLugar.getOutputStream());
             DataInputStream entrada = new DataInputStream(skLugar.getInputStream())) {

            String estado = entrada.readUTF();
            if (estado.equals("LUGAR_DESTRUIDO")) {
                 System.out.println("[DRAGON] Extrañeza: " + getNombreCompleto(lugar) + " ya estaba en ruinas, el Dragón quema las cenizas...");
                 return;
            }

            salida.writeUTF("DESTRUIR_LUGAR");
            // No esperamos respuesta porque el servidor cerrará el socket, lanzando un Exception o EOF.
        } catch (IOException e) {
            // El servidor habrá cerrado el socket agresivamente, es lo esperado.
        }
    }

    private String getNombreCompleto(String lugar) {
        switch (lugar) {
            case "Mercado": return "El Mercado";
            case "Porton": return "El Portón Norte";
            case "Taberna": return "La Taberna 'El Descanso del Guerrero'";
            default: return "";
        }
    }
}
