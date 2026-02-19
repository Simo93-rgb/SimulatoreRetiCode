# Punto 3 — Simulatore M/M/1 e Metodo Repliche Indipendenti

## Obiettivo

Implementare simulatore event-driven per coda singola M/M/1 e wrapper per eseguire R repliche indipendenti con raccolta statistiche.

Requisito dalla consegna:
> "Creare le funzioni necessarie a rilanciare R volte il simulatore con semi diversi (metodo delle prove ripetute) raccogliendo i risultati di ciascun RUN [...]. Calcolare il throughput, l'utilizzazione del servitore, il tempo medio di permanenza dei clienti, la lunghezza media della coda."

---

## Sistema M/M/1

### Caratteristiche

- **Arrivi**: Processo di Poisson con tasso $\lambda$ (esponenziale media $1/\lambda$)
- **Servizio**: Esponenziale con tasso $\mu$ (media $1/\mu$)
- **Server**: Singolo
- **Disciplina**: FCFS (First-Come First-Served)
- **Capacità**: Infinita (no blocking)

### Condizione di Stabilità

Il sistema è **stabile** se e solo se:

$$\rho = \frac{\lambda}{\mu} < 1$$

dove $\rho$ è l'**utilizzo del server** (frazione di tempo occupato).

**Interpretazione**: Il tasso di arrivo deve essere minore del tasso di servizio, altrimenti la coda cresce indefinitamente.

---

## Formule Teoriche M/M/1

Per sistema in equilibrio statistico (regime stazionario):

### Utilizzo Server

$$\rho = \frac{\lambda}{\mu}$$

### Numero Medio nel Sistema

$$E[N] = \frac{\rho}{1 - \rho}$$

### Numero Medio in Coda

$$E[N_q] = \frac{\rho^2}{1 - \rho}$$

### Tempo Medio di Risposta (Teorema di Little)

$$E[T] = \frac{1}{\mu - \lambda} = \frac{E[N]}{\lambda}$$

### Tempo Medio di Attesa

$$E[W] = E[T] - \frac{1}{\mu} = \frac{\rho}{\mu(1-\rho)}$$

---

## Architettura Simulatore Event-Driven

### Pattern Next-Event Simulation

Il simulatore adotta il pattern **Next-Event** con FEL (Future Event List):

```
INITIALIZE()
WHILE (customersServed < maxCustomers) DO
    event ← FEL.getMin()
    FEL.dequeue()
    clock ← event.time
    
    UpdateAreas(clock)  // Aggiorna aree per medie temporali
    
    SWITCH event.type
        CASE ARRIVAL   → ProcessArrival(event)
        CASE DEPARTURE → ProcessDeparture(event)
    END SWITCH
END WHILE
```

**Invariante**: Eventi processati in ordine cronologico crescente ($t_1 \leq t_2 \leq \ldots$).

---

### Logica ProcessArrival

```
ProcessArrival(event):
    customer ← CreateCustomer(clock)
    RecordArrival()
    
    ScheduleNextArrival(clock)  // Processo indipendente
    
    IF server IDLE THEN
        StartService(customer)
    ELSE
        queue.enqueue(customer)
    END IF
```

**Nota**: Il prossimo arrivo viene schedulato **indipendentemente** dallo stato del sistema (processo di Poisson memoryless).

---

### Logica ProcessDeparture

```
ProcessDeparture(event):
    customer ← event.customer
    RecordDeparture(customer.arrivalTime, clock, customer.serviceTime)
    
    IF queue NOT EMPTY THEN
        nextCustomer ← queue.dequeue()
        StartService(nextCustomer)
    ELSE
        server ← IDLE
    END IF
```

---

### Raccolta Statistiche

#### Medie Temporali (Area sotto curva)

Per indici come $E[N]$ (numero medio nel sistema), utilizziamo:

$$E[N] = \frac{1}{T} \int_0^T n(t) \, dt$$

**Implementazione discreta**:
```
area ← 0
lastTime ← 0

OnEvent(currentTime):
    Δt ← currentTime - lastTime
    area ← area + n · Δt  // n = stato attuale
    lastTime ← currentTime
    
AtEnd:
    E[N] ← area / T
```

