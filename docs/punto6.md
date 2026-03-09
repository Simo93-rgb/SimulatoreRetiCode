# Punto 6 — Sistema Chiuso con Q0, Q1, Q2

## Obiettivo

Estendere il simulatore implementando un **sistema chiuso** con $N$ clienti che circolano continuamente tra tre centri, come da Figura 1 della consegna:

$$Q_0 \text{ (Delay)} \xrightarrow{p_1 = 0.3} Q_1 \text{ (Server FCFS)} \longrightarrow Q_0$$
$$Q_0 \text{ (Delay)} \xrightarrow{1 - p_1 = 0.3} Q_2 \text{ (Server FCFS)} \longrightarrow Q_0$$

Simulare il sistema per 4 valori di $N$ e calcolare throughput, utilizzi, tempi medi di risposta e lunghezze medie delle code di $Q_1$ e $Q_2$.

---

## Architettura del Sistema

### Centri

| Centro | Tipo | Distribuzione | Parametro |
|--------|------|--------------|-----------|
| Q0 | Delay station (∞ server) | Esponenziale | $Z = 10.0$ s (think time) |
| Q1 | Singolo server FCFS | Esponenziale | $S_1 = 1.0$ s |
| Q2 | Singolo server FCFS | Esponenziale | $S_2 = 0.8$ s |

**Collo di bottiglia**: $Q_2$ in termini assoluti, $D_1 = p_1 \cdot S_1 = 0.3 \times 1.0 = 0.3s$ e $D_2 = (1-p_1) \cdot S_2 = 0.7 \times 0.8 = 0.56s$. Il massimo è $D_{\max} = 0.56$ (relativo a $Q_2$).

