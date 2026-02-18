### 1. Il "Motore" Matematico (File *Pagine48-69-RNG.pdf*)

Il PDF descrive i **Generatori di Lehmer** (Congruenziali Lineari Moltiplicativi) con la formula:

$$x_{x+1}=(a*x_i)\mod m$$


utilizzando il famoso moltiplicatore $a=48271$ e il modulo $m=2^{31}-1$.

* **Nel tuo progetto:** Il file `Rng.java` (che hai spostato in `libraries/`) implementa esattamente questo algoritmo. È il generatore "single stream" che garantisce la qualità statistica dei numeri casuali di base.

### 2. L'Indipendenza dei Flussi (File *Pagine107-114.pdf*)

Questa è la parte più importante per il tuo voto. Il PDF spiega che in una simulazione complessa non basta un solo generatore, ma servono **più sorgenti di casualità** (streams) per evitare che, ad esempio, i tempi di arrivo influenzino matematicamente i tempi di servizio ("correlazione indesiderata").

* **La teoria:** Il metodo Leemis divide il ciclo del generatore (che è lunghissimo) in 256 sottosequenze (streams) distanziate di 100.000 numeri l'una.
* **Nel tuo progetto:** Abbiamo incluso `Rngs.java`. Nel codice che ti ho proposto prima (e che useremo), utilizziamo:
```java
rngs.selectStream(0); // Per gli arrivi
// ... genera arrivo ...
rngs.selectStream(1); // Per i servizi
// ... genera servizio ...

```


Questo rispetta fedelmente il paragrafo "3.2.1 STREAMS" del PDF, garantendo che le variabili aleatorie del tuo simulatore siano stocasticamente indipendenti.

### 3. La gestione dei Seed (PlantSeeds)

Il PDF sottolinea l'importanza di inizializzare correttamente i generatori per avere risultati riproducibili (fondamentale per il debugging e la validazione).

* **Nel tuo progetto:** Nel `Main.java` abbiamo inserito:
```java
r.plantSeeds(123456789);

```


Questo comando inizializza tutti i 256 stream in un colpo solo, "piantando" lo stato iniziale come richiesto dalla documentazione tecnica allegata.