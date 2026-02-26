================================================================================
  CASO G - DRAGON LOCAL SYNCHRONIZED (SIN SOCKETS AL DRAGON)
  Exclusion mutua directa sobre los locks de personajes
  VERSION UNIFICADA
================================================================================

PATRON NUEVO: Exclusion mutua COMPETITIVA.
  El dragon es un hilo en Clientes que usa synchronized(lockLance) y
  synchronized(lockElisabetha) DIRECTAMENTE. Mientras el dragon tiene
  el lock, NADIE puede interactuar con ese personaje (ni damas, ni
  caballeros, ni alquimistas). Es bloqueo por exclusion mutua real,
  NO por flag volatile.

ESCENARIOS CUBIERTOS: 10 (Evacuacion), Comp.1 (Ataque persona), Comp.2 (bloqueo)

QUE CAMBIA:
  - HiloDragon.java (NUEVO): hilo que hace synchronized(lockLance) y
    synchronized(lockElisabetha) para atacar directamente a las personas
  - HiloDama.java (MODIFICADO): detecta dragonAtacando y evacua por Porton
  - HiloLance.java (MODIFICADO): +atributo valor, +metodo public para chispa
  - ClienteMaestro.java (MODIFICADO): +flags dragonAtacando, dragonDerrotado

  NO HAY NUEVO SERVIDOR. Solo 3 puertos: 5000, 5001, 5002.

EJECUCION:
  1. Ejecutar ServidorMaestro.java
  2. Ejecutar ClienteMaestro.java

DIFERENCIA CON CASOS A/B/C:
  - Caso A/B/C: Dragon se comunica via SOCKET con servidores
  - Caso G: Dragon se comunica via SYNCHRONIZED con locks de personajes
  Es el unico caso donde el dragon NO usa sockets en absoluto.
  Demuestra la diferencia entre comunicacion por sockets (entre procesos)
  y comunicacion por variables compartidas (entre hilos del mismo proceso).
================================================================================