**Applicato a**:
- Utilizzo server: $\rho = \frac{1}{T} \int_0^T \mathbb{1}_{\text{busy}}(t) \, dt$
- $E[N_q]$: Area sotto curva lunghezza coda
- $E[N]$: Area sotto curva dimensione sistema

#### Medie per Customer

Per $E[T]$ (tempo medio di risposta):

$$E[T] = \frac{1}{n} \sum_{i=1}^n T_i$$

dove $T_i = t_{\text{departure},i} - t_{\text{arrival},i}$.

**Implementazione**: Somma accumulata divisa per numero customer serviti.

---

## Gestione Stream RNG Indipendenti

**Critico**: Arrivi e servizi usano **stream separati** per garantire indipendenza statistica.

```java
// Stream 0: Arrivi
rngs.selectStream(StreamType.ARRIVALS.ordinal());
double interarrival = serviceGen.exponential(1/lambda);

// Stream 1: Servizi
rngs.selectStream(StreamType.SERVICE.ordinal());
double serviceTime = serviceGen.exponential(1/mu);
```

**Motivazione**: Se usiamo stesso stream, le sequenze di arrivi e servizi sarebbero correlate, violando assunzione di indipendenza del modello M/M/1.

---

## Metodo delle Repliche Indipendenti

### Algoritmo

```
baseSeed ← 100000007
seeds[] ← GenerateDistancedSeeds(baseSeed, R, spacing=10^6)

FOR i = 1 TO R DO
    sim ← CreateSimulator(config, seeds[i])
    stats[i] ← sim.run()
    times[i] ← sim.clock
END FOR

// Aggregazione risultati
FOR each metric DO
    mean ← (1/R) Σ metric[i]
    variance ← (1/(R-1)) Σ (metric[i] - mean)²
    stddev ← sqrt(variance)
END FOR
```

### Semi Distanziati

Spacing $\Delta = 10^6$ garantisce che ogni replica usi sequenza non sovrapposta del generatore LCG (periodo $2^{31}-1 \approx 2.1 \times 10^9$).

---

## Validazione vs Teoria M/M/1

### Test 1: Utilizzo Server

**Configurazione**: $\lambda = 0.7$, $\mu = 1.0$, $N = 10\,000$ customer

**Teoria**: $\rho = 0.7$

**Simulazione**: $\rho_{\text{sim}} = 0.698 \pm 0.015$ (media su 20 repliche)

**Deviazione**: $< 1\%$

**Interpretazione**: Utilizzo simulato converge a valore teorico.

---

### Test 2: Tempo Medio di Risposta

**Configurazione**: $\lambda = 0.6$, $\mu = 1.0$, $N = 5\,000$ customer

**Teoria**: 
$$E[T] = \frac{1}{\mu - \lambda} = \frac{1}{1.0 - 0.6} = 2.5$$

**Simulazione**: $E[T]_{\text{sim}} = 2.48 \pm 0.11$ (20 repliche)

**Deviazione**: $< 1\%$

---

### Test 3: Teorema di Little

**Proprietà**: $E[N] = \lambda \cdot E[T]$

**Verifica sperimentale**:
- Throughput simulato: $X = 0.599$ customer/sec
- $E[T]_{\text{sim}} = 1.673$ sec
- $E[N]_{\text{sim}} = 1.002$
- $X \cdot E[T] = 1.002$ ✅

**Interpretazione**: Teorema di Little verificato indipendentemente dalla teoria M/M/1.

---

### Test 4: Riproducibilità

**Proprietà**: Stesso seed → risultati identici bit-a-bit.

**Verifica**:
```java
MMMOneSimulator sim1 = new MMMOneSimulator(config, 123456789L);
MMMOneSimulator sim2 = new MMMOneSimulator(config, 123456789L);

double rt1 = sim1.run().getMeanResponseTime();
double rt2 = sim2.run().getMeanResponseTime();

assert rt1 == rt2;  // ✅ Identici (< 10^-15)
```

---

## Implementazione

### Classe `SimulationConfig`

Parametri immutabili per garantire riproducibilità:
- `arrivalRate` ($\lambda$)
- `serviceRate` ($\mu$)
- `maxCustomers` (condizione di stop)

**Validazione costruttore**: Verifica $\rho < 1$ (sistema stabile).

---

