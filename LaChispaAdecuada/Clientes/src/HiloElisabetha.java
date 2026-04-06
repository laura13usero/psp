import java.io.*;
import java.net.Socket;
import java.util.Random;

public class HiloElisabetha extends Thread {
    private int nivelChispa = 0;
    private boolean conoceALance = false;
    private boolean chispa100 = false;
    private Random random = new Random();

    @Override
    public void run() {
        System.out.println("[ELISABETHA] Comienza la simulacion. Chispa: " + nivelChispa);

        while (!ClienteMaestro.simulacionTerminada) {
            synchronized (ClienteMaestro.lockElisabetha) {
                if (ClienteMaestro.hayPeticionAlquimistaE) {
                    procesarAlquimista();
                }
            }

            if (chispa100) {
                System.out.println("[ELISABETHA] Chispa a 100! Se pone en la ventana a esperar a Lance...");
                esperarLanceEnTaberna();
                break;
            }

            int accion = random.nextInt(4);

            switch (accion) {
                case 0: atenderDamas(); break;
                case 1: asistirBaile(); break;
                case 2: leerPergaminos(); break;
                case 3: escaparseALugar(); break;
            }

            synchronized (ClienteMaestro.lockElisabetha) {
                ClienteMaestro.chispaElisabetha = nivelChispa;
            }

            if (nivelChispa >= 100 && conoceALance) {
                nivelChispa = 100;
                chispa100 = true;
            }
        }
        System.out.println("[ELISABETHA] *** SIMULACION TERMINADA ***");
    }

    private void atenderDamas() {
        System.out.println("[ELISABETHA] Decide atender a alguna de sus Damas del Lazo...");
        boolean[] intentadas = new boolean[4];
        boolean atendida = false;
        for (int intentos = 0; intentos < 4 && !atendida; intentos++) {
            int damaElegida;
            do {
                damaElegida = random.nextInt(4);
            } while (intentadas[damaElegida]);
            intentadas[damaElegida] = true;
            String nombreDama = "Dama" + (damaElegida + 1);
            System.out.println("[ELISABETHA] Intenta atender a " + nombreDama + "...");
            try { Thread.sleep(4000); } catch (InterruptedException e) { }
            synchronized (ClienteMaestro.lockElisabetha) {
                if (ClienteMaestro.hayPeticionDama && ClienteMaestro.damaQuePide != null && ClienteMaestro.damaQuePide.equals(nombreDama)) {
                    System.out.println("[ELISABETHA] Atiende a " + nombreDama);
                    String mensaje = ClienteMaestro.mensajeDama;
                    ClienteMaestro.hayPeticionDama = false;
                    ClienteMaestro.damaQuePide = null;
                    ClienteMaestro.mensajeDama = null;
                    ClienteMaestro.lockElisabetha.notifyAll();
                    if (mensaje != null && mensaje.equals("RUMOR_LANCE")) {
                        if (!chispa100) nivelChispa = Math.max(0, nivelChispa - 5);
                        System.out.println("[ELISABETHA] " + nombreDama + " cuenta un rumor sobre Lance. Chispa: " + nivelChispa);
                    } else if (mensaje != null && mensaje.equals("INVITACION_BAILE")) {
                        if (random.nextInt(100) < 20) {
                            System.out.println("[ELISABETHA] No puede esquivar la invitacion al baile de " + nombreDama);
                            asistirBaile();
                        } else {
                            System.out.println("[ELISABETHA] Esquiva la invitacion al baile de " + nombreDama);
                        }
                    } else {
                        System.out.println("[ELISABETHA] " + nombreDama + " le cuenta una confidencia. Sin efecto.");
                    }
                    atendida = true;
                } else {
                    System.out.println("[ELISABETHA] " + nombreDama + " no tiene nada que contarle.");
                }
            }
        }
        if (!atendida) System.out.println("[ELISABETHA] Ninguna dama tenia algo que contarle.");
    }

