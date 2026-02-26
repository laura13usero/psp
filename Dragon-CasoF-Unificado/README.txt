================================================================================
  CASO F - ALQUIMISTAS CURAN AL DRAGON (PRODUCTOR-CONSUMIDOR ANTAGONICO)
  Dragon = Servidor con vida + fases + los alquimistas lo curan
  VERSION UNIFICADA
================================================================================

PATRON NUEVO: Productor-Consumidor ANTAGONICO sobre un mismo monitor.
  El servidor del Dragon (puerto 5003) recibe DOS tipos de clientes:
    - Lance/Caballeros: envian ATACAR -> quitan vida al dragon
    - Alquimistas: envian CURAR_DRAGON -> suman vida al dragon

  ControlDragon gestiona una COMPETENCIA: mientras Lance quita vida,
  los alquimistas la restauran. El monitor synchronized coordina ambos.
  Ademas, el dragon tiene 3 FASES (vida > 200 / 100-200 / < 100) que
  modifican el dano del contraataque.

ESCENARIOS CUBIERTOS: 4 (Dragon servidor), 8 (Alquimistas aliados), 11 (Fases)

QUE CAMBIA:
  Servidores:
    - ControlDragon.java (NUEVO): monitor con vida+fases+curar+atacar
    - HiloServidorDragon.java (NUEVO): ServerSocket puerto 5003
    - HiloDragonServidor.java (NUEVO): protocolo ATACAR/CURAR/CONSULTAR
    - ServidorMaestro.java: +puerto 5003
  Clientes:
    - HiloAlquimista.java (MODIFICADO): +curarDragon() via socket
    - HiloLance.java (MODIFICADO): +atacarDragon() via socket
    - ClienteMaestro.java: +PUERTO_DRAGON

PUERTOS: 5000(Taberna), 5001(Mercado), 5002(Porton), 5003(Dragon)

EJECUCION:
  1. Ejecutar ServidorMaestro.java
  2. Ejecutar ClienteMaestro.java
================================================================================

