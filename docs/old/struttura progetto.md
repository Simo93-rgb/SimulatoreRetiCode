Questa guida passo-passo è stata elaborata analizzando i file forniti (in particolare *Consegna2025-26.pdf* e *TracciaRelazione-SimulatoreEsteso.pdf*) e le librerie Java incluse (*Rng*, *Rngs*, *Rvgs*).

Poiché utilizzerai **WSL** (Windows Subsystem for Linux), la guida è orientata all'uso da terminale, assumendo che "Antigravity" sia il tuo ambiente di scripting/automazione (o un alias per i tuoi strumenti Python per i grafici/analisi).

Ecco la roadmap per completare il progetto:

### Fase 1: Setup dell'Ambiente e delle Librerie

Prima di scrivere la logica del simulatore, devi assicurarti che le fondamenta siano solide.

1. **Organizzazione Cartelle (WSL):**
Crea una directory per il progetto.
```bash
mkdir SimProject
cd SimProject
mkdir src

```


2. **Integrazione Librerie di Leemis:**
Sposta i file `.java` forniti (`Rng.java`, `Rngs.java`, `Rvgs.java`, `Rvms.java`) nella cartella `src`.
* **Nota:** Queste classi sostituiscono la classe `Random` standard di Java per garantire una generazione di numeri pseudo-casuali statisticamente valida e controllabile (tramite i *seed*).



### Fase 2: Il Simulatore Base (Coda Singola M/M/1)

La consegna richiede di partire dal simulatore di coda singola (Cap. 4 del libro di testo) e modificarlo.

1. **Struttura Event-Driven:**
Il tuo `Main.java` (o `Simulator.java`) deve avere un loop principale che gestisce la **FEL (Future Event List)**.
* *Stato:* `number_in_node`, `server_status` (IDLE/BUSY), `area_node`, `area_queue`, `last_event_time`.
* *Eventi:* `ARRIVAL`, `DEPARTURE`.


2. **Integrazione `Rvgs` (Random Variate Generators):**
Modifica la generazione degli eventi usando le librerie fornite.
* Inizializza `Rngs` nel costruttore.
* Usa **stream diversi** per arrivi e servizi per garantire indipendenza (es. `stream 0` per arrivi, `stream 1` per servizi).
* *Esempio codice:*
```java
Rngs rngs = new Rngs();
rngs.plantSeeds(123456789); // Seed iniziale
Rvgs rvgs = new Rvgs(rngs);

// Per generare un arrivo (esponenziale)
rngs.selectStream(0);
double arrivalTime = rvgs.exponential(1.0/lambda);

```




3. **Distribuzioni Richieste:**
Implementa un meccanismo (switch case o config file) per scegliere la distribuzione dei servizi come richiesto dalla *Consegna2025-26.pdf*:
* Esponenziale
* Uniforme (range min, max)
* Erlang (k stadi)
* Iperesponenziale (richiede logica custom usando uniformi per scegliere il ramo e esponenziali per il servizio).



### Fase 3: Statistica e Intervalli di Confidenza

La consegna pone molta enfasi sulla validità statistica. Non basta una sola run.

1. **Metodo delle Repliche Indipendenti:**
Non fare una simulazione infinita. Fai `K` repliche (es. 32 o 64).
* *Loop Esterno:* Gestisce le repliche.
* *Loop Interno:* La simulazione vera e propria finché non raggiunge la condizione di stop (es. tempo massimo o numero job processati).


2. **Gestione dei Seed:**
È cruciale che ogni replica sia indipendente.
* Tra una replica e l'altra, il generatore di numeri casuali deve avanzare o essere reimpostato correttamente affinché le sequenze non si sovrappongano. La classe `Rngs` gestisce questo automaticamente se usata correttamente (i seed dei vari stream sono spaziati di 100.000 numeri).


3. **Calcolo Intervalli:**
Salva le medie di ogni replica (es. tempo medio di risposta). Usa la distribuzione t-Student (o approssimazione normale se K > 30) per calcolare l'intervallo di confidenza al 95%.

### Fase 4: Estensione al Sistema Centrale (Interactive + Batch)

Questa è la parte "core" dell'esercizio (rif. *TracciaRelazione* e *Consegna*).

1. **Modello Logico:**
Il sistema non è più una singola coda. È probabilmente un modello CPU + I/O (o solo CPU con feedback) con due classi:
* **Interactive (Classe A):** Utenti che pensano (Terminali) -> Arrivano alla CPU -> Escono o tornano ai terminali. (Sistema Chiuso o Aperto a seconda della specifica esatta, solitamente Chiuso per i terminali).
* **Batch (Classe B):** Arrivano dall'esterno (Poisson) -> Coda CPU -> Servizio -> Escono. (Sistema Aperto).


