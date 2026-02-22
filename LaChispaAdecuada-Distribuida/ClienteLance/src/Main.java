import java.io.*;
import java.net.*;
import java.util.Random;
import java.util.Scanner;

public class Main {
    // Configuracion de conexion
    static String hostTaberna = "localhost";
    static int puertoTaberna = 5000;
    static String hostPorton = "localhost";
    static int puertoPorton = 5002;
    static int puertoEscucha = 6001; // Puerto donde Lance escucha a Caballeros y Alquimistas

    // Estado de Lance
    static int nivelChispa = 0;
    static boolean conoceAElisabetha = false;
    static boolean chispa100 = false;
    static boolean simulacionTerminada = false;

    // Cola de peticiones de caballeros
    static String caballeroQuePide = null;
    static String mensajeCaballero = null;
    static boolean hayPeticionCaballero = false;

    // Peticion de alquimista
    static boolean hayPeticionAlquimista = false;
    static String mensajeAlquimista = null;
    static String tipoAccionAlquimista = null;

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        System.out.println("==============================================");
        System.out.println("  LANCE DU LAC - CABALLERO DE ROEDALIA");
        System.out.println("==============================================");

        System.out.print("IP de la Taberna (localhost): ");
        String input = sc.nextLine().trim();
        if (!input.isEmpty()) hostTaberna = input;

        System.out.print("Puerto de la Taberna (5000): ");
        input = sc.nextLine().trim();
        if (!input.isEmpty()) puertoTaberna = Integer.parseInt(input);

        System.out.print("IP del Porton Norte (localhost): ");
        input = sc.nextLine().trim();
        if (!input.isEmpty()) hostPorton = input;

        System.out.print("Puerto del Porton Norte (5002): ");
        input = sc.nextLine().trim();
        if (!input.isEmpty()) puertoPorton = Integer.parseInt(input);

        System.out.print("Puerto donde Lance escucha a Caballeros/Alquimistas (6001): ");
        input = sc.nextLine().trim();
        if (!input.isEmpty()) puertoEscucha = Integer.parseInt(input);

