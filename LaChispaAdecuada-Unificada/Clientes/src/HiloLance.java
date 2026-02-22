import java.io.*;
import java.net.Socket;
import java.util.Random;

/**
 * HILO LANCE - Caballero de Roedalia (Lance du Lac).
 * extends Thread -> misma estructura que HiloElisabetha.
 *
 * LOGICA PRINCIPAL: 3 acciones aleatorias (33% cada una):
 * 0. Hablar con companeros del Porton Norte
 * 1. Desafiar a duelo (si hay ofensa pendiente)
 * 2. Realizar guardia (Porton Norte o Taberna)
 *
 * COMUNICACION:
 * - Con Taberna/Porton Norte: SOCKETS
 * - Con Caballeros/Alquimistas: VARIABLES COMPARTIDAS (lockLance)
 *
 * DIFERENCIAS CON ELISABETHA:
 * - Lance tiene 3 acciones (no 4)
 * - Vigilar taberna dura 8 seg (Elisabetha 5 seg)
 * - Duelo: 100% victoria, 20% dano (chispa -5), 80% limpio (+7 max 50 sin conocer)
 * - Alquimista puede AMENAZAR (chispa -30) o POCION (chispa -20)
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
            // Comprobar peticion de alquimista
            synchronized (ClienteMaestro.lockLance) {
                if (ClienteMaestro.hayPeticionAlquimistaL) {
                    procesarAlquimista();
                }
            }

            if (chispa100) {
                System.out.println("[LANCE] Chispa a 100! Se pone en el Porton Norte a esperar a Elisabetha...");
                esperarElisabethaEnTaberna();
                break;
            }

            int accion = random.nextInt(3);

            switch (accion) {
                case 0:
                    hablarConCompaneros();
                    break;
                case 1:
                    desafiarDuelo();
                    break;
                case 2:
                    realizarGuardia();
                    break;
            }

            // Actualizar chispa global
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

    private void hablarConCompaneros() {
        System.out.println("[LANCE] Decide hablar con algun companero del Porton Norte...");

        boolean[] intentados = new boolean[4];
        boolean atendido = false;

        for (int intentos = 0; intentos < 4 && !atendido; intentos++) {
            int cabElegido;
            do {
                cabElegido = random.nextInt(4);
            } while (intentados[cabElegido]);
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
                        System.out.println("[LANCE] " + nombreCab + " ofende a Elisabetha! Lance lo reta a duelo!");
                        realizarDueloConCompanero(nombreCab);
                    } else {
                        System.out.println("[LANCE] " + nombreCab + " le cuenta una confidencia. Sin efecto.");
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
        System.out.println("[LANCE] No hay ofensas pendientes. Entrena con su espada.");
        try { Thread.sleep(3000); } catch (InterruptedException e) { }
    }

    private void realizarDueloConCompanero(String nombreCab) {
        System.out.println("[LANCE] Duelo con " + nombreCab + "!");
        try { Thread.sleep(5000); } catch (InterruptedException e) { }

        if (random.nextInt(100) < 20) {
            if (!chispa100) {
                nivelChispa = Math.max(0, nivelChispa - 5);
            }
            System.out.println("[LANCE] Vence a " + nombreCab + " pero lo dana gravemente. Chispa: " + nivelChispa);
        } else {
            if (conoceAElisabetha) {
                nivelChispa += 7;
            } else {
                nivelChispa = Math.min(50, nivelChispa + 7);
            }
            System.out.println("[LANCE] Vence a " + nombreCab + " sin dano. Chispa: " + nivelChispa);
        }
    }

    private void realizarGuardia() {
        System.out.println("[LANCE] Decide realizar un turno de guardia...");

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
            System.out.println("[LANCE] No pudo llegar al Porton Norte: " + e.getMessage());
        }
    }

    private void vigilarTaberna() {
        System.out.println("[LANCE] Se dirige a vigilar la Taberna 'El Descanso del Guerrero'...");
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
                    System.out.println("[LANCE] Elisabetha no aparecio. Vuelve al Porton. Chispa: " + nivelChispa);
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
            System.out.println("[LANCE] *** LA CHISPA ADECUADA HA NACIDO ***");
            System.out.println("[LANCE] Sus ojos se encontraron con los de Elisabetha... Chispa: " + nivelChispa);
        } else if (!conoceAElisabetha) {
            nivelChispa = 75;
            conoceAElisabetha = true;
            System.out.println("[LANCE] *** LA CHISPA ADECUADA HA NACIDO ***");
            System.out.println("[LANCE] Encuentra a Elisabetha en la taberna... Chispa: " + nivelChispa);
        } else {
            nivelChispa += 10;
            System.out.println("[LANCE] Coincide con Elisabetha en la taberna. Chispa: " + nivelChispa);
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
                System.out.println("[LANCE] *** FINAL FELIZ ***");
                System.out.println("[LANCE] Lance y Elisabetha se reencuentran con chispa maxima!");
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
                    System.out.println("[LANCE] El alquimista lo engano con una pocion! Chispa: " + nivelChispa);
                }
            } else {
                System.out.println("[LANCE] No se deja enganar por el alquimista.");
            }
        } else if (ClienteMaestro.tipoAccionAlquimistaL != null && ClienteMaestro.tipoAccionAlquimistaL.equals("AMENAZA")) {
            if (random.nextInt(100) < 20) {
                if (!chispa100) {
                    nivelChispa = Math.max(0, nivelChispa - 30);
                    System.out.println("[LANCE] La amenaza del Frente Norte afecta a Lance! Chispa: " + nivelChispa);
                }
            } else {
                System.out.println("[LANCE] No se deja amedrentar por el alquimista.");
            }
        }
        ClienteMaestro.hayPeticionAlquimistaL = false;
        ClienteMaestro.mensajeAlquimistaL = null;
        ClienteMaestro.tipoAccionAlquimistaL = null;
        ClienteMaestro.lockLance.notifyAll();
    }
}

