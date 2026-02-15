public class Main {
    public static void main(String[] args) {
        Contador contador = new Contador();

        for (int i = 0; i < 100; i++) {
            Hilo hilo = new Hilo(contador, i);
            hilo.start();
        }
    }
}

