import java.io.*;
import java.net.*;
import java.util.Random;
import java.util.Scanner;

public class Main {
    // Configuracion de conexion
    static String hostTaberna = "localhost";
    static int puertoTaberna = 5000;
    static String hostMercado = "localhost";
    static int puertoMercado = 5001;
    static int puertoEscucha = 6000; // Puerto donde Elisabetha escucha a Damas y Alquimistas

    // Estado de Elisabetha
    static int nivelChispa = 0;
    static boolean conoceALance = false;
    static boolean chispa100 = false;
    static boolean simulacionTerminada = false;

    // Cola de peticiones de damas
    static String damaQuePide = null;
    static String mensajeDama = null;
    static boolean hayPeticionDama = false;

    // Peticion de alquimista
    static boolean hayPeticionAlquimista = false;
    static String mensajeAlquimista = null;

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        System.out.println("==============================================");
        System.out.println("  ELISABETHA - PRINCESA DE ROEDALIA");
        System.out.println("==============================================");

        System.out.print("IP de la Taberna (localhost): ");
        String input = sc.nextLine().trim();
        if (!input.isEmpty()) hostTaberna = input;

        System.out.print("Puerto de la Taberna (5000): ");
        input = sc.nextLine().trim();
        if (!input.isEmpty()) puertoTaberna = Integer.parseInt(input);

        System.out.print("IP del Mercado (localhost): ");
        input = sc.nextLine().trim();
        if (!input.isEmpty()) hostMercado = input;

        System.out.print("Puerto del Mercado (5001): ");
        input = sc.nextLine().trim();
        if (!input.isEmpty()) puertoMercado = Integer.parseInt(input);

        System.out.print("Puerto donde Elisabetha escucha a Damas/Alquimistas (6000): ");
        input = sc.nextLine().trim();
        if (!input.isEmpty()) puertoEscucha = Integer.parseInt(input);

