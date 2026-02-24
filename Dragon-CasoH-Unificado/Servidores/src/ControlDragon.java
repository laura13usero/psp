/**
 * CONTROL DRAGON - CASO H: Monitor con vida + fases (como Caso F).
 * Gestiona la vida del dragon que recibe ataques (como SERVIDOR)
 * mientras que otro hilo del mismo proceso ataca lugares (como CLIENTE).
 */
public class ControlDragon {
    private int vidaDragon = 300;

    /** Lance/Caballeros atacan al dragon (bajan vida). */
    public synchronized int recibirAtaque(int dano) {
        if (vidaDragon <= 0) return 0;
        int fase = getFase();
        vidaDragon -= dano;
        System.out.println("[DRAGON-SRV] Recibe " + dano + " dano. Vida: " + vidaDragon + " (Fase " + fase + ")");
        if (vidaDragon <= 0) {
            vidaDragon = 0;
            System.out.println("[DRAGON-SRV] *** EL DRAGON HA CAIDO! ***");
            notifyAll();
            return 0;
        }
        int contra = getDanoContraataque();
        System.out.println("[DRAGON-SRV] Contraataca con " + contra + " (Fase " + fase + ")");
        notifyAll();
        return contra;
    }

    /** Fase del dragon segun su vida (Escenario 11). */
    public synchronized int getFase() {
        if (vidaDragon > 200) return 1;      // Joven: contraataque debil
        else if (vidaDragon > 100) return 2;  // Enfurecido: contraataque medio
        else return 3;                        // Ancestral: contraataque fuerte
    }

    /** Dano del contraataque segun la fase. */
    public synchronized int getDanoContraataque() {
        switch (getFase()) {
            case 1: return 5;
            case 2: return 15;
            case 3: return 30;
            default: return 10;
        }
    }

    public synchronized int getVidaDragon() { return vidaDragon; }
    public synchronized boolean dragonVivo() { return vidaDragon > 0; }

    /** Esperar hasta que el dragon muera. */
    public synchronized void esperarMuerte() {
        while (vidaDragon > 0) {
            try { wait(); } catch (InterruptedException e) { }
        }
    }
}

