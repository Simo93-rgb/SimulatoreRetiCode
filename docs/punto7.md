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

| $\lambda_{open}$ | $X_{sistema}$ (IC 95%) | $X_{chiuso}^{Q1}$ (IC 95%) | $X_{aperto}^{Q1}$ (IC 95%) | $X_{Q1}^{tot}$ (IC 95%) | $X_{Q2}$ (IC 95%) |
|---|---|---|---|---|---|
| 0.10 | 1.2331 ∈ [1.2317, 1.2345] | 0.3702 ∈ [0.3693, 0.3710] | 0.0997 ∈ [0.0991, 0.1002] | 0.4698 ∈ [0.4690, 0.4706] | 0.8630 ∈ [0.8619, 0.8640] |
| 0.20 | 1.1722 ∈ [1.1706, 1.1739] | 0.3516 ∈ [0.3505, 0.3526] | 0.1995 ∈ [0.1987, 0.2003] | 0.5511 ∈ [0.5502, 0.5519] | 0.8207 ∈ [0.8196, 0.8218] |
| 0.30 | 1.0286 ∈ [1.0242, 1.0330] | 0.3081 ∈ [0.3063, 0.3098] | 0.2992 ∈ [0.2981, 0.3003] | 0.6072 ∈ [0.6059, 0.6086] | 0.7205 ∈ [0.7177, 0.7234] |
| 0.40 | 0.6527 ∈ [0.6431, 0.6622] | 0.1955 ∈ [0.1925, 0.1986] | 0.3988 ∈ [0.3978, 0.3998] | 0.5943 ∈ [0.5920, 0.5966] | 0.4571 ∈ [0.4506, 0.4637] |

**Osservazione**: $X_{sistema} = X_{Q1}^{chiuso} + X_{Q2}$ (i clienti chiusi tornano a Q0 da entrambe le uscite del branch parallelo). All'aumentare di $\lambda_{open}$, $X_{Q1}^{chiuso}$ crolla (la classe aperta satura Q1) e di conseguenza si riduce anche $X_{Q2}$.

---

### Utilizzo Q1 (dettaglio per classe) e Q2

| $\lambda_{open}$ | $\rho_{Q1}^{tot}$ (IC 95%) | $\rho_{Q1}^{chiuso}$ | $\rho_{Q1}^{aperto}$ | $\rho_{Q2}$ (IC 95%) |
|---|---|---|---|---|
| 0.10 | 0.5697 ∈ [0.5681, 0.5712] | 0.3702 | 0.1995 | 0.6896 ∈ [0.6885, 0.6907] |
| 0.20 | 0.7504 ∈ [0.7485, 0.7524] | 0.3511 | 0.3993 | 0.6562 ∈ [0.6548, 0.6576] |
| 0.30 | 0.9061 ∈ [0.9037, 0.9085] | 0.3077 | 0.5983 | 0.5764 ∈ [0.5742, 0.5786] |
| 0.40 | 0.9924 ∈ [0.9918, 0.9931] | 0.1953 | 0.7971 | 0.3661 ∈ [0.3607, 0.3714] |

**Verifica legge di utilizzo**:
$\rho_{Q1}^{tot} = X_{Q1}^{chiuso} \cdot S_1 + X_{Q1}^{aperto} \cdot 2S_1$

Per $\lambda=0.10$: $0.3702 \cdot 1.0 + 0.0997 \cdot 2.0 = 0.5696 \approx 0.5697$ ✓

**Verifica legge di utilizzo Q2**: $\rho_{Q2} = X_{Q2} \cdot S_2$

Per $\lambda=0.10$: $0.8630 \times 0.8 = 0.6904 \approx 0.6896$ ✓

**Osservazione**: $\rho_{Q2}$ decresce con $\lambda_{open}$ perché il flusso verso Q2 (proporzionale a $X_{Q1}^{chiuso} \cdot (1-p_1)$) si riduce man mano che la classe aperta satura Q1.

---

### Tempi di Risposta

