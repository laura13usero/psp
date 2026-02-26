/**
 * CONTROL TABERNA - CASO C: MODIFICADO respecto al original.
 *
 * CAMBIO CLAVE: Nueva condicion de fin. Ademas de que ambos lleguen a 100,
 * tambien es necesario que el dragon haya sido derrotado.
 * Se anade: dragonDerrotado() y se modifica esperarAlOtro() para incluir
 * la condicion: while(!lance100 || !elisabetha100 || !dragonDerrotado) { wait(); }
 */
public class ControlTaberna {
    private boolean elisabethaPresente = false;
    private boolean lancePresente = false;
    private boolean yaSeConocen = false;
    private boolean elisabetha100 = false;
    private boolean lance100 = false;
    private boolean dragonDerrotado = false; // NUEVO: el dragon ha sido derrotado?

    public synchronized void entrar(String p) {
        if (p.equals("ELISABETHA")) { elisabethaPresente = true; }
        else if (p.equals("LANCE")) { lancePresente = true; }
        System.out.println("[TABERNA] " + p + " ha entrado.");
        notifyAll();
    }

    public synchronized void salir(String p) {
        if (p.equals("ELISABETHA")) { elisabethaPresente = false; }
        else if (p.equals("LANCE")) { lancePresente = false; }
        notifyAll();
    }

    public synchronized boolean estaElisabetha() { return elisabethaPresente; }
    public synchronized boolean estaLance() { return lancePresente; }
    public synchronized boolean yaSeConocen() { return yaSeConocen; }

    public synchronized void seConocen() {
        yaSeConocen = true;
        System.out.println("[TABERNA] *** LA CHISPA ADECUADA HA NACIDO ***");
        notifyAll();
    }

    public synchronized void registrarChispa100(String p) {
        if (p.equals("ELISABETHA")) { elisabetha100 = true; }
        else if (p.equals("LANCE")) { lance100 = true; }
        System.out.println("[TABERNA] " + p + " ha alcanzado chispa 100!");
        notifyAll();
    }

    /**
     * NUEVO: Marcar que el dragon ha sido derrotado.
     * Se llama cuando Lance mata al dragon en HiloLance.
     * Hace notifyAll() para despertar al hilo que espera en esperarAlOtro().
     */
    public synchronized void dragonDerrotado() {
        dragonDerrotado = true;
        System.out.println("[TABERNA] *** EL DRAGON CARMESI HA SIDO DERROTADO ***");
        notifyAll(); // Despertar a quien espere en esperarAlOtro
    }

    public synchronized boolean isDragonDerrotado() { return dragonDerrotado; }

    /**
     * MODIFICADO: Ahora espera a 3 condiciones (no 2):
     *   1. Lance a chispa 100
     *   2. Elisabetha a chispa 100
     *   3. Dragon derrotado
     *
     * while(!lance100 || !elisabetha100 || !dragonDerrotado) { wait(); }
     */
    public synchronized void esperarAlOtro(String p) {
        if (p.equals("ELISABETHA")) {
            System.out.println("[TABERNA] Elisabetha espera (Lance + dragon derrotado)...");
            // MODIFICADO: 3 condiciones
            while (!lance100 || !dragonDerrotado) {
                try { wait(); } catch (InterruptedException e) { }
            }
        } else if (p.equals("LANCE")) {
            System.out.println("[TABERNA] Lance espera (Elisabetha + dragon derrotado)...");
            while (!elisabetha100 || !dragonDerrotado) {
                try { wait(); } catch (InterruptedException e) { }
            }
        }
        System.out.println("[TABERNA] *** REENCUENTRO: Chispa 100 + Dragon derrotado! ***");
        notifyAll();
    }
}

