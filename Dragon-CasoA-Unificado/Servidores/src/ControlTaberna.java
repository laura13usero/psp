/**
 * CONTROL TABERNA - Monitor (objeto compartido synchronized) que gestiona
 * la sincronizacion del encuentro entre Elisabetha y Lance en la taberna.
 * 
 * IDENTICO al proyecto original. Todos los metodos son synchronized para
 * exclusion mutua. Usa wait()/notifyAll() para bloquear/despertar hilos.
 */
public class ControlTaberna {
    private boolean elisabethaPresente = false;  // Esta Elisabetha en la taberna ahora?
    private boolean lancePresente = false;       // Esta Lance en la taberna ahora?
    private boolean yaSeConocen = false;         // Ya se encontraron alguna vez?
    private boolean elisabetha100 = false;       // Elisabetha llego a chispa 100?
    private boolean lance100 = false;            // Lance llego a chispa 100?

    // Registra entrada de un personaje y avisa a los demas con notifyAll
    public synchronized void entrar(String personaje) {
        if (personaje.equals("ELISABETHA")) {
            elisabethaPresente = true;
            System.out.println("[TABERNA] Elisabetha ha entrado en la taberna.");
        } else if (personaje.equals("LANCE")) {
            lancePresente = true;
            System.out.println("[TABERNA] Lance ha entrado en la taberna.");
        }
        notifyAll(); // Despertar hilos que puedan estar consultando
    }

    // Registra salida de un personaje
    public synchronized void salir(String personaje) {
        if (personaje.equals("ELISABETHA")) {
            elisabethaPresente = false;
            System.out.println("[TABERNA] Elisabetha ha salido de la taberna.");
        } else if (personaje.equals("LANCE")) {
            lancePresente = false;
            System.out.println("[TABERNA] Lance ha salido de la taberna.");
        }
        notifyAll();
    }

    public synchronized boolean estaElisabetha() { return elisabethaPresente; }
    public synchronized boolean estaLance() { return lancePresente; }
    public synchronized boolean yaSeConocen() { return yaSeConocen; }

    // Primera vez que coinciden -> chispa sube a 75 en ambos
    public synchronized void seConocen() {
        yaSeConocen = true;
        System.out.println("[TABERNA] *** LA CHISPA ADECUADA HA NACIDO ***");
        notifyAll();
    }

    // Registra que un personaje llego a chispa 100 (fase final)
    public synchronized void registrarChispa100(String personaje) {
        if (personaje.equals("ELISABETHA")) {
            elisabetha100 = true;
            System.out.println("[TABERNA] Elisabetha ha alcanzado chispa 100!");
        } else if (personaje.equals("LANCE")) {
            lance100 = true;
            System.out.println("[TABERNA] Lance ha alcanzado chispa 100!");
        }
        notifyAll();
    }

    public synchronized boolean elisabethaChispa100() { return elisabetha100; }
    public synchronized boolean lanceChispa100() { return lance100; }
    public synchronized boolean ambosChispa100() { return elisabetha100 && lance100; }

    /**
     * BLOQUEA al personaje que llega primero a chispa 100 con wait()
     * hasta que el otro tambien llegue. Patron: while(!cond) { wait(); }
     */
    public synchronized void esperarAlOtro(String personaje) {
        if (personaje.equals("ELISABETHA")) {
            System.out.println("[TABERNA] Elisabetha espera en la ventana a Lance...");
            while (!lance100) {
                try { wait(); } catch (InterruptedException e) { }
            }
            System.out.println("[TABERNA] *** ELISABETHA Y LANCE SE REENCUENTRAN ***");
        } else if (personaje.equals("LANCE")) {
            System.out.println("[TABERNA] Lance espera en el Porton a Elisabetha...");
            while (!elisabetha100) {
                try { wait(); } catch (InterruptedException e) { }
            }
            System.out.println("[TABERNA] *** LANCE Y ELISABETHA SE REENCUENTRAN ***");
        }
        notifyAll(); // Despertar al otro si tambien esta esperando
    }
}

