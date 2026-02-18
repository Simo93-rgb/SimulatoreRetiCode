Ottimo lavoro con l'organizzazione. Avere una struttura pulita è il 50% del lavoro in Java.

Ecco la guida passo-passo basata sui punti della **Consegna 2025-26**, formattata come richiesto (Codice + Spiegazione Formale).

Ti consiglio di creare nuovi file dentro `src/sim/` (es. `Event.java`, `Stats.java`) per non far esplodere il `Main`.

---

### 1. Generazione delle Distribuzioni (Integrazione Rvgs)

La consegna richiede di implementare distribuzioni specifiche: Esponenziale, Uniforme, Erlang, Iperesponenziale.

**Spiegazione Formale:**
Le librerie di Leemis (`Rvgs`) forniscono già le prime tre. L'unica "tricky" è l'**Iperesponenziale**. Questa distribuzione modella un servizio che ha probabilità  di essere veloce (tasso ) e probabilità  di essere lento (tasso ) (o viceversa).
Algoritmicamente, si usa il metodo della composizione: si genera un numero uniforme ; se  si campiona dalla prima esponenziale, altrimenti dalla seconda.

**Codice Java (`src/sim/ServiceGenerator.java`):**

```java
package sim;
import libraries.*;

public class ServiceGenerator {
    private Rvgs rvgs;
    private Rngs rngs; // Serve per la scelta probabilistica nell'IperEsp

    public ServiceGenerator(Rngs r, Rvgs rv) {
        this.rngs = r;
        this.rvgs = rv;
    }

    public double getServiceTime(String type, double param1, double param2, double param3) {
        switch (type) {
            case "EXP": 
                // param1 = media (1/lambda)
                return rvgs.exponential(param1);
            
            case "UNIF": 
                // param1 = min, param2 = max
                return rvgs.uniform(param1, param2);
            
            case "ERLANG": 
                // param1 = media totale, param2 = k (stadi)
                // Rvgs richiede (k, media_di_ogni_stadio) oppure (k, media_totale/k)? 
                // Nota: Rvgs.erlang(n, b) di solito b è la media del singolo stadio.
                long k = (long) param2;
                return rvgs.erlang(k, param1 / k);

            case "HYPER":
                // param1 = p (prob prima esp), param2 = media1, param3 = media2
                double p = param1;
                double mean1 = param2;
                double mean2 = param3;
                
                // Uso il flusso corrente per decidere quale ramo prendere
                if (rngs.random() < p) {
                    return rvgs.exponential(mean1);
                } else {
                    return rvgs.exponential(mean2);
                }
            
            default: return 1.0;
        }
    }
}

```

---

### 2. Struttura Eventi e FEL (Future Event List)

La consegna richiede un approccio *Event-Driven*.

**Spiegazione Formale:**
La simulazione evolve saltando da un evento all'altro (Next-Event Time Advance). La **FEL** è una lista ordinata per tempo crescente.
Dobbiamo definire un oggetto `Event` che implementi l'interfaccia `Comparable` per essere ordinato automaticamente da una `PriorityQueue`.
Per l'estensione futura (Sistema Misto), aggiungiamo subito un campo `type` (es. 1 = Interactive, 2 = Batch).

**Codice Java (`src/sim/Event.java`):**

```java
package sim;

public class Event implements Comparable<Event> {
    public static final int ARRIVAL = 1;
    public static final int DEPARTURE = 2;

    public int type;       // ARRIVAL o DEPARTURE
    public double time;    // Tempo in cui accade
    public int jobClass;   // 0 = Interattivo, 1 = Batch (utile per dopo)

    public Event(int type, double time, int jobClass) {
        this.type = type;
        this.time = time;
        this.jobClass = jobClass;
    }

    @Override
    public int compareTo(Event other) {
        // Ordina per tempo crescente
        if (this.time < other.time) return -1;
        if (this.time > other.time) return 1;
        return 0;
    }
}

```

---

### 3. Il Motore di Simulazione (Logica Coda Singola)

La base richiesta è il simulatore Cap. 4. Qui implementiamo la logica di gestione eventi.

**Spiegazione Formale:**
Lo stato del sistema è definito da  (numero job nel sistema) e dallo stato del server (Idle/Busy).

* **Arrivo:** . Se Server Idle  Genera Servizio (schedula Departure). Se Busy  Accoda. Schedula *prossimo* Arrivo.
* **Partenza:** . Se   Genera Servizio per il prossimo (schedula Departure).

**Codice Java (Snippet per `src/sim/Main.java` o classe `Simulator`):**

```java
// Variabili di stato
double clock = 0.0;
int numberInService = 0;
int numberInQueue = 0;
boolean serverBusy = false;

// Strutture dati
PriorityQueue<Event> fel = new PriorityQueue<>();

// ... Setup iniziale (Primo arrivo) ...
fel.add(new Event(Event.ARRIVAL, getInterarrival(), 0));

while (!fel.isEmpty() && clock < END_TIME) {
    Event e = fel.poll();
    clock = e.time; // Avanzamento tempo

    if (e.type == Event.ARRIVAL) {
        // 1. Schedula prossimo arrivo
        double nextArrival = clock + getInterarrival();
        fel.add(new Event(Event.ARRIVAL, nextArrival, 0));

        // 2. Gestione Logica
        if (!serverBusy) {
            serverBusy = true;
            numberInService++;
            double serviceTime = serviceGen.getServiceTime("EXP", 1.0, 0, 0);
            fel.add(new Event(Event.DEPARTURE, clock + serviceTime, 0));
        } else {
            numberInQueue++;
        }
    } 
    else if (e.type == Event.DEPARTURE) {
        // 1. Gestione Logica
        if (numberInQueue > 0) {
            numberInQueue--;
            // C'è ancora gente, servi il prossimo
            double serviceTime = serviceGen.getServiceTime("EXP", 1.0, 0, 0);
            fel.add(new Event(Event.DEPARTURE, clock + serviceTime, 0));
        } else {
            serverBusy = false;
            numberInService--;
        }
    }
    
    // Raccogli statistiche qui (Area sotto la curva)
    stats.record(clock, numberInService + numberInQueue);
}

```

