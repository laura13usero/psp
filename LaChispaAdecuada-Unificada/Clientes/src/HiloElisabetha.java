import java.io.*;
import java.net.Socket;
import java.util.Random;

/**
 * HILO ELISABETHA - Princesa de Roedalia.
 * extends Thread -> sobreescribe run() para definir su comportamiento.
 *
 * LOGICA PRINCIPAL (bucle en run):
 * 1. Comprobar si hay alquimista esperando (prioridad alta)
 * 2. Si chispa >= 100, ir a esperar a Lance (fase final)
 * 3. Si no, elegir accion aleatoria entre 4 opciones (misma probabilidad)
 *
 * COMUNICACION:
 * - Con la Taberna/Mercado: via SOCKETS (DataInputStream/DataOutputStream)
 * - Con las Damas/Alquimistas: via VARIABLES COMPARTIDAS en ClienteMaestro
 *   protegidas con synchronized(lockElisabetha) + wait/notifyAll
 */
public class HiloElisabetha extends Thread {
    private int nivelChispa = 0;          // Nivel de chispa (0 a 100)
    private boolean conoceALance = false;  // Se activa tras primer encuentro en taberna
    private boolean chispa100 = false;     // Fase final: chispa llego a 100
    private Random random = new Random();  // Generador de aleatorios para decisiones

    @Override
    public void run() {
        System.out.println("[ELISABETHA] Comienza la simulacion. Chispa: " + nivelChispa);

        while (!ClienteMaestro.simulacionTerminada) {
            // PRIORIDAD: Atender al alquimista si hay uno esperando
            // synchronized garantiza exclusion mutua al acceder al buzon compartido
            synchronized (ClienteMaestro.lockElisabetha) {
                if (ClienteMaestro.hayPeticionAlquimistaE) {
                    procesarAlquimista();
                }
            }

            // FASE FINAL: Si chispa llego a 100, esperar a Lance
            if (chispa100) {
                System.out.println("[ELISABETHA] Chispa a 100! Se pone en la ventana a esperar a Lance...");
                esperarLanceEnTaberna(); // Se conecta al servidor taberna y hace wait()
                break; // Sale del bucle -> hilo termina
            }

            // ELEGIR ACCION ALEATORIA (4 opciones, 25% cada una)
            int accion = random.nextInt(4);

            switch (accion) {
                case 0:
                    atenderDamas();     // Hablar con alguna de las 4 damas
                    break;
                case 1:
                    asistirBaile();     // Baile de la corte (chispa -5)
                    break;
                case 2:
                    leerPergaminos();   // Biblioteca (chispa -7 o +5)
                    break;
                case 3:
                    escaparseALugar();  // Ir al Mercado o a la Taberna
                    break;
            }

            // Actualizar la copia global de chispa (para que alquimistas la vean)
            synchronized (ClienteMaestro.lockElisabetha) {
                ClienteMaestro.chispaElisabetha = nivelChispa;
            }

            // Comprobar si llego a 100 (solo si ya conoce a Lance)
            if (nivelChispa >= 100 && conoceALance) {
                nivelChispa = 100;
                chispa100 = true;
            }
        }

        System.out.println("[ELISABETHA] *** SIMULACION TERMINADA ***");
    }

