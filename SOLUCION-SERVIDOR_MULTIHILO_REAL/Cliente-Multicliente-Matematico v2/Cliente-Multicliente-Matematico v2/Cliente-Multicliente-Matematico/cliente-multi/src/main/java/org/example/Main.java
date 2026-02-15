package org.example;

import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        Scanner leer = new Scanner(System.in);

        System.out.println("=== CLIENTE MULTI-HILO PARA PRUEBAS ===");
        System.out.println("¿Cuántos clientes deseas crear? (recomendado: 2-5): ");
        int numClientes = leer.nextInt();

        if (numClientes > 10) {
            System.out.println("Limitando a 10 clientes para evitar sobrecarga");
            numClientes = 10;
        }

        String host = "localhost";
        int puerto = 5000;

        System.out.println("\nCreando " + numClientes + " clientes concurrentes...");
        System.out.println("Conectando a " + host + ":" + puerto);
        System.out.println("==========================================\n");

        Thread[] hilosClientes = new Thread[numClientes];

        // Crear y lanzar todos los hilos de clientes
        for (int i = 0; i < numClientes; i++) {
            ClienteHilo clienteHilo = new ClienteHilo(i + 1, host, puerto);
            hilosClientes[i] = new Thread(clienteHilo);
            hilosClientes[i].start();

            // Pequeña pausa para no saturar el servidor al conectar
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                System.out.println("Error en pausa: " + e.getMessage());
            }
        }

        // Esperar a que todos los clientes terminen
        System.out.println("\n[MAIN] Esperando a que todos los clientes terminen...\n");

        for (int i = 0; i < numClientes; i++) {
            try {
                hilosClientes[i].join();
                System.out.println("[MAIN] Cliente " + (i + 1) + " ha finalizado");
            } catch (InterruptedException e) {
                System.out.println("[MAIN] Error esperando al cliente " + (i + 1) + ": " + e.getMessage());
            }
        }

        System.out.println("\n==========================================");
        System.out.println("[MAIN] Todos los clientes han finalizado!");
        System.out.println("==========================================");

        leer.close();
    }
}