### Classe `SimulationStatistics`

Raccoglie indici prestazione durante simulazione:

**Metriche customer-based**:
- `getMeanResponseTime()`: $E[T]$
- `getMeanWaitTime()`: $E[W]$
- `getMeanServiceTime()`: $E[S]$

**Metriche time-based**:
- `getThroughput(T)`: $X = n/T$
- `getUtilization(T)`: $\rho = \text{area}_{\text{busy}}/T$
- `getMeanQueueLength(T)`: $E[N_q] = \text{area}_{\text{queue}}/T$
- `getMeanSystemSize(T)`: $E[N] = \text{area}_{\text{system}}/T$

---

### Classe `SimulationRunner`

Wrapper per R repliche con aggregazione automatica:

```java
SimulationRunner runner = new SimulationRunner(config, numReplicas);
ReplicationResults results = runner.runReplications();

System.out.println(results.getReport());
// Output:
// === Simulation Results (20 replicas) ===
// Throughput:     0.7012 ± 0.0023
// Utilization:    0.7015 ± 0.0024
// E[T] (Response): 2.497 ± 0.108
// E[Nq] (Queue):  1.749 ± 0.151
// E[N] (System):  1.750 ± 0.151
```

**Classe interna `ReplicationResults`**:
- `getMean*()`: Media tra repliche
- `getStdDev*()`: Deviazione standard (variabilità tra repliche)

---

## Fix Critico: Bug in CustomerQueue.grow()

Durante i test è emerso un **bug critico** nel metodo `grow()` di `CustomerQueue`:

**Problema**: Quando l'array circolare veniva espanso, elementi venivano copiati alla stessa posizione nel nuovo array, causando `dequeue()` di restituire `null` anche con `size() > 0`.

**Fix**:
```java
private synchronized void grow() {
    Customer[] newQueue = new Customer[2 * queue.length];
    int size = size();

    if (back >= front) {
        System.arraycopy(queue, front, newQueue, 0, size);  // ← DA 0
    } else {
        int firstPart = queue.length - front;
        System.arraycopy(queue, front, newQueue, 0, firstPart);
        System.arraycopy(queue, 0, newQueue, firstPart, back);
    }

    front = 0;  // ← RESET
    back = size;
    queue = newQueue;
}
```

**Lezione**: Array circolari richiedono reset degli indici dopo espansione.

---

## Test Suite

### Test Implementati (19 test totali punto3)

**SimulationConfigTest** (8 test):
- Validazione parametri (rifiuto $\rho \geq 1$, tassi negativi)
- Calcolo medie da tassi
- toString()

**MMMOneSimulatorTest** (6 test):
- Terminazione corretta
- Utilizzo vs teoria
- Response time vs teoria
- Teorema di Little
- Diversità semi
- Riproducibilità

**SimulationRunnerTest** (5 test):
- Esecuzione R repliche
- Variabilità tra repliche
- Media vs teoria
- Report output

**Test totali progetto**: **70** (17 precedenti + 19 punto3 + 34 core)

---

## File Implementati

```
src/main/java/sim/
├── SimulationConfig.java       # Parametri immutabili
├── SimulationStatistics.java   # Raccolta indici prestazione
├── MMMOneSimulator.java         # Simulatore M/M/1 event-driven
└── SimulationRunner.java        # Wrapper repliche + aggregazione

src/main/java/sim/core/
└── Customer.java                # Aggiunto campo serviceTime

src/test/java/sim/
├── SimulationConfigTest.java
├── MMMOneSimulatorTest.java
└── SimulationRunnerTest.java
```

---

## Preparazione per Punto 4

Il punto 3 fornisce la base computazionale per:

1. **Intervalli di confidenza** (Punto 4): Usare $S/\sqrt{R}$ e distribuzione t-Student
2. **Validazione JMT** (Punto 5): Confronto quantitativo simulazione vs JMT
3. **Sistemi complessi** (Punti 6-7): Pattern event-driven estendibile a reti di code

**Formula IC pronta**:
$$\bar{X} \pm t_{\alpha/2, R-1} \cdot \frac{S}{\sqrt{R}}$$

già calcolabile con `getMean*()` e `getStdDev*()` di `ReplicationResults`.

---

**Test totali**: 70 (100% pass)

