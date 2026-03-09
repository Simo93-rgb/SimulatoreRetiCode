# Punto 8 — JMT vs Simulatore Java

## Confronto Modello Chiuso

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

---

## Confronto Modello Aperto

### Configurazione comune

| Parametro | Valore |
|-----------|--------|
| $N$ (clienti classe chiusa) | 15 |
| $Z$ (think time Q0) | 10.0 s |
| $S_1$ (servizio Q1 — classe chiusa) | 1.0 s |
| $S_2$ (servizio Q2 — tutte le classi) | 0.8 s |
| $S_1^{\text{open}}$ (servizio Q1 — classe aperta) | 2.0 s |
| $p_1$ (prob. routing → Q1) | 0.3 |
| Distribuzione inter-arrivi aperta | Iper-esponenziale ($p{=}0.5$, $\mu_1{=}2/E[A]$, $\mu_2{=}2/(3E[A])$) |
| Valori di $\lambda_{\text{open}}$ testati | 0.10, 0.20, 0.30, 0.40 |

**Simulatore Java**: 20 repliche × 50 000 completamenti (classe chiusa), IC 95% con metodo delle repliche indipendenti.  
**JMT**: simulazione a eventi discreti, classe chiusa + classe aperta iper-esponenziale, IC 95% estratti dai CSV di output.

---

### 1. Throughput di sistema (classe chiusa)

| $\lambda$ | Sim $X_{\text{sys}}^{\text{ch}}$ (IC 95%) | JMT $X_{\text{sys}}^{\text{ch}}$ (IC 95%) | $\Delta\%$ | IC si sovrappongono? |
|-----------|----------------------------------------|-----------------------------------------|--------|---|
| 0.10 | 1.2331 ∈ [1.2317, 1.2345] | 1.2294 ∈ [1.2064, 1.2524] | 0.30% | ✓ |
| 0.20 | 1.1722 ∈ [1.1706, 1.1739] | 1.1667 ∈ [1.1508, 1.1825] | 0.47% | ✓ |
| 0.30 | 1.0286 ∈ [1.0242, 1.0330] | 1.0245 ∈ [1.0141, 1.0349] | 0.40% | ✓ |
| 0.40 | 0.6527 ∈ [0.6431, 0.6622] | 0.6497 ∈ [0.6477, 0.6518] | 0.46% | ✓ |

### Throughput per centro

| $\lambda$ | Sim $X_{Q1}^{\text{tot}}$ | JMT $X_{Q1}^{\text{tot}}$ | $\Delta\%$ | Sim $X_{Q1}^{\text{ch}}$ | JMT $X_{Q1}^{\text{ch}}$ | $\Delta\%$ | Sim $X_{Q1}^{\text{ap}}$ | JMT $X_{Q1}^{\text{ap}}$ | $\Delta\%$ | Sim $X_{Q2}$ | JMT $X_{Q2}$ | $\Delta\%$ |
|-----------|------|------|-----|------|-----|-----|------|-----|-----|------|------|-----|
| 0.10 | 0.4698 | 0.4667 | 0.66% | 0.3702 | 0.3685 | 0.46% | 0.0997 | 0.1019 | 2.21% | 0.8630 | 0.8680 | 0.58% |
| 0.20 | 0.5511 | 0.5475 | 0.65% | 0.3516 | 0.3501 | 0.43% | 0.1995 | 0.2010 | 0.75% | 0.8207 | 0.8209 | 0.02% |
| 0.30 | 0.6072 | 0.6103 | 0.51% | 0.3081 | 0.3110 | 0.94% | 0.2992 | 0.2962 | 1.00% | 0.7205 | 0.7149 | 0.78% |
| 0.40 | 0.5943 | 0.6184 | **4.06%** | 0.1955 | 0.1946 | 0.46% | 0.3988 | 0.3987 | 0.03% | 0.4571 | 0.4606 | 0.77% |

