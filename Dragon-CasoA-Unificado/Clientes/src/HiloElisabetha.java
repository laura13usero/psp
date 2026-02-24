import java.io.*;
import java.net.Socket;
import java.util.Random;

/**
 * HILO ELISABETHA - CASO A: DRAGON (Secuestro + Rescate + Duelo).
 *
 * MODIFICACIONES respecto al original:
 *   1. Antes de cada accion, comprueba si ha sido secuestrada (variable compartida)
 *   2. Si esta secuestrada, se conecta al Cubil del Dragon via socket y espera
 *      rescate con ESPERAR_RESCATE (que hace wait() en el servidor)
 *   3. Al salir del cubil, su chispa sube +50 (victoria) o baja -20 (malherida)
 *   4. La accion de ir al mercado ahora comprueba si el mercado esta atacado
 *
 * Todo lo demas (atenderDamas, asistirBaile, leerPergaminos, visitarTaberna,
 * procesarAlquimista, esperarLanceEnTaberna) es IDENTICO al original.
 */
public class HiloElisabetha extends Thread {
    private int nivelChispa = 0;
    private boolean conoceALance = false;
    private boolean chispa100 = false;
    private Random random = new Random();

    @Override
    public void run() {
        System.out.println("[ELISABETHA] Comienza la simulacion. Chispa: " + nivelChispa);

        while (!ClienteMaestro.simulacionTerminada) {

            // NUEVO: Comprobar si ha sido secuestrada por el Dragon
            if (ClienteMaestro.elisabethaSecuestrada) {
                irAlCubilComoPresonera();
            }

            // Atender alquimista si hay uno esperando (identico al original)
            synchronized (ClienteMaestro.lockElisabetha) {
                if (ClienteMaestro.hayPeticionAlquimistaE) {
                    procesarAlquimista();
                }
            }

            // Fase final: esperar a Lance (identico al original)
            if (chispa100) {
                System.out.println("[ELISABETHA] Chispa a 100! Espera en la ventana a Lance...");
                esperarLanceEnTaberna();
                break;
            }

            // Elegir accion aleatoria: 4 opciones, 25% cada una (identico al original)
            int accion = random.nextInt(4);
            switch (accion) {
                case 0: atenderDamas(); break;
                case 1: asistirBaile(); break;
                case 2: leerPergaminos(); break;
                case 3: escaparseALugar(); break;
            }

            // Actualizar chispa global (identico al original)
            synchronized (ClienteMaestro.lockElisabetha) {
                ClienteMaestro.chispaElisabetha = nivelChispa;
            }

            // Comprobar si llego a 100 (identico al original)
            if (nivelChispa >= 100 && conoceALance) {
                nivelChispa = 100;
                chispa100 = true;
            }
        }
        System.out.println("[ELISABETHA] *** SIMULACION TERMINADA ***");
    }

    // =========================================================================
    // NUEVO: METODO PARA EL SECUESTRO DEL DRAGON
    // =========================================================================

    /**
     * NUEVO: Elisabetha ha sido secuestrada por el Dragon Carmesi.
     * Se conecta al Cubil del Dragon via SOCKET y queda BLOQUEADA
     * esperando a que Lance la rescate.
     *
     * FLUJO:
     * 1. Conecta al ServidorCubil (puerto 5003) con Socket
     * 2. Envia ESPERAR_RESCATE -> el servidor hace wait() en ControlCubil
     * 3. El hilo queda BLOQUEADO hasta que Lance envie RESCATAR
     * 4. Recibe el resultado: victoria (true) o derrota parcial (false)
     * 5. Modifica chispa: +50 victoria / -20 malherida
     */
    private void irAlCubilComoPresonera() {
        System.out.println("[ELISABETHA] *** OH NO! El Dragon Carmesi la ha secuestrado! ***");
        System.out.println("[ELISABETHA] Llevada cautiva al cubil del dragon...");

        try {
            // Conectar al servidor del Cubil via socket
            Socket skCubil = new Socket(ClienteMaestro.HOST, ClienteMaestro.PUERTO_CUBIL);
            DataOutputStream salida = new DataOutputStream(skCubil.getOutputStream());
            DataInputStream entrada = new DataInputStream(skCubil.getInputStream());

            // Enviar ESPERAR_RESCATE -> este comando hace wait() en el servidor
            // El hilo de Elisabetha queda BLOQUEADO aqui hasta que Lance la rescate
            salida.writeUTF("ESPERAR_RESCATE");

            // --- BLOQUEO: Elisabetha espera aqui hasta que Lance haga notifyAll ---
            boolean fueVictoria = entrada.readBoolean();
            // Cuando llega aqui, Lance ya la rescato

            System.out.println("[ELISABETHA] Lance ha venido a rescatarla!");

            if (fueVictoria) {
                // Victoria: abaten al dragon, +50 chispa
                nivelChispa += 50;
                System.out.println("[ELISABETHA] *** VICTORIA contra el Dragon! +50 chispa! ***");
                System.out.println("[ELISABETHA] Regresan con la cabeza del dragon como trofeo. Chispa: " + nivelChispa);
            } else {
                // Derrota parcial: malheridos, -20 chispa
                if (!chispa100) {
                    nivelChispa = Math.max(0, nivelChispa - 20);
                }
                System.out.println("[ELISABETHA] Emergen victoriosos pero MALHERIDOS. -20 chispa.");
                System.out.println("[ELISABETHA] Chispa: " + nivelChispa);
            }

            // Desconectar del cubil
            salida.writeUTF("DESCONECTAR");
            entrada.readUTF();
            skCubil.close();

        } catch (IOException e) {
            System.out.println("[ELISABETHA] Error en el cubil: " + e.getMessage());
        }

        // Resetear el flag de secuestro
        synchronized (ClienteMaestro.lockSecuestro) {
            ClienteMaestro.elisabethaSecuestrada = false;
            ClienteMaestro.lockSecuestro.notifyAll();
        }
    }

