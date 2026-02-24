public class ControlTaberna {
    private boolean elisabethaPresente=false,lancePresente=false,yaSeConocen=false,elisabetha100=false,lance100=false;
    public synchronized void entrar(String p){if(p.equals("ELISABETHA"))elisabethaPresente=true;else if(p.equals("LANCE"))lancePresente=true;notifyAll();}
    public synchronized void salir(String p){if(p.equals("ELISABETHA"))elisabethaPresente=false;else if(p.equals("LANCE"))lancePresente=false;notifyAll();}
    public synchronized boolean estaElisabetha(){return elisabethaPresente;}
    public synchronized boolean estaLance(){return lancePresente;}
    public synchronized boolean yaSeConocen(){return yaSeConocen;}
    public synchronized void seConocen(){yaSeConocen=true;notifyAll();}
    public synchronized void registrarChispa100(String p){if(p.equals("ELISABETHA"))elisabetha100=true;else if(p.equals("LANCE"))lance100=true;notifyAll();}
    public synchronized void esperarAlOtro(String p){if(p.equals("ELISABETHA")){while(!lance100){try{wait();}catch(InterruptedException e){}}}else if(p.equals("LANCE")){while(!elisabetha100){try{wait();}catch(InterruptedException e){}}}notifyAll();}
}

