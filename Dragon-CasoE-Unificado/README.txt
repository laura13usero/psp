================================================================================
  CASO E - EVACUACION + SOCORRO SERVIDOR-SERVIDOR
  Comunicacion entre servidores via socket + Damas evacuan
  VERSION UNIFICADA
================================================================================

PATRON NUEVO: Comunicacion SERVIDOR -> SERVIDOR via socket.
  Cuando el dragon ataca el Mercado, el HiloMercado se conecta como CLIENTE
  al Porton Norte (socket) y envia "SOCORRO" + nombre del lugar atacado.
  El Porton recibe el socorro y abre las puertas para evacuacion.
  Las Damas detectan dragonAtacando (volatile) y cambian su comportamiento:
  dejan de hablar con Elisabetha y empiezan a evacuar por el Porton.

  ESTO ES UNICO: en A/B/C/G la comunicacion siempre es cliente->servidor.
  Aqui un SERVIDOR se conecta como CLIENTE a OTRO SERVIDOR.

ESCENARIOS CUBIERTOS: 5 (Destruir lugares), 10 (Evacuacion), 12 (Socorro),
                      6 (Condicion fin modificada)

QUE CAMBIA:
  Servidores:
    - HiloMercado.java: +ATAQUE_DRAGON +destruido + ENVIA SOCORRO al Porton
    - HiloTaberna.java: +ATAQUE_DRAGON +destruida
    - HiloPorton.java: +case SOCORRO +case EVACUAR
    - ControlTaberna.java: +dragonDerrotado en condicion de fin
  Clientes:
    - HiloDragon.java (NUEVO): ataca servidores existentes
    - HiloDama.java: +evacuacion cuando dragonAtacando
    - HiloLance.java: +matarDragon
    - ClienteMaestro.java: +flags dragon

PUERTOS: 5000(Taberna), 5001(Mercado), 5002(Porton). Sin nuevo servidor.

EJECUCION:
  1. Ejecutar ServidorMaestro.java
  2. Ejecutar ClienteMaestro.java
================================================================================

