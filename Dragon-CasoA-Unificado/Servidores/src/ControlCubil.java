/**
 * CONTROL CUBIL - NUEVO: Monitor (objeto compartido synchronized) que gestiona
 * el secuestro de Elisabetha por el Dragon Carmesi y su rescate por Lance.
 *
 * PATRON: synchronized + wait() + notifyAll() (igual que ControlTaberna).
 *
 * FLUJO:
 * 1. El Dragon secuestra a Elisabetha -> serSecuestrada() pone flag a true
 * 2. Elisabetha se conecta al cubil -> esperarRescate() hace wait() y se BLOQUEA
 * 3. Lance se conecta al cubil -> rescatar() hace notifyAll() y DESBLOQUEA a Elisabetha
 * 4. Ambos luchan contra el dragon -> luchar() decide resultado (50/50)
 *    - Victoria: +50 chispa para ambos
 *    - Derrota parcial (malheridos): -20 chispa para ambos
 */
public class ControlCubil {
    private boolean princesaSecuestrada = false; // Elisabetha esta cautiva?
    private boolean lancePresente = false;       // Lance ha llegado al cubil?
    private boolean batallaResuelta = false;     // Ya se resolvio el duelo?
    private boolean victoria = false;            // Resultado del duelo

    /**
     * El Dragon ha secuestrado a Elisabetha.
     * Pone el flag a true para que cuando Elisabetha llegue quede bloqueada.
     */
    public synchronized void secuestrar() {
        princesaSecuestrada = true;
        lancePresente = false;
        batallaResuelta = false;
        victoria = false;
        System.out.println("[CUBIL] El Dragon Carmesi ha secuestrado a Elisabetha!");
        notifyAll(); // Avisar a quien consulte
    }

    /** Consultar si Elisabetha esta secuestrada */
    public synchronized boolean estaSecuestrada() {
        return princesaSecuestrada;
    }

    /**
     * Elisabetha espera en el cubil hasta que Lance la rescate.
     * PATRON CLAVE: while(!condicion) { wait(); } -> el hilo se duerme
     * hasta que rescatar() haga notifyAll().
     */
    public synchronized void esperarRescate() {
        System.out.println("[CUBIL] Elisabetha esta prisionera... esperando rescate...");
        // Elisabetha espera hasta que Lance llegue Y la batalla se resuelva
        while (!batallaResuelta) {
            try {
                wait(); // BLOQUEO: el hilo se duerme aqui
            } catch (InterruptedException e) { }
        }
        // Cuando despierta, la batalla ya se resolvio
        System.out.println("[CUBIL] Elisabetha ha sido liberada!");
    }

    /**
     * Lance llega al cubil para rescatar a Elisabetha.
     * Marca su presencia y realiza el duelo contra el dragon.
     * El resultado es aleatorio (50/50 como dice el enunciado).
     * Despues hace notifyAll() para despertar a Elisabetha.
     */
    public synchronized boolean rescatar() {
        if (!princesaSecuestrada) {
            System.out.println("[CUBIL] El cubil esta vacio. No hay nadie que rescatar.");
            return false; // No habia secuestro
        }

        lancePresente = true;
        System.out.println("[CUBIL] Lance irrumpe en el cubil del Dragon!");
        System.out.println("[CUBIL] *** AMBOS SE BATEN EN DUELO CONTRA LA BESTIA ***");

        // Simular el duelo (3 segundos de combate epico)
        try { Thread.sleep(3000); } catch (InterruptedException e) { }

        // Resultado 50/50 como dice el enunciado
        if (Math.random() < 0.50) {
            // VICTORIA: abaten al dragon, +50 chispa
            victoria = true;
            System.out.println("[CUBIL] *** VICTORIA! Abaten al dragon y regresan con su cabeza! ***");
            System.out.println("[CUBIL] Ambos ganan +50 puntos de chispa por la gloria.");
        } else {
            // DERROTA PARCIAL: victoriosos pero malheridos, -20 chispa
            victoria = false;
            System.out.println("[CUBIL] Emergen victoriosos pero MALHERIDOS...");
            System.out.println("[CUBIL] Ambos pierden -20 puntos de chispa por el esfuerzo.");
        }

        // Resolver la batalla y liberar a Elisabetha
        batallaResuelta = true;
        princesaSecuestrada = false; // Ya no esta cautiva
        notifyAll(); // DESPERTAR a Elisabetha que estaba en wait()
        return true;
    }

    /** Consultar si fue victoria (true) o derrota parcial (false) */
    public synchronized boolean fueVictoria() {
        return victoria;
    }

    /** Consultar si la batalla ya se resolvio */
    public synchronized boolean batallaResuelta() {
        return batallaResuelta;
    }
}

