# Simulatore Reti di Code
Progetto per l'esame di **Valutazione delle Prestazioni** - A.A. 2025-26
Simulatore event-driven di reti di code in Java, basato su generatori RNG Leemis-Park.

---

## 📚 Documentazione

Tutta la documentazione si trova in [`docs/`](docs/):

### Consegna e Riferimenti
- **[`consegna2025-06.md`](docs/consegna2025-06.md)** - Testo ufficiale esercizio (8 punti)
- **[`Figura1.jpg`](docs/Figura1.jpg)** - Schema sistema misto (chiuso + classe aperta)
- **[`Leemis-Park.md`](docs/Leemis-Park.md)** - Estratti teorici libro di testo

### Relazioni Punti
- ✅ **[`punto1.md`](docs/punto1.md)** - Generazione numeri casuali (Leemis-Park)
- ✅ **[`punto2.md`](docs/punto2.md)** - Semi distanziati e stream RNG indipendenti
- ✅ **[`punto3.md`](docs/punto3.md)** - Simulatore M/M/1 event-driven + repliche
- ✅ **[`punto4.md`](docs/punto4.md)** - Stime intervallari e errore relativo (IC 95%)
- ✅ **[`punto5.md`](docs/punto5.md)** - Validazione vs JMT e impatto variabilità servizio
- ✅ **[`punto6.md`](docs/punto6.md)** - Sistema chiuso Q0 → Q1 → Q2 (N variabile)
- ✅ **[`punto7.md`](docs/punto7.md)** - Sistema misto: classe chiusa + classe aperta su Q1

### Documentazione Tecnica
- **[`architettura-event-driven.md`](docs/architettura-event-driven.md)** - Architettura event-driven
- **[`BRANCH_MAPPING.md`](docs/BRANCH_MAPPING.md)** - Mapping branch ↔ punti

---

## 🚀 Build e Esecuzione

```bash
# Compilazione e test
mvn clean test

# Esegui raccolta dati per un punto specifico
mvn compile exec:java "-Dexec.args=punto5"
mvn compile exec:java "-Dexec.args=punto6"
mvn compile exec:java "-Dexec.args=punto7"

# Esegui e salva raccola dati per un punto specifico (Windows)
java -cp target/classes sim.runners.Punto5Runner | Out-File -Encoding utf8 "results/punto5_results.md"
java -cp target/classes sim.runners.Punto6Runner | Out-File -Encoding utf8 "results/punto6_results.md"
java -cp target/classes sim.runners.Punto7Runner | Out-File -Encoding utf8 "results/punto7_results.md"
```

**Test totali**: 115 (tutti i punti 1–7)

---

## 🗂️ Struttura Sorgenti

```
src/main/java/sim/
├── core/                        # Strutture dati event-driven
│   ├── Customer.java
│   ├── CustomerQueue.java
│   ├── Event.java
│   ├── EventList.java           # FEL (Splay Tree)
│   └── EventType.java
├── runners/                     # Entry point per raccolta dati per punto
│   ├── Punto5Runner.java
│   ├── Punto6Runner.java
│   └── Punto7Runner.java
├── Main.java                    # Dispatcher CLI  →  java sim.Main <puntoX>
├── ClosedNetworkConfig.java     # Configurazione sistema chiuso
├── ClosedNetworkSimulator.java  # Simulatore Q0→Q1→Q2
├── ClosedNetworkStatistics.java # Accumulatori sistema chiuso
├── ClosedNetworkRunner.java     # Repliche sistema chiuso + IC
├── MixedNetworkConfig.java      # Configurazione sistema misto
├── MixedNetworkSimulator.java   # Simulatore classe chiusa + aperta
├── MixedNetworkStatistics.java  # Accumulatori per classe
├── MixedNetworkRunner.java      # Repliche sistema misto + IC
├── MMMOneSimulator.java         # Simulatore M/M/1 (punti 3–5)
├── SeedManager.java             # Semi distanziati + stream
├── ServiceGenerator.java        # Generatore distribuzioni (Leemis-Park)
├── SimulationConfig.java        # Configurazione M/M/1
├── SimulationRunner.java        # Repliche M/M/1 + IC
└── SimulationStatistics.java    # Accumulatori M/M/1
```