2. **Gestione Classi:**
Il tuo oggetto `Job` o `Customer` deve avere un attributo `type` (INTERACTIVE o BATCH).
3. **Scheduling:**
Devi implementare la logica di priorità nella coda.
* Se la specifica chiede priorità, la coda non è più una semplice FIFO unica. Potresti aver bisogno di due liste o una PriorityQueue.
* *Attenzione:* Se la CPU è occupata da un Batch e arriva un Interactive, c'è preemption? (Di solito in questi esercizi base no, è *non-preemptive*, ma controlla se devi implementare priorità statica o Round Robin).


4. **Nuovi Eventi:**
La tua FEL dovrà gestire:
* `ARRIVAL_BATCH` (dal mondo esterno).
* `ARRIVAL_INTERACTIVE` (o fine "think time" dai terminali).
* `END_SERVICE` (qui devi controllare se ci sono job in coda di quale classe).



### Fase 5: Validazione e Sperimentazione (Antigravity/Python time)

Qui entra in gioco il tuo setup con WSL e script.

1. **Output CSV:**
Fai in modo che il tuo programma Java stampi su `stdout` o su file `.csv` in un formato pulito:
`ReplicaID, Rho, E[Ts], E[Ta], AvgResponseTime_Int, AvgResponseTime_Batch, ...`
2. **Validazione (Python/Excel):**
Usa le formule del file *Formulario (1).pdf* (Legge di Little, formule M/M/1) per calcolare i valori teorici attesi.
* Confronta Teoria vs Simulazione. Se l'intervallo di confidenza non include il valore teorico, c'è un bug.


3. **Automazione Esperimenti:**
Crea uno script (bash o Python "Antigravity") che:
* Compila: `javac src/*.java -d bin`
* Esegue loop variando parametri: Esegui il simulatore incrementando il tasso di arrivo Batch (lambda) mantenendo fissi gli interattivi.
* Salva i risultati.


4. **Grafici:**
Genera i grafici richiesti dalla *TracciaRelazione*:
* X-axis: Carico Batch (Lambda).
* Y-axis: Tempo di risposta medio Interattivi.
* Questo mostrerà come il traffico di background degrada le prestazioni degli utenti.



### Checklist Finale per la Relazione (PDF Traccia)

Assicurati di avere dati per queste sezioni:

* **Diagramma delle classi:** Mostra come hai esteso il simulatore base (es. classe `SimulatorMisto` che estende `SimulatorBase`).
* **Verifica del modello:** Tabella "Simulazione vs Analitico" (per il caso M/M/1 o rete semplice).
* **Analisi Impatto:** Grafico "Tempo Risposta Interattivi vs Carico Batch".

**Comando rapido per compilare ed eseguire (WSL):**

```bash
# Dalla root del progetto
javac -d bin src/*.java
java -cp bin MainSim

```

# Attuale struttura progetto (2026 Febbraio 18)

```bash
simon@BimboMio:~/GitHub/SimulatoreRetiCode$ tree -L 4
simon@BimboMio:~/GitHub/SimulatoreRetiCode$ tree -L 5
.
├── LICENSE
├── README.md
├── docs
│   ├── Figura1.jpg
│   ├── Leemis-Park.md
│   ├── consegna2025-06.md
│   ├── step1.md
│   ├── struttura progetto.md
│   └── svolgimento.md
├── pom.xml
├── src
│   ├── main
│   │   └── java
│   │       ├── libraries
│   │       │   ├── Rng.java
│   │       │   ├── Rngs.java
│   │       │   ├── Rvgs.java
│   │       │   └── Rvms.java
│   │       └── sim
│   │           ├── Main.java
│   │           └── ServiceGenerator.java
│   └── test
│       └── java
│           └── sim
│               └── ServiceGeneratorTest.java
└── target
    ├── classes
    │   ├── libraries
    │   │   ├── Rng.class
    │   │   ├── Rngs.class
    │   │   ├── Rvgs.class
    │   │   └── Rvms.class
    │   └── sim
    │       ├── Main.class
    │       └── ServiceGenerator.class
    ├── generated-sources
    │   └── annotations
    ├── generated-test-sources
    │   └── test-annotations
    ├── maven-status
    │   └── maven-compiler-plugin
    │       ├── compile
    │       │   └── default-compile
    │       └── testCompile
    │           └── default-testCompile
    ├── surefire-reports
    │   ├── TEST-sim.ServiceGeneratorTest.xml
    │   └── sim.ServiceGeneratorTest.txt
    └── test-classes
        └── sim
            └── ServiceGeneratorTest.class

27 directories, 25 files
```
Versione Java:
```bash
simon@BimboMio:~/GitHub/SimulatoreRetiCode$ java --version
openjdk 21.0.10 2026-01-20
OpenJDK Runtime Environment (build 21.0.10+7-Ubuntu-124.04)
OpenJDK 64-Bit Server VM (build 21.0.10+7-Ubuntu-124.04, mixed mode, sharing)
```