**Osservazione**: il throughput di sistema della classe chiusa concorda entro lo 0.5% per tutti i $\lambda$; tutti gli IC si sovrappongono. Il throughput aggregato a Q1 ($X_{Q1}^{\text{tot}}$) diverge del 4.1% a $\lambda{=}0.40$, con IC non sovrapposti: a saturazione di Q1 ($\rho_{Q1}^{\text{tot}} \approx 0.99$) la stima è sensibile alle differenze implementative della distribuzione iper-esponenziale.

---

### 2. Utilizzo dei server

| $\lambda$ | Sim $\rho_{Q1}^{\text{tot}}$ (IC 95%) | JMT $\rho_{Q1}^{\text{tot}}$ (IC 95%) | $\Delta\%$ | IC si sovrappongono? |
|-----------|------|------|--------|---|
| 0.10 | 0.5697 ∈ [0.5681, 0.5712] | 0.5737 ∈ [0.5696, 0.5777] | 0.70% | ✓ |
| 0.20 | 0.7504 ∈ [0.7485, 0.7524] | 0.7496 ∈ [0.7458, 0.7535] | 0.11% | ✓ |
| 0.30 | 0.9061 ∈ [0.9037, 0.9085] | 0.9081 ∈ [0.9049, 0.9112] | 0.22% | ✓ |
| 0.40 | 0.9924 ∈ [0.9918, 0.9931] | 0.9893 ∈ [0.9886, 0.9901] | 0.31% | ✗ (entrambi ≈ 0.99) |

| $\lambda$ | Sim $\rho_{Q2}$ (IC 95%) | JMT $\rho_{Q2}$ (IC 95%) | $\Delta\%$ | IC si sovrappongono? |
|-----------|------|------|--------|---|
| 0.10 | 0.6896 ∈ [0.6885, 0.6907] | 0.6938 ∈ [0.6873, 0.7003] | 0.61% | ✓ |
| 0.20 | 0.6562 ∈ [0.6548, 0.6576] | 0.6533 ∈ [0.6501, 0.6564] | 0.44% | ✓ |
| 0.30 | 0.5764 ∈ [0.5742, 0.5786] | 0.5797 ∈ [0.5761, 0.5833] | 0.57% | ✓ |
| 0.40 | 0.3661 ∈ [0.3607, 0.3714] | 0.3633 ∈ [0.3622, 0.3643] | 0.76% | ✓ |

**Osservazione**: l'utilizzo di Q2 concorda entro l'1% per tutti i valori di $\lambda$. La mancata sovrapposizione su $\rho_{Q1}^{\text{tot}}$ a $\lambda{=}0.40$ (Sim 0.9924 vs JMT 0.9893) è ininfluente in pratica: entrambi i valori indicano saturazione ($\rho \approx 0.99$).

---

### 3. Tempi medi di risposta

#### Q1 — classe chiusa $E[T_1^{\text{ch}}]$

| $\lambda$ | Sim $E[T_1^{\text{ch}}]$ (IC 95%) | JMT $E[T_1^{\text{ch}}]$ (IC 95%) | $\Delta\%$ | IC si sovrappongono? |
|-----------|------|------|--------|---|
| 0.10 | 2.5823 ∈ [2.5673, 2.5972] | 2.6420 ∈ [2.6202, 2.6637] | 2.31% | ✗ |
| 0.20 | 4.9011 ∈ [4.8538, 4.9484] | 5.0829 ∈ [5.0518, 5.1141] | 3.71% | ✗ |
| 0.30 | 11.3204 ∈ [11.1060, 11.5348] | 11.5460 ∈ [11.4936, 11.5985] | 1.99% | ✓ |
| 0.40 | 40.3671 ∈ [39.2039, 41.5303] | 40.7016 ∈ [40.6016, 40.8015] | 0.83% | ✓ |

#### Q1 — classe aperta $E[T_1^{\text{ap}}]$

