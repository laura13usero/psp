/**
 * CONTROL DRAGON - Monitor (synchronized) que gestiona la vida del dragon
 * y la coordinacion del combate conjunto de Elisabetha y Lance.
 *
 * PATRON BARRERA: Ambos deben estar presentes para atacar juntos.
 * Usa wait() para bloquear al primero que llega hasta que el segundo llegue.
 * Usa notifyAll() para despertar a ambos cuando la batalla empieza.
 *
 * El resultado del combate es incierto (50/50):
 *   - Victoria: +50 chispa para ambos
 *   - Derrota parcial (malheridos): -20 chispa para ambos
 */
public class ControlDragon {
    private int vidaDragon = 200;         // Puntos de vida del dragon
    private boolean elisabethaLista = false; // Elisabetha esta lista para pelear?
    private boolean lanceListo = false;      // Lance esta listo para pelear?
    private boolean batallaEnCurso = false;  // Hay una batalla activa?
    private boolean batallaResuelta = false; // Ya se resolvio?
    private boolean victoria = false;        // Resultado: true=victoria, false=derrota

    /**
     * Un guerrero llega al campo de batalla.
     * Si es el primero, espera con wait() al otro.
     * Si es el segundo, despierta al primero con notifyAll() y empieza la batalla.
     * PATRON BARRERA: while(!ambosListos) { wait(); }
     */
    public synchronized void llegarABatalla(String personaje) {
        // Marcar que el personaje ha llegado
        if (personaje.equals("ELISABETHA")) {
            elisabethaLista = true;
            System.out.println("[DRAGON-SRV] Elisabetha llega al campo de batalla!");
        } else if (personaje.equals("LANCE")) {
            lanceListo = true;
            System.out.println("[DRAGON-SRV] Lance llega al campo de batalla!");
        }

        // Si ambos estan listos, iniciar la batalla
        if (elisabethaLista && lanceListo && !batallaEnCurso) {
            batallaEnCurso = true;
            notifyAll(); // DESPERTAR al que estaba esperando
        }

        // Esperar a que el otro llegue (BARRERA)
        while (!batallaEnCurso) {
            try {
                wait(); // El primero en llegar se BLOQUEA aqui
            } catch (InterruptedException e) { }
        }

        // Si la batalla no se ha resuelto, resolverla (solo un hilo lo hace)
        if (!batallaResuelta) {
            resolverBatalla();
        }
    }

    /**
     * Resuelve la batalla. Se ejecuta una sola vez (el primer hilo en llegar aqui).
     * 50/50: victoria o derrota parcial.
     */
    private void resolverBatalla() {
        batallaResuelta = true;
        System.out.println("[DRAGON-SRV] *** AMBOS SE BATEN EN DUELO CONTRA LA BESTIA ***");

        try { Thread.sleep(3000); } catch (InterruptedException e) { }

        if (Math.random() < 0.50) {
            victoria = true;
            vidaDragon = 0;
            System.out.println("[DRAGON-SRV] *** VICTORIA! El dragon cae derrotado! ***");
            System.out.println("[DRAGON-SRV] +50 chispa para ambos por la gloria.");
        } else {
            victoria = false;
            vidaDragon = Math.max(0, vidaDragon - 50); // Le quitan vida pero no lo matan
            System.out.println("[DRAGON-SRV] Victoriosos pero MALHERIDOS...");
            System.out.println("[DRAGON-SRV] -20 chispa para ambos por el esfuerzo.");
        }

        // Resetear para futuras batallas
        elisabethaLista = false;
        lanceListo = false;
        batallaEnCurso = false;

        notifyAll(); // Despertar a todos para que lean el resultado
    }

    /** Esperar a que la batalla termine (para el segundo hilo) */
    public synchronized void esperarResultado() {
        while (!batallaResuelta) {
            try { wait(); } catch (InterruptedException e) { }
        }
    }

    public synchronized boolean fueVictoria() { return victoria; }
    public synchronized int getVidaDragon() { return vidaDragon; }
    public synchronized boolean dragonVivo() { return vidaDragon > 0; }

    /** Resetear para una nueva batalla */
    public synchronized void resetear() {
        batallaResuelta = false;
        victoria = false;
    }
}

