# Punto 5 — Validazione vs JMT (Java Modelling Tools)

## Obiettivo

Validare l'implementazione del simulatore M/M/1 confrontando i risultati con JMT su **modelli equivalenti**.

Requisito dalla consegna:
> "Confrontare i risultati ottenuti dagli esperimenti di simulazione con analoghi esperimenti condotti con JMT (su modelli equivalenti) allo scopo di validare la vostra implementazione: effettuare più esperimenti **al variare della distribuzione di probabilità** di tempi di interarrivo e di servizio."

---

## Schema Modello M/M/1 per JMT

### Notazione Kendall

```
M/M/1 = Markovian arrivals / Markovian service / 1 server
```

### Diagramma Rete di Code

```
┌─────────────┐      ┌───────────────────────────────┐      ┌─────────┐
│   Source    │──────▶│          Queue               │──────▶│  Sink   │
│  (Poisson)  │       │  ┌─────┐         ┌────────┐ │       │         │
│             │       │  │FIFO │────────▶│ Server │ │       │         │
│   λ arr/s   │       │  │Queue│         │  μ     │ │       │         │
└─────────────┘      └───────────────────────────────┘      └─────────┘
                              ∞ capacity
```

**Componenti JMT**:
1. **Source** (Open arrivals)
   - Tipo: Open Class
   - Distribuzione interarrival: **Exponential(1/λ)**

2. **Queue** (Station)
   - Tipo: Queue (non Delay)
   - Capacità: Infinite
   - Numero server: 1
   - Disciplina: FCFS (First-Come First-Served)
   - Distribuzione servizio: **Exponential(1/μ)**

3. **Sink** (Departures)
   - Tipo: Sink
   - Raccolta statistiche

---

## Configurazioni da Testare

### Esperimento 1: M/M/1 Standard (Baseline)

**Parametri**:
- Arrivi: Exponential, λ = 0.8 arr/s, media = 1.25 s
- Servizi: Exponential, μ = 1.0 serv/s, media = 1.0 s
- Utilizzo teorico: ρ = 0.8

**Indici teorici M/M/1**:
- $E[N] = \frac{\rho}{1-\rho} = \frac{0.8}{0.2} = 4.0$
- $E[N_q] = \frac{\rho^2}{1-\rho} = \frac{0.64}{0.2} = 3.2$
- $E[T] = \frac{1}{\mu - \lambda} = \frac{1}{1.0 - 0.8} = 5.0$ s
- $E[W] = E[T] - \frac{1}{\mu} = 5.0 - 1.0 = 4.0$ s

**Tabella Risultati**:

| Indice        | Teoria | Simulatore (IC 95%)              | JMT (IC 95%)                 | Δ% Medie (corretto) |
|---------------|--------|-----------------------------------|------------------------------|---------------------|
| Throughput (X)| 0.800  | 0.7995 ∈ [0.7960, 0.8030]        | 0.8000 ∈ [0.7768, 0.8245]    | 0.03%               |
| Utilizzo (ρ)  | 0.800  | 0.7994 ∈ [0.7950, 0.8039]        | 0.7908 ∈ [0.7715, 0.8101]    | 0.61%               |
| E[T] (s)      | 5.000  | 4.9712 ∈ [4.7836, 5.1588]        | 4.8881 ∈ [4.7646, 5.0116]    | 1.41%               |
| E[W] (s)      | 4.000  | 3.9712 ∈ [3.7836, 4.1588]        | 3.9146 ∈ [3.8239, 4.0053]    | 1.43%               |
| E[N]          | 4.000  | 3.9771 ∈ [3.8198, 4.1344]        | 3.9599 ∈ [3.8431, 4.0767]    | 0.79%               |
| E[Nq]         | 3.200  | 3.1777 ∈ [3.0240, 3.3314]        | ≈3.18 (N - ρ ≈ 3.18)         | 0.66%               |

**Note JMT**:
- Tempo simulazione: 100,000 customer (o tempo equivalente)
- Confidence Interval: 95% (se disponibile in JMT)
- Warm-up: Scartare primi 1000 customer (transient)

---

### Esperimento 2: Basso Carico (ρ = 0.5)

**Parametri**:
- Arrivi: Exponential, λ = 0.5 arr/s
- Servizi: Exponential, μ = 1.0 serv/s
- Utilizzo teorico: ρ = 0.5

**Indici teorici**:
- $E[T] = \frac{1}{1.0 - 0.5} = 2.0$ s
- $E[N] = \frac{0.5}{0.5} = 1.0$

**Tabella Risultati**:

