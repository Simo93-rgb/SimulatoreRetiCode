# Mapping Branch Git ↔ Punti Consegna

## 📋 Struttura Branch del Progetto

Questo file documenta la corrispondenza tra i branch Git e i punti della consegna (`docs/consegna2025-06.md`).

---

## 🗂️ Branch Organizzati per Punto Consegna

| Branch Git | Punto Consegna | Descrizione | Stato |
|------------|----------------|-------------|-------|
| `punto1` | **Punto 1** | Generazione numeri casuali (4 distribuzioni) | ✅ **COMPLETATO** |
| `punto2` | **Punto 2** | Gestione semi distanziati + stream indipendenti | ⬜ **DA FARE** |
| `punto3` | **Punto 3** | Simulatore M/M/1 + metodo repliche ripetute | ⬜ In preparazione |
| `punto4` | **Punto 4** | Stime puntuali/intervallari + IC | ⬜ Futuro |
| `punto5` | **Punto 5** | Validazione vs JMT (M/M/1, M/G/1) | ⬜ Futuro |
| `punto6` | **Punto 6** | Sistema chiuso (Q0-Q1-Q2, N clienti) | ⬜ Futuro |
| `punto7` | **Punto 7** | Sistema misto (classe aperta + chiusa) | ⬜ Futuro |
| `punto8` | **Punto 8** | Relazione finale + presentazione | ⬜ Futuro |

---

## 📚 Dettaglio Punti Consegna

### ✅ Punto 1 — Generazione Numeri Casuali (Branch: `punto1`)

**Requisiti**:
- ✅ Integrare generatore Leemis-Park (classi `Rng`, `Rvg`)
- ✅ Implementare 4 distribuzioni:
  1. Esponenziale (1 parametro: media)
  2. Uniforme (2 parametri: min, max)
  3. Erlang a k stadi (2 parametri: media, k)
  4. Iperesponenziale (3 parametri: p, mean1, mean2)

**Implementazione**:
- `src/main/java/sim/ServiceGenerator.java`
- `src/test/java/sim/ServiceGeneratorTest.java`

**Relazione**: `docs/punto1.md`

**Commit finale**: `8d4339f`

---

### ✅ Punto 2 — Gestione Semi e Stream (Branch: `punto2`) — COMPLETATO

**Requisiti** (da `consegna2025-06.md`):
> "Aggiungere i metodi per ottenere una lista di semi iniziali sufficientemente distanziati da usare per replicare la stessa simulazione più volte con semi diversi [...] e da usare per gestire sequenze casuali indipendenti per diverse attività in ciascuna esecuzione (es. sequenza dei tempi di inter-arrivo, la sequenza dei tempi di servizio [...]). Potete usare le classi **Rngs e Rvgs**."

**Cosa Implementare**:
1. ✅ Classe `SeedManager.java`:
   - Metodo `generateDistancedSeeds(long baseSeed, int numSeeds)` → lista semi spaziati di $10^6$
   - Documentazione utilizzo `Rngs.plantSeeds()`
   
2. ✅ Esempio utilizzo stream indipendenti:
   ```java
   rngs.selectStream(0); // Stream arrivi
   double interarrival = rvgs.exponential(lambda);
   
   rngs.selectStream(1); // Stream servizi
   double service = rvgs.exponential(mu);
   ```

3. ✅ Test indipendenza statistica:
   - Verifica correlazione tra stream 0 e stream 1 → ≈ 0
   - Test periodo generatore (Leemis: $2^{31} - 1$)

**File implementati**:
- `src/main/java/sim/SeedManager.java` ✅
- `src/test/java/sim/SeedManagerTest.java` ✅ (13 test)
- `docs/punto2.md` ✅ (relazione accademica)

**Stato**: ✅ **COMPLETATO** - Merged in `main`

**Commit finale**: `fefbf44`

---

### 🔄 Punto 3 — Simulatore + Repliche (Branch: `punto3-final`)

**Requisiti**:
- Simulatore coda singola M/M/1 event-driven
- Metodo delle prove ripetute (R run con semi diversi)
- Calcolo indici: throughput, utilization, tempo medio, lunghezza coda

**Nota**: Il branch `punto3-final` contiene:
- Punto 1 + Punto 2 (merged da `main`)
- Architettura event-driven da `punto3-prep` (Event, EventList, CustomerQueue)

**File da creare**:
- `src/main/java/sim/MMMOneSimulator.java`
- `src/main/java/sim/SimulationConfig.java`
- `src/main/java/sim/SimulationRunner.java`
- `src/test/java/sim/MMMOneSimulatorTest.java`
- `docs/punto3.md`

**Stato**: In preparazione (architettura event-driven pronta)

---

### ⬜ Punto 4 — Stime + IC (Branch: `punto4`)

**Requisiti**:
- Stime puntuali e intervallari degli indici
- Intervalli di confidenza al 95% (t-Student)
- Valutazione errore relativo

**Stato**: Futuro

---

### ⬜ Punto 5 — Validazione JMT (Branch: `punto5`)

**Requisiti**:
- Confronto simulazione vs JMT
- Esperimenti variando distribuzione arrivi/servizi
- Analisi impatto variabilità (Cv)

**Stato**: Futuro

---

### ⬜ Punto 6 — Sistema Chiuso (Branch: `punto6`)

**Requisiti**:
- Modello Q0 (terminali) → Q1 (CPU) → Q2 (I/O) → Q0
- N clienti fissi (classe chiusa)
- Esperimenti con 3-4 valori di N

**Stato**: Futuro

---

### ⬜ Punto 7 — Sistema Misto (Branch: `punto7`)

**Requisiti**:
- Classe chiusa Interactive (N clienti, terminali)
- Classe aperta Batch (arrivi iperesponenziali)
- Priorità/scheduling in Q1
- Analisi impatto carico Batch su Interactive

**Stato**: Futuro

---

### ⬜ Punto 8 — Relazione Finale (Branch: `punto8`)

**Requisiti**:
- Relazione sintetica (descrizione modelli, algoritmi, risultati)
- Presentazione PowerPoint/PDF per colloquio orale
- Grafici indici prestazione vs parametri

**Stato**: Futuro

---

## 🔧 Branch Tecnici Ausiliari

| Branch | Scopo | Nota |
|--------|-------|------|
| `main` | Branch principale (merge punti completati) | Produzione |
| `punto3-prep` | Preparazione architettura event-driven per punto3 | Tecnico (non corrisponde a punto consegna) |
| `fases-1-to-5` | Branch legacy (da eliminare) | Obsoleto |

---

## 📝 Workflow Git

### Implementazione Nuovo Punto

```bash
# 1. Checkout del branch punto
git checkout puntoX

# 2. Implementa codice + test
# ...

# 3. Commit
git add -A
git commit -m "feat(puntoX): Implementa [descrizione]"

# 4. Push
git push -u origin puntoX

# 5. Quando completo: merge in main
git checkout main
git merge puntoX
git push
```

### Sequenza Dipendenze

```
punto1 (base) ──→ punto2 ──→ punto3 ──→ punto4 ──→ punto5
                                ↓
                              punto6 ──→ punto7 ──→ punto8
```

**Nota**: `punto3` richiede anche `punto3-prep` (architettura event-driven).

---

## 🎯 Prossima Azione

**Attualmente su**: `punto2`

**Da implementare**: Gestione semi distanziati + stream indipendenti (vedi sezione dettagliata sopra).

**Dopo punto2**: Merge in `punto3` per avere sia gestione RNG che architettura event-driven.

---

**Ultima modifica**: 2026-02-18  
**Branch attivo**: `punto2`