    // =========================================================================
    // METODOS IDENTICOS AL ORIGINAL (sin cambios)
    // =========================================================================

    private void atenderDamas() {
        System.out.println("[ELISABETHA] Decide atender a alguna de sus Damas del Lazo...");
        boolean[] intentadas = new boolean[4];
        boolean atendida = false;

        for (int intentos = 0; intentos < 4 && !atendida; intentos++) {
            int damaElegida;
            do { damaElegida = random.nextInt(4); } while (intentadas[damaElegida]);
            intentadas[damaElegida] = true;

            String nombreDama = "Dama" + (damaElegida + 1);
            System.out.println("[ELISABETHA] Intenta atender a " + nombreDama + "...");
            try { Thread.sleep(4000); } catch (InterruptedException e) { }

            synchronized (ClienteMaestro.lockElisabetha) {
                if (ClienteMaestro.hayPeticionDama && ClienteMaestro.damaQuePide != null
                        && ClienteMaestro.damaQuePide.equals(nombreDama)) {
                    System.out.println("[ELISABETHA] Atiende a " + nombreDama);
                    String mensaje = ClienteMaestro.mensajeDama;
                    ClienteMaestro.hayPeticionDama = false;
                    ClienteMaestro.damaQuePide = null;
                    ClienteMaestro.mensajeDama = null;
                    ClienteMaestro.lockElisabetha.notifyAll();

                    if (mensaje != null && mensaje.equals("RUMOR_LANCE")) {
                        if (!chispa100) { nivelChispa = Math.max(0, nivelChispa - 5); }
                        System.out.println("[ELISABETHA] Rumor sobre Lance. Chispa: " + nivelChispa);
                    } else if (mensaje != null && mensaje.equals("INVITACION_BAILE")) {
                        if (random.nextInt(100) < 20) {
                            System.out.println("[ELISABETHA] No puede esquivar la invitacion al baile.");
                            asistirBaile();
                        } else {
                            System.out.println("[ELISABETHA] Esquiva la invitacion al baile.");
                        }
                    } else {
                        System.out.println("[ELISABETHA] " + nombreDama + " cuenta una confidencia.");
                    }
                    atendida = true;
                } else {
                    System.out.println("[ELISABETHA] " + nombreDama + " no tiene nada que contarle.");
                }
            }
        }
        if (!atendida) {
            System.out.println("[ELISABETHA] Ninguna dama tenia algo que contarle.");
        }
    }

    private void asistirBaile() {
        System.out.println("[ELISABETHA] Asiste a un baile de la Corte...");
        try { Thread.sleep(5000); } catch (InterruptedException e) { }
        if (!chispa100) {
            nivelChispa = Math.max(0, nivelChispa - 5);
            System.out.println("[ELISABETHA] El baile la aburre. Chispa: " + nivelChispa);
        }
    }

