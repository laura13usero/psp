================================================================================
  CASO H - DRAGON COMO CLIENTE-SERVIDOR A LA VEZ
  El dragon es un ServerSocket Y TAMBIEN abre Sockets hacia otros servidores
  VERSION UNIFICADA
================================================================================

PATRON NUEVO: CLIENTE-SERVIDOR SIMULTANEO (doble rol).
  El dragon tiene DOS partes que funcionan a la vez:

  1. PARTE SERVIDOR (puerto 5003): El dragon abre un ServerSocket y acepta
     conexiones de Lance y Caballeros que quieren atacarlo. Usa ControlDragon
     (monitor synchronized) para gestionar la vida del dragon.
     Protocolo: ATACAR + dano -> contraataque + vidaRestante + vivo

  2. PARTE CLIENTE (hilo atacante): A la vez, el dragon lanza un hilo
     separado que ABRE SOCKETS como cliente hacia los servidores existentes
     (Mercado, Porton, Taberna) para destruirlos. Envia ATAQUE_DRAGON.

  ESTO ES UNICO: en todos los demas casos el dragon es O cliente O servidor
  O hilo local. Aqui es LAS DOS COSAS a la vez. El dragon RECIBE ataques
  (como servidor) y ENVIA ataques (como cliente) simultaneamente.

ESCENARIOS CUBIERTOS:
  1 (Nuevo personaje), 4 (Dragon servidor), 5 (Destruir lugares),
  6 (Condicion fin), 10 (Evacuacion), 11 (Fases)

PUERTOS: 5000(Taberna) 5001(Mercado) 5002(Porton) 5003(Dragon)

EJECUCION:
  1. Ejecutar ServidorMaestro.java
  2. Ejecutar ClienteMaestro.java
================================================================================