| $\lambda_{open}$ | $E[T_1^{chiuso}]$ (IC 95%) | $E[T_1^{aperto}]$ (IC 95%) | $E[T_2]$ (IC 95%) | $E[T_{sys}^{chiuso}]$ (IC 95%) |
|---|---|---|---|---|
| 0.10 | 2.5823 ∈ [2.5673, 2.5972] | 3.9664 ∈ [3.9340, 3.9987] | 1.9976 ∈ [1.9896, 2.0056] | 2.1731 ∈ [2.1678, 2.1784] |
| 0.20 | 4.9011 ∈ [4.8538, 4.9484] | 6.8032 ∈ [6.7299, 6.8765] | 1.9069 ∈ [1.8981, 1.9157] | 2.8048 ∈ [2.7917, 2.8179] |
| 0.30 | 11.3204 ∈ [11.1060, 11.5348] | 14.7553 ∈ [14.4672, 15.0434] | 1.7160 ∈ [1.7056, 1.7264] | 4.5920 ∈ [4.5342, 4.6498] |
| 0.40 | 40.3671 ∈ [39.2039, 41.5303] | 50.9676 ∈ [49.5928, 52.3423] | 1.3078 ∈ [1.2964, 1.3192] | 13.0070 ∈ [12.6741, 13.3399] |

**Osservazioni**:
- $E[T_1^{chiuso}]$ cresce drasticamente con $\lambda_{open}$: la classe aperta sottrae capacità a Q1.
- $E[T_1^{aperto}] > E[T_1^{chiuso}]$ perché il servizio è doppio (2S₁ = 2.0 s).
- $E[T_2]$ *decresce* con $\lambda_{open}$: Q2 riceve meno traffico e si svuota.
- **$E[T_{sys}^{chiuso}]$** è la media pesata sul mix Q1/Q2 reali (non più la somma sequenziale), risultando molto più bassa dei valori vecchi.
- **Confronto con JMT**: i valori di $X_{sistema}$ ora corrispondono a quelli di JMT (es. $\lambda=0.10$: 1.23).

---

### Lunghezza Media delle Code

| $\lambda_{open}$ | $E[N_{q1}]$ (IC 95%) | $E[N_{q2}]$ (IC 95%) |
|---|---|---|
| 0.10 | 0.7816 ∈ [0.7733, 0.7898] | 1.0342 ∈ [1.0273, 1.0412] |
| 0.20 | 2.3301 ∈ [2.3006, 2.3597] | 0.9087 ∈ [0.9017, 0.9157] |
| 0.30 | 6.9956 ∈ [6.8549, 7.1362] | 0.6601 ∈ [0.6516, 0.6687] |
| 0.40 | 27.2231 ∈ [26.5415, 27.9046] | 0.2321 ∈ [0.2241, 0.2401] |

**Osservazione**: $E[N_{q1}]$ cresce quasi esponenzialmente avvicinandosi alla saturazione di Q1. $E[N_{q2}]$ decresce perché Q2 riceve sempre meno traffico.

---

## Confronto con il Punto 6 (N=15, solo classe chiusa)

| Indice | Punto 6 ($\lambda_{open}=0$, $p_1=0.3$) | Punto 7 ($\lambda_{open}=0.10$) | Punto 7 ($\lambda_{open}=0.30$) |
|--------|---|---|---|
| $X_{sistema}$ | 1.2622 | 1.2331 (−2%) | 1.0286 (−18%) |
| $X_{Q1}^{chiuso}$ | 0.3775 | 0.3702 (−2%) | 0.3081 (−18%) |
| $\rho_{Q1}^{tot}$ | 0.3771 | 0.5697 (+51%) | 0.9061 (+140%) |
| $E[T_1^{chiuso}]$ | 1.53 s | 2.58 s (+69%) | 11.32 s (+640%) |
| $E[T_{sys}^{chiuso}]$ | 1.90 s | 2.17 s (+14%) | 4.59 s (+142%) |
| $E[N_{q1}]$ | 0.1993 | 0.7816 (+292%) | 6.9956 (+3410%) |

**Osservazione**: Anche un carico aperto leggero ($\lambda_{open}=0.10$, $\rho_{Q1}^{aperto}=0.20$) porta $\rho_{Q1}^{tot}$ da 0.38 a 0.57 (+51%) e $E[T_1^{chiuso}]$ da 1.53 s a 2.58 s (+69%). L'interferenza di risorse condivise ha un impatto non lineare: raddoppiare il carico aperto triplica i tempi di attesa.

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