        // Hilo que escucha conexiones de Caballeros y Alquimistas
        Thread hiloEscucha = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    ServerSocket skServidor = new ServerSocket(puertoEscucha);
                    System.out.println("[LANCE] Escuchando en puerto " + puertoEscucha + " para Caballeros y Alquimistas");
                    while (!simulacionTerminada) {
                        Socket skCliente = skServidor.accept();
                        Thread hiloAtencion = new Thread(new AtenderVisitante(skCliente));
                        hiloAtencion.start();
                    }
                } catch (IOException e) {
                    System.out.println("[LANCE] Error en escucha: " + e.getMessage());
                }
            }
        });
        hiloEscucha.setDaemon(true);
        hiloEscucha.start();

        // Bucle principal de acciones de Lance
        Random random = new Random();
        System.out.println("[LANCE] Comienza la simulacion. Nivel de chispa: " + nivelChispa);

        while (!simulacionTerminada) {
            // Comprobar peticion de alquimista (prioridad alta)
            synchronized (Main.class) {
                if (hayPeticionAlquimista) {
                    procesarAlquimista();
                }
            }

            if (chispa100) {
                System.out.println("[LANCE] Chispa a 100! Se pone en el Porton Norte a esperar a Elisabetha...");
                esperarElisabethaEnTaberna();
                break;
            }

            // Elegir accion aleatoria (3 opciones con la misma probabilidad)
            int accion = random.nextInt(3);

            switch (accion) {
                case 0:
                    hablarConCompaneros(random);
                    break;
                case 1:
                    desafiarDuelo(random);
                    break;
                case 2:
                    realizarGuardia(random);
                    break;
            }

            // Comprobar chispa 100
            if (nivelChispa >= 100 && conoceAElisabetha) {
                nivelChispa = 100;
                chispa100 = true;
            }
        }

        System.out.println("[LANCE] *** SIMULACION TERMINADA ***");
        System.out.println("[LANCE] Elisabetha y Lance son felices y comen perdices!");
    }

    static void hablarConCompaneros(Random random) {
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

            synchronized (Main.class) {
                if (hayPeticionCaballero && caballeroQuePide != null && caballeroQuePide.equals(nombreCab)) {
                    System.out.println("[LANCE] Habla con " + nombreCab);
                    String mensaje = mensajeCaballero;
                    hayPeticionCaballero = false;
                    caballeroQuePide = null;
                    mensajeCaballero = null;
                    Main.class.notifyAll();

                    if (mensaje != null && mensaje.equals("OFENSA_ELISABETHA")) {
                        System.out.println("[LANCE] " + nombreCab + " ha ofendido a Elisabetha! Lance lo reta a duelo!");
                        realizarDueloConCompanero(nombreCab, random);
                    } else {
                        System.out.println("[LANCE] " + nombreCab + " le cuenta una confidencia personal. Sin efecto.");
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

    static void desafiarDuelo(Random random) {
        // Lance solo desafia si ha recibido una ofensa, si no simplemente patrulla
        synchronized (Main.class) {
            if (hayPeticionCaballero && mensajeCaballero != null && mensajeCaballero.equals("OFENSA_ELISABETHA")) {
                String nombreCab = caballeroQuePide;
                hayPeticionCaballero = false;
                caballeroQuePide = null;
                mensajeCaballero = null;
                Main.class.notifyAll();
                realizarDueloConCompanero(nombreCab, random);
                return;
            }
        }
        System.out.println("[LANCE] No hay ofensas pendientes. Entrena con su espada.");
        try { Thread.sleep(3000); } catch (InterruptedException e) { }
    }

    static void realizarDueloConCompanero(String nombreCab, Random random) {
        System.out.println("[LANCE] Duelo con " + nombreCab + "!");
        try { Thread.sleep(5000); } catch (InterruptedException e) { }

        // 100% victoria, 20% danar gravemente al oponente
        if (random.nextInt(100) < 20) {
            // Dano grave
            if (!chispa100) {
                nivelChispa = Math.max(0, nivelChispa - 5);
            }
            System.out.println("[LANCE] Vence a " + nombreCab + " pero lo dana gravemente. Chispa: " + nivelChispa);
            System.out.println("[LANCE] " + nombreCab + " necesitara 30 segundos para recuperarse.");
        } else {
            // Victoria limpia
            if (conoceAElisabetha) {
                nivelChispa += 7;
            } else {
                nivelChispa = Math.min(50, nivelChispa + 7);
            }
            System.out.println("[LANCE] Vence a " + nombreCab + " sin hacerle dano. Chispa: " + nivelChispa);
        }
    }

    static void realizarGuardia(Random random) {
        System.out.println("[LANCE] Decide realizar un turno de guardia...");

        int lugar = random.nextInt(2);

        if (lugar == 0) {
            vigilarPortonNorte();
        } else {
            vigilarTaberna();
        }
    }

    static void vigilarPortonNorte() {
        System.out.println("[LANCE] Se dirige al Porton Norte a vigilar...");
        try {
            Socket skPorton = new Socket(hostPorton, puertoPorton);
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

    static void vigilarTaberna() {
        System.out.println("[LANCE] Se dirige a vigilar la Taberna 'El Descanso del Guerrero'...");
        try {
            Socket skTaberna = new Socket(hostTaberna, puertoTaberna);
            DataOutputStream salida = new DataOutputStream(skTaberna.getOutputStream());
            DataInputStream entrada = new DataInputStream(skTaberna.getInputStream());

            // Anunciar llegada
            salida.writeUTF("ENTRAR");
            salida.writeUTF("LANCE");
            entrada.readUTF(); // OK

            // Comprobar si Elisabetha esta
            salida.writeUTF("CONSULTAR_ELISABETHA");
            boolean elisabethaEsta = entrada.readBoolean();

            long tiempoEntrada = System.currentTimeMillis();

            if (elisabethaEsta) {
                encuentroConElisabetha(salida, entrada);
            } else {
                // Esperar 8 segundos (vigilancia de Lance)
                System.out.println("[LANCE] Elisabetha no esta en la taberna... vigilando 8 segundos...");
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
                    System.out.println("[LANCE] Elisabetha no aparecio. Vuelve al Porton con chispa: " + nivelChispa);
                }
            }

            // Anunciar salida
            salida.writeUTF("SALIR_TABERNA");
            salida.writeUTF("LANCE");
            entrada.readUTF(); // OK

            salida.writeUTF("DESCONECTAR");
            entrada.readUTF();
            skTaberna.close();
        } catch (IOException e) {
            System.out.println("[LANCE] No pudo llegar a la taberna: " + e.getMessage());
        }
    }

    static void encuentroConElisabetha(DataOutputStream salida, DataInputStream entrada) throws IOException {
        salida.writeUTF("YA_SE_CONOCEN");
        boolean yaSeConocian = entrada.readBoolean();

        if (!yaSeConocian) {
            salida.writeUTF("SE_CONOCEN");
            entrada.readUTF(); // CHISPA_GENERADA
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

    static void esperarElisabethaEnTaberna() {
        try {
            Socket skTaberna = new Socket(hostTaberna, puertoTaberna);
            DataOutputStream salida = new DataOutputStream(skTaberna.getOutputStream());
            DataInputStream entrada = new DataInputStream(skTaberna.getInputStream());

            salida.writeUTF("REGISTRAR_CHISPA_100");
            salida.writeUTF("LANCE");
            entrada.readUTF(); // OK

            salida.writeUTF("ESPERAR_AL_OTRO");
            salida.writeUTF("LANCE");
            String resultado = entrada.readUTF(); // FINAL_FELIZ

            if (resultado.equals("FINAL_FELIZ")) {
                simulacionTerminada = true;
                System.out.println("[LANCE] *** FINAL FELIZ ***");
                System.out.println("[LANCE] Lance y Elisabetha se reencuentran con chispa maxima!");
                System.out.println("[LANCE] Fueron felices y comieron perdices!");
            }

            salida.writeUTF("DESCONECTAR");
            entrada.readUTF();
            skTaberna.close();
        } catch (IOException e) {
            System.out.println("[LANCE] Error esperando a Elisabetha: " + e.getMessage());
        }
    }

    static synchronized void procesarAlquimista() {
        Random random = new Random();
        if (tipoAccionAlquimista != null && tipoAccionAlquimista.equals("POCION")) {
            if (random.nextInt(100) < 20) {
                if (!chispa100) {
                    nivelChispa = Math.max(0, nivelChispa - 20);
                    System.out.println("[LANCE] El alquimista lo engano con una pocion! Chispa: " + nivelChispa);
                }
            } else {
                System.out.println("[LANCE] Lance no se deja enganar por el alquimista.");
            }
        } else if (tipoAccionAlquimista != null && tipoAccionAlquimista.equals("AMENAZA")) {
            if (random.nextInt(100) < 20) {
                if (!chispa100) {
                    nivelChispa = Math.max(0, nivelChispa - 30);
                    System.out.println("[LANCE] La amenaza del Frente Norte afecta a Lance! Chispa: " + nivelChispa);
                }
            } else {
                System.out.println("[LANCE] Lance no se deja amedrentar por el alquimista.");
            }
        }
        hayPeticionAlquimista = false;
        mensajeAlquimista = null;
        tipoAccionAlquimista = null;
        Main.class.notifyAll();
    }

    // Clase interna para atender visitantes (Caballeros y Alquimistas)
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

                if (tipo.equals("CABALLERO")) {
                    String nombreCab = entrada.readUTF();
                    String mensaje = entrada.readUTF();

                    synchronized (Main.class) {
                        long inicio = System.currentTimeMillis();
                        boolean atendido = false;

                        caballeroQuePide = nombreCab;
                        mensajeCaballero = mensaje;
                        hayPeticionCaballero = true;

                        while (System.currentTimeMillis() - inicio < 25000 && hayPeticionCaballero) {
                            try {
                                Main.class.wait(2000);
                            } catch (InterruptedException e) { }
                        }

                        atendido = !hayPeticionCaballero;
                        if (!atendido) {
                            hayPeticionCaballero = false;
                            caballeroQuePide = null;
                            mensajeCaballero = null;
                        }

                        salida.writeBoolean(atendido);
                        // Si fue ofensa y hubo duelo, enviar si hubo dano
                        if (atendido && mensaje.equals("OFENSA_ELISABETHA")) {
                            salida.writeBoolean(true); // hubo duelo
                        }
                    }
                } else if (tipo.equals("ALQUIMISTA")) {
                    String nombreAlq = entrada.readUTF();
                    String accion = entrada.readUTF(); // POCION o AMENAZA

                    synchronized (Main.class) {
                        hayPeticionAlquimista = true;
                        tipoAccionAlquimista = accion;
                        mensajeAlquimista = accion;
                        System.out.println("[LANCE] El alquimista " + nombreAlq + " viene a visitarlo...");

                        long inicio = System.currentTimeMillis();
                        while (hayPeticionAlquimista && System.currentTimeMillis() - inicio < 15000) {
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
                System.out.println("[LANCE] Error atendiendo visitante: " + e.getMessage());
            }
        }
    }
}