    private void leerPergaminos() {
        System.out.println("[ELISABETHA] Lee pergaminos en la biblioteca...");
        try { Thread.sleep(5000); } catch (InterruptedException e) { }
        if (chispa100) return;
        if (random.nextBoolean()) {
            nivelChispa = Math.max(0, nivelChispa - 7);
            System.out.println("[ELISABETHA] Pergaminos soporiferos. Chispa: " + nivelChispa);
        } else {
            if (conoceALance) {
                nivelChispa += 5;
            } else {
                nivelChispa = Math.min(30, nivelChispa + 5);
            }
            System.out.println("[ELISABETHA] Leyendas de caballeros. Chispa: " + nivelChispa);
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

    /**
     * VISITAR MERCADO - MODIFICADO: ahora comprueba si el mercado esta atacado.
     * Si recibe "MERCADO_ATACADO" del servidor, no puede comprar.
     */
    private void visitarMercado() {
        System.out.println("[ELISABETHA] Se dirige al Mercado...");
        try {
            Socket skMercado = new Socket(ClienteMaestro.HOST, ClienteMaestro.PUERTO_MERCADO);
            DataOutputStream salida = new DataOutputStream(skMercado.getOutputStream());
            DataInputStream entrada = new DataInputStream(skMercado.getInputStream());

            salida.writeUTF("VISITAR_MERCADO");
            salida.writeUTF("Elisabetha");

            // MODIFICADO: leer estado del mercado antes de comprar
            String estado = entrada.readUTF();
            if (estado.equals("MERCADO_ATACADO")) {
                System.out.println("[ELISABETHA] El mercado esta en llamas por el dragon! Huye!");
            } else {
                // Mercado OK, comprar normalmente (identico al original)
                int numProductos = entrada.readInt();
                String[] productos = new String[numProductos];
                for (int i = 0; i < numProductos; i++) {
                    productos[i] = entrada.readUTF();
                }
                int eleccion = random.nextInt(numProductos);
                salida.writeInt(eleccion);
                System.out.println("[ELISABETHA] Compra: " + productos[eleccion]);
                String respuesta = entrada.readUTF();
                System.out.println("[ELISABETHA] Mercader: " + respuesta);
            }

            salida.writeUTF("DESCONECTAR");
            entrada.readUTF();
            skMercado.close();

            try { Thread.sleep(5000); } catch (InterruptedException e) { }
        } catch (IOException e) {
            System.out.println("[ELISABETHA] No pudo llegar al mercado: " + e.getMessage());
        }
    }

    // Visitar Taberna (identico al original)
    private void visitarTaberna() {
        System.out.println("[ELISABETHA] Se dirige a la Taberna 'El Descanso del Guerrero'...");
        try {
            Socket skTaberna = new Socket(ClienteMaestro.HOST, ClienteMaestro.PUERTO_TABERNA);
            DataOutputStream salida = new DataOutputStream(skTaberna.getOutputStream());
            DataInputStream entrada = new DataInputStream(skTaberna.getInputStream());

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
                    lanceEsta = entrada.readBoolean();
                    if (lanceEsta) {
                        encuentroConLance(salida, entrada);
                        break;
                    }
                }
                if (!lanceEsta) {
                    System.out.println("[ELISABETHA] Lance no aparecio. Chispa: " + nivelChispa);
                }
            }

            salida.writeUTF("SALIR_TABERNA");
            salida.writeUTF("ELISABETHA");
            entrada.readUTF();
            salida.writeUTF("DESCONECTAR");
            entrada.readUTF();
            skTaberna.close();
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
            System.out.println("[ELISABETHA] *** LA CHISPA ADECUADA HA NACIDO *** Chispa: 75");
        } else if (!conoceALance) {
            nivelChispa = 75;
            conoceALance = true;
            System.out.println("[ELISABETHA] *** LA CHISPA HA NACIDO *** Chispa: 75");
        } else {
            nivelChispa += 10;
            System.out.println("[ELISABETHA] Coincide con Lance. Chispa: " + nivelChispa);
        }
    }

    private void esperarLanceEnTaberna() {
        try {
            Socket skTaberna = new Socket(ClienteMaestro.HOST, ClienteMaestro.PUERTO_TABERNA);
            DataOutputStream salida = new DataOutputStream(skTaberna.getOutputStream());
            DataInputStream entrada = new DataInputStream(skTaberna.getInputStream());

            salida.writeUTF("REGISTRAR_CHISPA_100");
            salida.writeUTF("ELISABETHA");
            entrada.readUTF();

            salida.writeUTF("ESPERAR_AL_OTRO");
            salida.writeUTF("ELISABETHA");
            String resultado = entrada.readUTF();
            if (resultado.equals("FINAL_FELIZ")) {
                System.out.println("[ELISABETHA] *** FINAL FELIZ *** Chispa maxima!");
            }

            salida.writeUTF("DESCONECTAR");
            entrada.readUTF();
            skTaberna.close();
        } catch (IOException e) {
            System.out.println("[ELISABETHA] Error esperando a Lance: " + e.getMessage());
        }
    }

    private void procesarAlquimista() {
        if (ClienteMaestro.mensajeAlquimistaE != null && ClienteMaestro.mensajeAlquimistaE.equals("POCION")) {
            if (random.nextInt(100) < 30) {
                if (!chispa100) {
                    nivelChispa = Math.max(0, nivelChispa - 20);
                    System.out.println("[ELISABETHA] Engañada por el alquimista! Chispa: " + nivelChispa);
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

