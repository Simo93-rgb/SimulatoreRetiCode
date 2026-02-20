# Punto 5 — Validazione vs JMT con Variabilità Servizio
**Branch**: `punto5`  
**Data**: 2026-02-20
---
## Obiettivo
Validare simulatore confrontando risultati con JMT su modelli M/M/1 e **variando distribuzione servizio** per studiare impatto variabilità (Cap. 4 Leemis-Park).
**Requisito consegna** (punto 5):
> "Confrontare i risultati con JMT effettuando più esperimenti **al variare della distribuzione di probabilità** di tempi di servizio (impatto variabilità come cap. 4 Leemis-Park)."
---
## Implementazione Tecnica
### 1. Estensione `SimulationConfig`
Aggiunto enum `ServiceDistribution` e costruttore sovraccaricato:
```java
public enum ServiceDistribution {
    EXPONENTIAL,      // M/M/1 (cv=1, default)
    DETERMINISTIC,    // M/D/1 (cv≈0)
    ERLANG,           // M/Ek/1 (cv=1/√k)
    HYPEREXPONENTIAL  // M/H₂/1 (cv>1)
}
```
**Costruttore default** (retrocompatibilità):
```java
public SimulationConfig(double λ, double μ, long N) {
    this(λ, μ, N, ServiceDistribution.EXPONENTIAL, 0, 0, 0, 0);
}
```
**Costruttore esteso**:
```java
public SimulationConfig(double λ, double μ, long N,
                         ServiceDistribution dist,
                         int erlangK, double hyperP, 
                         double hyperMean1, double hyperMean2)
```
**Parametri aggiuntivi**:
- `erlangK`: Parametro k (solo per ERLANG)
- `hyperP`, `hyperMean1`, `hyperMean2`: Parametri Hyperexponential
---
### 2. Modifico `MMMOneSimulator.startService()`
Aggiunto metodo `generateServiceTime()` con switch su tipo distribuzione:
```java
private double generateServiceTime() {
    double mean = config.getMeanService();
    return switch (config.getServiceDistribution()) {
        case EXPONENTIAL -> serviceGen.exponential(mean);
        case DETERMINISTIC -> 
            // Approssimazione: Uniform[mean*0.9999, mean*1.0001]
            serviceGen.uniform(mean * 0.9999, mean * 1.0001);
        case ERLANG -> 
            serviceGen.erlang(mean, config.getErlangK());
        case HYPEREXPONENTIAL -> 
            serviceGen.hyperExponential(
                config.getHyperP(),
                config.getHyperMean1(),
                config.getHyperMean2()
            );
    };
}
```
**Nota**: Usa distribuzioni **già implementate** in `ServiceGenerator` (Punto 1).
---
### 3. Aggiornamento `Punto5DataCollection`
Implementati Esperimenti 4, 5, 6:
**Esperimento 4 - M/D/1** (cv²=0):
```java
new SimulationConfig(0.8, 1.0, 100k, DETERMINISTIC, 0, 0, 0, 0);
```
**Esperimento 5 - M/H₂/1** (cv²>1):
```java
new SimulationConfig(0.8, 1.0, 100k, HYPEREXPONENTIAL, 
                     0, 0.5, 0.5, 1.5);
// E[S] = 0.5*0.5 + 0.5*1.5 = 1.0 ✅
```
**Esperimento 6 - M/Ek/1** (cv²=0.25):
```java
new SimulationConfig(0.8, 1.0, 100k, ERLANG, 4, 0, 0, 0);
// cv² = 1/k = 1/4 = 0.25
```
---
## Risultati Simulatore (6 Esperimenti)
### Exp 1-3: M/M/1 Baseline
| Exp | ρ | E[T] Sim | E[T] Teoria | Δ% |
|-----|---|----------|-------------|-----|
| 1 | 0.8 | 4.97 | 5.00 | 0.6% |
| 2 | 0.5 | 2.00 | 2.00 | 0.0% |
| 3 | 0.9 | 9.82 | 10.00 | 1.8% |
**Validazione**: ✅ Δ < 2% su M/M/1
---
### Exp 4: M/D/1 (Deterministic, cv²≈0)
**Parametri**: λ=0.8, D=1.0 (approx Uniform[0.9999, 1.0001])
**Risultati**:
```
E[T]: 2.9870 ∈ [2.9645, 3.0095]  (Teoria: 3.0)  Δ=0.43%
E[Nq]: 1.5891 ∈ [1.5692, 1.6089]  (Teoria: 1.6)  Δ=0.68%
E[N]: 2.3887 ∈ [2.3679, 2.4096]  (Teoria: 2.4)  Δ=0.47%
```
**Validazione**: ✅ Match perfetto teoria M/D/1 (Pollaczek-Khinchine)  
**Vs M/M/1**: E[T] ridotto del **40%** (3.0 vs 5.0) con stessa ρ!
---
### Exp 5: M/H₂/1 (Hyperexponential, cv²>1)
**Parametri**: λ=0.8, Hyperexp(p=0.5, m1=0.5, m2=1.5)
**Risultati**:
```
E[T]: 5.9904 ∈ [5.9243, 6.0565]
E[Nq]: 3.9902 ∈ [3.9357, 4.0447]
E[N]: 4.7905 ∈ [4.7345, 4.8465]
ρ: 0.8003 ∈ [0.7984, 0.8022]
```
**Validazione**: ✅ E[T] > M/M/1 (5.99 vs 4.97) → Impatto alta variabilità  
**Vs M/M/1**: E[T] aumentato del **20%** con stessa ρ e media servizio!
---
### Exp 6: M/Ek/1 (Erlang-4, cv²=0.25)
**Parametri**: λ=0.8, Erlang(k=4, mean=1.0)
**Risultati**:
```
E[T]: 3.4920 ∈ [3.4550, 3.5289]
E[Nq]: 1.9928 ∈ [1.9615, 2.0240]
E[N]: 2.7926 ∈ [2.7603, 2.8249]
```
**Validazione**: ✅ E[T] tra M/D/1 e M/M/1 (3.49 vs 3.0 vs 4.97)  
**Vs M/M/1**: E[T] ridotto del **30%** (3.49 vs 4.97)
---
## Confronto Impatto Variabilità
**Stesso ρ=0.8, stessa E[S]=1.0, SOLO varianza cambia**:
| Distribuzione | cv² | E[T] | Δ vs M/M/1 |
|---------------|-----|------|------------|
| **Deterministic** | ~0 | 2.99 | -40% |
| **Erlang-4** | 0.25 | 3.49 | -30% |
| **Exponential** | 1.0 | 4.97 | baseline |
| **Hyperexp** | >1 | 5.99 | +20% |
**Conclusione chiave**: **Varianza servizio impatta drasticamente E[T]** anche con media e ρ identici!
Questo conferma Cap. 4 Leemis-Park: "conoscere solo media e ρ non basta per prevedere prestazioni".
---
## Test e Retrocompatibilità
**Test totali**: 79/79 (100% pass) ✅
**Retrocompatibilità garantita**:
- Costruttore default `SimulationConfig(λ, μ, N)` → EXPONENTIAL
- Tutti i test esistenti passano senza modifiche
- Nessuna breaking change
---
## File Modificati
```
src/main/java/sim/
├── SimulationConfig.java          # +enum ServiceDistribution, +parametri
├── MMMOneSimulator.java            # +generateServiceTime()
└── Punto5DataCollection.java      # +Exp 4,5,6 implementati
docs/
└── punto5.md                       # Tabelle dati (compilate dall'utente)
```
---
## Preparazione per JMT
L'utente eseguirà esperimenti JMT con:
- **Exp 4**: Service Distribution = Deterministic (D=1.0)
- **Exp 5**: Service Distribution = Hyperexponential (parametri specificati)
- **Exp 6**: Service Distribution = Erlang (k=4, mean=1.0)
Dopo raccolta dati JMT, completeremo tabelle in `punto5.md` con confronto quantitativo.
---
## Note Tecniche
**Bug fix Hyperexponential**:
- Parametri iniziali (p=0.8, m1=0.8333, m2=5.0) davano E[S]=1.67 → ρ>1!
- Corretti a (p=0.5, m1=0.5, m2=1.5) → E[S]=1.0 ✅
**Approssimazione Deterministic**:
- JMT supporta "Deterministic" nativo
- Simulatore usa Uniform[0.9999mean, 1.0001mean] (cv² ≈ 10⁻⁸)
---
**Branch**: punto5  
**Pronto per**: Raccolta dati JMT Exp 4-6, poi completamento relazione
