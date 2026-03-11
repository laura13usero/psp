import java.io.*;
import java.net.Socket;
import java.util.Random;

public class HiloDragon extends Thread {
    private Random random = new Random();

    @Override
    public void run() {
        System.out.println("[DRAGON] El Dragon de Ceniza Carmesi duerme en las montanas del Este...");

        while (!ClienteMaestro.simulacionTerminada) {
            // Dormir 120 segundos antes de comprobar si emerge
            System.out.println("[DRAGON] El Dragon duerme en su guarida. Proxima comprobacion en 120 segundos...");
            try { Thread.sleep(120000); } catch (InterruptedException e) { }

            if (ClienteMaestro.simulacionTerminada) break;

            // 25% de probabilidad de aparecer
            int prob = random.nextInt(100);
            if (prob < 25) {
                // El dragon aparece
                System.out.println("");
                System.out.println("============================================================");
                System.out.println("  *** EL DRAGON DE CENIZA CARMESI HA DESPERTADO ***");
                System.out.println("  ¡Un rugido ensordecedor recorre las tierras de Roedalia!");
                System.out.println("   ¡El cielo se torna color rojo y las montanas tiemblan");
                System.out.println("============================================================");
                System.out.println("");

                ClienteMaestro.dragonAtacando = true;

                // Elegir 2 lugares DISTINTOS para atacar
                // 0 = Mercado, 1 = Porton Norte, 2 = Taberna
                String[] nombresLugares = {"MERCADO", "PORTON", "TABERNA"};
                int[] puertos = {ClienteMaestro.PUERTO_MERCADO, ClienteMaestro.PUERTO_PORTON, ClienteMaestro.PUERTO_TABERNA};
                int[] probDestruccion = {80, 60, 30};

                int lugar1 = random.nextInt(3);
                int lugar2;
                do { lugar2 = random.nextInt(3); } while (lugar2 == lugar1);

                // Primer ataque
                System.out.println("[DRAGON] Dirige su furia hacia: " + nombresLugares[lugar1] + "!");

                // Comprobar si los Ratones Caballero detienen el ataque
                boolean detenido1 = false;
                if (!ClienteMaestro.ratonesDanados) {
                    int defensa = random.nextInt(100);
                    if (defensa < 20) {
                        detenido1 = true;
                        System.out.println("[RATONES CABALLERO] Han detenido el ataque al " + nombresLugares[lugar1] + "!");
                        System.out.println("[DRAGON] RUGE CON FURIA! Los ratones le han frenado!");
                    } else {
                        System.out.println("[RATONES CABALLERO] No logran detener al Dragon en " + nombresLugares[lugar1] + "!");
                        // 50% de que los ratones queden danados
                        if (random.nextInt(100) < 50) {
                            ClienteMaestro.ratonesDanados = true;
                            System.out.println("[RATONES CABALLERO] Han resultado DANADOS en el enfrentamiento!");
                            System.out.println("[RATONES CABALLERO] Necesitan 20 segundos para recuperarse...");
                            // Hilo de recuperacion
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    try { Thread.sleep(20000); } catch (InterruptedException e) { }
                                    ClienteMaestro.ratonesDanados = false;
                                    System.out.println("[RATONES CABALLERO] Se han recuperado! Vuelven al combate!");
                                }
                            }).start();
                        }
                    }
                } else {
                    System.out.println("[RATONES CABALLERO] Estan danados! No pueden intervenir!");
                }

                if (!detenido1) {
                    int tirada = random.nextInt(100);
                    if (tirada < probDestruccion[lugar1]) {
                        atacarLugar(puertos[lugar1], nombresLugares[lugar1]);
                        System.out.println("[DRAGON] " + nombresLugares[lugar1] + " ha sido REDUCIDO A CENIZAS!");
                    } else {
                        System.out.println("[DRAGON] " + nombresLugares[lugar1] + " resiste el embate del Dragon!");
                    }
                }

                // 6 segundos por intento de ataque
                try { Thread.sleep(6000); } catch (InterruptedException e) { }

                // Segundo ataque
                System.out.println("[DRAGON] Ahora dirige su furia hacia: " + nombresLugares[lugar2] + "!");

                boolean detenido2 = false;
                if (!ClienteMaestro.ratonesDanados) {
                    int defensa2 = random.nextInt(100);
                    if (defensa2 < 20) {
                        detenido2 = true;
                        System.out.println("[RATONES CABALLERO] Han detenido el ataque al " + nombresLugares[lugar2] + "!");
                        System.out.println("[DRAGON] RUGE CON FURIA! Los ratones le han frenado!");
                    } else {
                        System.out.println("[RATONES CABALLERO] No logran detener al Dragon en " + nombresLugares[lugar2] + "!");
                        // 50% de que los ratones queden danados
                        if (random.nextInt(100) < 50) {
                            ClienteMaestro.ratonesDanados = true;
                            System.out.println("[RATONES CABALLERO] Han resultado DANADOS en el enfrentamiento!");
                            System.out.println("[RATONES CABALLERO] Necesitan 20 segundos para recuperarse...");
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    try { Thread.sleep(20000); } catch (InterruptedException e) { }
                                    ClienteMaestro.ratonesDanados = false;
                                    System.out.println("[RATONES CABALLERO] Se han recuperado! Vuelven al combate!");
                                }
                            }).start();
                        }
                    }
                } else {
                    System.out.println("[RATONES CABALLERO] Estan danados! No pueden intervenir!");
                }

                if (!detenido2) {
                    int tirada2 = random.nextInt(100);
                    if (tirada2 < probDestruccion[lugar2]) {
                        atacarLugar(puertos[lugar2], nombresLugares[lugar2]);
                        System.out.println("[DRAGON] " + nombresLugares[lugar2] + " ha sido REDUCIDO A CENIZAS!");
                    } else {
                        System.out.println("[DRAGON] " + nombresLugares[lugar2] + " resiste el embate del Dragon!");
                    }
                }

                // 6 segundos por intento de ataque
                try { Thread.sleep(6000); } catch (InterruptedException e) { }

                ClienteMaestro.dragonAtacando = false;

                System.out.println("");
                System.out.println("============================================================");
                System.out.println("  El Dragon de Ceniza Carmesi se retira a su guarida...");
                System.out.println("  Las montanas del Este guardan silencio una vez mas.");
                System.out.println("============================================================");
                System.out.println("");

            } else {
                // No aparece, sigue dormido
                System.out.println("[DRAGON] El Dragon sigue dormido en las montanas...");
            }
        }
    }

    private void atacarLugar(int puerto, String nombreLugar) {
        try {
            Socket sk = new Socket(ClienteMaestro.HOST, puerto);
            DataOutputStream salida = new DataOutputStream(sk.getOutputStream());
            DataInputStream entrada = new DataInputStream(sk.getInputStream());

            salida.writeUTF("ATAQUE_DRAGON");
            String respuesta = entrada.readUTF();
            System.out.println("[DRAGON] Ataca " + nombreLugar + "! Respuesta: " + respuesta);

            salida.writeUTF("DESCONECTAR");
            entrada.readUTF();
            sk.close();
        } catch (IOException e) {
            System.out.println("[DRAGON] No pudo atacar " + nombreLugar + ": " + e.getMessage());
        }
    }
}
