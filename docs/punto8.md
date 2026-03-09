# Punto 8 — Validazione del Modello: confronto Simulatore vs JMT

## Configurazione comune

| Parametro | Valore |
|-----------|--------|
| $Z$ (think time Q0) | 10.0 s |
| $S_1$ (tempo servizio Q1) | 1.0 s |
| $S_2$ (tempo servizio Q2) | 0.8 s |
| $p_1$ (prob. routing → Q1) | 0.3 |
| Valori di N testati | 5, 11, 17, 23, 30 |

**Simulatore Java**: 20 repliche × 50 000 completamenti, IC 95% con metodo delle repliche indipendenti.  
**JMT**: simulazione a eventi discreti, classe chiusa unica, IC 95% estratti da `SAMPLE`/`WEIGHT` dei CSV di output.

---

## 1. Throughput di sistema

| N | Regime | Sim $X$ (IC 95%) | JMT $X$ (IC 95%) | $\Delta\%$ | IC si sovrappongono? |
|---|--------|------|------|--------|-------------------|
| 5  | Leggero         | 0.4539 ∈ [0.4531, 0.4547] | 0.4506 ∈ [0.4459, 0.4552] | 0.73% | ✓ |
| 11 | Medio           | 0.9629 ∈ [0.9612, 0.9646] | 0.9698 ∈ [0.9511, 0.9884] | 0.71% | ✓ |
| 17 | Intenso         | 1.3919 ∈ [1.3898, 1.3940] | 1.3874 ∈ [1.3724, 1.4025] | 0.32% | ✓ |
| 23 | Vicino satur.   | 1.6704 ∈ [1.6672, 1.6735] | 1.6395 ∈ [1.5998, 1.6792] | 1.88% | ✓ |
| 30 | Saturazione     | 1.7771 ∈ [1.7728, 1.7813] | 1.7781 ∈ [1.7627, 1.7935] | 0.06% | ✓ |

### Throughput per centro

| N | Sim $X_1$ | JMT $X_1$ | $\Delta\%$ | Sim $X_2$ | JMT $X_2$ | $\Delta\%$ |
|---|-----------|-----------|--------|-----------|-----------|--------|
| 5  | 0.1361 | 0.1357 | 0.29% | 0.3178 | 0.3160 | 0.57% |
| 11 | 0.2884 | 0.2901 | 0.59% | 0.6745 | 0.6706 | 0.58% |
| 17 | 0.4172 | 0.4164 | 0.19% | 0.9747 | 0.9743 | 0.04% |
| 23 | 0.5010 | 0.4993 | 0.34% | 1.1694 | 1.1688 | 0.05% |
| 30 | 0.5330 | 0.5405 | 1.39% | 1.2440 | 1.2420 | 0.16% |

**Osservazione**: tutti gli IC si sovrappongono per il throughput di sistema. Le differenze sono $< 2\%$ per ogni configurazione. La concordanza è molto buona, inclusa la zona di saturazione ($N = 30$) dove entrambi convergono al bound teorico $X^* = 1/D_{\max} = 1/0.56 = 1.7857$.

---

## 2. Utilizzo dei server

| N | Sim $\rho_1$ (IC 95%) | JMT $\rho_1$ (IC 95%) | $\Delta\%$ | IC si sovrappongono? |
|---|---------|---------|--------|-----|
| 5  | 0.1359 ∈ [0.1352, 0.1366] | 0.1348 ∈ [0.1311, 0.1385] | 0.82% | ✓ |
| 11 | 0.2882 ∈ [0.2867, 0.2896] | 0.2898 ∈ [0.2860, 0.2936] | 0.55% | ✓ |
| 17 | 0.4168 ∈ [0.4144, 0.4192] | 0.4141 ∈ [0.4105, 0.4178] | 0.65% | ✓ |
| 23 | 0.5005 ∈ [0.4977, 0.5032] | 0.4957 ∈ [0.4923, 0.4991] | 0.97% | ✓ |
| 30 | 0.5325 ∈ [0.5292, 0.5357] | 0.5380 ∈ [0.5320, 0.5441] | 1.02% | ✓ |

