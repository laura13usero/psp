public class Hilo extends Thread {
    private Contador contador;
    private int miTurno;

    Hilo(Contador contador, int turno) {
        this.contador = contador;
        this.miTurno = turno;
    }

    @Override
    public void run() {
        contador.incrementar(miTurno);
    }
}