        // Hilo que escucha conexiones de Damas y Alquimistas
        Thread hiloEscucha = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    ServerSocket skServidor = new ServerSocket(puertoEscucha);
                    System.out.println("[ELISABETHA] Escuchando en puerto " + puertoEscucha + " para Damas y Alquimistas");
                    while (!simulacionTerminada) {
                        Socket skCliente = skServidor.accept();
                        Thread hiloAtencion = new Thread(new AtenderVisitante(skCliente));
                        hiloAtencion.start();
                    }
                } catch (IOException e) {
                    System.out.println("[ELISABETHA] Error en escucha: " + e.getMessage());
                }
            }
        });
        hiloEscucha.setDaemon(true);
        hiloEscucha.start();

        // Bucle principal de acciones de Elisabetha
        Random random = new Random();
        System.out.println("[ELISABETHA] Comienza la simulacion. Nivel de chispa: " + nivelChispa);

        while (!simulacionTerminada) {
            // Comprobar si hay peticion de alquimista (prioridad alta, atender tras terminar accion actual)
            synchronized (Main.class) {
                if (hayPeticionAlquimista) {
                    procesarAlquimista();
                }
            }

            if (chispa100) {
                // Fase final: esperar a Lance
                System.out.println("[ELISABETHA] Chispa a 100! Se pone en la ventana a esperar a Lance...");
                esperarLanceEnTaberna();
                break;
            }

            // Elegir accion aleatoria (4 opciones con la misma probabilidad)
            int accion = random.nextInt(4);

            switch (accion) {
                case 0:
                    atenderDamas(random);
                    break;
                case 1:
                    asistirBaile();
                    break;
                case 2:
                    leerPergaminos(random);
                    break;
                case 3:
                    escaparseALugar(random);
                    break;
            }

            // Comprobar si chispa llego a 100
            if (nivelChispa >= 100 && conoceALance) {
                nivelChispa = 100;
                chispa100 = true;
            }
        }

        System.out.println("[ELISABETHA] *** SIMULACION TERMINADA ***");
        System.out.println("[ELISABETHA] Elisabetha y Lance son felices y comen perdices!");
    }

    static void atenderDamas(Random random) {
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

            // Esperar 4 segundos por la atencion
            try { Thread.sleep(4000); } catch (InterruptedException e) { }

            synchronized (Main.class) {
                if (hayPeticionDama && damaQuePide != null && damaQuePide.equals(nombreDama)) {
                    System.out.println("[ELISABETHA] Atiende a " + nombreDama);
                    String mensaje = mensajeDama;
                    hayPeticionDama = false;
                    damaQuePide = null;
                    mensajeDama = null;
                    Main.class.notifyAll();

                    // Procesar lo que dice la dama
                    if (mensaje != null && mensaje.equals("RUMOR_LANCE")) {
                        // Es un rumor infundado -> baja chispa 5
                        nivelChispa = Math.max(0, nivelChispa - 5);
                        System.out.println("[ELISABETHA] " + nombreDama + " le cuenta un rumor infundado sobre Lance. Chispa: " + nivelChispa);
                    } else if (mensaje != null && mensaje.equals("INVITACION_BAILE")) {
                        // 80% esquivar, 20% ir
                        if (random.nextInt(100) < 20) {
                            System.out.println("[ELISABETHA] No puede esquivar la invitacion al baile de " + nombreDama);
                            asistirBaile();
                        } else {
                            System.out.println("[ELISABETHA] Esquiva la invitacion al baile de " + nombreDama);
                        }
                    } else {
                        System.out.println("[ELISABETHA] " + nombreDama + " le cuenta una confidencia personal. Sin efecto.");
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

    static void asistirBaile() {
        System.out.println("[ELISABETHA] Asiste a un baile de la Corte...");
        try { Thread.sleep(5000); } catch (InterruptedException e) { }

        if (!chispa100) {
            nivelChispa = Math.max(0, nivelChispa - 5);
            System.out.println("[ELISABETHA] El baile la aburre soberanamente. Chispa: " + nivelChispa);
        }
    }

    static void leerPergaminos(Random random) {
        System.out.println("[ELISABETHA] Lee pergaminos en la biblioteca del castillo...");
        try { Thread.sleep(5000); } catch (InterruptedException e) { }

        if (chispa100) return;

        if (random.nextBoolean()) {
            // Pergaminos soporiferos
            nivelChispa = Math.max(0, nivelChispa - 7);
            System.out.println("[ELISABETHA] Lee pergaminos soporiferos sobre la historia del reino. Chispa: " + nivelChispa);
        } else {
            // Historias de caballeros valientes
            if (conoceALance) {
                nivelChispa += 5;
                System.out.println("[ELISABETHA] Lee leyendas de valientes caballeros. Le recuerdan a Lance. Chispa: " + nivelChispa);
            } else {
                nivelChispa = Math.min(30, nivelChispa + 5);
                System.out.println("[ELISABETHA] Lee leyendas de valientes caballeros. Chispa: " + nivelChispa);
            }
        }
    }

    static void escaparseALugar(Random random) {
        System.out.println("[ELISABETHA] Se escapa por los pasadizos del castillo...");

        int lugar = random.nextInt(2);

        if (lugar == 0) {
            visitarMercado(random);
        } else {
            visitarTaberna();
        }
    }

    static void visitarMercado(Random random) {
        System.out.println("[ELISABETHA] Se dirige al Mercado de Roedalia...");
        try {
            Socket skMercado = new Socket(hostMercado, puertoMercado);
            DataOutputStream salida = new DataOutputStream(skMercado.getOutputStream());
            DataInputStream entrada = new DataInputStream(skMercado.getInputStream());

            salida.writeUTF("VISITAR_MERCADO");
            salida.writeUTF("Elisabetha");

            // Recibir productos
            int numProductos = entrada.readInt();
            String[] productos = new String[numProductos];
            System.out.println("[ELISABETHA] El mercader le ofrece:");
            for (int i = 0; i < numProductos; i++) {
                productos[i] = entrada.readUTF();
                System.out.println("  " + (i + 1) + ". " + productos[i]);
            }

            // Elegir aleatoriamente
            int eleccion = random.nextInt(numProductos);
            salida.writeInt(eleccion);
            System.out.println("[ELISABETHA] Compra: " + productos[eleccion]);

            String respuesta = entrada.readUTF();
            System.out.println("[ELISABETHA] Mercader dice: " + respuesta);

            salida.writeUTF("DESCONECTAR");
            entrada.readUTF();
            skMercado.close();

            try { Thread.sleep(5000); } catch (InterruptedException e) { }
        } catch (IOException e) {
            System.out.println("[ELISABETHA] No pudo llegar al mercado: " + e.getMessage());
        }
    }

    static void visitarTaberna() {
        System.out.println("[ELISABETHA] Se dirige a la Taberna 'El Descanso del Guerrero'...");
        try {
            Socket skTaberna = new Socket(hostTaberna, puertoTaberna);
            DataOutputStream salida = new DataOutputStream(skTaberna.getOutputStream());
            DataInputStream entrada = new DataInputStream(skTaberna.getInputStream());

            // Anunciar llegada
            salida.writeUTF("ENTRAR");
            salida.writeUTF("ELISABETHA");
            entrada.readUTF(); // OK

            // Comprobar si Lance esta
            salida.writeUTF("CONSULTAR_LANCE");
            boolean lanceEsta = entrada.readBoolean();

            long tiempoEntrada = System.currentTimeMillis();

            if (lanceEsta) {
                encuentroConLance(salida, entrada);
            } else {
                // Esperar 5 segundos a ver si llega Lance
                System.out.println("[ELISABETHA] Lance no esta en la taberna... esperando...");
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
                    System.out.println("[ELISABETHA] Lance no aparecio. Vuelve al castillo con chispa: " + nivelChispa);
                }
            }

            // Anunciar salida
            salida.writeUTF("SALIR_TABERNA");
            salida.writeUTF("ELISABETHA");
            entrada.readUTF(); // OK

            salida.writeUTF("DESCONECTAR");
            entrada.readUTF();
            skTaberna.close();
        } catch (IOException e) {
            System.out.println("[ELISABETHA] No pudo llegar a la taberna: " + e.getMessage());
        }
    }

    static void encuentroConLance(DataOutputStream salida, DataInputStream entrada) throws IOException {
        salida.writeUTF("YA_SE_CONOCEN");
        boolean yaSeConocian = entrada.readBoolean();

        if (!yaSeConocian) {
            // Primera vez que se encuentran!
            salida.writeUTF("SE_CONOCEN");
            entrada.readUTF(); // CHISPA_GENERADA
            nivelChispa = 75;
            conoceALance = true;
            System.out.println("[ELISABETHA] *** LA CHISPA ADECUADA HA NACIDO ***");
            System.out.println("[ELISABETHA] Sus ojos se encontraron con los de Lance... Chispa: " + nivelChispa);
        } else if (!conoceALance) {
            // El servidor ya los marco como conocidos (Lance llego primero)
            nivelChispa = 75;
            conoceALance = true;
            System.out.println("[ELISABETHA] *** LA CHISPA ADECUADA HA NACIDO ***");
            System.out.println("[ELISABETHA] Encuentra a Lance en la taberna... Chispa: " + nivelChispa);
        } else {
            // Ya se conocian, sube 10
            nivelChispa += 10;
            System.out.println("[ELISABETHA] Coincide con Lance en la taberna. Chispa: " + nivelChispa);
        }
    }

    static void esperarLanceEnTaberna() {
        try {
            Socket skTaberna = new Socket(hostTaberna, puertoTaberna);
            DataOutputStream salida = new DataOutputStream(skTaberna.getOutputStream());
            DataInputStream entrada = new DataInputStream(skTaberna.getInputStream());

            salida.writeUTF("REGISTRAR_CHISPA_100");
            salida.writeUTF("ELISABETHA");
            entrada.readUTF(); // OK

            salida.writeUTF("ESPERAR_AL_OTRO");
            salida.writeUTF("ELISABETHA");
            String resultado = entrada.readUTF(); // FINAL_FELIZ

            if (resultado.equals("FINAL_FELIZ")) {
                simulacionTerminada = true;
                System.out.println("[ELISABETHA] *** FINAL FELIZ ***");
                System.out.println("[ELISABETHA] Elisabetha y Lance se reencuentran con chispa maxima!");
                System.out.println("[ELISABETHA] Fueron felices y comieron perdices!");
            }

            salida.writeUTF("DESCONECTAR");
            entrada.readUTF();
            skTaberna.close();
        } catch (IOException e) {
            System.out.println("[ELISABETHA] Error esperando a Lance: " + e.getMessage());
        }
    }

    static synchronized void procesarAlquimista() {
        if (mensajeAlquimista != null && mensajeAlquimista.equals("POCION")) {
            Random random = new Random();
            if (random.nextInt(100) < 30) {
                // 30% exito del alquimista
                if (!chispa100) {
                    nivelChispa = Math.max(0, nivelChispa - 20);
                    System.out.println("[ELISABETHA] El alquimista la engano con una pocion! Chispa: " + nivelChispa);
                }
            } else {
                System.out.println("[ELISABETHA] Elisabetha no se deja enganar por el alquimista.");
            }
        }
        hayPeticionAlquimista = false;
        mensajeAlquimista = null;
        Main.class.notifyAll();
    }

    // Clase interna para atender visitantes (Damas y Alquimistas)
    static class AtenderVisitante implements Runnable {
        private Socket skCliente;

        public AtenderVisitante(Socket skCliente) {
            this.skCliente = skCliente;
        }

        @Override
        public void run() {
            try {
                DataInputStream entrada = new DataInputStream(skCliente.getInputStream());
                DataOutputStream salida = new DataOutputStream(skCliente.getOutputStream());

                String tipo = entrada.readUTF();

                if (tipo.equals("DAMA")) {
                    String nombreDama = entrada.readUTF();
                    String mensaje = entrada.readUTF();

                    synchronized (Main.class) {
                        // La dama intenta contactar durante 20 segundos
                        long inicio = System.currentTimeMillis();
                        boolean atendida = false;

                        damaQuePide = nombreDama;
                        mensajeDama = mensaje;
                        hayPeticionDama = true;

                        while (System.currentTimeMillis() - inicio < 20000 && hayPeticionDama) {
                            try {
                                Main.class.wait(2000);
                            } catch (InterruptedException e) { }
                        }

                        atendida = !hayPeticionDama;
                        if (!atendida) {
                            // Timeout
                            hayPeticionDama = false;
                            damaQuePide = null;
                            mensajeDama = null;
                        }

                        salida.writeBoolean(atendida);
                    }
                } else if (tipo.equals("ALQUIMISTA")) {
                    String nombreAlq = entrada.readUTF();
                    String mensaje = entrada.readUTF();

                    synchronized (Main.class) {
                        // El alquimista sera atendido justo despues de la accion actual
                        hayPeticionAlquimista = true;
                        mensajeAlquimista = mensaje;
                        System.out.println("[ELISABETHA] El alquimista " + nombreAlq + " viene a visitarla...");

                        // Esperar a que Elisabetha lo procese
                        long inicio = System.currentTimeMillis();
                        while (hayPeticionAlquimista && System.currentTimeMillis() - inicio < 10000) {
                            try {
                                Main.class.wait(2000);
                            } catch (InterruptedException e) { }
                        }

                        salida.writeBoolean(!hayPeticionAlquimista);
                        salida.writeInt(nivelChispa);
                    }
                }

                skCliente.close();
            } catch (IOException e) {
                System.out.println("[ELISABETHA] Error atendiendo visitante: " + e.getMessage());
            }
        }
    }
}

