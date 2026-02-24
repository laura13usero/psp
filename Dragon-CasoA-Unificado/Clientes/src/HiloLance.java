import java.io.*;
import java.net.Socket;
import java.util.Random;

/**
 * HILO LANCE - CASO A: DRAGON (Secuestro + Rescate + Duelo).
 *
 * MODIFICACIONES respecto al original:
 *   1. Antes de cada accion, comprueba si Elisabetha ha sido secuestrada
 *   2. Si esta secuestrada, se conecta al Cubil del Dragon via socket
 *      y envia RESCATAR -> hace notifyAll() en ControlCubil para liberar a Elisabetha
 *   3. Segun el resultado del duelo: +50 chispa (victoria) o -20 chispa (malherido)
 *
 * Todo lo demas (hablarConCompaneros, desafiarDuelo, realizarGuardia,
 * vigilarPortonNorte, vigilarTaberna, procesarAlquimista, esperarElisabethaEnTaberna)
 * es IDENTICO al original.
 */
public class HiloLance extends Thread {
    private int nivelChispa = 0;
    private boolean conoceAElisabetha = false;
    private boolean chispa100 = false;
    private Random random = new Random();

    @Override
    public void run() {
        System.out.println("[LANCE] Comienza la simulacion. Chispa: " + nivelChispa);

        while (!ClienteMaestro.simulacionTerminada) {

            // NUEVO: Comprobar si Elisabetha ha sido secuestrada por el Dragon
            if (ClienteMaestro.elisabethaSecuestrada) {
                irARescatarAlCubil();
            }

            // Comprobar alquimista (identico al original)
            synchronized (ClienteMaestro.lockLance) {
                if (ClienteMaestro.hayPeticionAlquimistaL) {
                    procesarAlquimista();
                }
            }

            // Fase final (identico al original)
            if (chispa100) {
                System.out.println("[LANCE] Chispa a 100! Espera en el Porton a Elisabetha...");
                esperarElisabethaEnTaberna();
                break;
            }

            // 3 acciones aleatorias, 33% cada una (identico al original)
            int accion = random.nextInt(3);
            switch (accion) {
                case 0: hablarConCompaneros(); break;
                case 1: desafiarDuelo(); break;
                case 2: realizarGuardia(); break;
            }

            // Actualizar chispa global (identico al original)
            synchronized (ClienteMaestro.lockLance) {
                ClienteMaestro.chispaLance = nivelChispa;
            }

            if (nivelChispa >= 100 && conoceAElisabetha) {
                nivelChispa = 100;
                chispa100 = true;
            }
        }
        System.out.println("[LANCE] *** SIMULACION TERMINADA ***");
    }

    // =========================================================================
    // NUEVO: METODO PARA EL RESCATE EN EL CUBIL DEL DRAGON
    // =========================================================================

