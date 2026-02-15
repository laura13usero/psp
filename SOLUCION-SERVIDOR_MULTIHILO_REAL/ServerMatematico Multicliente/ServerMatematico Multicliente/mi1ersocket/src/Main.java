// Author dgenzor

import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {
        Cliente cliente = new Cliente();

        Boolean continuar;

        do{
            continuar = cliente.execute();
        } while (continuar);

        cliente.out.close();
        cliente.in.close();
        cliente.sc.close();
    }
}