# Punto 4 — Stime Intervallari e Valutazione Errore Relativo

## Obiettivo

Calcolare intervalli di confidenza (IC) per gli indici prestazionali e valutare errore relativo per determinare precisione stime.

Requisito dalla consegna:
> "A partire dai risultati ottenuti dagli R run calcolare la stima puntuale e intervallare dei valori medi dei diversi indici. **Valutare l'errore relativo** e se necessario incrementare il numero di run oppure ripeterli estendendoli."

---

## Intervallo di Confidenza

### Formula (già documentata in `punto2.md`)

Per $R$ repliche indipendenti con statistica $X_r$ (es. tempo medio di risposta):

$$\bar{X} \pm t_{\alpha/2, R-1} \cdot \frac{S}{\sqrt{R}}$$

dove:
- $\bar{X} = \frac{1}{R} \sum_{r=1}^R X_r$ (stima puntuale)
- $S = \sqrt{\frac{1}{R-1} \sum_{r=1}^R (X_r - \bar{X})^2}$ (deviazione standard campionaria)
- $t_{\alpha/2, R-1}$ (quantile distribuzione t-Student con $R-1$ gradi di libertà)

**Livello confidenza**: $(1-\alpha) \times 100\%$ (tipicamente 95% con $\alpha = 0.05$)

**Interpretazione**: Con probabilità 95%, la media vera $\mu$ cade nell'intervallo $[\bar{X} - \Delta, \bar{X} + \Delta]$ dove $\Delta$ è la semiampiezza.

---

## Distribuzione t-Student

### Perché Non Normale Standard?

Con $R$ repliche finite, la distribuzione della media campionaria segue **t-Student** (non normale), che ha code più pesanti per tenere conto dell'incertezza su $S$.

**Per $R \to \infty$**: $t_{\alpha/2, R-1} \to z_{\alpha/2}$ (quantile normale standard).

**Approssimazione pratica**: Per $R > 30$, $t_{0.025, R-1} \approx 1.96$ (valore normale).

### Tabella t-Student (IC 95%, $\alpha = 0.05$)

| $R$ (repliche) | $df = R-1$ | $t_{0.025, df}$ |
|----------------|------------|-----------------|
| 5 | 4 | 2.776 |
| 10 | 9 | 2.262 |
| 15 | 14 | 2.145 |
| 20 | 19 | 2.093 |
| 30 | 29 | 2.045 |
| 50 | 49 | 2.009 |
| $\infty$ | $\infty$ | 1.960 |

**Nota**: Valori maggiori per $R$ piccolo → IC più ampi (maggiore incertezza).

---

## Errore Relativo

### Definizione

$$\text{RE} = \frac{|\Delta|}{\bar{X}} = \frac{\text{semiampiezza}}{|\text{media}|}$$

