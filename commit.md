feat(punto7): sistema misto classe chiusa + classe aperta con inter-arrivi iperesponenziali

Implementazione completa del punto 7 della consegna:
simulatore event-driven di rete mista a due classi (chiusa N=15
e aperta con inter-arrivi iperesponenziali) che condividono Q1.

Nuove classi:
- MixedNetworkConfig: configurazione immutabile per il sistema misto
  (parametri classe chiusa + aperta: p, mean1, mean2, S1_open)
- MixedNetworkStatistics: accumulatori separati per classe in Q1
  (areaBusyQ1Closed / areaBusyQ1Open, E[T] per classe, throughput
  per classe, lunghezza code)
- MixedNetworkSimulator: simulatore event-driven con routing
  differenziato per classe a Q1 (OPEN → esce, CLOSED → Q2);
  generazione inter-arrivi iperesponenziali via Leemis-Park
- MixedNetworkRunner: R repliche con semi distanziati + IC 95%
  per tutti gli indici (throughput, utilizzo, tempi di risposta,
  lunghezze code) distinti per classe
- sim/runners/Punto7Runner: esperimenti al variare di
  λ_open ∈ {0.10, 0.20, 0.30, 0.40} con stampa tabelle

Modifiche a classi esistenti:
- Customer: aggiunto enum CustomerClass { CLOSED, OPEN }
  (default CLOSED, retrocompatibile con tutti i punti precedenti)
  e metodo isOpen(); aggiunti arrivalTimeAtQ1 / arrivalTimeAtQ2
- SeedManager.StreamType: aggiunti OPEN_ARRIVALS (stream 4)
  e OPEN_SERVICE (stream 5)
- Main: dispatcher CLI abilitato per punto7

Documentazione:
- docs/punto7.md: relazione con architettura, scelte implementative,
  risultati simulati (throughput, utilizzo, tempi di risposta,
  lunghezze code), verifica legge di utilizzo, confronto con punto 6

Test: 17 nuovi test JUnit 5 (MixedNetworkConfigTest x5,
MixedNetworkSimulatorTest x12) — totale suite: 115/115

---



