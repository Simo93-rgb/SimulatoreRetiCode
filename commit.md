

# **FASE 2.A — Migrazione Strutture Dati Event-Driven**

**Obiettivo**: Portare nel nostro package `sim.*` le classi `Event`, `EventList`, `Queue` con refactoring minimo.

**Azioni**:
1. **Copiare e adattare** `SimUtils/*.java` → `src/main/java/sim/core/`:
   ```
   src/main/java/sim/core/
   ├── Event.java          (già esistente, da refactoring)
   ├── EventList.java      (Splay Tree FEL)
   └── CustomerQueue.java  (rinominata da Queue.java per chiarezza)
   ```

2. **Refactoring di `Event.java`**:
    - Aggiungere **tipo evento** come enum invece di `int`:
      ```java
      public enum EventType { ARRIVAL, DEPARTURE, END_THINK_TIME, ... }
      ```
    - Aggiungere campo `Customer` (o `Job`) invece di usare `Event` stesso come customer descriptor
    - Mantenere `Comparable<Event>` per ordinamento FEL

3. **Test strutture dati**:
    - Test JUnit5 per `EventList` (insert, getMin, dequeue)
    - Test JUnit5 per `CustomerQueue` (enqueue, dequeue, FIFO order)

**Output**:
- Package `sim.core.*` con strutture dati testate
- File: `EventTest.java`, `EventListTest.java`, `CustomerQueueTest.java`

---

Vedere [step2a](docs/step2a.md) per relazione completa.