---

### 4. Statistiche e Repliche Indipendenti

La consegna richiede stime intervallari (Intervalli di Confidenza).

**Spiegazione Formale:**
Non basta una run. Usiamo il **Metodo delle Repliche Indipendenti**.
Eseguiamo  simulazioni (repliche). Ogni replica deve usare sequenze di numeri casuali indipendenti (ottenute non sovrapponendo i seed o usando stream diversi di `Rngs`).
Calcoliamo la media  di ogni replica. La media finale è la media delle medie. L'errore si stima con la t-Student: .

**Codice Java (Struttura del Loop nel `Main`):**

```java
package sim;
import libraries.*;

public class Main {
    public static void main(String[] args) {
        int K = 64; // Numero repliche
        Rngs rngs = new Rngs(); 
        rngs.plantSeeds(123456789); 

        StatsAccumulator globalStats = new StatsAccumulator();

        for (int i = 0; i < K; i++) {
            // 1. Setup Stream per questa replica
            // Rngs ha 256 stream. Usiamo stream diversi per Arrivi e Servizi
            // Per garantire indipendenza tra repliche, rngs avanza automaticamente
            // se non resettiamo i seed, ma dobbiamo selezionare lo stream giusto.
            
            rngs.selectStream(0); // Stream Arrivi
            rngs.selectStream(1); // Stream Servizi
            
            // IMPORTANTE: Tra una replica e l'altra i generatori devono avanzare.
            // Le librerie di Leemis gestiscono questo se non richiami plantSeeds dentro il loop.
            // Tuttavia, per sicurezza matematica, spesso si fa un salto sui seed.
            // Per ora fidiamoci della gestione stream di Rngs.
            
            // 2. Esegui UNA simulazione completa
            Simulator sim = new Simulator(rngs);
            double resultReplica = sim.run(); // Ritorna es. tempo medio risposta
            
            // 3. Salva il risultato
            globalStats.add(resultReplica);
            
            // Avanza i seed degli stream usati per la prossima replica (opzionale con Rngs java, 
            // ma buona norma formale per assicurare il salto)
            rngs.selectStream(0); 
            long seed0 = rngs.getSeed(); // legge stato corrente
            rngs.putSeed(seed0); // "salva" lo stato
            
            rngs.selectStream(1);
            long seed1 = rngs.getSeed();
            rngs.putSeed(seed1);
        }

        // 4. Stampa risultati finali
        globalStats.printConfidenceInterval();
    }
}

```

---

### 5. Estensione Sistema Misto (Batch + Interattivo)

Questo è il cuore della "Relazione".

**Spiegazione Formale:**
Il sistema diventa una coda con due classi di utenti:

1. **Batch:** Sistema aperto (Arrivi Poissoniani ).
2. **Interactive:** Sistema chiuso ( utenti). Quando un utente finisce il servizio, entra in uno stato di "Think Time" (ritardo ), poi torna in coda.
La disciplina della coda CPU deve gestire entrambi. Spesso si usa una FCFS globale o priorità.
Modifiche necessarie:

* Due generatori di arrivi: uno per Batch (esponenziale), uno scaturito dalla fine del "Think Time".
* Quando parte un evento `DEPARTURE`, se era Interactive, si genera un evento `ARRIVAL` (futuro) distante  (Think Time). Se era Batch, esce dal sistema.

**Codice Java (Snippet Logica Estesa):**

```java
// ... dentro il processamento eventi ...

if (e.type == Event.ARRIVAL) {
    // Gestione Arrivo (comune)
    queue.add(e); // Aggiungi alla coda CPU
    
    // Se è un arrivo BATCH, dobbiamo schedulare il prossimo arrivo BATCH dall'esterno
    if (e.jobClass == CLASS_BATCH) {
        double nextBatch = clock + rvgs.exponential(1.0/lambdaBatch);
        fel.add(new Event(Event.ARRIVAL, nextBatch, CLASS_BATCH));
    }
    // Se è INTERACTIVE, non scheduliamo nulla qui (l'arrivo è generato dalla fine del think time)
} 
else if (e.type == Event.DEPARTURE) {
    // Chi ha finito?
    if (currentJobClass == CLASS_INTERACTIVE) {
        // Torna ai terminali -> Think Time -> Nuovo Arrivo
        double thinkTime = rvgs.exponential(meanThinkTime);
        fel.add(new Event(Event.ARRIVAL, clock + thinkTime, CLASS_INTERACTIVE));
    } else {
        // Batch: Esce dal sistema (statistiche throughput batch)
    }

    // Servi il prossimo in coda (se c'è)
    if (!queue.isEmpty()) {
       // ... logica start service ...
    }
}

```