    /**
     * NUEVO: Lance acude al Cubil del Dragon a rescatar a Elisabetha.
     * Se conecta via SOCKET al ServidorCubil y envia RESCATAR.
     * El servidor ejecuta ControlCubil.rescatar() que hace el duelo
     * y luego notifyAll() para despertar a Elisabetha.
     *
     * "En el climax de esta gesta, ambos se batiran en duelo contra la bestia.
     *  El resultado es incierto: podrian emerger victoriosos pero malheridos,
     *  perdiendo 20 puntos de chispa; o bien, podrian abatir al dragon
     *  y regresar con su cabeza como trofeo, elevando su chispa en 50 puntos."
     */
    private void irARescatarAlCubil() {
        System.out.println("[LANCE] *** Elisabetha ha sido secuestrada! ***");
        System.out.println("[LANCE] Movido por la urgencia, cabalga hacia el cubil del dragon!");

        // Simular el viaje al cubil (4 segundos)
        try { Thread.sleep(4000); } catch (InterruptedException e) { }

        try {
            // Conectar al ServidorCubil via socket
            Socket skCubil = new Socket(ClienteMaestro.HOST, ClienteMaestro.PUERTO_CUBIL);
            DataOutputStream salida = new DataOutputStream(skCubil.getOutputStream());
            DataInputStream entrada = new DataInputStream(skCubil.getInputStream());

            // Enviar RESCATAR -> el servidor hace el duelo y libera a Elisabetha
            salida.writeUTF("RESCATAR");
            boolean huboRescate = entrada.readBoolean();

            if (huboRescate) {
                boolean fueVictoria = entrada.readBoolean();

                System.out.println("[LANCE] *** SE BATEN EN DUELO CONTRA LA BESTIA! ***");

                if (fueVictoria) {
                    // Victoria: +50 chispa
                    nivelChispa += 50;
                    System.out.println("[LANCE] *** VICTORIA! Abaten al dragon! +50 chispa! ***");
                    System.out.println("[LANCE] Regresan con la cabeza del dragon. Chispa: " + nivelChispa);
                } else {
                    // Derrota parcial: -20 chispa
                    if (!chispa100) {
                        nivelChispa = Math.max(0, nivelChispa - 20);
                    }
                    System.out.println("[LANCE] Victoriosos pero MALHERIDOS. -20 chispa.");
                    System.out.println("[LANCE] Chispa: " + nivelChispa);
                }
            } else {
                System.out.println("[LANCE] El cubil estaba vacio. El dragon ya huyo.");
            }

            // Desconectar
            salida.writeUTF("DESCONECTAR");
            entrada.readUTF();
            skCubil.close();

        } catch (IOException e) {
            System.out.println("[LANCE] Error en el cubil: " + e.getMessage());
        }
    }

    // =========================================================================
    // METODOS IDENTICOS AL ORIGINAL (sin cambios)
    // =========================================================================

    private void hablarConCompaneros() {
        System.out.println("[LANCE] Decide hablar con algun companero del Porton Norte...");
        boolean[] intentados = new boolean[4];
        boolean atendido = false;

        for (int intentos = 0; intentos < 4 && !atendido; intentos++) {
            int cabElegido;
            do { cabElegido = random.nextInt(4); } while (intentados[cabElegido]);
            intentados[cabElegido] = true;

            String nombreCab = "Caballero" + (cabElegido + 1);
            System.out.println("[LANCE] Intenta hablar con " + nombreCab + "...");
            try { Thread.sleep(4000); } catch (InterruptedException e) { }

            synchronized (ClienteMaestro.lockLance) {
                if (ClienteMaestro.hayPeticionCaballero && ClienteMaestro.caballeroQuePide != null
                        && ClienteMaestro.caballeroQuePide.equals(nombreCab)) {
                    System.out.println("[LANCE] Habla con " + nombreCab);
                    String mensaje = ClienteMaestro.mensajeCaballero;
                    ClienteMaestro.hayPeticionCaballero = false;
                    ClienteMaestro.caballeroQuePide = null;
                    ClienteMaestro.mensajeCaballero = null;
                    ClienteMaestro.lockLance.notifyAll();

                    if (mensaje != null && mensaje.equals("OFENSA_ELISABETHA")) {
                        System.out.println("[LANCE] " + nombreCab + " ofende a Elisabetha! Duelo!");
                        realizarDueloConCompanero(nombreCab);
                    } else {
                        System.out.println("[LANCE] " + nombreCab + " cuenta una confidencia.");
                    }
                    atendido = true;
                } else {
                    System.out.println("[LANCE] " + nombreCab + " no tiene nada que contarle.");
                }
            }
        }
        if (!atendido) {
            System.out.println("[LANCE] Ningun companero tenia algo que contarle.");
        }
    }

