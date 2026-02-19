# Simulatore Reti di Code
Progetto per l'esame di **Valutazione delle Prestazioni** - A.A. 2025-26
Simulatore event-driven di reti di code in Java, basato su generatori RNG Leemis-Park.
---
## 📚 Documentazione
Tutta la documentazione si trova in [`docs/`](docs/):
### Consegna e Riferimenti
- **[`consegna2025-06.md`](docs/consegna2025-06.md)** - Testo ufficiale esercizio (8 punti)
- **[`Figura1.jpg`](docs/Figura1.jpg)** - Schema sistema misto
- **[`Leemis-Park.md`](docs/Leemis-Park.md)** - Estratti teorici
### Relazioni Punti
- ✅ **[`punto1.md`](docs/punto1.md)** - Generazione numeri casuali
- ✅ **[`punto2.md`](docs/punto2.md)** - Semi distanziati + stream
- 🔄 **`punto3.md`** - Simulatore M/M/1 + repliche (in corso)
### Documentazione Tecnica
- **[`architettura-event-driven.md`](docs/architettura-event-driven.md)** - Architettura event-driven
- **[`BRANCH_MAPPING.md`](docs/BRANCH_MAPPING.md)** - Mapping branch ↔ punti
---
## 🚀 Build
```bash
mvn clean test
```
**Test totali**: 17 (punto1: 4, punto2: 13)