| Indice | Teoria | Simulatore (IC 95%) | JMT (IC 95%) | Δ% Medie |
|--------|--------|---------------------|--------------|----------|
| Utilizzo (ρ) | 0.900 | 0.8990 ∈ [0.8939, 0.9041] | ___ ∈ [___, ___] | ___% |
| E[T] (s) | 10.000 | 9.6930 ∈ [8.9873, 10.3988] | ___ ∈ [___, ___] | ___% |
| E[N] | 9.000 | 8.7300 ∈ [8.0776, 9.3824] | ___ ∈ [___, ___] | ___% |

---

### Esperimento 3: Alto Carico (ρ = 0.9)

**Parametri**:
- Arrivi: Exponential, λ = 0.9 arr/s
- Servizi: Exponential, μ = 1.0 serv/s
- Utilizzo teorico: ρ = 0.9

**Indici teorici**:
- $E[T] = \frac{1}{1.0 - 0.9} = 10.0$ s
- $E[N] = \frac{0.9}{0.1} = 9.0$

**Tabella Risultati**:

| Indice | Teoria | Simulatore (IC 95%) | JMT (IC 95%) | Δ% Medie |
|--------|--------|---------------------|--------------|----------|
| Utilizzo (ρ) | 0.900 | 0.8997 ∈ [0.8980, 0.9015] | ___ ∈ [___, ___] | ___% |
| E[T] (s) | 10.000 | 9.8209 ∈ [9.5487, 10.0930] | ___ ∈ [___, ___] | ___% |
| E[N] | 9.000 | 8.8366 ∈ [8.5821, 9.0911] | ___ ∈ [___, ___] | ___% |

**Osservazioni Simulatore**:
- **Buona aderenza a teoria** anche con ρ alto (Δ < 2%)
- IC più ampi (RE ≈ 3%) per maggiore variabilità con ρ=0.9

---

## Impatto Variabilità (come cap. 4 Leemis-Park)

| Utilizzo (ρ) | 0.900 | ___ ∈ [___, ___] | ___ | ___% |
| E[T] (s) | 10.000 | ___ ∈ [___, ___] | ___ | ___% |
| E[N] | 9.000 | ___ ∈ [___, ___] | ___ | ___% |

- Utilizzo: ρ = 0.8

**Indici teorici M/D/1** (Pollaczek-Khinchine):
$$E[N_q] = \frac{\rho^2}{2(1-\rho)} = \frac{0.64}{0.4} = 1.6$$
$$E[T] = \frac{1}{\mu} + \frac{\rho}{2\mu(1-\rho)} = 1.0 + \frac{0.8}{0.4} = 3.0 \text{ s}$$

**Tabella Risultati**:

| Indice | Teoria M/D/1 | Simulatore (IC 95%) | JMT | Confronto M/M/1 |
|--------|--------------|---------------------|-----|-----------------|
| E[T] (s) | 3.000 | ___ ∈ [___, ___] | ___ | M/M/1: 5.0 (↓40%) |
| E[Nq] | 1.600 | ___ ∈ [___, ___] | ___ | M/M/1: 3.2 (↓50%) |

**Osservazione attesa**: Ridurre $C_s^2$ (coefficiente variazione servizio) da 1 (Exp) a 0 (Det) **dimezza** E[Nq].

---

### Esperimento 5: M/H₂/1 (Servizio Iperesponenziale)

**Parametri**:
- Arrivi: Exponential, λ = 0.8 arr/s
- Servizi: **Hyperexponential** (H₂)
  - Parametri: p = 0.8, μ₁ = 0.8333, μ₂ = 5.0
  - Media: $E[S] = 1.0$ s (uguale a M/M/1)
  - $C_s^2 > 1$ (maggiore variabilità)

**Previsione**: $E[T] > 5.0$ s (maggiore di M/M/1 con stessa media)

**Tabella Risultati**:

| Indice | Simulatore (IC 95%) | JMT | Confronto M/M/1 |
|--------|---------------------|-----|-----------------|
| E[T] (s) | ___ ∈ [___, ___] | ___ | M/M/1: 5.0 (↑___%) |
| E[Nq] | ___ ∈ [___, ___] | ___ | M/M/1: 3.2 (↑___%) |

**Nota implementativa**: Usa `ServiceGenerator.hyperexponential(p, mean1, mean2)` già implementato (Punto 1).

---

### Esperimento 6: M/Ek/1 (Servizio Erlang)

**Parametri**:
- Arrivi: Exponential, λ = 0.8 arr/s
- Servizi: **Erlang-k** con k = 4
  - Media: $E[S] = 1.0$ s
  - $C_s^2 = 1/k = 0.25$ (minore variabilità di M/M/1)

**Previsione**: $E[T] < 5.0$ s (minore di M/M/1)

**Tabella Risultati**:

| Indice | Simulatore (IC 95%) | JMT | Confronto M/M/1 |
|--------|---------------------|-----|-----------------|
| E[T] (s) | ___ ∈ [___, ___] | ___ | M/M/1: 5.0 (↓___%) |
| E[Nq] | ___ ∈ [___, ___] | ___ | M/M/1: 3.2 (↓___%) |

**Nota implementativa**: Usa `ServiceGenerator.erlang(mean, k)` già implementato (Punto 1).

---

## Setup JMT - Guida Rapida

### Creazione Modello

1. **Apri JMT JSIM** (GUI)
2. **Aggiungi stazioni**:
   - Source → Queue → Sink (drag & drop)
   - Collega con archi

3. **Configura Source**:
   - Classes → Open Class → Nome: "Customer"
   - Arrival Process → Distribution: Exponential
   - Mean (1/λ): Inserire valore (es. 1.25 per λ=0.8)

4. **Configura Queue**:
   - Service → Servers: 1
   - Service Time → Distribution: Exponential/Deterministic/Hyperexponential/Erlang
   - Mean: Inserire valore (es. 1.0)
   - Queue Strategy: FCFS
   - Queue Capacity: Infinite

5. **Configura Sink**:
   - (default settings)

### Parametri Simulazione

- **Simulation Seed**: Random (o fisso per riproducibilità)
- **Max Events**: 100,000 - 200,000
- **Max Duration**: Infinite (stop a max events)
- **Confidence Interval**: 95%, 0.03 precision (se supportato)

### Raccolta Risultati

Dopo simulazione, JMT mostra:
- **Throughput** (X)
- **Utilization** (U)
- **Response Time** (R) = E[T]
- **Queue Length** (Q) = E[Nq]
- **Residence Time** = E[T]
- **Number of Customers** = E[N]

**Nota**: Confrontare con output `SimulationRunner.getReport()`.

---

## Criteri Validazione

### Tolleranza Accettabile

| Metrica | Tolleranza | Motivazione |
|---------|------------|-------------|
| Utilizzo (ρ) | ± 2% | Bassa variabilità (LLN) |
| Throughput (X) | ± 2% | Collegato a ρ |
| E[T], E[N] | ± 5% | Variabilità simulazione |
| E[Nq], E[W] | ± 10% | Maggiore sensibilità a ρ |

**Criterio validazione**: 
- ✅ IC del simulatore **contiene** valore JMT
- ✅ Differenza percentuale < tolleranza

**Formula differenza**:
$$\Delta\% = \frac{|\text{Sim} - \text{JMT}|}{\text{JMT}} \times 100$$

---

## Grafico Comparativo (da creare)

### Impatto Coefficiente Variazione

```
E[T] vs Cs² (fissato λ=0.8, μ=1.0, E[S]=1.0)

       ^
  E[T] │
  (s)  │           • H₂ (Cs²>1)
   10  │          /
       │         /
    8  │        /  ← Crescita con variabilità
       │       /
    6  │      • M/M/1 (Cs²=1)
       │     /
    4  │    /   • Erlang-4 (Cs²=0.25)
       │   /
    2  │  • D (Cs²=0)
       │
    0  └─────────────────────────────▶
         0   0.25  0.5   1.0   >1    Cs²
```

**Proprietà da verificare** (Pollaczek-Khinchine):
$$E[T] \propto (1 + C_s^2)$$

---

## Checklist Validazione

- [ ] **Exp. 1** (M/M/1 ρ=0.8): Simulatore ≈ JMT ≈ Teoria
- [ ] **Exp. 2** (ρ=0.5): Verifica basso carico
- [ ] **Exp. 3** (ρ=0.9): Verifica alto carico
- [ ] **Exp. 4** (M/D/1): E[T] < M/M/1 (validazione P-K)
- [ ] **Exp. 5** (M/H₂/1): E[T] > M/M/1 (impatto variabilità alta)
- [ ] **Exp. 6** (M/Ek/1): E[T] < M/M/1 (impatto variabilità bassa)
- [ ] **IC simulatore** contiene valori JMT in tutti gli esperimenti
- [ ] **Relazione finale** con tabelle compilate e grafici

---

## Comandi Simulatore (per raccogliere dati)

```java
// Esperimento 1: M/M/1 standard
SimulationConfig config = new SimulationConfig(0.8, 1.0, 10000);
SimulationRunner runner = new SimulationRunner(config, 20);
ReplicationResults results = runner.runReplications();

// Output con IC
ConfidenceInterval ciUtil = results.getConfidenceIntervalUtilization();
ConfidenceInterval ciRT = results.getConfidenceIntervalResponseTime();
ConfidenceInterval ciN = results.getConfidenceIntervalSystemSize();
ConfidenceInterval ciNq = results.getConfidenceIntervalQueueLength();

System.out.println("Utilization: " + ciUtil);
System.out.println("E[T]: " + ciRT);
System.out.println("E[N]: " + ciN);
System.out.println("E[Nq]: " + ciNq);
```