    private void desafiarDuelo() {
        synchronized (ClienteMaestro.lockLance) {
            if (ClienteMaestro.hayPeticionCaballero && ClienteMaestro.mensajeCaballero != null
                    && ClienteMaestro.mensajeCaballero.equals("OFENSA_ELISABETHA")) {
                String nombreCab = ClienteMaestro.caballeroQuePide;
                ClienteMaestro.hayPeticionCaballero = false;
                ClienteMaestro.caballeroQuePide = null;
                ClienteMaestro.mensajeCaballero = null;
                ClienteMaestro.lockLance.notifyAll();
                realizarDueloConCompanero(nombreCab);
                return;
            }
        }
        System.out.println("[LANCE] No hay ofensas. Entrena con su espada.");
        try { Thread.sleep(3000); } catch (InterruptedException e) { }
    }

    private void realizarDueloConCompanero(String nombreCab) {
        System.out.println("[LANCE] Duelo con " + nombreCab + "!");
        try { Thread.sleep(5000); } catch (InterruptedException e) { }

        if (random.nextInt(100) < 20) {
            if (!chispa100) { nivelChispa = Math.max(0, nivelChispa - 5); }
            System.out.println("[LANCE] Vence pero dana a " + nombreCab + ". Chispa: " + nivelChispa);
        } else {
            if (conoceAElisabetha) {
                nivelChispa += 7;
            } else {
                nivelChispa = Math.min(50, nivelChispa + 7);
            }
            System.out.println("[LANCE] Vence sin dano a " + nombreCab + ". Chispa: " + nivelChispa);
        }
    }

    private void realizarGuardia() {
        System.out.println("[LANCE] Realiza un turno de guardia...");
        if (random.nextBoolean()) {
            vigilarPortonNorte();
        } else {
            vigilarTaberna();
        }
    }

    private void vigilarPortonNorte() {
        System.out.println("[LANCE] Se dirige al Porton Norte a vigilar...");
        try {
            Socket skPorton = new Socket(ClienteMaestro.HOST, ClienteMaestro.PUERTO_PORTON);
            DataOutputStream salida = new DataOutputStream(skPorton.getOutputStream());
            DataInputStream entrada = new DataInputStream(skPorton.getInputStream());

            salida.writeUTF("VIGILAR_PORTON");
            salida.writeUTF("Lance");

            String tipoCarreta = entrada.readUTF();
            String raton = entrada.readUTF();
            String mensaje = entrada.readUTF();
            System.out.println("[LANCE] Inspeccion: " + tipoCarreta + " - " + raton);
            System.out.println("[LANCE] " + mensaje);

            salida.writeUTF("DESCONECTAR");
            entrada.readUTF();
            skPorton.close();

            try { Thread.sleep(5000); } catch (InterruptedException e) { }
        } catch (IOException e) {
            System.out.println("[LANCE] No pudo llegar al Porton: " + e.getMessage());
        }
    }

    private void vigilarTaberna() {
        System.out.println("[LANCE] Vigila la Taberna 'El Descanso del Guerrero'...");
        try {
            Socket skTaberna = new Socket(ClienteMaestro.HOST, ClienteMaestro.PUERTO_TABERNA);
            DataOutputStream salida = new DataOutputStream(skTaberna.getOutputStream());
            DataInputStream entrada = new DataInputStream(skTaberna.getInputStream());

            salida.writeUTF("ENTRAR");
            salida.writeUTF("LANCE");
            entrada.readUTF();

            salida.writeUTF("CONSULTAR_ELISABETHA");
            boolean elisabethaEsta = entrada.readBoolean();
            long tiempoEntrada = System.currentTimeMillis();

            if (elisabethaEsta) {
                encuentroConElisabetha(salida, entrada);
            } else {
                System.out.println("[LANCE] Elisabetha no esta... vigilando 8 segundos...");
                while (System.currentTimeMillis() - tiempoEntrada < 8000) {
                    try { Thread.sleep(1000); } catch (InterruptedException e) { }
                    salida.writeUTF("CONSULTAR_ELISABETHA");
                    elisabethaEsta = entrada.readBoolean();
                    if (elisabethaEsta) {
                        encuentroConElisabetha(salida, entrada);
                        break;
                    }
                }
                if (!elisabethaEsta) {
                    System.out.println("[LANCE] Elisabetha no aparecio. Chispa: " + nivelChispa);
                }
            }

            salida.writeUTF("SALIR_TABERNA");
            salida.writeUTF("LANCE");
            entrada.readUTF();
            salida.writeUTF("DESCONECTAR");
            entrada.readUTF();
            skTaberna.close();
        } catch (IOException e) {
            System.out.println("[LANCE] No pudo llegar a la taberna: " + e.getMessage());
        }
    }

