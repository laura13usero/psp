package org.example;

import java.io.*;
import java.net.Socket;
import java.util.Random;

public class ClienteHilo implements Runnable {
    private int clienteId;
    private String host;
    private int puerto;

    public ClienteHilo(int clienteId, String host, int puerto) {
        this.clienteId = clienteId;
        this.host = host;
        this.puerto = puerto;
    }

    @Override
    public void run() {
        Socket skCliente = null;
        Random random = new Random();

        System.out.println("[Cliente-" + clienteId + "] Intentando conectar...");

        try {
            skCliente = new Socket(host, puerto);
            System.out.println("[Cliente-" + clienteId + "] Conectado exitosamente!");

            OutputStream post = skCliente.getOutputStream();
            DataOutputStream flujo_salida = new DataOutputStream(post);
            InputStream get = skCliente.getInputStream();
            DataInputStream flujo_entrada = new DataInputStream(get);

            boolean encendido = true;
            int operacionesRealizadas = 0;
            int maxOperaciones = 3 + random.nextInt(3); // Entre 3 y 5 operaciones

            while (encendido && operacionesRealizadas < maxOperaciones) {
                flujo_salida.writeInt(100000);
                String lecturaString = flujo_entrada.readUTF();
                System.out.println("[Cliente-" + clienteId + "] " + lecturaString);

                Boolean run = true;
                while (run) {
                    lecturaString = flujo_entrada.readUTF();

                    if (lecturaString.equals("preguntar_al_usuario")) {
                        lecturaString = flujo_entrada.readUTF();
                        System.out.println("[Cliente-" + clienteId + "] " + lecturaString);

                        // Respuesta automática basada en la pregunta
                        int respuesta = generarRespuestaAutomatica(lecturaString, random);
                        System.out.println("[Cliente-" + clienteId + "] => " + respuesta);
                        flujo_salida.writeInt(respuesta);
                        lecturaString = "";
                    }

                    if (lecturaString.equals("mostrar_al_usuario")) {
                        lecturaString = flujo_entrada.readUTF();
                        System.out.println("[Cliente-" + clienteId + "] " + lecturaString);
                        System.out.println();
                        lecturaString = "";
                    }

                    if (lecturaString.equals("restear_interacion")) {
                        System.out.println("[Cliente-" + clienteId + "] Reset - Operación completada");
                        run = false;
                        lecturaString = "";
                        operacionesRealizadas++;

                        // Pequeña pausa entre operaciones
                        Thread.sleep(500);
                    }

                    if (lecturaString.equals("desconectar_usuario")) {
                        System.out.println("[Cliente-" + clienteId + "] Desconectado por el servidor");
                        run = false;
                        lecturaString = "";
                        encendido = false;
                    }

                    if (lecturaString.equals("preguntar_al_usuario_UTF")) {
                        lecturaString = flujo_entrada.readUTF();
                        System.out.println("[Cliente-" + clienteId + "] " + lecturaString);

                        String respuesta = generarRespuestaUTFAutomatica(lecturaString, random);
                        System.out.println("[Cliente-" + clienteId + "] => " + respuesta);
                        flujo_salida.writeUTF(respuesta);
                        lecturaString = "";
                    }
                }
            }

            // Después de hacer las operaciones, desconectarse
            if (encendido) {
                System.out.println("[Cliente-" + clienteId + "] Desconectándose voluntariamente...");
                flujo_salida.writeInt(100000);
                flujo_entrada.readUTF();
                flujo_entrada.readUTF();
                flujo_salida.writeInt(4); // Opción de desconectar
            }

            System.out.println("[Cliente-" + clienteId + "] Terminado - Total operaciones: " + operacionesRealizadas);

        } catch (IOException e) {
            System.out.println("[Cliente-" + clienteId + "] Error: " + e.getMessage());
        } catch (InterruptedException e) {
            System.out.println("[Cliente-" + clienteId + "] Interrumpido: " + e.getMessage());
        } finally {
            try {
                if (skCliente != null) {
                    skCliente.close();
                }
            } catch (IOException e) {
                System.out.println("[Cliente-" + clienteId + "] Error al cerrar: " + e.getMessage());
            }
        }
    }

    private int generarRespuestaAutomatica(String pregunta, Random random) {
        // Generar respuestas basadas en la pregunta
        if (pregunta.toLowerCase().contains("operación") || pregunta.toLowerCase().contains("opción")) {
            // Seleccionar operación (1-4)
            return 1 + random.nextInt(4);
        } else if (pregunta.toLowerCase().contains("numero") || pregunta.toLowerCase().contains("número")) {
            // Generar números aleatorios para las operaciones
            return 1 + random.nextInt(100);
        } else if (pregunta.toLowerCase().contains("serie")) {
            // Para longitud de serie
            return 5 + random.nextInt(10);
        }
        // Por defecto
        return 1 + random.nextInt(50);
    }

    private String generarRespuestaUTFAutomatica(String pregunta, Random random) {
        // Si pregunta por strings, generar respuestas apropiadas
        if (pregunta.toLowerCase().contains("continuar")) {
            return random.nextBoolean() ? "si" : "no";
        }
        return "test_" + random.nextInt(100);
    }
}
