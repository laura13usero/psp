/**
 * CONTROL TABERNA - Objeto compartido (monitor) que gestiona la sincronizacion
 * del encuentro entre Elisabetha y Lance en la taberna.
 *
 * CONCEPTO CLAVE: Todos los metodos son "synchronized", lo que significa que
 * solo un hilo puede ejecutarlos a la vez (exclusion mutua).
 * Usa wait() para bloquear un hilo hasta que se cumpla una condicion,
 * y notifyAll() para despertar a todos los hilos que estan esperando.
 *
 * Es el patron clasico de MONITOR visto en clase (como el Contador de Mis100Hilos).
 */
public class ControlTaberna {
    // Flags que indican si cada personaje esta FISICAMENTE en la taberna ahora mismo
    private boolean elisabethaPresente = false;
    private boolean lancePresente = false;
    // Flag que indica si ya se han conocido alguna vez (primera vez = chispa 75)
    private boolean yaSeConocen = false;
    // Flags que indican si cada uno ha llegado al nivel 100 de chispa (fase final)
    private boolean elisabetha100 = false;
    private boolean lance100 = false;

    // Registra que un personaje ENTRA en la taberna y avisa a los demas hilos
    public synchronized void entrar(String personaje) {
        if (personaje.equals("ELISABETHA")) {
            elisabethaPresente = true;
            System.out.println("[TABERNA] Elisabetha ha entrado en la taberna.");
        } else if (personaje.equals("LANCE")) {
            lancePresente = true;
            System.out.println("[TABERNA] Lance ha entrado en la taberna.");
        }
        notifyAll();
    }

    // Registra que un personaje SALE de la taberna
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

    // Consultas simples - synchronized para garantizar lectura consistente
    public synchronized boolean estaElisabetha() {
        return elisabethaPresente;
    }

    public synchronized boolean estaLance() {
        return lancePresente;
    }

    public synchronized boolean yaSeConocen() {
        return yaSeConocen;
    }

    // Se llama cuando coinciden por PRIMERA VEZ -> ambos suben a chispa 75
    public synchronized void seConocen() {
        yaSeConocen = true;
        System.out.println("[TABERNA] *** LA CHISPA ADECUADA HA NACIDO ***");
        System.out.println("[TABERNA] Elisabetha y Lance se han conocido!");
        notifyAll(); // Despertar a cualquier hilo que este esperando
    }

    // Registra que un personaje ha llegado a chispa 100 (fase final del cantar)
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

    public synchronized boolean elisabethaChispa100() {
        return elisabetha100;
    }

    public synchronized boolean lanceChispa100() {
        return lance100;
    }

    public synchronized boolean ambosChispa100() {
        return elisabetha100 && lance100;
    }

    /**
     * METODO CLAVE: Bloquea al personaje que llega primero a chispa 100
     * usando wait() hasta que el otro tambien llegue a 100.
     * Cuando ambos estan a 100, se desbloquea y la simulacion termina.
     *
     * PATRON: while(!condicion) { wait(); } -> espera activa controlada
     * El notifyAll() en registrarChispa100() despierta a este wait().
     */
    public synchronized void esperarAlOtro(String personaje) {
        if (personaje.equals("ELISABETHA")) {
            System.out.println("[TABERNA] Elisabetha espera en la ventana a que Lance llegue...");
            // BUCLE CON WAIT: el hilo se duerme hasta que lance100 sea true
            while (!lance100) {
                try {
                    wait(); // Se desbloquea cuando alguien haga notifyAll()
                } catch (InterruptedException e) { }
            }
            System.out.println("[TABERNA] *** ELISABETHA Y LANCE SE REENCUENTRAN CON CHISPA 100 ***");
        } else if (personaje.equals("LANCE")) {
            System.out.println("[TABERNA] Lance espera en el Porton Norte a que Elisabetha llegue...");
            while (!elisabetha100) {
                try {
                    wait();
                } catch (InterruptedException e) { }
            }
            System.out.println("[TABERNA] *** LANCE Y ELISABETHA SE REENCUENTRAN CON CHISPA 100 ***");
        }
        notifyAll(); // Despertar al otro por si tambien esta esperando
    }
}