    private void asistirBaile() {
        System.out.println("[ELISABETHA] Asiste a un baile de la Corte...");
        try { Thread.sleep(5000); } catch (InterruptedException e) { }
        if (!chispa100) {
            nivelChispa = Math.max(0, nivelChispa - 5);
            System.out.println("[ELISABETHA] El baile la aburre soberanamente. Chispa: " + nivelChispa);
        }
    }

    private void leerPergaminos() {
        System.out.println("[ELISABETHA] Lee pergaminos en la biblioteca...");
        try { Thread.sleep(5000); } catch (InterruptedException e) { }
        if (chispa100) return;
        if (random.nextBoolean()) {
            nivelChispa = Math.max(0, nivelChispa - 7);
            System.out.println("[ELISABETHA] Pergaminos soporiferos sobre historia del reino. Chispa: " + nivelChispa);
        } else {
            if (conoceALance) {
                nivelChispa += 5;
                System.out.println("[ELISABETHA] Leyendas de valientes caballeros. Le recuerdan a Lance. Chispa: " + nivelChispa);
            } else {
                nivelChispa = Math.min(30, nivelChispa + 5);
                System.out.println("[ELISABETHA] Leyendas de valientes caballeros. Chispa: " + nivelChispa);
            }
        }
    }

    private void escaparseALugar() {
        System.out.println("[ELISABETHA] Se escapa por los pasadizos del castillo...");
        if (random.nextBoolean()) {
            visitarMercado();
        } else {
            visitarTaberna();
        }
    }

    private void visitarMercado() {
        System.out.println("[ELISABETHA] Se dirige al Mercado de Roedalia...");
        try (Socket skMercado = new Socket(ClienteMaestro.HOST, ClienteMaestro.PUERTO_MERCADO);
             DataOutputStream salida = new DataOutputStream(skMercado.getOutputStream());
             DataInputStream entrada = new DataInputStream(skMercado.getInputStream())) {

            String estadoLugar = entrada.readUTF();
            if (estadoLugar.equals("LUGAR_DESTRUIDO")) {
                System.out.println("[ELISABETHA] ¡HORROR! ¡El Mercado ha sido destruido por el Dragón! Da media vuelta.");
                return;
            }

            salida.writeUTF("VISITAR_MERCADO");
            salida.writeUTF("Elisabetha");

            int numProductos = entrada.readInt();
            String[] productos = new String[numProductos];
            System.out.println("[ELISABETHA] El mercader le ofrece:");
            for (int i = 0; i < numProductos; i++) {
                productos[i] = entrada.readUTF();
                System.out.println("  " + (i + 1) + ". " + productos[i]);
            }

            int eleccion = random.nextInt(numProductos);
            salida.writeInt(eleccion);
            System.out.println("[ELISABETHA] Compra: " + productos[eleccion]);

            String respuesta = entrada.readUTF();
            System.out.println("[ELISABETHA] Mercader: " + respuesta);

            salida.writeUTF("DESCONECTAR");
            entrada.readUTF();
            try { Thread.sleep(5000); } catch (InterruptedException e) { }
        } catch (IOException e) {
            System.out.println("[ELISABETHA] No pudo llegar al mercado: " + e.getMessage());
        }
    }

