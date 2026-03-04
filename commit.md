feat(punto6): sistema chiuso Q0→Q1→Q2 con N clienti circolanti

Implementazione completa del punto 6 della consegna:
simulatore event-driven di rete chiusa a tre centri con
N clienti che circolano indefinitamente.

Nuove classi:
- ClosedNetworkConfig: configurazione immutabile (N, Z, S1, S2)
  con calcolo del throughput bound di saturazione
- ClosedNetworkStatistics: accumulatori per Q1 e Q2
  (utilizzo, E[T], E[N], E[Nq], throughput)
- ClosedNetworkSimulator: simulatore event-driven con tre tipi
  di evento (END_THINK_TIME, DEPARTURE da Q1, TIMEOUT da Q2);
  invariante di conservazione N clienti garantita
- ClosedNetworkRunner: R repliche con semi distanziati + IC 95%
  per tutti gli indici (inclusi X1 e X2 separati)
- sim/runners/Punto5Runner: ex Punto5DataCollection, spostato
  nel package runners e aggiunto metodo statico run()
- sim/runners/Punto6Runner: raccolta dati per 4 valori di N
  (5, 15, 30, 50) con stampa tabelle per la relazione

Modifiche a classi esistenti:
- Customer: aggiunti arrivalTimeAtQ1 / arrivalTimeAtQ2
- Main: dispatcher CLI abilitato per punto5 e punto6
- pom.xml: aggiunto exec-maven-plugin (mvn exec:java)

Documentazione:
- docs/punto6.md: relazione con risultati simulati, verifica
  legge di utilizzo, legge di Little, analisi regimi di carico
- README.md: aggiornato a stato attuale (punto 6, 98 test,
  struttura runners, comandi Maven)

Test: 19 nuovi test JUnit 5 (ClosedNetworkConfigTest x8,
ClosedNetworkSimulatorTest x11) — totale suite: 98/98

