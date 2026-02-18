# Piano di Integrazione - Simulatore Base Proposto

## 📋 Analisi del Simulatore Proposto

### Struttura Esistente

Il simulatore fornito (`Simulatore_base_proposto/`) è un esempio **M/G/1** event-driven tratto dal libro di testo con le seguenti caratteristiche:

#### ✅ **Punti di Forza**
1. **Architettura event-driven solida**:
   - `Event.java`: classe base per eventi (tipo + timestamp)
   - `EventList.java`: FEL implementata come **Splay Tree** (O(log n) ammortizzato)
   - `Queue.java`: coda FIFO circolare con array dinamico
   
2. **Pattern corretto**:
   - Loop principale basato su FEL
   - Separazione `ProcessArrival()` / `ProcessDeparture()`
   - Statistiche incrementali (area sotto curva, utilization)

3. **Metriche già implementate**:
   - Server utilization (ρ)
   - Tempo medio di risposta
   - Lunghezza massima coda
   - Percentuale job con tempo > soglia

#### ❌ **Limitazioni Critiche (da README)**
1. **`java.util.Random`**: viola il vincolo imperativo del progetto
2. **Single-run**: nessuna replica indipendente
3. **Nessuna analisi statistica**: mancano intervalli di confidenza
4. **Hardcoded**: parametri in-line, nessuna configurazione
5. **Distribuzione servizi fissa**: normale troncata, non supporta le 4 richieste dalla consegna

---

## 🎯 Obiettivo dell'Integrazione

**Migrare le componenti riusabili** del simulatore proposto nel nostro progetto **mantenendo la conformità ai vincoli**:
- ✅ Usare **solo** librerie Leemis-Park (`Rngs`, `Rvgs`)
- ✅ Supportare le **4 distribuzioni** richieste (Esponenziale, Uniforme, Erlang, Iperesponenziale)
- ✅ Implementare **metodo delle repliche indipendenti** + intervalli di confidenza
- ✅ Preparare l'estensione al **sistema misto** (Interactive + Batch)

---

## 🗺️ Piano di Integrazione a Fasi

### **FASE 2.A — Migrazione Strutture Dati Event-Driven**

**Obiettivo**: Portare nel nostro package `sim.*` le classi `Event`, `EventList`, `Queue` con refactoring minimo.

**Azioni**:
1. **Copiare e adattare** `SimUtils/*.java` → `src/main/java/sim/core/`:
   ```
   src/main/java/sim/core/
   ├── Event.java          (già esistente, da refactoring)
   ├── EventList.java      (Splay Tree FEL)
   └── CustomerQueue.java  (rinominata da Queue.java per chiarezza)
   ```

2. **Refactoring di `Event.java`**:
   - Aggiungere **tipo evento** come enum invece di `int`:
     ```java
     public enum EventType { ARRIVAL, DEPARTURE, END_THINK_TIME, ... }
     ```
   - Aggiungere campo `Customer` (o `Job`) invece di usare `Event` stesso come customer descriptor
   - Mantenere `Comparable<Event>` per ordinamento FEL

3. **Test strutture dati**:
   - Test JUnit5 per `EventList` (insert, getMin, dequeue)
   - Test JUnit5 per `CustomerQueue` (enqueue, dequeue, FIFO order)

**Output**: 
- Package `sim.core.*` con strutture dati testate
- File: `EventTest.java`, `EventListTest.java`, `CustomerQueueTest.java`

---

### **FASE 2.B — Simulatore M/M/1 Base con Librerie Leemis**

**Obiettivo**: Riscrivere `Sim.java` come `MMMOneSimulator.java` usando **solo** `Rngs`/`Rvgs` e il nostro `ServiceGenerator`.

**Azioni**:
1. **Creare `MMMOneSimulator.java`**:
   - Stato: `clock`, `queueLength`, `numberInService`, `statistics`
   - Eventi: `ARRIVAL`, `DEPARTURE`
   - **NO** `java.util.Random`, **SÌ** `ServiceGenerator`

2. **Integrazione con Step 1**:
   ```java
   // Nel costruttore
   Rngs rngs = new Rngs();
   rngs.plantSeeds(seed);
   Rvgs rvgs = new Rvgs(rngs);
   ServiceGenerator serviceGen = new ServiceGenerator(rngs, rvgs);
   
   // Per arrivi (Poisson = esponenziale)
   rngs.selectStream(0);
   double interarrival = serviceGen.exponential(meanInterarrival);
   
   // Per servizi (distribuzione configurabile)
   rngs.selectStream(1);
   double serviceTime = serviceGen.exponential(meanService); // o erlang(), uniform(), ecc.
   ```

3. **Parametrizzazione distribuzioni**:
   - Creare `SimulationConfig.java` con:
     ```java
     public enum ServiceDistribution { EXPONENTIAL, UNIFORM, ERLANG, HYPEREXPONENTIAL }
     ```
   - Constructor pattern per settare parametri (mean, k, p, ecc.)

