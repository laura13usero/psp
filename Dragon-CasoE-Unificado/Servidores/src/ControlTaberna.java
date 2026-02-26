/**
 * CONTROL TABERNA - CASO E: +dragonDerrotado en condicion de fin.
 * Identico al Caso C: while(!lance100 || !elisabetha100 || !dragonDerrotado) wait()
 */
public class ControlTaberna {
    private boolean elisabethaPresente = false, lancePresente = false;
    private boolean yaSeConocen = false;
    private boolean elisabetha100 = false, lance100 = false;
    private boolean dragonDerrotado = false; // NUEVO

    public synchronized void entrar(String p) {
        if (p.equals("ELISABETHA")) elisabethaPresente = true;
        else if (p.equals("LANCE")) lancePresente = true;
        notifyAll();
    }
    public synchronized void salir(String p) {
        if (p.equals("ELISABETHA")) elisabethaPresente = false;
        else if (p.equals("LANCE")) lancePresente = false;
        notifyAll();
    }
    public synchronized boolean estaElisabetha() { return elisabethaPresente; }
    public synchronized boolean estaLance() { return lancePresente; }
    public synchronized boolean yaSeConocen() { return yaSeConocen; }
    public synchronized void seConocen() { yaSeConocen = true; notifyAll(); }
    public synchronized void registrarChispa100(String p) {
        if (p.equals("ELISABETHA")) elisabetha100 = true;
        else if (p.equals("LANCE")) lance100 = true;
        notifyAll();
    }
    // NUEVO: Marcar dragon derrotado
    public synchronized void dragonDerrotado() {
        dragonDerrotado = true;
        System.out.println("[TABERNA] *** DRAGON DERROTADO ***");
        notifyAll();
    }
    public synchronized boolean isDragonDerrotado() { return dragonDerrotado; }

    // MODIFICADO: 3 condiciones para el final feliz
    public synchronized void esperarAlOtro(String p) {
        if (p.equals("ELISABETHA")) {
            while (!lance100 || !dragonDerrotado) { try { wait(); } catch (InterruptedException e) { } }
        } else if (p.equals("LANCE")) {
            while (!elisabetha100 || !dragonDerrotado) { try { wait(); } catch (InterruptedException e) { } }
        }
        notifyAll();
    }
}

