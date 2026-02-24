================================================================================
  CASO B - DRAGON COMO SERVIDOR INDEPENDIENTE
  Lance y Elisabetha LUCHAN JUNTOS contra la bestia
  VERSION UNIFICADA
================================================================================

ENUNCIADO CLAVE (frase resaltada):
  "En el climax de esta gesta, AMBOS se batiran en duelo contra la bestia."

QUE CAMBIA:
  - El Dragon es un SERVIDOR (puerto 5003) con vida = 200
  - Lance y Elisabetha se conectan como clientes para ATACAR al dragon
  - Usa una BARRERA (patron wait/notifyAll) para que ambos ataquen juntos
  - Cuando la vida del dragon llega a 0: victoria (+50 chispa)
  - Si el dragon contraataca y ambos caen: derrota (-20 chispa)

PUERTOS:
  Taberna: 5000 / Mercado: 5001 / Porton: 5002 / Dragon: 5003

EJECUCION:
  1. Ejecutar ServidorMaestro.java
  2. Ejecutar ClienteMaestro.java

DIFERENCIA CON CASO A:
  En el Caso A, el dragon secuestra a Elisabetha y Lance la rescata.
  En el Caso B, el dragon es un servidor pasivo al que ambos se conectan
  voluntariamente para combatirlo juntos (no hay secuestro, hay combate
  coordinado entre los dos protagonistas).
================================================================================

