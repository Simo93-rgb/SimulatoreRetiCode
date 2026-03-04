# Punto 7 — Sistema Misto: Classe Chiusa + Classe Aperta

## Obiettivo

Estendere il sistema chiuso del punto 6 aggiungendo una seconda classe di clienti **aperta** che si sovrappone al flusso esistente, servita esclusivamente da Q1 con tempo di servizio doppio rispetto alla classe chiusa.

---

## Architettura del Sistema

### Topologia

```
        [Classe aperta: arrivi iperesponenziali]
                         │
   ┌─────────────────────▼───────────────────────┐
   │                     │                       │
   ▼                     ▼                       │
  Q0 (Delay) ──► Q1 (Server FCFS, 2 classi) ──► Q2 (Server FCFS)
                         │
                  [Classe aperta: esce]
```

### Parametri fissi (N=15, stessi del Punto 6)

| Centro | Tipo | Distribuzione | Parametro |
|--------|------|--------------|-----------|
| Q0 | Delay station | Esponenziale | Z = 10.0 s |
| Q1 (classe chiusa) | Singolo server FCFS | Esponenziale | S₁ = 1.0 s |
| Q1 (classe aperta) | Singolo server FCFS | Esponenziale | S₁_open = **2·S₁ = 2.0 s** |
| Q2 | Singolo server FCFS | Esponenziale | S₂ = 0.8 s |

### Classe aperta

- **Inter-arrivi**: iperesponenziale con $p = 0.5$, $\mu_1 = 0.5 \cdot E[A]$, $\mu_2 = 1.5 \cdot E[A]$
- **Routing**: Q1 esclusivamente → dopo il servizio il cliente **esce dal sistema**
- **Non visita Q2 né Q0**: non circola, non pensa

---

## Scelte Implementative

### Distinzione delle classi

`Customer` è stato esteso con l'enum `CustomerClass { CLOSED, OPEN }` (default `CLOSED`, retrocompatibile con tutti i punti precedenti). Il simulatore legge `customer.isOpen()` nei punti di routing e nelle statistiche.

### Routing differenziato in `processDepartureQ1`

```
SE customer.isOpen():
    → esce dal sistema (nessuna azione)
ALTRIMENTI (CLOSED):
    customer.arrivalTimeAtQ2 ← clock
    → si dirige verso Q2
```

### Stream RNG (tutti Leemis-Park, mai java.util.Random)

| Stream | Sorgente |
|--------|----------|
| THINK_TIME (2) | Think time Q0 |
| SERVICE (1) | Servizio Q1 classe chiusa |
| ROUTING (3) | Servizio Q2 |
| OPEN_ARRIVALS (4) | Inter-arrivi classe aperta (iperesponenziale) |
| OPEN_SERVICE (5) | Servizio Q1 classe aperta |

### Generazione inter-arrivi iperesponenziali

```
rngs.selectStream(OPEN_ARRIVALS)
u ← rngs.random()           // U(0,1) Leemis-Park
SE u < p:
    interarrival ← exp(mean1)
ALTRIMENTI:
    interarrival ← exp(mean2)
```

La scelta della componente usa lo stesso stream `OPEN_ARRIVALS` — non serve un stream separato perché `rngs.random()` consuma un campione dallo stream corrente prima di `rvgs.exponential()`.

### Condizione di stop

`completionsQ1Closed >= maxCompletions` — usa i completamenti della classe chiusa per garantire un orizzonte di simulazione stabile indipendente dal carico aperto.

### Accumulatori per classe