    private void visitarTaberna() {
        System.out.println("[ELISABETHA] Se dirige a la Taberna 'El Descanso del Guerrero'...");
        try (Socket skTaberna = new Socket(ClienteMaestro.HOST, ClienteMaestro.PUERTO_TABERNA);
             DataOutputStream salida = new DataOutputStream(skTaberna.getOutputStream());
             DataInputStream entrada = new DataInputStream(skTaberna.getInputStream())) {

            String estadoLugar = entrada.readUTF();
            if (estadoLugar.equals("LUGAR_DESTRUIDO")) {
                System.out.println("[ELISABETHA] ¡CENIZAS! ¡La Taberna ha sido arrasada por el Dragón! No puede entrar.");
                return;
            }

            salida.writeUTF("ENTRAR");
            salida.writeUTF("ELISABETHA");
            entrada.readUTF();

            salida.writeUTF("CONSULTAR_LANCE");
            boolean lanceEsta = entrada.readBoolean();
            long tiempoEntrada = System.currentTimeMillis();

            if (lanceEsta) {
                encuentroConLance(salida, entrada);
            } else {
                System.out.println("[ELISABETHA] Lance no esta... esperando 5 segundos...");
                while (System.currentTimeMillis() - tiempoEntrada < 5000) {
                    try { Thread.sleep(1000); } catch (InterruptedException e) { }
                    salida.writeUTF("CONSULTAR_LANCE");
                    if (entrada.readBoolean()) {
                        encuentroConLance(salida, entrada);
                        lanceEsta = true;
                        break;
                    }
                }
                if (!lanceEsta) {
                    System.out.println("[ELISABETHA] Lance no aparecio. Vuelve al castillo. Chispa: " + nivelChispa);
                }
            }

            salida.writeUTF("SALIR_TABERNA");
            salida.writeUTF("ELISABETHA");
            entrada.readUTF();
            salida.writeUTF("DESCONECTAR");
            entrada.readUTF();
        } catch (IOException e) {
            System.out.println("[ELISABETHA] No pudo llegar a la taberna: " + e.getMessage());
        }
    }

    private void encuentroConLance(DataOutputStream salida, DataInputStream entrada) throws IOException {
        salida.writeUTF("YA_SE_CONOCEN");
        boolean yaSeConocian = entrada.readBoolean();
        if (!yaSeConocian) {
            salida.writeUTF("SE_CONOCEN");
            entrada.readUTF();
            nivelChispa = 75;
            conoceALance = true;
            System.out.println("[ELISABETHA] *** LA CHISPA ADECUADA HA NACIDO *** Chispa: " + nivelChispa);
        } else {
            if (!conoceALance) { // Si se conocían pero este hilo no lo sabía
                nivelChispa = 75;
                conoceALance = true;
            }
            nivelChispa += 10;
            System.out.println("[ELISABETHA] Coincide con Lance en la taberna. Chispa: " + nivelChispa);
        }
    }

    private void esperarLanceEnTaberna() {
        try (Socket skTaberna = new Socket(ClienteMaestro.HOST, ClienteMaestro.PUERTO_TABERNA);
             DataOutputStream salida = new DataOutputStream(skTaberna.getOutputStream());
             DataInputStream entrada = new DataInputStream(skTaberna.getInputStream())) {
            
            String estadoLugar = entrada.readUTF();
            if (estadoLugar.equals("LUGAR_DESTRUIDO")) {
                System.out.println("[ELISABETHA] Iba a esperar a Lance, pero la Taberna está destruida. Esperará en el castillo.");
                try { Thread.sleep(10000); } catch (InterruptedException e) {} // Simula espera
                return;
            }

            salida.writeUTF("REGISTRAR_CHISPA_100");
            salida.writeUTF("ELISABETHA");
            entrada.readUTF();
            salida.writeUTF("ESPERAR_AL_OTRO");
            salida.writeUTF("ELISABETHA");
            String resultado = entrada.readUTF();
            if (resultado.equals("FINAL_FELIZ")) {
                System.out.println("[ELISABETHA] *** FINAL FELIZ *** ¡Se reencuentra con Lance!");
            }
            salida.writeUTF("DESCONECTAR");
            entrada.readUTF();
        } catch (IOException e) {
            System.out.println("[ELISABETHA] Error esperando a Lance: " + e.getMessage());
        }
    }

    private void procesarAlquimista() {
        if (ClienteMaestro.mensajeAlquimistaE != null && ClienteMaestro.mensajeAlquimistaE.equals("POCION")) {
            if (random.nextInt(100) < 30) {
                if (!chispa100) {
                    nivelChispa = Math.max(0, nivelChispa - 20);
                    System.out.println("[ELISABETHA] El alquimista la engano con una pocion! Chispa: " + nivelChispa);
                }
            } else {
                System.out.println("[ELISABETHA] No se deja enganar por el alquimista.");
            }
        }
        ClienteMaestro.hayPeticionAlquimistaE = false;
        ClienteMaestro.mensajeAlquimistaE = null;
        ClienteMaestro.lockElisabetha.notifyAll();
    }
}