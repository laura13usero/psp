public class ControlTaberna {
    private boolean elisabethaPresente = false;
    private boolean lancePresente = false;
    private boolean yaSeConocen = false;
    private boolean elisabetha100 = false;
    private boolean lance100 = false;

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

    public synchronized boolean estaElisabetha() {
        return elisabethaPresente;
    }

    public synchronized boolean estaLance() {
        return lancePresente;
    }

    public synchronized boolean yaSeConocen() {
        return yaSeConocen;
    }

    public synchronized void seConocen() {
        yaSeConocen = true;
        System.out.println("[TABERNA] *** LA CHISPA ADECUADA HA NACIDO ***");
        System.out.println("[TABERNA] Elisabetha y Lance se han conocido!");
        notifyAll();
    }

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

    public synchronized void esperarAlOtro(String personaje) {
        if (personaje.equals("ELISABETHA")) {
            System.out.println("[TABERNA] Elisabetha espera en la ventana a que Lance llegue...");
            while (!lance100) {
                try {
                    wait();
                } catch (InterruptedException e) {
                    // seguir esperando
                }
            }
            System.out.println("[TABERNA] *** ELISABETHA Y LANCE SE REENCUENTRAN CON CHISPA 100 ***");
        } else if (personaje.equals("LANCE")) {
            System.out.println("[TABERNA] Lance espera en el Porton Norte a que Elisabetha llegue...");
            while (!elisabetha100) {
                try {
                    wait();
                } catch (InterruptedException e) {
                    // seguir esperando
                }
            }
            System.out.println("[TABERNA] *** LANCE Y ELISABETHA SE REENCUENTRAN CON CHISPA 100 ***");
        }
        notifyAll();
    }
}