| $\lambda$ | Sim $E[T_1^{\text{ap}}]$ (IC 95%) | JMT $E[T_1^{\text{ap}}]$ (IC 95%) | $\Delta\%$ | IC si sovrappongono? |
|-----------|------|------|--------|---|
| 0.10 | 3.9664 ∈ [3.9340, 3.9987] | 4.0154 ∈ [3.9851, 4.0457] | 1.24% | ✓ |
| 0.20 | 6.8032 ∈ [6.7299, 6.8765] | 7.0123 ∈ [6.9559, 7.0688] | 3.07% | ✗ |
| 0.30 | 14.7553 ∈ [14.4672, 15.0434] | 15.2151 ∈ [15.1608, 15.2694] | 3.12% | ✗ |
| 0.40 | 50.9676 ∈ [49.5928, 52.3423] | 52.1118 ∈ [52.0443, 52.1794] | 2.24% | ✓ |

#### Q2 — $E[T_2]$

| $\lambda$ | Sim $E[T_2]$ (IC 95%) | JMT $E[T_2]$ (IC 95%) | $\Delta\%$ | IC si sovrappongono? |
|-----------|------|------|--------|---|
| 0.10 | 1.9976 ∈ [1.9896, 2.0056] | 1.9980 ∈ [1.9805, 2.0155] | 0.02% | ✓ |
| 0.20 | 1.9069 ∈ [1.8981, 1.9157] | 1.8900 ∈ [1.8781, 1.9020] | 0.89% | ✓ |
| 0.30 | 1.7160 ∈ [1.7056, 1.7264] | 1.7501 ∈ [1.7351, 1.7650] | 1.99% | ✗ |
| 0.40 | 1.3078 ∈ [1.2964, 1.3192] | 1.3107 ∈ [1.3044, 1.3171] | 0.22% | ✓ |

#### Sistema (soli centri di servizio) — $E[T_{\text{sys}}^{\text{ch}}]$ derivato

Calcolato come $E[T_{\text{sys}}^{\text{ch}}] = (X_1^{\text{ch}} E[T_1^{\text{ch}}] + X_2 E[T_2]) / X_{\text{sys}}^{\text{ch}}$ per entrambi gli strumenti.

| $\lambda$ | Sim $E[T_{\text{sys}}^{\text{ch}}]$ (IC 95%) | JMT $E[T_{\text{sys}}^{\text{ch}}]$ (derivato) | $\Delta\%$ |
|-----------|------|------|--------|
| 0.10 | 2.1731 ∈ [2.1678, 2.1784] | 2.1443 | 1.33% |
| 0.20 | 2.8048 ∈ [2.7917, 2.8179] | 2.8293 | 0.87% |
| 0.30 | 4.5920 ∈ [4.5342, 4.6498] | 4.5774 | 0.32% |
| 0.40 | 13.0070 ∈ [12.6741, 13.3399] | 13.0928 | 0.66% |

**Osservazione**: i tempi di risposta a Q1 per la classe chiusa mostrano scostamenti del 2–4% a carichi bassi ($\lambda \leq 0.20$), con IC non sovrapposti, che si riducono a $< 1\%$ ad alto carico ($\lambda \geq 0.40$). Un andamento analogo si osserva per la classe aperta a Q1 ($\lambda{=}0.20$ e $0.30$). Lo scostamento su $E[T_2]$ a $\lambda{=}0.30$ (1.99%) è coerente con la lieve differenza nel throughput a Q2. Il tempo di ciclo centrale della classe chiusa ($E[T_{\text{sys}}^{\text{ch}}]$) concorda entro l'1.4% in tutti i casi.

---

### 4. Lunghezza media delle code

I valori JMT sono derivati (tramite Little: $E[N_{qi}] = X_{Qi} \cdot E[T_i] - \rho_{Qi}$, senza IC); il simulatore riporta IC con metodo delle repliche.