4. **Statistiche Welford** (streaming mean/variance):
   - Sostituire somme ingenue con algoritmo numericamente stabile
   - Calcolare varianza campionaria per ogni metrica

**Output**:
- `MMMOneSimulator.java` (single-run funzionante)
- `SimulationConfig.java`
- Test: `MMMOneSimulatorTest.java` (validazione contro teoria M/M/1)

---

### **FASE 2.C — Metodo delle Repliche Indipendenti**

**Obiettivo**: Wrapper per eseguire N repliche e calcolare intervalli di confidenza.

**Azioni**:
1. **Creare `SimulationRunner.java`**:
   ```java
   public class SimulationRunner {
       public StatisticalResults runReplications(
           int numReplications, 
           long baseSeed, 
           SimulationConfig config
       ) {
           double[] meanResponseTimes = new double[numReplications];
           
           for (int i = 0; i < numReplications; i++) {
               long seed = baseSeed + i * 1000000; // seed spaziati
               MMMOneSimulator sim = new MMMOneSimulator(seed, config);
               sim.run();
               meanResponseTimes[i] = sim.getMeanResponseTime();
           }
           
           return computeConfidenceIntervals(meanResponseTimes);
       }
   }
   ```

2. **Creare `StatisticalResults.java`**:
   - Media delle medie
   - Deviazione standard campionaria
   - Intervallo di confidenza al 95% (t-Student)
   - Metodo `toString()` formattato

3. **Test di validazione**:
   - Eseguire 32 repliche di M/M/1 con ρ = 0.8
   - Verificare che E[T] teorico cada nell'IC al 95%

**Output**:
- `SimulationRunner.java`
- `StatisticalResults.java`
- Test: `ReplicationMethodTest.java`

---

### **FASE 2.D — Validazione Completa M/M/1**

**Obiettivo**: Dimostrare correttezza del simulatore contro teoria delle code.

**Azioni**:
1. **Implementare `TheoreticalFormulas.java`**:
   ```java
   public static double mmOneAvgResponseTime(double lambda, double mu) {
       double rho = lambda / mu;
       return 1.0 / (mu - lambda); // E[T] = 1/(μ-λ)
   }
   ```

2. **Esperimento sistematico**:
   - Variare ρ ∈ {0.5, 0.6, 0.7, 0.8, 0.9}
   - Per ogni ρ: 64 repliche, 10.000 job/replica
   - Confrontare teoria vs simulazione

3. **Generare tabella Markdown**:
   | ρ | E[T] Teorico | E[T] Simulato | IC 95% | Match |
   |---|--------------|---------------|--------|-------|
   | 0.5 | 2.0 | 1.98 | [1.95, 2.01] | ✅ |

**Output**:
- `TheoreticalFormulas.java`
- Script: `validation_mm1.sh`
- Report: `docs/step2-validazione.md`

---

### **FASE 2.E — Estensione a M/G/1 (Distribuzioni Generali)**

**Obiettivo**: Supportare tutte le 4 distribuzioni con validazione Pollaczek-Khinchine.

**Azioni**:
1. **Test con Erlang** (Cv < 1):
   - Servizi: Erlang(mean=1.0, k=4) → Cv = 0.5
   - Validare E[T] con formula P-K:
     $$E[T] = E[S] + \frac{\lambda E[S^2]}{2(1-\rho)}$$

2. **Test con Iperesponenziale** (Cv > 1):
   - Servizi: HyperExp(p=0.7, μ₁=0.5, μ₂=2.0)
   - Validare alta variabilità impatti pesantemente

3. **Test con Uniforme**:
   - Servizi: Uniform(0.5, 1.5) → mean=1.0, Cv < 1
   - Confrontare con teoria

**Output**:
- Test: `MGOneValidationTest.java`
- Report: aggiornamento `docs/step2-validazione.md`

---

## 📊 Deliverables Finali Step 2

### File da creare/modificare:

| Percorso | Stato | Descrizione |
|----------|-------|-------------|
| `src/main/java/sim/core/Event.java` | **[NEW]** | Evento con enum type + customer |
| `src/main/java/sim/core/EventList.java` | **[NEW]** | Splay Tree FEL |
| `src/main/java/sim/core/CustomerQueue.java` | **[NEW]** | FIFO circolare |
| `src/main/java/sim/MMMOneSimulator.java` | **[NEW]** | Simulatore M/M/1 base |
| `src/main/java/sim/SimulationConfig.java` | **[NEW]** | Configurazione parametri |
| `src/main/java/sim/SimulationRunner.java` | **[NEW]** | Runner per repliche |
| `src/main/java/sim/StatisticalResults.java` | **[NEW]** | IC + statistiche |
| `src/main/java/sim/TheoreticalFormulas.java` | **[NEW]** | Formule analitiche |
| `src/test/java/sim/core/EventListTest.java` | **[NEW]** | Test FEL |
| `src/test/java/sim/MMMOneSimulatorTest.java` | **[NEW]** | Test M/M/1 |
| `src/test/java/sim/ReplicationMethodTest.java` | **[NEW]** | Test repliche |
| `src/test/java/sim/MGOneValidationTest.java` | **[NEW]** | Test M/G/1 completo |
| `docs/step2.md` | **[NEW]** | Relazione matematica Step 2 |