| N | Sim $\rho_2$ (IC 95%) | JMT $\rho_2$ (IC 95%) | $\Delta\%$ | IC si sovrappongono? |
|---|---------|---------|--------|-----|
| 5  | 0.2542 ∈ [0.2535, 0.2549] | 0.2541 ∈ [0.2495, 0.2587] | 0.04% | ✓ |
| 11 | 0.5391 ∈ [0.5378, 0.5404] | 0.5337 ∈ [0.5280, 0.5393] | 1.01% | ✓ |
| 17 | 0.7800 ∈ [0.7788, 0.7812] | 0.7734 ∈ [0.7688, 0.7779] | 0.85% | ✗ (margine: 0.0009) |
| 23 | 0.9355 ∈ [0.9341, 0.9368] | 0.9349 ∈ [0.9327, 0.9371] | 0.06% | ✓ |
| 30 | 0.9942 ∈ [0.9939, 0.9946] | 0.9933 ∈ [0.9927, 0.9938] | 0.09% | ✗ (entrambi ≈ 1) |

**Osservazione**: l'unica mancata sovrapposizione su $\rho_2$ è a $N=17$ (margine di 0.0009, differenza assoluta $< 0.001$) e a $N=30$ dove entrambi i valori sono $\approx 0.994$–$0.999$, ossia sostanzialmente a saturazione. La legge di utilizzo $\rho_i = X_i \cdot S_i$ è verificata in entrambi gli strumenti con precisione $< 1\%$.

---

## 3. Tempi medi di risposta

### Q1 — $E[T_1]$

| N | Sim $E[T_1]$ (IC 95%) | JMT $E[T_1]$ (IC 95%) | $\Delta\%$ | IC si sovrappongono? |
|---|---------|---------|--------|-----|
| 5  | 1.1172 ∈ [1.1118, 1.1225] | 1.1283 ∈ [1.1174, 1.1392] | 0.98% | ✓ |
| 11 | 1.3436 ∈ [1.3375, 1.3497] | 1.3392 ∈ [1.3278, 1.3506] | 0.33% | ✓ |
| 17 | 1.6261 ∈ [1.6129, 1.6393] | 1.6118 ∈ [1.5993, 1.6243] | 0.89% | ✓ |
| 23 | 1.9110 ∈ [1.8923, 1.9297] | 1.9024 ∈ [1.8842, 1.9206] | 0.45% | ✓ |
| 30 | 2.0999 ∈ [2.0787, 2.1211] | 2.1535 ∈ [2.1353, 2.1717] | 2.49% | ✗ |

### Q2 — $E[T_2]$

| N | Sim $E[T_2]$ (IC 95%) | JMT $E[T_2]$ (IC 95%) | $\Delta\%$ | IC si sovrappongono? |
|---|---------|---------|--------|-----|
| 5  | 0.9903 ∈ [0.9877, 0.9928] | 0.9867 ∈ [0.9791, 0.9944] | 0.36% | ✓ |
| 11 | 1.4763 ∈ [1.4698, 1.4827] | 1.4830 ∈ [1.4720, 1.4939] | 0.45% | ✓ |
| 17 | 2.4829 ∈ [2.4705, 2.4953] | 2.4380 ∈ [2.4200, 2.4561] | 1.84% | ✗ |
| 23 | 4.5821 ∈ [4.5388, 4.6254] | 4.5837 ∈ [4.5656, 4.6018] | 0.03% | ✓ |
| 30 | 8.9440 ∈ [8.8867, 9.0012] | 8.8004 ∈ [8.7725, 8.8284] | 1.63% | ✗ |

### Sistema centrale (Q1 + Q2) — $E[T_\text{sys}]$

Calcolato come media pesata $E[T_\text{sys}] = (X_1 \cdot E[T_1] + X_2 \cdot E[T_2]) \,/\, X$ per entrambi gli strumenti.

| N | Sim $E[T_\text{sys}]$ | JMT $E[T_\text{sys}]$ | $\Delta\%$ |
|---|---------|---------|--------|
| 5  | 1.0283 | 1.0317 | 0.33% |
| 11 | 1.4365 | 1.4256 | 0.76% |
| 17 | 2.2261 | 2.1963 | 1.36% |
| 23 | 3.7811 | 3.8470 | 1.71% |
| 30 | 6.8912 | 6.8018 | 1.31% |