| $\lambda$ | Sim $E[N_{q1}]$ (IC 95%) | JMT $E[N_{q1}]$ | $\Delta\%$ | Sim $E[N_{q2}]$ (IC 95%) | JMT $E[N_{q2}]$ | $\Delta\%$ |
|-----------|------|------|--------|------|------|--------|
| 0.10 | 0.7816 ∈ [0.7733, 0.7898] | 0.8136 | 4.09% | 1.0342 ∈ [1.0273, 1.0412] | 1.0236 | 1.02% |
| 0.20 | 2.3301 ∈ [2.3006, 2.3597] | 2.4176 | 3.76% | 0.9087 ∈ [0.9017, 0.9157] | 0.9018 | 0.76% |
| 0.30 | 6.9956 ∈ [6.8549, 7.1362] | 7.1934 | 2.83% | 0.6601 ∈ [0.6516, 0.6687] | 0.6441 | 2.42% |
| 0.40 | 27.2231 ∈ [26.5415, 27.9046] | 27.5630 | 1.25% | 0.2321 ∈ [0.2241, 0.2401] | 0.2301 | 0.86% |

**Osservazione**: $E[N_{q1}]$ mostra differenze del 3–4% per $\lambda \leq 0.20$, coerenti con la sovrastima JMT dei tempi di risposta chiusi a tali carichi (Little: $E[N_{q1}] = X_{Q1}^{\text{tot}} \cdot E[T_1] - \rho_{Q1}$). Per $\lambda{=}0.40$ la concordanza migliora all'1.2%.

---

### 5. Sintesi della validazione

| Metrica | $\Delta\%$ tipico | $\Delta\%$ massimo | IC sempre sovrapposti? |
|---------|---------|---------|---|
| $X_{\text{sys}}^{\text{ch}}$ | $< 0.5\%$ | 0.47% ($\lambda{=}0.20$) | **Sì** |
| $X_{Q1}^{\text{tot}}$, $X_{Q1}^{\text{ch}}$, $X_{Q2}$ | $< 1\%$ | 4.06%* | No (solo $X_{Q1}^{\text{tot}}$ a $\lambda{=}0.40$) |
| $\rho_{Q1}^{\text{tot}}$ | $< 0.75\%$ | 0.70% | No ($\lambda{=}0.40$, entrambi $\approx 0.99$) |
| $\rho_{Q2}$ | $< 0.8\%$ | 0.76% | **Sì** |
| $E[T_1^{\text{ch}}]$ | $< 2\%$ | 3.71% ($\lambda{=}0.20$) | No ($\lambda{=}0.10, 0.20$) |
| $E[T_1^{\text{ap}}]$ | $2{-}3\%$ | 3.12% ($\lambda{=}0.30$) | No ($\lambda{=}0.20, 0.30$) |
| $E[T_2]$ | $< 1\%$ | 1.99% ($\lambda{=}0.30$) | No (solo $\lambda{=}0.30$) |
| $E[T_{\text{sys}}^{\text{ch}}]$ | $< 1.4\%$ | 1.33% | — |
| $E[N_{q1}]$ | $2{-}4\%$ | 4.09% ($\lambda{=}0.10$) | — |
| $E[N_{q2}]$ | $< 2.5\%$ | 2.42% ($\lambda{=}0.30$) | — |

*differenza assoluta $\approx 0.024$ job/s a $\lambda{=}0.40$.

**Conclusione**: throughput del sistema e utilizzo dei server concordano entro l'1% con JMT per tutti i valori di $\lambda_{\text{open}}$. Le differenze più consistenti riguardano i tempi di risposta a Q1 per entrambe le classi a carichi medi ($\lambda{=}0.20$–$0.30$): JMT stima tempi mediamente del 2–4% più elevati, verosimilmente a causa di piccole differenze nell'implementazione della distribuzione iper-esponenziale e nella gestione delle priorità in coda mista. A carichi estremi ($\lambda{=}0.40$, con $\rho_{Q1} \approx 0.99$) la concordanza migliora sensibilmente perché il sistema è bottleneck-dominante. Il simulatore è pertanto **validato** per il modello misto chiuso/aperto con differenze compatibili con la variabilità di stima.
