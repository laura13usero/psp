public class Contador {
    private int valor;
    private int turnoActual;

    public Contador() {
        this.valor = 0;
        this.turnoActual = 0;
    }

    public int mirarContador() {
        return this.valor;
    }

    public void ponerValor(int nuevoValor) {
        this.valor = nuevoValor;
    }

    public synchronized void incrementar(int miTurno) {
        while (turnoActual != miTurno) {
            try {
                wait();
            } catch (InterruptedException e) {
            }
        }

        this.valor++;
        System.out.println("Holaa! Soy el hilo " + miTurno);

        turnoActual++;
        notifyAll();
    }
}