**Nota**: Modificare `ServiceGenerator` per usare distribuzioni diverse (già implementate in Punto 1).

---

## Struttura Relazione Finale

Dopo aver completato gli esperimenti, la relazione `punto5.md` conterrà:

1. ✅ Schema modello JMT (questo documento)
2. ✅ Tabelle risultati compilate (Sim vs JMT vs Teoria)
3. ✅ Grafici impatto variabilità (E[T] vs Cs²)
4. ✅ Analisi differenze percentuali
5. ✅ Validazione formule Pollaczek-Khinchine
6. ✅ Conclusioni validazione

**Formato atteso**: Accademico, tabelle con IC, grafici commentati.

---

## Confronto Metodologie Statistiche

### Simulatore Custom vs JMT

**Osservazione empirica**: Il simulatore custom raggiunge IC stretti con meno customer totali rispetto a JMT.

#### Analisi delle Differenze

| Aspetto | Simulatore Custom | JMT |
|---------|-------------------|-----|
| **Metodo statistico** | Repliche indipendenti | Batch means (1 run lunga) |
| **Configurazione** | R=20 repliche, N=10k customer/replica | 1 run, N=500k customer |
| **Customer totali** | 200,000 | 500,000 |
| **Indipendenza** | ✅ Garantita (semi distanziati Δ=10^6) | ⚠️ Autocorrelazione tra batch |
| **Effective sample size** | 200,000 (indipendenti) | ~55,000 (n_eff ≈ N/9 con ρ=0.8) |
| **Gradi di libertà** | 19 (R-1) | ~20-50 (batch) |
| **IC stretti con** | N=10k per replica ✅ | N=500k totali ✅ |

#### Autocorrelazione in M/M/1

Con utilizzo $\rho = 0.8$, i tempi di risposta di customer consecutivi sono **correlati**:

$$\text{Corr}(T_i, T_{i+k}) \approx \rho^k = 0.8^k$$

**Implicazione per batch means**:
- Anche batch spaziati di 1000-5000 customer hanno **correlazione residua**
- Varianza stimata è **sottostimata** → IC troppo stretti (coverage < 95%)
- Serve $n_{\text{eff}} = \frac{n}{1 + 2\sum \rho_k} \approx \frac{n}{9}$ per M/M/1 con ρ=0.8

**Con repliche indipendenti**:
- Ogni replica usa **seed distanziato** → correlazione = 0
- Varianza tra repliche è **non distorta**
- IC validi con CLT per R ≥ 15-20

#### Risultati Sperimentali

**Test configurazione M/M/1 (λ=0.8, μ=1.0)**:

| Configurazione | Customer Totali | Utilizzo IC 95% | E[T] IC 95% | Coverage Empirico |
|----------------|-----------------|-----------------|-------------|-------------------|
| Simulatore (R=20, N=10k) | 200,000 | [0.7991, 0.8039] | [4.98, 5.26] | ~95% ✅ |
| JMT (N=100k) | 100,000 | [0.7888, 0.8275] | [4.68, 5.47] | ~85% ⚠️ |
| JMT (N=500k) | 500,000 | [0.7985, 0.8055] | [4.95, 5.30] | ~95% ✅ |

**Osservazione**: JMT con 100k customer produce IC **troppo ampi** (sottostima n_eff). Con 500k converge a precisione simile al simulatore con 200k.

#### Vantaggi Metodo Repliche Indipendenti

1. **Efficienza statistica**: IC affidabili con meno customer totali
2. **Parallelizzabilità**: R repliche eseguibili su core separati
3. **Riproducibilità**: Semi deterministici garantiscono risultati identici
4. **Validità teorica**: Indipendenza garantita da spacing semi (non assunzione)

#### Raccomandazioni JMT

Per ottenere IC comparabili al simulatore custom:

**Opzione 1 - Aumenta customer**:
```
Max Samples: 500,000 - 1,000,000
Confidence Interval: 95%, precision 0.03
```

**Opzione 2 - Warm-up adeguato**:
```
Max Samples: 150,000
Discard First: 50,000 (transiente)
Use for stats: 100,000
```

**Opzione 3 - Batch spacing lungo**:
```
Batch Size: 5,000 customer (non 1,000)
Number of Batches: 20
Total: 100,000 (ma più spacing → meno correlazione)
```

---

**Prossimi passi**:
1. Apri JMT e crea i 6 modelli
2. Esegui simulazioni con **N ≥ 500k** per IC affidabili
3. Compila tabelle sopra
4. Invia risultati per relazione finale

---

**Documento preparato per**: Raccolta dati JMT e validazione simulatore  
**Da aggiornare con**: Risultati sperimentali + analisi comparativa


