# Step 1 — ServiceGenerator: Generazione Variabili Casuali

## Obiettivo

Implementare un generatore di tempi di servizio e di interarrivo basato sulle **librerie Leemis-Park** (`Rngs`, `Rvgs`), supportando le 4 distribuzioni richieste dalla consegna.

> **Vincolo imperativo**: nessun uso di `java.util.Random`. Tutta la generazione passa per `libraries.*`.

---

## Cosa è stato fatto

1. **Creato `ServiceGenerator.java`**: wrapper tipizzato che espone metodi per generare variabili casuali secondo le distribuzioni specificate.
2. **Creato `ServiceGeneratorTest.java`**: suite di test JUnit5 con validazione statistica di **media** e **varianza** su $N = 100.000$ campioni.
3. **Validazione completa**: tutti i 4 test passano con tolleranza del 5% (media) e 10% (varianza).

---

## Distribuzioni Implementate

### 1. Esponenziale — `exponential(μ)`

**Funzione di densità**:
$$f(x) = \frac{1}{\mu} e^{-x/\mu}, \quad x > 0$$

**Proprietà**:
- **Media**: $E[X] = \mu$
- **Varianza**: $\text{Var}(X) = \mu^2$
- **Coefficiente di variazione**: $C_v = \frac{\sigma}{\mu} = 1$

**Uso**: modella arrivi Poissoniani e servizi "memoryless" (FCFS, M/M/1).

**Test**: $\mu = 5.0$, campioni: 100.000 → ✅ **PASS**

---

### 2. Uniforme — `uniform(a, b)`

**Funzione di densità**:
$$f(x) = \frac{1}{b - a}, \quad a < x < b$$

**Proprietà**:
- **Media**: $E[X] = \frac{a + b}{2}$
- **Varianza**: $\text{Var}(X) = \frac{(b - a)^2}{12}$
- **Coefficiente di variazione**: $C_v = \frac{1}{\sqrt{3}} \cdot \frac{b-a}{a+b}$

**Uso**: servizi con durata costante entro un range noto.

**Test**: $a = 2.0$, $b = 8.0$, campioni: 100.000 → ✅ **PASS**

---

### 3. Erlang a k stadi — `erlang(μ, k)`

Somma di $k$ variabili esponenziali i.i.d., ciascuna con media $\mu/k$.

**Funzione di densità**:
$$f(x) = \frac{(k/\mu)^k \cdot x^{k-1} \cdot e^{-kx/\mu}}{(k-1)!}, \quad x > 0$$

**Proprietà**:
- **Media**: $E[X] = \mu$ (media totale, non del singolo stadio)
- **Varianza**: $\text{Var}(X) = \frac{\mu^2}{k}$
- **Coefficiente di variazione**: $C_v = \frac{1}{\sqrt{k}} < 1$

**Interpretazione**: all'aumentare di $k$, la distribuzione si concentra attorno alla media (meno variabile dell'esponenziale). Per $k \to \infty$, converge a una distribuzione deterministica.

> **Nota implementativa**: `Rvgs.erlang(n, b)` richiede `b` = media del **singolo stadio**. Internamente calcoliamo `stageMean = μ / k`.

**Test**: $\mu = 10.0$, $k = 5$, campioni: 100.000 → ✅ **PASS**

---

### 4. Iperesponenziale — `hyperExponential(p, μ₁, μ₂)`

Distribuzione a **composizione stocastica**: con probabilità $p$ si campiona da $\text{Exp}(\mu_1)$, con probabilità $(1-p)$ da $\text{Exp}(\mu_2)$.

**Funzione di densità**:
$$f(x) = \frac{p}{\mu_1} e^{-x/\mu_1} + \frac{1-p}{\mu_2} e^{-x/\mu_2}, \quad x > 0$$

**Proprietà**:
- **Media**: $E[X] = p \cdot \mu_1 + (1-p) \cdot \mu_2$
- **Momento secondo**: $E[X^2] = 2 \left( p \cdot \mu_1^2 + (1-p) \cdot \mu_2^2 \right)$
- **Varianza**: $\text{Var}(X) = E[X^2] - (E[X])^2$
- **Coefficiente di variazione**: $C_v > 1$ (sempre più variabile dell'esponenziale)

**Uso**: modella servizi con alta variabilità (job eterogenei, es. classe Batch con mix di job corti/lunghi).

**Test**: $p = 0.7$, $\mu_1 = 10.0$, $\mu_2 = 2.0$, campioni: 100.000 → ✅ **PASS**

---

## Metodi Aggiuntivi per Tempi di Interarrivo

La classe espone anche:
- `getInterarrival(μ)`: wrapper per arrivi Poissoniani (esponenziale).
- `getHyperExpInterarrival(p, μ₁, μ₂)`: arrivi iperesponenziali per la classe **Batch** (punto 7 della consegna).

---

## Validazione Statistica

**Metodo**: Monte Carlo con $N = 100.000$ campioni per distribuzione.

**Metriche verificate**:
- **Media campionaria**: $\bar{X} = \frac{1}{N} \sum_{i=1}^N X_i$
- **Varianza campionaria**: $S^2 = \frac{1}{N} \sum_{i=1}^N X_i^2 - \bar{X}^2$

**Tolleranze**:
- Media: $\pm 5\%$ del valore teorico
- Varianza: $\pm 5\%$ del valore teorico (più sensibile a outlier, ma con tanti campionamenti converge bene. Attenzione a falsi negativi)

**Risultato**: ✅ **4/4 test superati** (`mvn test` eseguito con successo).

---

## File coinvolti

| File | Stato | Descrizione |
|---|---|---|
| `src/main/java/sim/ServiceGenerator.java` | **[NEW]** | Wrapper distribuzioni Leemis-Park |
| `src/test/java/sim/ServiceGeneratorTest.java` | **[NEW]** | Suite test JUnit5 (4 test) |

---

## Prossimi Step

- **Step 2**: Implementare la struttura event-driven (`Event.java`, `EventQueue.java`) e la logica FEL (Future Event List).
- **Step 3**: Simulatore base coda singola M/M/1 con raccolta statistiche ($E[N_s]$, $E[N_q]$, $E[T_s]$, $E[T_q]$).
- **Step 4**: Metodo delle repliche indipendenti + intervalli di confidenza.
- **Step 5+**: Estensione al sistema misto (Interactive + Batch) con priorità.
