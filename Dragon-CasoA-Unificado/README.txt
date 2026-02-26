================================================================================
  CASO A - SECUESTRO DE ELISABETHA + RESCATE + DUELO CONTRA EL DRAGON
  VERSION UNIFICADA (todo en localhost)
================================================================================

ENUNCIADO QUE IMPLEMENTA:
"Nuestro dragon morara en paz hasta el dia en que, por capricho del destino,
 irrumpa en el mercado para secuestrar a Elisabetha, llevandola cautiva a su
 cubil. Alli permanecera prisionera hasta que Lancelot, movido por la urgencia
 de su rescate, acuda a enfrentarse a las garras del mito. En el climax de
 esta gesta, ambos se batiran en duelo contra la bestia. El resultado es
 incierto: podrian emerger victoriosos pero malheridos, perdiendo 20 puntos
 de chispa por el esfuerzo; o bien, podrian abatir al dragon y regresar con
 su cabeza como trofeo, elevando su chispa en 50 puntos por la gloria."

QUE SE ANADE RESPECTO AL PROYECTO ORIGINAL:
  - Nuevo servidor: ServidorCubil (puerto 5003) con ControlCubil (monitor)
  - Nuevo hilo cliente: HiloDragon (extends Thread) en paquete Clientes
  - Modificacion en HiloElisabetha: nueva accion "pasearPorElBosque" (secuestro)
  - Modificacion en HiloLance: nueva accion "explorarMontanas" (rescate)
  - Modificacion en ClienteMaestro: nuevas variables y arranque del dragon

PUERTOS:
  Taberna:       5000
  Mercado:       5001
  Porton Norte:  5002
  Cubil Dragon:  5003   <-- NUEVO

INSTRUCCIONES DE EJECUCION:
  1. Ejecutar ServidorMaestro.java (arranca los 4 servidores)
  2. Ejecutar ClienteMaestro.java (arranca todos los personajes + dragon)

FLUJO DEL SECUESTRO:
  1. HiloDragon decide atacar el mercado (via socket al ServidorMercado)
  2. El Mercado avisa que Elisabetha ha sido secuestrada (flag compartido)
  3. Elisabetha, al intentar ir al mercado, se entera de su secuestro
  4. Elisabetha se conecta al ServidorCubil y queda BLOQUEADA (wait)
  5. Lance detecta el secuestro y se conecta al ServidorCubil
  6. AMBOS luchan contra el dragon (ControlCubil coordina con wait/notifyAll)
  7. Resultado 50/50: victoria (+50 chispa) o derrota (-20 chispa)
  8. Elisabetha es liberada, ambos continuan su aventura

ARCHIVOS NUEVOS O MODIFICADOS (respecto al proyecto original):
  Servidores/src/
    ServidorMaestro.java      (modificado: nuevo puerto 5003 + arrancar cubil)
    HiloServidorCubil.java    (NUEVO: ServerSocket para el cubil del dragon)
    HiloCubil.java            (NUEVO: atiende peticiones del cubil)
    ControlCubil.java         (NUEVO: monitor synchronized para el secuestro)
    ControlTaberna.java        (sin cambios)
    HiloServidorTaberna.java   (sin cambios)
    HiloTaberna.java           (sin cambios)
    HiloServidorMercado.java   (sin cambios)
    HiloMercado.java           (modificado: nuevo comando ATAQUE_DRAGON)
    HiloServidorPorton.java    (sin cambios)
    HiloPorton.java            (sin cambios)

  Clientes/src/
    ClienteMaestro.java        (modificado: nuevas variables + dragon)
    HiloElisabetha.java        (modificado: nueva accion + secuestro)
    HiloLance.java             (modificado: nueva accion + rescate)
    HiloDragon.java            (NUEVO: hilo del dragon carmesi)
    HiloDama.java              (sin cambios)
    HiloCaballero.java         (sin cambios)
    HiloAlquimista.java        (sin cambios)
================================================================================

