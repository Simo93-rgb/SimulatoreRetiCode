# Consegna — Valutazione delle Prestazioni A.A. 2025-26

Realizzare un simulatore in Java (o altro linguaggio general purpose a scelta), partendo dal simulatore di coda singola descritto nel libro di testo (Cap. 4) il cui codice è a disposizione sul DIR.

---

## Punto 1 — Generazione dei Numeri Casuali

Integrare nel simulatore un generatore di numeri casuali basato sul metodo congruente moltiplicativo (per generare U(0,1)) descritto nel libro Leemis-Park. Potete usare le classi `Rng` e `Rvg` reperibili sulla pagina web del prof. Leemis.

Distribuzioni di probabilità da includere:
- **Esponenziale** — 1 parametro: la media (o il tasso = 1/media). C_v = 1.
- **Uniforme** — 2 parametri: min, max.
- **Erlang a k stadi** — 2 parametri: media totale e numero di stadi k. C_v < 1 (tanto minore quanti più stadi, a parità di media).
- **Iperesponenziale** — 3 parametri: p (probabilità di scegliere la prima esponenziale), media/tasso della prima esponenziale, media/tasso della seconda. C_v > 1.

---

## Punto 2 — Semi Distanziati e Stream Indipendenti

Aggiungere i metodi per ottenere una lista di semi iniziali sufficientemente distanziati, da usare per:
- Replicare la stessa simulazione più volte con semi diversi (le simulazioni possono essere lanciate in parallelo con garanzia di non sovrapposizione delle sequenze generate).
- Gestire sequenze casuali indipendenti per diverse attività nella stessa esecuzione (es. tempi di inter-arrivo e tempi di servizio su stream separati).

Potete usare le classi `Rngs` e `Rvgs` reperibili sulla pagina web del prof. Leemis.

---

## Punto 3 — Repliche e Report degli Indici

Creare le funzioni necessarie a rilanciare R volte il simulatore con semi diversi (metodo delle prove ripetute), raccogliendo i risultati di ciascun run e producendo un report con gli indici di prestazione calcolati.

> È possibile eseguire i run in sequenza oppure in parallelo con semi sufficientemente distanziati. Si può anche lanciare il simulatore R volte tramite script, prevedendo la possibilità di passare il seme iniziale a ogni replica.

Indici da calcolare: throughput, utilizzazione del servitore, tempo medio di permanenza, lunghezza media della coda.

---

## Punto 4 — Stime Intervallari ed Errore Relativo

A partire dai risultati degli R run, calcolare la stima puntuale e intervallare dei valori medi dei diversi indici. Valutare l'errore relativo e, se necessario, incrementare il numero di run oppure ripeterli estendendoli.

---

## Punto 5 — Validazione vs JMT e Impatto della Variabilità

Confrontare i risultati degli esperimenti di simulazione con analoghi esperimenti condotti con JMT (su modelli equivalenti) allo scopo di validare l'implementazione. Effettuare più esperimenti al variare della distribuzione di probabilità dei tempi di inter-arrivo e di servizio (per es. come mostrato nel Cap. 4 di Leemis-Park: *"Impact of Variability of Interarrival and Service Times"*).

---

## Punto 6 — Sistema Chiuso con Q0, Q1, Q2

Realizzare un sistema chiuso in cui circolano N clienti, aggiungendo al primo centro di servizio Q1 un secondo centro (Q2) e una stazione di puro ritardo (i terminali, Q0), collegati secondo la configurazione di Figura 1. Tempi di servizio (diversi in Q1 e Q2) e di ritardo (in Q0) distribuiti esponenzialmente.

Simulare per 3 o 4 valori di N e calcolare:
- Throughput del sistema (visto da Q0) e dei due centri Q1, Q2
- Tempi medi di risposta del *sistema centrale* (Q1 + Q2)
- Utilizzazione di Q1 e Q2
- Lunghezza media delle code di Q1 e Q2

---

## Punto 7 — Classe Aperta Mista

Aggiungere una seconda classe di clienti **aperta**, con tempi di inter-arrivo distribuiti secondo un'iperesponenziale (parametri: probabilità p, due tassi λ₁ e λ₂), che viene servita **esclusivamente da Q1** con un tempo medio di servizio doppio rispetto a quello dei clienti della classe chiusa.

Fissato N, eseguire il sistema per valori crescenti del tasso medio di inter-arrivo e misurare:
- Tempi di risposta per le due classi di clienti
- Throughput di ciascuna stazione
- Utilizzazione di ciascuna stazione (per Q1: dettagliato per classe)

![FIGURA 1: schema del sistema. In rosso il flusso di clienti della classe aperta](Figura1.jpg)

Il diagramma rappresenta un modello di rete di code mista multiclasse (mixed queueing network) costituita da tre centri di servizio ($Q_0$, $Q_1$, $Q_2$) in cui transitano due distinte classi di job:
- **Classe Chiusa** (flusso blu): Caratterizzata da una popolazione costante nel sistema. I job in uscita dal nodo $Q_0$ subiscono un instradamento probabilistico (routing): accedono al nodo $Q_1$ con probabilità $p_1$ e al nodo $Q_2$ con probabilità $1-p_1$. Al termine del servizio presso $Q_1$ o $Q_2$, i job vengono re-instradati deterministicamente (probabilità unitaria) verso $Q_0$, chiudendo il ciclo.
- **Classe Aperta** (flusso rosso): Caratterizzata da un tasso di arrivi dall'esterno (throughput esterno). I job di questa classe transitano esclusivamente attraverso il nodo $Q_1$ e, completato il servizio, abbandonano definitivamente la rete.
- **Risorsa Condivisa**: Il centro di servizio $Q_1$ funge da nodo condiviso, elaborando simultaneamente le richieste di entrambe le classi (aperta e chiusa), il che implica una mutua dipendenza prestazionale tra i due flussi in base alla disciplina di servizio (es. FCFS, PS).

---

## Punto 8 — Validazione del Modello Misto

Validare entrambi i modelli (solo classe chiusa; modello misto) confrontando i risultati, per alcune configurazioni, con quelli calcolati su modelli analoghi tramite JMT.

---

## Punto 9 — Relazione Finale e Presentazione

Preparare una relazione finale sintetica e una presentazione (PowerPoint o simile) contenente:
- Descrizione dei modelli (stato, eventi, procedure di gestione eventi, aggiornamento accumulatori), in particolare del modello di Figura 1 nelle due versioni.
- Tutti i risultati sotto forma di tabelle con stime puntuali e intervallari.
- Eventuali grafici dell'andamento di indici significativi al variare dei parametri.
- Paragrafi di interpretazione: come varia un indice al variare di un parametro e se l'andamento è in linea con le attese.

Vedere anche [Traccia Relazione](docs/traccia-relazione-simulatore-esteso.md) per ulteriori precisazioni.