    private void encuentroConElisabetha(DataOutputStream salida, DataInputStream entrada) throws IOException {
        salida.writeUTF("YA_SE_CONOCEN");
        boolean yaSeConocian = entrada.readBoolean();
        if (!yaSeConocian) {
            salida.writeUTF("SE_CONOCEN");
            entrada.readUTF();
            nivelChispa = 75;
            conoceAElisabetha = true;
            System.out.println("[LANCE] *** LA CHISPA ADECUADA HA NACIDO *** Chispa: 75");
        } else if (!conoceAElisabetha) {
            nivelChispa = 75;
            conoceAElisabetha = true;
            System.out.println("[LANCE] *** LA CHISPA HA NACIDO *** Chispa: 75");
        } else {
            nivelChispa += 10;
            System.out.println("[LANCE] Coincide con Elisabetha. Chispa: " + nivelChispa);
        }
    }

    private void esperarElisabethaEnTaberna() {
        try {
            Socket skTaberna = new Socket(ClienteMaestro.HOST, ClienteMaestro.PUERTO_TABERNA);
            DataOutputStream salida = new DataOutputStream(skTaberna.getOutputStream());
            DataInputStream entrada = new DataInputStream(skTaberna.getInputStream());

            salida.writeUTF("REGISTRAR_CHISPA_100");
            salida.writeUTF("LANCE");
            entrada.readUTF();

            salida.writeUTF("ESPERAR_AL_OTRO");
            salida.writeUTF("LANCE");
            String resultado = entrada.readUTF();
            if (resultado.equals("FINAL_FELIZ")) {
                System.out.println("[LANCE] *** FINAL FELIZ *** Chispa maxima!");
            }

            salida.writeUTF("DESCONECTAR");
            entrada.readUTF();
            skTaberna.close();
        } catch (IOException e) {
            System.out.println("[LANCE] Error esperando a Elisabetha: " + e.getMessage());
        }
    }

    private void procesarAlquimista() {
        if (ClienteMaestro.tipoAccionAlquimistaL != null && ClienteMaestro.tipoAccionAlquimistaL.equals("POCION")) {
            if (random.nextInt(100) < 20) {
                if (!chispa100) {
                    nivelChispa = Math.max(0, nivelChispa - 20);
                    System.out.println("[LANCE] Enganado por el alquimista! Chispa: " + nivelChispa);
                }
            } else {
                System.out.println("[LANCE] No se deja enganar por el alquimista.");
            }
        } else if (ClienteMaestro.tipoAccionAlquimistaL != null && ClienteMaestro.tipoAccionAlquimistaL.equals("AMENAZA")) {
            if (random.nextInt(100) < 20) {
                if (!chispa100) {
                    nivelChispa = Math.max(0, nivelChispa - 30);
                    System.out.println("[LANCE] Amenaza del Frente Norte! Chispa: " + nivelChispa);
                }
            } else {
                System.out.println("[LANCE] No se deja amedrentar.");
            }
        }
        ClienteMaestro.hayPeticionAlquimistaL = false;
        ClienteMaestro.mensajeAlquimistaL = null;
        ClienteMaestro.tipoAccionAlquimistaL = null;
        ClienteMaestro.lockLance.notifyAll();
    }
}