**Osservazione**: i tempi di risposta concordano entro il $2.5\%$ in tutti i casi. Le mancate sovrapposizioni riguardano configurazioni ad alto carico ($N = 17, 30$) dove la coda in Q2 è lunga e la varianza campionaria è naturalmente più elevata. La divergenza massima su $E[T_1]$ a $N=30$ (2.49%) è attribuibile alla diversa modalità di stima a ciclo completo: il simulatore campiona direttamente i soggiorni in Q1, JMT misura l'inter-departure time su più repliche con diversa gestione del transitorio.

---

## 4. Lunghezza media delle code

I valori JMT sono derivati ($E[N_q] = E[N] - \rho$, senza IC); il simulatore riporta IC con il metodo delle repliche.

| N | Sim $E[N_{q1}]$ (IC 95%) | JMT $E[N_{q1}]$ | $\Delta\%$ | Sim $E[N_{q2}]$ (IC 95%) | JMT $E[N_{q2}]$ | $\Delta\%$ |
|---|---------|---------|--------|---------|---------|--------|
| 5  | 0.0161 ∈ [0.0158, 0.0164] | 0.0180 | 11.8% | 0.0606 ∈ [0.0600, 0.0611] | 0.0615 | 1.49% |
| 11 | 0.0994 ∈ [0.0981, 0.1007] | 0.0978 | 1.61% | 0.4566 ∈ [0.4532, 0.4601] | 0.4583 | 0.37% |
| 17 | 0.2617 ∈ [0.2566, 0.2669] | 0.2618 | 0.04% | 1.6401 ∈ [1.6288, 1.6514] | 1.6073 | 2.04% |
| 23 | 0.4570 ∈ [0.4480, 0.4660] | 0.4581 | 0.24% | 4.4231 ∈ [4.3785, 4.4678] | 4.4415 | 0.41% |
| 30 | 0.5870 ∈ [0.5758, 0.5981] | 0.6050 | 2.97% | 10.1344 ∈ [10.0836, 10.1852] | 9.9661 | 1.69% |

**Osservazione**: l'unica differenza > 5% è $E[N_{q1}]$ a $N=5$ (0.0161 vs 0.0180), dove il valore assoluto è molto piccolo (ordine $10^{-2}$): piccole differenze assolute si amplificano in percentuale. Per tutti gli altri valori la concordanza è entro il 3%.

---

## 5. Sintesi della validazione

| Metrica | Differenza tipica | Differenza massima | IC sempre sovrapposti? |
|---------|------------------|--------------------|----------------------|
| $X$ sistema | < 1% | 1.88% (N=23) | **Sì** |
| $X_1$, $X_2$ | < 1% | 1.39% | **Sì** |
| $\rho_1$ | < 1% | 1.02% | **Sì** |
| $\rho_2$ | < 1% | 1.01% | No (N=17, N=30 — margine $< 0.001$) |
| $E[T_1]$ | < 1% | 2.49% (N=30) | No (solo N=30) |
| $E[T_2]$ | < 1% | 1.84% (N=17) | No (N=17, N=30) |
| $E[T_\text{sys}]$ | < 1.5% | 1.71% | — |
| $E[N_{q1}]$ | < 3% | 11.8%* | — |
| $E[N_{q2}]$ | < 2.5% | 2.97% | — |

*valore assoluto $E[N_{q1}] \approx 0.016$ a $N=5$: errore assoluto $\approx 0.002$.

**Conclusione**: il simulatore Java e JMT producono stime concordanti per tutte le metriche e tutti i regimi di carico. Le poche mancate sovrapposizioni degli IC riguardano configurazioni prossime alla saturazione ($N \geq 17$ per $\rho_2$ ed $E[T_2]$) dove la varianza campionaria è più elevata per entrambi gli strumenti; le differenze in valore assoluto restano $< 2.5\%$, compatibili con la variabilità statistica attesa. Il simulatore è pertanto **validato** per il modello chiuso con $Q_0, Q_1, Q_2$.