**Throughput asintotico** (bound di saturazione dall'analisi operazionale):
$$X^*(N) = \min\!\left(\frac{1}{D_{\max}},\; \frac{N}{Z + D_1 + D_2}\right) = \min\!\left(\frac{1}{0.56},\; \frac{N}{10.0 + 0.3 + 0.56}\right) = \min\!\left(1.7857,\; \frac{N}{10.86}\right)$$

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
| `END_THINK_TIME` | Cliente termina think time in $Q_0$ | Arriva probabilisticamente a $Q_1$ o $Q_2$ (o code relative) |
| `DEPARTURE` *(da Q1)* | Cliente completa servizio in $Q_1$ | Torna a $Q_0$ (nuovo think time); libera $Q_1$ |
| `TIMEOUT` *(da Q2)* | Cliente completa servizio in $Q_2$ | Torna a $Q_0$ (nuovo think time); libera $Q_2$ |

> *Nota*: i tipi di evento `DEPARTURE` e `TIMEOUT` sono enum già presenti nell'`EventType` da punti precedenti, riusati con semantica specifica per i centri del sistema chiuso.

### Stream RNG

| Stream | Sorgente stocastica |
|--------|---------------------|
| `THINK_TIME` (2) | Think time in $Q_0$ |
| `SERVICE` (1) | Tempo di servizio in $Q_1$ |
| `ROUTING` (3) | Tempo di servizio in $Q_2$ |

### Accumulatori statistici

Per ogni centro di servizio $i \in \{1, 2\}$:

$$
\begin{aligned}
\\
&\bullet \ \text{areaServerBusy}_i = \int_0^T \mathbb{1}[\text{busy}_i(t)]\, dt \rightarrow \rho_i = \text{area}/T \\
&\bullet \ \text{areaQueueLength}_i = \int_0^T n_i^{(q)}(t)\, dt \rightarrow E[Nq_i] = \text{area}/T \\
&\bullet \ \text{areaSystemSize}_i = \int_0^T n_i(t)\, dt \rightarrow E[N_i] = \text{area}/T \\
&\bullet \ \text{sumResponseTime}_i = \sum_j (t^{\text{dep}}_{i,j} - t^{\text{arr}}_{i,j}) \implies E[T_i] = \frac{\text{sum}_i}{\text{completamenti}_i} \\
&\bullet \ \mathbf{T_{\text{sys}}} = \text{media pesata dei tempi di risposta in } Q_1 \text{ e } Q_2.
\end{aligned}
$$

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
scheduleEndThinkTime(customer)
inThinkQ0++
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
| 5  | 0.4604 | 0.4539 ∈ [0.4531, 0.4547] | 0.18% |
| 15 | 1.3812 | 1.2622 ∈ [1.2599, 1.2646] | 0.18% |
| 30 | 1.7857 | 1.7771 ∈ [1.7728, 1.7813] | 0.24% |
| 50 | 1.7857 | 1.7869 ∈ [1.7818, 1.7920] | 0.29% |

**Osservazione**: Per $N=5$ il sistema è in regime leggero ($X < X^*$). A partire da $N=30$ il throughput si avvicina al bound di saturazione di $Q_2$ (1.7857).

### Throughput dei Centri Q1 e Q2

| N | $X_1$ (IC 95%) | $X_2$ (IC 95%) |
|---|---|---|
| 5  | 0.1361 ∈ [0.1357, 0.1365] | 0.3178 ∈ [0.3171, 0.3186] |
| 15 | 0.3775 ∈ [0.3761, 0.3789] | 0.8847 ∈ [0.8827, 0.8867] |
| 30 | 0.5330 ∈ [0.5309, 0.5351] | 1.2440 ∈ [1.2410, 1.2470] |
| 50 | 0.5360 ∈ [0.5333, 0.5386] | 1.2509 ∈ [1.2474, 1.2545] |

**Osservazione**: $X_1$ coincide approssimativamente con $0.3X$ e $X_2$ con $0.7X$, come impostato dalla topologia probabilistica.

---

### Utilizzo dei Server

| N | $\rho_1$ Simulato (IC 95%) | $\rho_1$ Atteso $(X_1 \cdot S_1)$ | $\rho_2$ Simulato (IC 95%) | $\rho_2$ Atteso $(X_2 \cdot S_2)$ |
|---|---|---|---|---|
| 5  | 0.1359 ∈ [0.1352, 0.1366] | 0.1361 | 0.2542 ∈ [0.2535, 0.2549] | 0.2542 |
| 15 | 0.3771 ∈ [0.3750, 0.3791] | 0.3775 | 0.7069 ∈ [0.7052, 0.7086] | 0.7077 |
| 30 | 0.5325 ∈ [0.5292, 0.5357] | 0.5330 | 0.9942 ∈ [0.9939, 0.9946] | 0.9952 |
| 50 | 0.5354 ∈ [0.5317, 0.5391] | 0.5360 | 1.0000 ∈ [1.0000, 1.0000] | 1.0007 |

**Verifica legge di utilizzo** $\rho_i = X_i \cdot S_i$: i valori simulati coincidono con quelli attesi, confermando la correttezza degli accumulatori.

**Osservazione**: $Q_2$ in questo scenario satura raggiungendo utilizzo $\approx 1$, dato che $p_1 < 0.5$ invia molto più traffico al secondo nodo (con servizio $S_2 = 0.8$).

---

### Tempi Medi di Risposta

| N | $E[T_1]$ (IC 95%) | $E[T_2]$ (IC 95%) | $E[T_{\text{sys}}]$ (IC 95%) |
|---|---|---|---|
| 5  | 1.1172 ∈ [1.1118, 1.1225] | 0.9903 ∈ [0.9877, 0.9928] | 1.0283 ∈ [1.0262, 1.0304] |
| 15 | 1.5267 ∈ [1.5143, 1.5390] | 2.0538 ∈ [2.0396, 2.0681] | 1.8962 ∈ [1.8878, 1.9046] |
| 30 | 2.0999 ∈ [2.0787, 2.1211] | 8.9440 ∈ [8.8867, 9.0012] | 6.8912 ∈ [6.8513, 6.9310] |
| 50 | 2.1633 ∈ [2.1332, 2.1934] | 24.7570 ∈ [24.6373, 24.8768] | 17.9805 ∈ [17.9000, 18.0610] |

**Osservazione**: $E[T_2]$ cresce molto più rapidamente di $E[T_1]$ all'aumentare di $N$: la coda si accumula quasi esclusivamente in $Q_2$ (collo di bottiglia asintotico). $E[T_1]$ si stabilizza verso $\approx 2.1$ s (valore per $M/M/1$ con utilizzo limite $p_1 / D_{\max} \cdot S_1 \approx 0.53$).

---

### Lunghezze Medie delle Code

| N | $E[N_{q1}]$ (IC 95%) | $E[N_{q2}]$ (IC 95%) |
|---|---|---|
| 5  | 0.0161 ∈ [0.0158, 0.0164] | 0.0606 ∈ [0.0600, 0.0611] |
| 15 | 0.1993 ∈ [0.1950, 0.2036] | 1.1102 ∈ [1.0993, 1.1211] |
| 30 | 0.5870 ∈ [0.5758, 0.5981] | 10.1344 ∈ [10.0836, 10.1852] |
| 50 | 0.6244 ∈ [0.6070, 0.6417] | 29.9829 ∈ [29.9119, 30.0538] |

**Osservazione**: $E[N_{q2}]$ cresce linearmente con $N$ a saturazione (quasi tutti i clienti in eccesso si accodano in $Q_2$). $E[N_{q1}]$ si stabilizza come previsto dal fatto che il limite di $X_1$ è strettamente inferiore a $1/S_1$.

---

## Verifica delle Leggi Operative

### Legge di Little

Verifica per $N = 15$ ($X_1 = 0.3775, X_2 = 0.8847$):

| Centro | $E[N_i]$ (simulato) | $X_i \cdot E[T_i]$ (atteso) | Δ% |
|--------|---------------------|--------------------------|-----|
| Q1 | $E[N_1] = \rho_1 + E[N_{q1}] = 0.3771 + 0.1993 = 0.5764$ | $0.3775 \times 1.5267 = 0.5763$ | < 0.1% |
| Q2 | $E[N_2] = \rho_2 + E[N_{q2}] = 0.7069 + 1.1102 = 1.8171$ | $0.8847 \times 2.0538 = 1.8170$ | < 0.1% |

La legge di Little $E[N_i] = X_i \cdot E[T_i]$ è verificata con precisione < 0.1%, confermando la correttezza degli accumulatori.

---

## Identificazione dei Regimi di Carico

Dalla formula del throughput bound:
$$N^* = \frac{Z + D_1 + D_2}{D_{\max}} = \frac{10.0 + 0.3 + 0.56}{0.56} = \frac{10.86}{0.56} \approx 19.4$$

| Regime | Condizione | Valori sperimentati |
|--------|-----------|---------------------|
| **Leggero** | $N \ll N^*$ | $N = 5$ ($X = 0.45, X^* = 1.78$) |
| **Medio** | $N \approx N^*$ | $N = 15$ ($X = 1.26, X^* = 1.78$) |
| **Intenso** | $N > N^*$ | $N = 30$ ($X = 1.77, X^* = 1.78$) |
| **Saturazione** | $N \gg N^*$ | $N = 50$ ($X = 1.7869, X^* = 1.78$) |

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