### Relazione `step2.md` conterrà:

1. **Architettura Event-Driven**:
   - Diagramma UML classi core
   - Pseudocodice loop principale
   - Gestione FEL (Splay Tree vs heap)

2. **Formule Teoriche**:
   - M/M/1: E[N], E[T], E[W], ρ
   - M/G/1: Pollaczek-Khinchine
   - Varianza e IC t-Student

3. **Risultati Validazione**:
   - Tabelle ρ vs E[T] per 4 distribuzioni
   - Grafici: Teoria vs Simulazione (con barre errore IC)

4. **Analisi Coefficiente di Variazione**:
   - Impatto Cv su E[T] a parità di ρ
   - Confronto Erlang vs Expo vs HyperExp

---

## 🚧 Rischi e Mitigazioni

| Rischio | Probabilità | Impatto | Mitigazione |
|---------|-------------|---------|-------------|
| Splay Tree non necessario (overhead) | Media | Basso | Profiling; se lento, sostituire con `PriorityQueue` |
| Seed spacing inadeguato | Bassa | Alto | Usare offset >= 10⁶ tra repliche |
| Validazione fallisce per bug sottili | Media | Alto | TDD: scrivere test PRIMA del codice |
| Formule P-K sbagliate | Bassa | Medio | Doppio check con libro Leemis-Park |

---

## ⏱️ Stima Temporale

| Fase | Complessità | Tempo Stimato |
|------|-------------|---------------|
| 2.A — Strutture dati | Bassa (copia + refactor) | 1-2 ore |
| 2.B — Simulatore base | Media | 2-3 ore |
| 2.C — Repliche | Bassa | 1 ora |
| 2.D — Validazione M/M/1 | Media | 2 ore |
| 2.E — M/G/1 esteso | Media | 2 ore |
| **TOTALE** | | **8-10 ore** |

---

## 🎯 Criteri di Successo

Lo **Step 2 è completo** quando:

1. ✅ Tutte le classi `sim.core.*` hanno test JUnit5 che passano
2. ✅ `MMMOneSimulator` + `ServiceGenerator` **non usano mai** `java.util.Random`
3. ✅ Validazione M/M/1: IC al 95% contiene valore teorico per ρ ∈ [0.5, 0.9]
4. ✅ Validazione M/G/1: formula P-K verificata per tutte le 4 distribuzioni
5. ✅ `mvn clean test` esegue **almeno 15 test** (core + sim + validation) → **BUILD SUCCESS**
6. ✅ Relazione `docs/step2.md` contiene:
   - Formule matematiche complete
   - Tabelle risultati sperimentali
   - Confronto teoria vs simulazione
   - Grafico ρ vs E[T]

---

## 📝 Note Implementative

### Gestione Stream Rngs
```java
// SEMPRE separare stream per sorgenti diverse
rngs.selectStream(0); // Arrivi
double interarrival = rvgs.exponential(lambda);

rngs.selectStream(1); // Servizi
double service = serviceGen.erlang(mean, k);
```

### Seed per Repliche
```java
// Spacing 10^6 garantisce indipendenza statistica (Leemis p.512)
for (int i = 0; i < numReps; i++) {
    long seed = baseSeed + i * 1_000_000;
    // ...
}
```

### Welford Algorithm (varianza stabile)
```java
// Evita cancellazione catastrofica con grandi N
for (double x : samples) {
    count++;
    delta = x - mean;
    mean += delta / count;
    M2 += delta * (x - mean);
}
variance = M2 / (count - 1);
```

---

## 🔗 Riferimenti

- **Leemis-Park Cap. 2**: Random Number Generation (Rngs)
- **Leemis-Park Cap. 6**: Random Variate Generation (Rvgs)
- **Leemis-Park Cap. 8**: Next-Event Simulation
- **Consegna 2025-06**: Specifiche distribuzioni servizi
- **Simulatore proposto**: Pattern event-driven da libro

---

## ✅ Prossimi Step dopo Step 2

- **Step 3**: Sistema misto Interactive + Batch (priorità, classi multiple)
- **Step 4**: Rete di code (CPU + I/O, feedback loop)
- **Step 5**: Analisi prestazioni, grafici impatto carico Batch su Interactive

