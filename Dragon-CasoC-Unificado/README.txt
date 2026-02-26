================================================================================
  CASO C - DRAGON DESTRUYE LUGARES + CONDICION DE FIN MODIFICADA
  El Dragon como hilo que ataca servidores existentes
  VERSION UNIFICADA
================================================================================

QUE CAMBIA RESPECTO AL ORIGINAL:
  - HiloDragon (NUEVO en Clientes): hilo que ataca Mercado/Porton/Taberna via socket
  - HiloMercado, HiloPorton, HiloTaberna (MODIFICADOS): nuevo case ATAQUE_DRAGON
    + flag volatile "destruido" + reconstruccion automatica tras 20 segundos
  - ControlTaberna (MODIFICADO): nueva condicion de fin: dragonDerrotado
    -> while(!lance100 || !elisabetha100 || !dragonDerrotado) { wait(); }
  - HiloElisabetha y HiloLance: manejan respuesta "LUGAR_DESTRUIDO"
    + nueva accion "irAMatarDragon" que marca dragonDerrotado=true

NO HAY NUEVO SERVIDOR: el dragon ataca los servidores YA existentes.

PUERTOS (sin cambios): Taberna:5000, Mercado:5001, Porton:5002.

EJECUCION:
  1. Ejecutar ServidorMaestro.java
  2. Ejecutar ClienteMaestro.java

DIFERENCIA CON CASOS A y B:
  - Caso A: Dragon secuestra a Elisabetha en nuevo servidor (Cubil)
  - Caso B: Dragon ES un servidor al que ambos se conectan (barrera)
  - Caso C: Dragon ataca los servidores existentes (destruccion temporal)
            + para ganar hay que matar al dragon (condicion extra de fin)
================================================================================