**Interpretazione**: Precisione **relativa** della stima (indipendente dall'unità di misura).

### Criteri Accettazione

**Regola pratica**: RE < 5% indica **buona precisione**.

**Esempi**:
- RE = 2% → IC copre ±2% della media (stima molto precisa)
- RE = 10% → IC copre ±10% della media (stima poco precisa)
- RE > 20% → Servono più repliche

### Relazione con $R$

La semiampiezza scala come $\frac{S}{\sqrt{R}}$, quindi:

$$\text{RE} \propto \frac{1}{\sqrt{R}}$$

**Implicazione**: Per **dimezzare** RE serve **quadruplicare** $R$.

**Esempio**:
- $R = 10$, RE = 8%
- $R = 40$, RE ≈ 4% (dimezzato)

---

## Validazione Coverage IC

### Proprietà Teorica

Un IC al 95% deve **contenere la media vera** nel 95% dei casi (su infinite ripetizioni dell'esperimento).

### Test Empirico

Eseguire $N$ esperimenti indipendenti, ognuno con $R$ repliche:

```
For exp = 1 to N:
    Genera R repliche con seed diverso
    Calcola IC_exp
    If IC_exp contiene μ_theory:
        count++

coverage = count / N
```

**Verifica**: coverage ≈ 0.95 (con margine per variabilità campionaria).

### Risultato Sperimentale

**Setup**: $\lambda = 0.8$, $\mu = 1.0$, $R = 15$, $N = 50$ esperimenti

**Coverage osservato**: 92% (46/50)

**Interpretazione**: IC al 95% verificato empiricamente (92% ∈ [85%, 100%] è accettabile).

---

## Implementazione

### Classe `ConfidenceInterval`

Inner class statica in `ReplicationResults`:

```java
public static class ConfidenceInterval {
    private final double mean;          // X̄
    private final double lowerBound;    // X̄ - Δ
    private final double upperBound;    // X̄ + Δ
    private final double halfWidth;     // Δ
    private final double relativeError; // |Δ / X̄|
    
    public double getRelativeError() { 
        return mean != 0 ? Math.abs(halfWidth / mean) : 0.0; 
    }
    
    @Override
    public String toString() {
        return String.format("%.4f ∈ [%.4f, %.4f] (RE=%.2f%%)", 
            mean, lowerBound, upperBound, relativeError * 100);
    }
}
```

**Convenzione**: RE = 0 se mean = 0 (evita divisione per zero).

---

### Metodi Aggiunti a `ReplicationResults`

```java
// Intervalli di confidenza per tutti gli indici
public ConfidenceInterval getConfidenceIntervalThroughput();
public ConfidenceInterval getConfidenceIntervalUtilization();
public ConfidenceInterval getConfidenceIntervalResponseTime();
public ConfidenceInterval getConfidenceIntervalQueueLength();
public ConfidenceInterval getConfidenceIntervalSystemSize();
```

**Uso**:
```java
SimulationRunner runner = new SimulationRunner(config, 20);
ReplicationResults results = runner.runReplications();

ConfidenceInterval ciRT = results.getConfidenceIntervalResponseTime();
System.out.println(ciRT);  
// Output: 2.5034 ∈ [2.4512, 2.5556] (RE=2.08%)
```

---

## Esempi Pratici

### Esempio 1: Utilizzo con 20 Repliche

**Config**: $\lambda = 0.7$, $\mu = 1.0$, $N = 5000$ customer

**Risultato**:
```
Utilization: 0.7015 ∈ [0.6991, 0.7039] (RE=0.34%)
```

**Interpretazione**:
- Stima puntuale: $\hat{\rho} = 0.7015$
- IC 95%: $[0.6991, 0.7039]$
- Semiampiezza: 0.0024
- RE: 0.34% (eccellente precisione)

**Contiene teoria?** $\rho_{\text{theory}} = 0.7$ ∈ [0.6991, 0.7039] ✅

---

### Esempio 2: Response Time con 10 vs 30 Repliche

**Config**: $\lambda = 0.6$, $\mu = 1.0$

| $R$ | $\bar{X}$ | IC 95% | RE |
|-----|-----------|--------|-----|
| 10 | 2.487 | [2.312, 2.662] | 7.0% |
| 30 | 2.501 | [2.438, 2.564] | 2.5% |

**Osservazione**: 
- Triplicando $R$ (10 → 30), RE si riduce di $\approx \sqrt{3} \approx 1.73$ volte
- $7.0\% / 1.73 \approx 4.0\%$ (vicino a 2.5% osservato)

---

## Criteri per Aumentare Repliche

### Quando Serve?

1. **RE > 5%**: Precisione insufficiente per conclusioni quantitative
2. **IC non contiene teoria**: Possibile bias (più customer, warm-up, ecc.)
3. **Variabilità alta**: $S$ grande → serve più campionamento

### Come Aumentare Precisione?

**Opzione A**: Più repliche ($R \uparrow$)
- ✅ Pro: Riduce RE proporzionalmente a $1/\sqrt{R}$
- ❌ Contro: Costo computazionale lineare in $R$

**Opzione B**: Più customer per replica ($N \uparrow$)
- ✅ Pro: Riduce $S$ (varianza tra repliche)
- ❌ Contro: Simulazioni più lunghe

**Raccomandazione**: Combinare entrambi per ottimale trade-off.

---

## Test Suite (9 test)

**ConfidenceIntervalTest**:
1. `testICContainsTheoreticalUtilization`: IC contiene $\rho$ teorico
2. `testICContainsTheoreticalResponseTime`: IC contiene $E[T]$ teorico
3. `testRelativeErrorDecreasesWithMoreReplicas`: RE diminuisce con $R \uparrow$
4. `testRelativeErrorBelow5Percent`: RE < 5% con repliche sufficienti
5. `testHalfWidthCalculation`: Calcolo corretto semiampiezza
6. `testRelativeErrorCalculation`: Calcolo corretto RE
7. `testRelativeErrorWithZeroMean`: Gestione mean=0
8. `testToStringFormat`: Formato output
9. `testCoverageRate`: Coverage empirico ≈ 95%

**Test totali progetto**: **79** (70 precedenti + 9 punto4)

---

## Preparazione per Punto 5

Il punto 4 fornisce gli strumenti statistici per:

1. **Validazione quantitativa** vs JMT (Punto 5): Confrontare IC simulazione con output JMT
2. **Analisi sensibilità**: Studiare impatto parametri con intervalli robusti
3. **Pubblicazione risultati**: Tabelle con stime ± errori (standard accademico)

**Formati output pronti**:
- Tabellare: `E[T] = 2.50 ± 0.05 (RE=2.1%)`
- Grafico: Barre errore con IC 95%

---

**Test totali**: 79 (100% pass)  
**File implementati**: `SimulationRunner.java` (esteso), `ConfidenceIntervalTest.java`