Le statistiche di utilizzo Q1 sono distinte per classe:
- `areaBusyQ1Closed` / `areaBusyQ1Open` → $\rho_{Q1}^{chiuso}$ e $\rho_{Q1}^{aperto}$
- `ρ_Q1^{tot} = ρ_{Q1}^{chiuso} + ρ_{Q1}^{aperto}$

---

## Risultati Sperimentali

**Configurazione**: N=15, Z=10.0 s, S₁=1.0 s, S₂=0.8 s, S₁_open=2.0 s.
Inter-arrivi aperti: Hyperexp(p=0.5, mean1=0.5·E[A], mean2=1.5·E[A]).
$R = 20$ repliche × 50.000 completamenti classe chiusa. Tutti gli IC al **95%**.

**Soglia di saturazione**: $\lambda_{open} \cdot 2 \cdot S_1 < 1 \Rightarrow \lambda_{open} < 0.5$.
La classe aperta da sola satura Q1 quando $\lambda_{open} \to 0.5$.

---

### Throughput

| $\lambda_{open}$ | $X_{chiuso}$ (IC 95%) | $X_{aperto}$ (IC 95%) | $X_{Q1}^{tot}$ (IC 95%) | $X_{Q2}$ (IC 95%) |
|---|---|---|---|---|
| 0.10 | 0.7632 ∈ [0.7610, 0.7653] | 0.0994 ∈ [0.0986, 0.1001] | 0.8625 ∈ [0.8609, 0.8642] | 0.7631 ∈ [0.7610, 0.7653] |
| 0.20 | 0.5922 ∈ [0.5894, 0.5950] | 0.1995 ∈ [0.1985, 0.2004] | 0.7917 ∈ [0.7896, 0.7938] | 0.5922 ∈ [0.5894, 0.5950] |
| 0.30 | 0.4011 ∈ [0.3974, 0.4049] | 0.2989 ∈ [0.2977, 0.3001] | 0.7001 ∈ [0.6972, 0.7029] | 0.4011 ∈ [0.3974, 0.4049] |
| 0.40 | 0.2032 ∈ [0.1996, 0.2067] | 0.3986 ∈ [0.3975, 0.3997] | 0.6018 ∈ [0.5990, 0.6046] | 0.2032 ∈ [0.1996, 0.2067] |

**Osservazione**: All'aumentare di $\lambda_{open}$, il throughput della classe chiusa crolla (la classe aperta occupa Q1 sottraendo capacità), mentre $X_{Q2}$ segue perfettamente $X_{chiuso}$ confermando che la classe aperta non transita per Q2.

---

### Utilizzo Q1 (dettaglio per classe) e Q2

| $\lambda_{open}$ | $\rho_{Q1}^{tot}$ | $\rho_{Q1}^{chiuso}$ | $\rho_{Q1}^{aperto}$ | $\rho_{Q2}$ |
|---|---|---|---|---|
| 0.10 | 0.9618 ∈ [0.9607, 0.9628] | 0.7632 | 0.1986 | 0.6101 ∈ [0.6078, 0.6125] |
| 0.20 | 0.9909 ∈ [0.9905, 0.9914] | 0.5920 | 0.3990 | 0.4735 ∈ [0.4709, 0.4760] |
| 0.30 | 0.9991 ∈ [0.9989, 0.9992] | 0.4009 | 0.5982 | 0.3208 ∈ [0.3175, 0.3241] |
| 0.40 | 1.0000 ∈ [1.0000, 1.0000] | 0.2030 | 0.7970 | 0.1624 ∈ [0.1594, 0.1654] |

**Verifica legge di utilizzo**:
$\rho_{Q1}^{tot} = X_{chiuso} \cdot S_1 + X_{aperto} \cdot 2S_1$

Per $\lambda=0.10$: $0.7632 \cdot 1.0 + 0.0994 \cdot 2.0 = 0.9620 \approx 0.9618$ ✓

**Osservazione**: $\rho_{Q2}$ decresce con $\lambda_{open}$ perché il throughput della classe chiusa (l'unica che transita per Q2) diminuisce.

---

### Tempi di Risposta

| $\lambda_{open}$ | $E[T_1^{chiuso}]$ (IC 95%) | $E[T_1^{aperto}]$ (IC 95%) | $E[T_2]$ (IC 95%) | $E[T_{sys}^{chiuso}]$ (IC 95%) |
|---|---|---|---|---|
| 0.10 | 7.640 ∈ [7.574, 7.706] | 10.289 ∈ [10.188, 10.391] | 2.023 ∈ [2.008, 2.038] | 9.663 ∈ [9.606, 9.721] |
| 0.20 | 13.631 ∈ [13.507, 13.755] | 17.568 ∈ [17.381, 17.755] | 1.712 ∈ [1.700, 1.724] | 15.343 ∈ [15.222, 15.463] |
| 0.30 | 26.029 ∈ [25.672, 26.386] | 32.165 ∈ [31.726, 32.605] | 1.386 ∈ [1.377, 1.395] | 27.415 ∈ [27.066, 27.765] |
| 0.40 | 62.819 ∈ [61.509, 64.130] | 75.013 ∈ [73.617, 76.410] | 1.086 ∈ [1.077, 1.094] | 63.905 ∈ [62.601, 65.209] |

**Osservazioni**:
- $E[T_1^{chiuso}]$ cresce drasticamente con $\lambda_{open}$: la classe aperta sottrae capacità di servizio a Q1, allungando l'attesa in coda per tutti.
- $E[T_1^{aperto}] > E[T_1^{chiuso}]$ perché il servizio della classe aperta è il doppio (2.0 s vs 1.0 s), quindi anche a parità di attesa in coda il tempo totale è maggiore.
- $E[T_2]$ *decresce* con $\lambda_{open}$: poiché $X_{chiuso}$ diminuisce, Q2 è sempre meno carica e il tempo di attesa si riduce.
- **Interferenza tra classi**: a $\lambda=0.10$ il sistema è ancora bilanciato (i valori di $E[T_1^{chiuso}]$ e del punto 6 con N=15 erano 4.43 s; qui con la classe aperta diventano già 7.64 s, +72%).

---

### Lunghezza Media delle Code

| $\lambda_{open}$ | $E[N_{q1}]$ (IC 95%) | $E[N_{q2}]$ (IC 95%) |
|---|---|---|
| 0.10 | 5.891 ∈ [5.842, 5.941] | 0.934 ∈ [0.922, 0.946] |
| 0.20 | 10.586 ∈ [10.504, 10.669] | 0.540 ∈ [0.533, 0.548] |
| 0.30 | 19.058 ∈ [18.857, 19.259] | 0.235 ∈ [0.230, 0.241] |
| 0.40 | 41.671 ∈ [41.020, 42.322] | 0.058 ∈ [0.056, 0.061] |

**Osservazione**: $E[N_{q1}]$ cresce quasi esponenzialmente avvicinandosi alla saturazione di Q1 ($\lambda_{open} \to 0.45$). $E[N_{q2}]$ decresce specularmente per le stesse ragioni di $E[T_2]$.

---

## Confronto con il Punto 6 (N=15, solo classe chiusa)

| Indice | Punto 6 (λ_open=0) | Punto 7 (λ_open=0.10) | Punto 7 (λ_open=0.30) |
|--------|---|---|---|
| $X_{chiuso}$ | 0.898 | 0.763 (−15%) | 0.401 (−55%) |
| $\rho_{Q1}^{tot}$ | 0.898 | 0.962 (+7%) | 0.999 (+11%) |
| $E[T_1^{chiuso}]$ | 4.430 s | 7.640 s (+72%) | 26.03 s (+488%) |
| $E[T_{sys}^{chiuso}]$ | 6.715 s | 9.663 s (+44%) | 27.42 s (+308%) |
| $E[N_{q1}]$ | 3.080 | 5.891 (+91%) | 19.06 (+519%) |

**Osservazione**: L'impatto della classe aperta sulla classe chiusa è molto severo. Anche un carico aperto moderato ($\lambda_{open}=0.10$, $\rho_{Q1}^{aperto}=0.20$) allunga il tempo di risposta della classe chiusa del 72%. Questo illustra il **problema della competizione per la risorsa condivisa** Q1 in un sistema a classi miste.

---

## Classi Introdotte

| Classe | Responsabilità |
|--------|---------------|
| `MixedNetworkConfig` | Configurazione immutabile: parametri classe chiusa + aperta (p, λ₁, λ₂) |
| `MixedNetworkStatistics` | Accumulatori separati per classe (utilizzo Q1 per classe, E[T] per classe) |
| `MixedNetworkSimulator` | Simulatore event-driven; routing differenziato per classe a Q1 |
| `MixedNetworkRunner` | R repliche con semi distanziati + IC 95% per tutti gli indici |
| `sim/runners/Punto7Runner` | Esperimenti al variare di λ_open ∈ {0.10, 0.20, 0.30, 0.40} |

`Customer` è stato esteso con `CustomerClass { CLOSED, OPEN }` (default `CLOSED`).
`SeedManager.StreamType` è stato esteso con `OPEN_ARRIVALS` (stream 4) e `OPEN_SERVICE` (stream 5).

