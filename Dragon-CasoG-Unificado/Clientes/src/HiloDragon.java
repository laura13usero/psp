import java.util.Random;

/**
 * NUEVO: HILO DRAGON - CASO G: Dragon Local Synchronized.
 *
 * PATRON CLAVE: El dragon NO usa sockets. Accede DIRECTAMENTE a los locks
 * de los personajes (lockElisabetha, lockLance) con synchronized.
 *
 * EXCLUSION MUTUA COMPETITIVA:
 *   Cuando el dragon hace synchronized(lockLance), NADIE MAS puede usar ese
 *   lock: ni los caballeros, ni los alquimistas. El buzon de Lance queda
 *   BLOQUEADO durante el ataque. Esto es exclusion mutua REAL por monitor,
 *   no por flag volatile.
 *
 * COMPORTAMIENTO:
 *   1. Duerme 15-30 segundos (mora en paz)
 *   2. Elige a quien atacar: Elisabetha (40%) o Lance (60%)
 *   3. Hace synchronized(lockPersonaje) -> ataque de 8 segundos
 *      Durante esos 8 seg, nadie mas puede escribir en el buzon del personaje
 *   4. 40% chance de que el personaje se defienda (+15 chispa)
 *      60% chance de que el dragon lo queme (-30 chispa)
 *   5. Marca dragonAtacando = true para que las Damas evacuen
 *
 * COMO LO DERROTA LANCE:
 *   Lance comprueba dragonAtacando en cada iteracion. Si el dragon esta
 *   atacando y Lance tiene valor >= 30, puede intentar matarlo.
 *   Para matar al dragon, Lance hace synchronized(lockDragon) y lucha.
 *   50/50: si gana, dragonDerrotado = true. Si pierde, -20 chispa.
 */
public class HiloDragon extends Thread {
    private Random random = new Random();
    private int vidaDragon = 150; // Puntos de vida del dragon

    @Override
    public void run() {
        System.out.println("[DRAGON] El Dragon Carmesi mora en paz en las montanas...");

        // Esperar un poco antes de atacar por primera vez
        try { Thread.sleep(20000); } catch (InterruptedException e) { }

        while (!ClienteMaestro.simulacionTerminada && !ClienteMaestro.dragonDerrotado) {
            // El dragon elige a quien atacar
            if (random.nextInt(100) < 40) {
                atacarElisabetha();
            } else {
                atacarLance();
            }

            if (ClienteMaestro.dragonDerrotado) break;

            // Dormir antes de volver a atacar (15-30 seg)
            int sueno = 15000 + random.nextInt(16000);
            System.out.println("[DRAGON] Se retira " + (sueno/1000) + " segundos...");
            try { Thread.sleep(sueno); } catch (InterruptedException e) { }
        }

        if (ClienteMaestro.dragonDerrotado) {
            System.out.println("[DRAGON] El Dragon Carmesi ha sido derrotado!");
        } else {
            System.out.println("[DRAGON] El Dragon Carmesi huye de Roedalia.");
        }
    }

    /**
     * ATAQUE A ELISABETHA - PATRON: synchronized(lockElisabetha)
     *
     * Al hacer synchronized(lockElisabetha), el dragon BLOQUEA el buzon:
     * - Las Damas que intentan hablar con Elisabetha quedan ESPERANDO
     * - Los Alquimistas que intentan darle pociones quedan ESPERANDO
     * - Elisabetha misma, si intenta atender a damas, queda ESPERANDO
     *
     * Esto demuestra EXCLUSION MUTUA REAL: el dragon "secuestra" el lock
     * durante 8 segundos, impidiendo toda interaccion con Elisabetha.
     */
    private void atacarElisabetha() {
        System.out.println("[DRAGON] *** EL DRAGON ATACA A ELISABETHA! ***");
        ClienteMaestro.dragonAtacando = true; // Flag para que Damas evacuen

        // synchronized: BLOQUEA el buzon de Elisabetha durante el ataque
        synchronized (ClienteMaestro.lockElisabetha) {
            System.out.println("[DRAGON] Tiene a Elisabetha acorralada! (lock adquirido)");
            System.out.println("[DRAGON] Nadie puede interactuar con ella durante 8 seg...");

            try { Thread.sleep(8000); } catch (InterruptedException e) { }

            // 40% Elisabetha se defiende, 60% la quema
            if (random.nextInt(100) < 40) {
                System.out.println("[DRAGON] Elisabetha esquiva el fuego! +15 chispa.");
                ClienteMaestro.chispaElisabetha = Math.min(100,
                    ClienteMaestro.chispaElisabetha + 15);
            } else {
                System.out.println("[DRAGON] FUEGO! Elisabetha sufre quemaduras! -30 chispa.");
                ClienteMaestro.chispaElisabetha = Math.max(0,
                    ClienteMaestro.chispaElisabetha - 30);
            }

            // notifyAll para despertar a quienes estaban esperando el lock
            ClienteMaestro.lockElisabetha.notifyAll();
        }
        // Al salir del synchronized, el lock se libera y los demas hilos
        // pueden volver a acceder al buzon de Elisabetha

        ClienteMaestro.dragonAtacando = false;
        System.out.println("[DRAGON] Se aleja de Elisabetha. Chispa E: " + ClienteMaestro.chispaElisabetha);
    }

    /**
     * ATAQUE A LANCE - PATRON: synchronized(lockLance)
     * Mismo patron que atacarElisabetha pero con el lock de Lance.
     * Mientras el dragon tiene lockLance, los caballeros NO pueden
     * hablar con Lance ni hacerle peticiones.
     */
    private void atacarLance() {
        System.out.println("[DRAGON] *** EL DRAGON ATACA A LANCE! ***");
        ClienteMaestro.dragonAtacando = true;

        synchronized (ClienteMaestro.lockLance) {
            System.out.println("[DRAGON] Tiene a Lance acorralado! (lock adquirido)");
            System.out.println("[DRAGON] Nadie puede interactuar con el durante 8 seg...");

            try { Thread.sleep(8000); } catch (InterruptedException e) { }

            // 40% Lance se defiende, 60% lo quema
            if (random.nextInt(100) < 40) {
                System.out.println("[DRAGON] Lance alza su escudo! +15 chispa.");
                ClienteMaestro.chispaLance = Math.min(100,
                    ClienteMaestro.chispaLance + 15);
            } else {
                System.out.println("[DRAGON] FUEGO! Lance sufre quemaduras! -30 chispa.");
                ClienteMaestro.chispaLance = Math.max(0,
                    ClienteMaestro.chispaLance - 30);
            }

            ClienteMaestro.lockLance.notifyAll();
        }

        ClienteMaestro.dragonAtacando = false;
        System.out.println("[DRAGON] Se aleja de Lance. Chispa L: " + ClienteMaestro.chispaLance);
    }
}

