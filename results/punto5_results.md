# PUNTO 5 - RACCOLTA DATI SIMULATORE

**Configurazione:** `R=20` repliche, `N=100000` customer/replica

## ESPERIMENTO 1: M/M/1 Standard ($\rho=0.8$)

- **Parametri:** $\lambda=0.8$, $\mu=1.0$
- **Teoria:** $E[T]=5.0$, $E[N]=4.0$, $E[N_q]=3.2$, $\rho=0.8$

### Risultati Simulatore (IC 95%)

| Metrica | Media | Intervallo di Confidenza | Errore Relativo |
|---|---|---|---|
| **X** (Throughput) | `0.7997` | $\in [0.7984, 0.8010]$ | `0.16%` |
| **$\rho$** (Utilizzo) | `0.7998` | $\in [0.7983, 0.8014]$ | `0.19%` |
| **$E[T]$** (Response Time) | `4.9707` | $\in [4.9116, 5.0299]$ | `1.19%` |
| **$E[N_q]$** (Queue Length) | `3.1754` | $\in [3.1257, 3.2250]$ | `1.56%` |
| **$E[N]$** (System Size) | `3.9752` | $\in [3.9242, 4.0261]$ | `1.28%` |

## ESPERIMENTO 2: M/M/1 Basso Carico ($\rho=0.5$)

- **Parametri:** $\lambda=0.5$, $\mu=1.0$
- **Teoria:** $E[T]=2.0$, $E[N]=1.0$, $\rho=0.5$

### Risultati Simulatore (IC 95%)

| Metrica | Media | Intervallo di Confidenza | Errore Relativo |
|---|---|---|---|
| **X** (Throughput) | `0.4998` | $\in [0.4990, 0.5006]$ | `0.16%` |
| **$\rho$** (Utilizzo) | `0.4999` | $\in [0.4989, 0.5009]$ | `0.19%` |
| **$E[T]$** (Response Time) | `2.0000` | $\in [1.9933, 2.0067]$ | `0.34%` |
| **$E[N_q]$** (Queue Length) | `0.4997` | $\in [0.4964, 0.5030]$ | `0.66%` |
| **$E[N]$** (System Size) | `0.9996` | $\in [0.9955, 1.0037]$ | `0.41%` |

## ESPERIMENTO 3: M/M/1 Alto Carico ($\rho=0.9$)

- **Parametri:** $\lambda=0.9$, $\mu=1.0$
- **Teoria:** $E[T]=10.0$, $E[N]=9.0$, $\rho=0.9$

### Risultati Simulatore (IC 95%)

| Metrica | Media | Intervallo di Confidenza | Errore Relativo |
|---|---|---|---|
| **X** (Throughput) | `0.8996` | $\in [0.8981, 0.9011]$ | `0.17%` |
| **$\rho$** (Utilizzo) | `0.8997` | $\in [0.8980, 0.9015]$ | `0.20%` |
| **$E[T]$** (Response Time) | `9.8209` | $\in [9.5487, 10.0930]$ | `2.77%` |
| **$E[N_q]$** (Queue Length) | `7.9369` | $\in [7.6839, 8.1899]$ | `3.19%` |
| **$E[N]$** (System Size) | `8.8366` | $\in [8.5821, 9.0911]$ | `2.88%` |

## ESPERIMENTO 4: M/D/1 (Servizio Deterministico)

- **Parametri:** $\lambda=0.8$, servizio D=1.0 ($cv^2=0$)
- **Teoria M/D/1:** $E[T]=3.0$, $E[N_q]=1.6$, $E[N]=2.4$

### Risultati Simulatore (IC 95%)

| Metrica | Media | Intervallo di Confidenza | Errore Relativo |
|---|---|---|---|
| **X** (Throughput) | `0.7997` | $\in [0.7984, 0.8010]$ | `0.16%` |
| **$\rho$** (Utilizzo) | `0.7997` | $\in [0.7984, 0.8010]$ | `0.16%` |
| **$E[T]$** (Response Time) | `2.9870` | $\in [2.9645, 3.0095]$ | `0.75%` |
| **$E[N_q]$** (Queue Length) | `1.5891` | $\in [1.5692, 1.6089]$ | `1.25%` |
| **$E[N]$** (System Size) | `2.3887` | $\in [2.3679, 2.4096]$ | `0.87%` |

## ESPERIMENTO 5: M/H$_2$/1 (Servizio Iperesponenziale)

- **Parametri:** $\lambda=0.8$, servizio Hyperexp(p=0.5, mean1=0.5, mean2=1.5)
- **Media servizio:** $E[S]=1.0$, $cv^2>1$
- **Previsione:** $E[T] > 5.0$ (M/M/1)

### Risultati Simulatore (IC 95%)

| Metrica | Media | Intervallo di Confidenza | Errore Relativo |
|---|---|---|---|
| **X** (Throughput) | `0.7996` | $\in [0.7983, 0.8010]$ | `0.16%` |
| **$\rho$** (Utilizzo) | `0.8003` | $\in [0.7984, 0.8022]$ | `0.24%` |
| **$E[T]$** (Response Time) | `5.9904` | $\in [5.9243, 6.0565]$ | `1.10%` |
| **$E[N_q]$** (Queue Length) | `3.9902` | $\in [3.9357, 4.0447]$ | `1.37%` |
| **$E[N]$** (System Size) | `4.7905` | $\in [4.7345, 4.8465]$ | `1.17%` |

## ESPERIMENTO 6: M/E$_k$/1 (Servizio Erlang)

- **Parametri:** $\lambda=0.8$, servizio Erlang(k=4, mean=1.0)
- **cv$^2$:** $1/k=0.25$
- **Previsione:** $E[T] < 5.0$ (M/M/1)

### Risultati Simulatore (IC 95%)

| Metrica | Media | Intervallo di Confidenza | Errore Relativo |
|---|---|---|---|
| **X** (Throughput) | `0.7997` | $\in [0.7984, 0.8010]$ | `0.16%` |
| **$\rho$** (Utilizzo) | `0.7998` | $\in [0.7984, 0.8013]$ | `0.18%` |
| **$E[T]$** (Response Time) | `3.4920` | $\in [3.4550, 3.5289]$ | `1.06%` |
| **$E[N_q]$** (Queue Length) | `1.9928` | $\in [1.9615, 2.0240]$ | `1.57%` |
| **$E[N]$** (System Size) | `2.7926` | $\in [2.7603, 2.8249]$ | `1.16%` |

---

## RACCOLTA DATI COMPLETATA

I risultati sono pronti per essere inseriti in `docs/punto5.md`.

