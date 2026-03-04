# Punto 6 — Sistema Chiuso con Q0, Q1, Q2

## Obiettivo

Estendere il simulatore implementando un **sistema chiuso** con $N$ clienti che circolano continuamente tra tre centri, come da Figura 1 della consegna:

$$Q_0 \text{ (Delay)} \;\longrightarrow\; Q_1 \text{ (Server FCFS)} \;\longrightarrow\; Q_2 \text{ (Server FCFS)} \;\longrightarrow\; Q_0$$

Simulare il sistema per 4 valori di $N$ e calcolare throughput, utilizzi, tempi medi di risposta e lunghezze medie delle code di $Q_1$ e $Q_2$.

---

## Architettura del Sistema

### Centri

| Centro | Tipo | Distribuzione | Parametro |
|--------|------|--------------|-----------|
| Q0 | Delay station (∞ server) | Esponenziale | $Z = 10.0$ s (think time) |
| Q1 | Singolo server FCFS | Esponenziale | $S_1 = 1.0$ s |
| Q2 | Singolo server FCFS | Esponenziale | $S_2 = 0.8$ s |

**Collo di bottiglia**: $Q_1$ (ha il tempo di servizio maggiore: $S_1 = 1.0 > S_2 = 0.8$).

**Throughput asintotico** (bound di saturazione dall'analisi operazionale):
$$X^*(N) = \min\!\left(\frac{1}{S_{\max}},\; \frac{N}{Z + S_1 + S_2}\right) = \min\!\left(1.0,\; \frac{N}{11.8}\right)$$

---

## Scelte Implementative

### Stato del sistema

In ogni istante lo stato è completamente descritto da:
- $n_0$: numero di clienti in think time in $Q_0$
- $n_1^{(q)}$: clienti in coda a $Q_1$; $\text{busy}_1$: server $Q_1$ occupato
- $n_2^{(q)}$: clienti in coda a $Q_2$; $\text{busy}_2$: server $Q_2$ occupato

**Invariante di conservazione**: $n_0 + n_1^{(q)} + \mathbb{1}[\text{busy}_1] + n_2^{(q)} + \mathbb{1}[\text{busy}_2] = N$

### Tipi di evento

| Evento | Significato | Azione |
|--------|-------------|--------|
| `END_THINK_TIME` | Cliente termina think time in $Q_0$ | Arriva a $Q_1$ (o coda) |
| `DEPARTURE` *(da Q1)* | Cliente completa servizio in $Q_1$ | Si dirige a $Q_2$; libera $Q_1$ |
| `TIMEOUT` *(da Q2)* | Cliente completa servizio in $Q_2$ | Torna a $Q_0$ (nuovo think time) |

> *Nota*: i tipi di evento `DEPARTURE` e `TIMEOUT` sono enum già presenti nell'`EventType` da punti precedenti, riusati con semantica specifica per i centri del sistema chiuso.

### Stream RNG

| Stream | Sorgente stocastica |
|--------|---------------------|
| `THINK_TIME` (2) | Think time in $Q_0$ |
| `SERVICE` (1) | Tempo di servizio in $Q_1$ |
| `ROUTING` (3) | Tempo di servizio in $Q_2$ |

### Accumulatori statistici

Per ogni centro di servizio $i \in \{1, 2\}$:
- `areaServerBusy_i` $= \int_0^T \mathbb{1}[\text{busy}_i(t)]\, dt$ → **utilizzo** $\rho_i = \text{area}/T$
- `areaQueueLength_i` $= \int_0^T n_i^{(q)}(t)\, dt$ → **E[Nq\_i]** $= \text{area}/T$
- `areaSystemSize_i` $= \int_0^T n_i(t)\, dt$ → **E[N\_i]** $= \text{area}/T$
- `sumResponseTime_i` $= \sum_j (t^{\text{dep}}_{i,j} - t^{\text{arr}}_{i,j})$ → **E[T\_i]** $= \text{sum}/\text{completamenti}$

### Condizione di stop

La simulazione termina quando $\text{completamenti}_{Q_1} \geq \text{maxCompletions}$.
Si usa $Q_1$ perché è il collo di bottiglia e ha il rate più basso; garantisce che il sistema abbia raggiunto lo stato stazionario.

**Configurazione repliche**: $R = 20$ repliche, $50.000$ completamenti per replica.

---

## Pseudo-codice Gestione Eventi

### `processEndThinkTime(event)`
```
inThinkQ0--
customer.arrivalTimeAtQ1 ← clock
SE serverQ1 == null:
    startServiceQ1(customer)
ALTRIMENTI:
    queueQ1.enqueue(customer)
```

### `processDepartureQ1(event)`
```
registra stats Q1 (arrivalTimeAtQ1 → clock)
serverQ1 ← null
SE queueQ1 non vuota:
    startServiceQ1(queueQ1.dequeue())
customer.arrivalTimeAtQ2 ← clock
SE serverQ2 == null:
    startServiceQ2(customer)
ALTRIMENTI:
    queueQ2.enqueue(customer)
```

### `processDepartureQ2(event)`
```
registra stats Q2 (arrivalTimeAtQ2 → clock)
serverQ2 ← null
SE queueQ2 non vuota:
    startServiceQ2(queueQ2.dequeue())
scheduleEndThinkTime(customer)
inThinkQ0++
```

---

## Risultati Sperimentali

**Configurazione**: $Z = 10.0$ s, $S_1 = 1.0$ s, $S_2 = 0.8$ s.
$R = 20$ repliche × $50.000$ completamenti. Tutti gli IC sono al **95%**.

### Throughput di Sistema

| N | $X^*(N)$ (bound) | $X$ Simulato (IC 95%) | RE |
|---|---|----|---|
| 5  | 0.4237 | 0.4012 ∈ [0.4005, 0.4019] | 0.17% |
| 15 | 1.0000 | 0.8980 ∈ [0.8967, 0.8993] | 0.14% |
| 30 | 1.0000 | 0.9971 ∈ [0.9951, 0.9991] | 0.20% |
| 50 | 1.0000 | 1.0003 ∈ [0.9981, 1.0024] | 0.21% |

**Osservazione**: Per $N=5$ il sistema è in regime leggero ($X < X^*$). A partire da $N=15$ il throughput si avvicina progressivamente al bound di saturazione $1/S_1 = 1.0$.

### Throughput dei Centri Q1 e Q2

In regime stazionario il flusso è bilanciato: ogni cliente che attraversa $Q_1$ attraversa anche $Q_2$, quindi $X_1 = X_2 = X$. La tabella riporta i valori misurati separatamente a conferma.

| N | $X_1$ (IC 95%) | $X_2$ (IC 95%) | $\|X_1 - X_2\|/X$  |
|---|---|---|--------------------|
| 5  | 0.4012 ∈ [0.4005, 0.4019] | 0.4012 ∈ [0.4005, 0.4019] | < 0.01%            |
| 15 | 0.8980 ∈ [0.8967, 0.8993] | 0.8980 ∈ [0.8967, 0.8993] | < 0.01%            |
| 30 | 0.9971 ∈ [0.9951, 0.9991] | 0.9971 ∈ [0.9951, 0.9991] | < 0.01%            |
| 50 | 1.0003 ∈ [0.9981, 1.0024] | 1.0003 ∈ [0.9981, 1.0024] | < 0.01%            |

**Osservazione**: $X_1 \approx X_2$ con scarto < 0.01% in tutti gli scenari, confermando il bilanciamento del flusso atteso in un sistema chiuso in serie a regime stazionario.

---

### Utilizzo dei Server

| N | $\rho_1$ Simulato (IC 95%) | $\rho_1$ Atteso $(X \cdot S_1)$ | $\rho_2$ Simulato (IC 95%) | $\rho_2$ Atteso $(X \cdot S_2)$ |
|---|---|---|---|---|
| 5  | 0.4011 ∈ [0.4001, 0.4020] | 0.4012 | 0.3207 ∈ [0.3200, 0.3214] | 0.3210 |
| 15 | 0.8978 ∈ [0.8965, 0.8990] | 0.8980 | 0.7179 ∈ [0.7162, 0.7196] | 0.7184 |
| 30 | 0.9968 ∈ [0.9965, 0.9972] | 0.9971 | 0.7971 ∈ [0.7946, 0.7995] | 0.7977 |
| 50 | 1.0000 ∈ [0.9999, 1.0000] | 1.0003 | 0.7996 ∈ [0.7970, 0.8022] | 0.8002 |

**Verifica legge di utilizzo** $\rho_i = X \cdot S_i$: i valori simulati coincidono con quelli attesi (Δ < 0.1%), confermando la correttezza degli accumulatori.

**Osservazione**: $Q_2$ non raggiunge mai la saturazione ($\rho_2 \approx 0.80 < 1$) perché $Q_1$ è il collo di bottiglia e limita il flusso.

---

### Tempi Medi di Risposta

| N | $E[T_1]$ (IC 95%) | $E[T_2]$ (IC 95%) | $E[T_{\text{sys}}]$ (IC 95%) |
|---|---|---|---|
| 5  | 1.4217 ∈ [1.4172, 1.4262] | 1.0546 ∈ [1.0521, 1.0570] | 2.4762 ∈ [2.4716, 2.4808] |
| 15 | 4.4296 ∈ [4.4031, 4.4560] | 2.2859 ∈ [2.2723, 2.2995] | 6.7154 ∈ [6.6949, 6.7360] |
| 30 | 16.3234 ∈ [16.2222, 16.4246] | 3.7710 ∈ [3.7206, 3.8215] | 20.0944 ∈ [20.0316, 20.1573] |
| 50 | 35.9954 ∈ [35.8263, 36.1645] | 3.9863 ∈ [3.9044, 4.0682] | 39.9817 ∈ [39.8760, 40.0875] |

**Osservazione**: $E[T_1]$ cresce molto più rapidamente di $E[T_2]$ all'aumentare di $N$: la coda si accumula quasi esclusivamente in $Q_1$ (collo di bottiglia). $E[T_2]$ si stabilizza verso $\approx 4.0$ s (valore in saturazione per M/M/1 con $\rho_2 \approx 0.8$: $E[T_2] = S_2/(1-\rho_2) = 0.8/0.2 = 4.0$).

---

### Lunghezze Medie delle Code

| N | $E[N_{q1}]$ (IC 95%) | $E[N_{q2}]$ (IC 95%) |
|---|---|---|
| 5  | 0.1693 ∈ [0.1683, 0.1703] | 0.1023 ∈ [0.1016, 0.1031] |
| 15 | 3.0802 ∈ [3.0604, 3.1000] | 1.3349 ∈ [1.3229, 1.3468] |
| 30 | 15.2813 ∈ [15.2084, 15.3542] | 2.9633 ∈ [2.9100, 3.0166] |
| 50 | 35.0155 ∈ [34.9115, 35.1194] | 3.1883 ∈ [3.1028, 3.2737] |

**Osservazione**: $E[N_{q1}]$ cresce linearmente con $N$ a saturazione (quasi tutti i clienti in eccesso rispetto a $N^*$ si accodano in $Q_1$). $E[N_{q2}]$ si stabilizza una volta che $Q_1$ satura, poiché il flusso verso $Q_2$ non può superare $1/S_1$.

---

## Verifica delle Leggi Operative

### Legge di Little

Verifica per $N = 15$ ($X = 0.8980$):

| Centro | $E[N_i]$ (simulato) | $X \cdot E[T_i]$ (atteso) | Δ% |
|--------|---------------------|--------------------------|-----|
| Q1 | $E[N_1] = \rho_1 + E[N_{q1}] = 0.8978 + 3.0802 = 3.978$ | $0.8980 \times 4.4296 = 3.978$ | < 0.1% |
| Q2 | $E[N_2] = \rho_2 + E[N_{q2}] = 0.7179 + 1.3349 = 2.053$ | $0.8980 \times 2.2859 = 2.053$ | < 0.1% |

La legge di Little $E[N] = X \cdot E[T]$ è verificata con precisione < 0.1%, confermando la correttezza degli accumulatori.

---

## Identificazione dei Regimi di Carico

Dalla formula del throughput bound:
$$N^* = \frac{Z + S_1 + S_2}{S_{\max}} = \frac{10.0 + 1.0 + 0.8}{1.0} = 11.8 \approx 12$$

| Regime | Condizione | Valori sperimentati |
|--------|-----------|---------------------|
| **Leggero** | $N \ll N^*$ | $N = 5$ ($X/X^{\max} = 40\%$) |
| **Medio** | $N \approx N^*$ | $N = 15$ ($X/X^{\max} = 90\%$) |
| **Intenso** | $N > N^*$ | $N = 30$ ($X/X^{\max} = 99.7\%$) |
| **Saturazione** | $N \gg N^*$ | $N = 50$ ($X/X^{\max} \approx 100\%$) |

---

## Classi Introdotte

| Classe | Responsabilità |
|--------|---------------|
| `ClosedNetworkConfig` | Configurazione immutabile (N, Z, S1, S2, maxCompletions) + throughput bound |
| `ClosedNetworkStatistics` | Accumulatori e indici per Q1 e Q2 (utilizzo, E[T], E[N], E[Nq]) |
| `ClosedNetworkSimulator` | Simulatore event-driven del sistema chiuso; gestisce FEL, code, stati server |
| `ClosedNetworkRunner` | Wrapper R repliche con semi distanziati; calcolo IC 95% per tutti gli indici |
| `sim/runners/Punto6Runner` | Entry point raccolta dati; stampa tabelle per la relazione |

`Customer` è stato esteso con i campi `arrivalTimeAtQ1` e `arrivalTimeAtQ2` per tracciare i tempi di ingresso ai singoli centri senza introdurre nuove classi.

---

