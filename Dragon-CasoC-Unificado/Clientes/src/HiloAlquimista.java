import java.util.Random;

/** HILO ALQUIMISTA - identico al original. */
public class HiloAlquimista extends Thread {
    private String nombreAlq;
    private Random random = new Random();
    private int pocionesE = 0, pocionesL = 0;
    public HiloAlquimista(int n) { this.nombreAlq = "Alquimista" + n; }

    @Override
    public void run() {
        estudiarCalderos();
        while (!ClienteMaestro.simulacionTerminada) {
            int a = random.nextInt(100);
            if (a < 60) { estudiarCalderos(); }
            else if (a < 80) { visitarElisabetha(); }
            else { visitarLance(); }
        }
    }

    private void estudiarCalderos() {
        System.out.println("[" + nombreAlq + "] Estudia calderos...");
        try { Thread.sleep(30000); } catch (InterruptedException e) { }
        int r = random.nextInt(100);
        if (r < 30) { pocionesE++; } else if (r < 60) { pocionesL++; }
    }

    private void visitarElisabetha() {
        if (pocionesE <= 0) { try { Thread.sleep(3000); } catch (InterruptedException e) { } return; }
        try { Thread.sleep(5000); } catch (InterruptedException e) { }
        synchronized (ClienteMaestro.lockElisabetha) {
            ClienteMaestro.hayPeticionAlquimistaE = true; ClienteMaestro.mensajeAlquimistaE = "POCION";
            long t0 = System.currentTimeMillis();
            while (ClienteMaestro.hayPeticionAlquimistaE && System.currentTimeMillis()-t0 < 10000) {
                try { ClienteMaestro.lockElisabetha.wait(2000); } catch (InterruptedException e) { }
            }
            pocionesE--;
            if (ClienteMaestro.hayPeticionAlquimistaE) { ClienteMaestro.hayPeticionAlquimistaE=false; ClienteMaestro.mensajeAlquimistaE=null; }
        }
    }

    private void visitarLance() {
        if (random.nextInt(100) < 80) {
            if (pocionesL <= 0) { try { Thread.sleep(3000); } catch (InterruptedException e) { } return; }
            try { Thread.sleep(7000); } catch (InterruptedException e) { }
            synchronized (ClienteMaestro.lockLance) {
                ClienteMaestro.hayPeticionAlquimistaL=true; ClienteMaestro.tipoAccionAlquimistaL="POCION"; ClienteMaestro.mensajeAlquimistaL="POCION";
                long t0=System.currentTimeMillis();
                while(ClienteMaestro.hayPeticionAlquimistaL && System.currentTimeMillis()-t0<15000){ try{ClienteMaestro.lockLance.wait(2000);}catch(InterruptedException e){} }
                pocionesL--;
                if(ClienteMaestro.hayPeticionAlquimistaL){ClienteMaestro.hayPeticionAlquimistaL=false;ClienteMaestro.mensajeAlquimistaL=null;ClienteMaestro.tipoAccionAlquimistaL=null;}
            }
        } else {
            try { Thread.sleep(7000); } catch (InterruptedException e) { }
            synchronized (ClienteMaestro.lockLance) {
                ClienteMaestro.hayPeticionAlquimistaL=true; ClienteMaestro.tipoAccionAlquimistaL="AMENAZA"; ClienteMaestro.mensajeAlquimistaL="AMENAZA";
                long t0=System.currentTimeMillis();
                while(ClienteMaestro.hayPeticionAlquimistaL && System.currentTimeMillis()-t0<15000){ try{ClienteMaestro.lockLance.wait(2000);}catch(InterruptedException e){} }
                if(ClienteMaestro.hayPeticionAlquimistaL){ClienteMaestro.hayPeticionAlquimistaL=false;ClienteMaestro.mensajeAlquimistaL=null;ClienteMaestro.tipoAccionAlquimistaL=null;}
            }
        }
    }
}