    /**
     * ATENDER DAMAS: Elige aleatoriamente a cual de las 4 damas atender.
     * Mira si esa dama tiene algo que contarle (consultando el buzon compartido).
     * Si no, prueba con otra. Cada intento cuesta 4 segundos.
     *
     * SINCRONIZACION: synchronized(lockElisabetha) para leer/escribir el buzon
     * de peticiones de damas. notifyAll() para despertar a la dama que esperaba.
     */
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
                if (ClienteMaestro.hayPeticionDama && ClienteMaestro.damaQuePide != null
                        && ClienteMaestro.damaQuePide.equals(nombreDama)) {
                    System.out.println("[ELISABETHA] Atiende a " + nombreDama);
                    String mensaje = ClienteMaestro.mensajeDama;
                    ClienteMaestro.hayPeticionDama = false;
                    ClienteMaestro.damaQuePide = null;
                    ClienteMaestro.mensajeDama = null;
                    ClienteMaestro.lockElisabetha.notifyAll();

                    if (mensaje != null && mensaje.equals("RUMOR_LANCE")) {
                        if (!chispa100) {
                            nivelChispa = Math.max(0, nivelChispa - 5);
                        }
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

        if (!atendida) {
            System.out.println("[ELISABETHA] Ninguna dama tenia algo que contarle.");
        }
    }

    /**
     * ASISTIR A BAILE: Dura 5 segundos. Baja chispa en 5 (minimo 0).
     * No baja si chispa ya esta a 100 (fase final, nada baja la chispa).
     */
    private void asistirBaile() {
        System.out.println("[ELISABETHA] Asiste a un baile de la Corte...");
        try { Thread.sleep(5000); } catch (InterruptedException e) { }

        if (!chispa100) {
            nivelChispa = Math.max(0, nivelChispa - 5);
            System.out.println("[ELISABETHA] El baile la aburre soberanamente. Chispa: " + nivelChispa);
        }
    }

    /**
     * LEER PERGAMINOS: Dura 5 segundos. 50% probabilidad:
     * - Soporiferos: chispa -7 (min 0)
     * - Leyendas heroicas: chispa +5 (max 30 si NO conoce a Lance, ilimitado si SI)
     */
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

    /**
     * ESCAPARSE: Elige aleatoriamente entre Mercado y Taberna.
     */
    private void escaparseALugar() {
        System.out.println("[ELISABETHA] Se escapa por los pasadizos del castillo...");

        if (random.nextBoolean()) {
            visitarMercado();
        } else {
            visitarTaberna();
        }
    }

    /**
     * VISITAR MERCADO: Se conecta al servidor Mercado por SOCKET.
     * Protocolo: enviar "VISITAR_MERCADO" + nombre, recibir 5 productos,
     * elegir uno aleatoriamente, comprarlo. Dura 5 segundos. Sin efecto en chispa.
     */
    private void visitarMercado() {
        System.out.println("[ELISABETHA] Se dirige al Mercado de Roedalia...");
        try {
            Socket skMercado = new Socket(ClienteMaestro.HOST, ClienteMaestro.PUERTO_MERCADO);
            DataOutputStream salida = new DataOutputStream(skMercado.getOutputStream());
            DataInputStream entrada = new DataInputStream(skMercado.getInputStream());

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
            skMercado.close();

            try { Thread.sleep(5000); } catch (InterruptedException e) { }
        } catch (IOException e) {
            System.out.println("[ELISABETHA] No pudo llegar al mercado: " + e.getMessage());
        }
    }

    /**
     * VISITAR TABERNA: Se conecta al servidor Taberna por SOCKET.
     * 1. Anuncia su entrada (ENTRAR + ELISABETHA)
     * 2. Consulta si Lance esta (CONSULTAR_LANCE -> boolean)
     * 3. Si esta: ejecuta encuentroConLance()
     * 4. Si no: espera 5 segundos consultando cada segundo
     * 5. Al salir: anuncia salida (SALIR_TABERNA)
     */
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
                    System.out.println("[ELISABETHA] Lance no aparecio. Vuelve al castillo. Chispa: " + nivelChispa);
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

    /**
     * ENCUENTRO CON LANCE en la taberna:
     * - Primera vez (YA_SE_CONOCEN = false): marca SE_CONOCEN, chispa sube a 75
     * - Veces siguientes: chispa +10
     */
    private void encuentroConLance(DataOutputStream salida, DataInputStream entrada) throws IOException {
        salida.writeUTF("YA_SE_CONOCEN");
        boolean yaSeConocian = entrada.readBoolean();

        if (!yaSeConocian) {
            salida.writeUTF("SE_CONOCEN");
            entrada.readUTF();
            nivelChispa = 75;
            conoceALance = true;
            System.out.println("[ELISABETHA] *** LA CHISPA ADECUADA HA NACIDO ***");
            System.out.println("[ELISABETHA] Sus ojos se encontraron con los de Lance... Chispa: " + nivelChispa);
        } else if (!conoceALance) {
            nivelChispa = 75;
            conoceALance = true;
            System.out.println("[ELISABETHA] *** LA CHISPA ADECUADA HA NACIDO ***");
            System.out.println("[ELISABETHA] Encuentra a Lance en la taberna... Chispa: " + nivelChispa);
        } else {
            nivelChispa += 10;
            System.out.println("[ELISABETHA] Coincide con Lance en la taberna. Chispa: " + nivelChispa);
        }
    }

    /**
     * FASE FINAL: Cuando chispa llega a 100, se conecta a la Taberna
     * y ejecuta ESPERAR_AL_OTRO. Este comando hace wait() en el servidor
     * hasta que Lance tambien registre su chispa 100.
     */
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
                System.out.println("[ELISABETHA] *** FINAL FELIZ ***");
                System.out.println("[ELISABETHA] Elisabetha y Lance se reencuentran con chispa maxima!");
            }

            salida.writeUTF("DESCONECTAR");
            entrada.readUTF();
            skTaberna.close();
        } catch (IOException e) {
            System.out.println("[ELISABETHA] Error esperando a Lance: " + e.getMessage());
        }
    }

    /**
     * PROCESAR ALQUIMISTA: Se ejecuta dentro de synchronized(lockElisabetha).
     * 30% de probabilidad de que la pocion funcione -> chispa -20.
     * Limpia el buzon y hace notifyAll() para despertar al alquimista.
     */
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
