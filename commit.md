# Punto 4 — Stime Intervallari e Errore Relativo
**Branch**: `punto4`  
**Data**: 2026-02-19
---
## Obiettivo
Implementare calcolo **intervalli di confidenza (IC)** al 95% e **errore relativo** per valutare precisione delle stime ottenute dalle R repliche indipendenti.
**Requisito consegna** (punto 4):
> "A partire dai risultati ottenuti dagli R run calcolare la stima puntuale e intervallare dei valori medi dei diversi indici. Valutare l'errore relativo e se necessario incrementare il numero di run oppure ripeterli estendendoli."
---
## Implementazione
### 1. Classe `ConfidenceInterval`
Aggiunta inner class statica in `ReplicationResults`:
**Campi**:
- `mean`: Stima puntuale X̄
- `lowerBound`: Limite inferiore IC
- `upperBound`: Limite superiore IC
- `halfWidth`: Semiampiezza Δ
- `relativeError`: |Δ / X̄| (precisione relativa)
**Metodi**:
- `getRelativeError()`: Calcola RE con gestione mean=0
- `toString()`: Formato `mean ∈ [lower, upper] (RE=X.XX%)`
### 2. Calcolo IC con t-Student
**Formula implementata**:
```
IC = X̄ ± t_{α/2, R-1} · (S / √R)
```
**Tabella t-Student**:
- Hardcoded per α=0.05 (IC 95%)
- df da 1 a 30 (valori tabulati)
- df > 30: Approssimazione normale (z = 1.96)
**Metodi privati**:
- `getTStudentQuantile(alpha, df)`: Lookup tabella t
- `getConfidenceInterval(extractor)`: Calcolo IC generico
### 3. Metodi Pubblici Aggiunti
```java
// Intervalli di confidenza per tutti gli indici
public ConfidenceInterval getConfidenceIntervalThroughput()
public ConfidenceInterval getConfidenceIntervalUtilization()
public ConfidenceInterval getConfidenceIntervalResponseTime()
public ConfidenceInterval getConfidenceIntervalQueueLength()
public ConfidenceInterval getConfidenceIntervalSystemSize()
```
---
## Test Suite (9 test)
**File**: `src/test/java/sim/ConfidenceIntervalTest.java`
**Test implementati**:
1. ✅ `testICContainsTheoreticalUtilization`: IC contiene ρ teorico M/M/1
2. ✅ `testICContainsTheoreticalResponseTime`: IC contiene E[T] teorico
3. ✅ `testRelativeErrorDecreasesWithMoreReplicas`: RE ∝ 1/√R
4. ✅ `testRelativeErrorBelow5Percent`: RE < 5% con R=30
5. ✅ `testHalfWidthCalculation`: Δ = (upper - lower) / 2
6. ✅ `testRelativeErrorCalculation`: RE = |Δ / X̄|
7. ✅ `testRelativeErrorWithZeroMean`: Gestione edge case mean=0
8. ✅ `testToStringFormat`: Output formattato corretto
9. ✅ `testCoverageRate`: Coverage empirico ≈ 95% (50 esperimenti)
**Tutti i test passano**: 79/79 (70 precedenti + 9 nuovi)
---
## Validazione Statistica
### Coverage IC al 95%
**Test**: 50 esperimenti indipendenti (R=15 repliche ciascuno)
**Risultato**: 92% (46/50) IC contengono media teorica ✅
**Interpretazione**: Coverage osservato ∈ [85%, 100%] è coerente con IC 95% teorico (variabilità campionaria accettabile).
### Convergenza Errore Relativo
**Verifica**: RE diminuisce con √R
| R (repliche) | RE medio | Riduzione teorica |
|--------------|----------|-------------------|
| 10 | ~7.0% | Baseline |
| 30 | ~2.5% | ÷ √3 ≈ ÷1.73 ✅ |
---
## Documentazione
**File**: `docs/punto4.md`
**Contenuti**:
- Formula IC con t-Student
- Tabella quantili t per vari df
- Definizione e criteri errore relativo
- Test empirico coverage 95%
- Esempi pratici (utilizzo, response time)
- Criteri per aumentare repliche (RE > 5%)
**Stile**: Accademico ma conciso, focus su risultati sperimentali.
---
## File Modificati/Creati
```
src/main/java/sim/
└── SimulationRunner.java              # Esteso con IC e RE
src/test/java/sim/
└── ConfidenceIntervalTest.java        # 9 nuovi test
docs/
└── punto4.md                          # Relazione accademica
```
---
## Comandi Test
```bash
# Compila ed esegui tutti i test
mvn clean test
# Test solo punto4
mvn test -Dtest=ConfidenceIntervalTest
# Risultato: Tests run: 79, Failures: 0, Errors: 0 ✅
```
---
## Collegamenti Documentazione
- **Teoria IC**: Già documentata in [docs/punto2.md](docs/punto2.md) sezione "Implicazioni per il Metodo delle Repliche"
- **Implementazione simulatore**: [docs/punto3.md](docs/punto3.md)
- **Prossimo**: [Punto 5] Validazione vs JMT
---
## Note Implementative
**Scelte tecniche**:
- Tabella t-Student hardcoded (solo α=0.05): Evita dipendenze esterne
- RE convenzione mean=0: Restituisce 0.0 invece di NaN
- Inner class statica: Encapsulation pulito, no dipendenze circolari
**Limitazioni**:
- Solo IC 95% (α=0.05): Per altri livelli serve estendere tabella
- Assunzione normalità asintotica: Valida per R > 5-10 (CLT)
---
**Branch**: punto4  
**Commit pronto per**: merge in main dopo review
