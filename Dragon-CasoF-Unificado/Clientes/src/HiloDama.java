import java.util.Random;
public class HiloDama extends Thread {
    private String nombreDama;private Random random=new Random();
    public HiloDama(int n){this.nombreDama="Dama"+n;}
    @Override public void run(){while(!ClienteMaestro.simulacionTerminada){if(random.nextInt(2)==0){String[]l={"montar a caballo","practicar esgrima","enterarse de rumores"};try{Thread.sleep(5000);}catch(InterruptedException e){}}else{String msg=random.nextBoolean()?"CONFIDENCIA":(random.nextBoolean()?"RUMOR_LANCE":"INVITACION_BAILE");synchronized(ClienteMaestro.lockElisabetha){long t0=System.currentTimeMillis();ClienteMaestro.damaQuePide=nombreDama;ClienteMaestro.mensajeDama=msg;ClienteMaestro.hayPeticionDama=true;while(System.currentTimeMillis()-t0<20000&&ClienteMaestro.hayPeticionDama){try{ClienteMaestro.lockElisabetha.wait(2000);}catch(InterruptedException e){}}if(ClienteMaestro.hayPeticionDama){ClienteMaestro.hayPeticionDama=false;ClienteMaestro.damaQuePide=null;ClienteMaestro.mensajeDama=null;}}}}}
}

