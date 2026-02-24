/**
 * CONTROL DRAGON - CASO F: Productor-Consumidor Antagonico.
 *
 * PATRON NUEVO: Un mismo recurso compartido (vidaDragon) es modificado
 * por DOS TIPOS de hilos con objetivos OPUESTOS:
 *   - Lance/Caballeros: recibirAtaque() -> bajan vida
 *   - Alquimistas: curar() -> suben vida
 *
 * Tambien implementa FASES (Escenario 11 de la guia):
 *   Fase 1 (vida > 200): dragon joven, contraataque 5 chispa
 *   Fase 2 (vida 100-200): dragon enfurecido, contraataque 15 chispa
 *   Fase 3 (vida < 100): dragon ancestral, contraataque 30 chispa
 *
 * Todos los metodos son synchronized: mientras un alquimista cura,
 * Lance no puede atacar (y viceversa). Exclusion mutua sobre el recurso.
 */
public class ControlDragon {
    private int vidaDragon = 300; // Vida total del dragon (3 fases de 100)

    /**
     * Un atacante (Lance o Caballero) dana al dragon.
     * El dano depende del atacante (10-30 aleatorio).
     * Devuelve el dano del CONTRAATAQUE del dragon (depende de la fase).
     *
     * @param dano cantidad de dano que inflige el atacante
     * @return dano del contraataque del dragon al atacante (5, 15 o 30)
     */
    public synchronized int recibirAtaque(int dano) {
        if (vidaDragon <= 0) return 0; // Dragon ya muerto

        int fase = getFase();
        vidaDragon -= dano;
        System.out.println("[DRAGON-SRV] Recibe " + dano + " de dano. Vida: " + vidaDragon + " (Fase " + fase + ")");

        if (vidaDragon <= 0) {
            vidaDragon = 0;
            System.out.println("[DRAGON-SRV] *** EL DRAGON HA CAIDO! ***");
            notifyAll(); // Despertar a quien espere en esperarVictoria()
            return 0;    // No hay contraataque si muere
        }

        // Contraataque: dano depende de la fase
        int contraataque = getDanoContraataque();
        System.out.println("[DRAGON-SRV] Contraataca con " + contraataque + " de dano (Fase " + fase + ")");
        notifyAll();
        return contraataque;
    }

    /**
     * Un alquimista cura al dragon (le sube vida).
     * PATRON ANTAGONICO: el alquimista AYUDA al dragon, lo opuesto a Lance.
     *
     * @param curacion cantidad de vida que recupera el dragon
     */
    public synchronized void curar(int curacion) {
        if (vidaDragon <= 0) {
            System.out.println("[DRAGON-SRV] El dragon ya esta muerto. No se puede curar.");
            return;
        }
        vidaDragon += curacion;
        int fase = getFase();
        System.out.println("[DRAGON-SRV] Alquimista cura +" + curacion + ". Vida: " + vidaDragon + " (Fase " + fase + ")");
        notifyAll();
    }

    /** Fase actual del dragon (Escenario 11) */
    public synchronized int getFase() {
        if (vidaDragon > 200) return 1;      // Dragon joven
        else if (vidaDragon > 100) return 2;  // Dragon enfurecido
        else return 3;                        // Dragon ancestral
    }

    /** Dano del contraataque segun la fase */
    public synchronized int getDanoContraataque() {
        switch (getFase()) {
            case 1: return 5;   // Fase 1: poco dano
            case 2: return 15;  // Fase 2: medio
            case 3: return 30;  // Fase 3: mucho
            default: return 10;
        }
    }

    public synchronized int getVidaDragon() { return vidaDragon; }
    public synchronized boolean dragonVivo() { return vidaDragon > 0; }

    /** Esperar hasta que el dragon muera */
    public synchronized void esperarVictoria() {
        while (vidaDragon > 0) {
            try { wait(); } catch (InterruptedException e) { }
        }
    }
}

