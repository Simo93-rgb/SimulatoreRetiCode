# 🧠 FAQ e Scelte Progettuali del Simulatore

In questa sezione sono documentate le principali considerazioni teoriche, statistiche e algoritmiche che hanno guidato lo sviluppo del simulatore e l'analisi dei modelli a coda.

## 🎲 Statistica e Generazione di Numeri Casuali

<details>
<summary><b>Perché è fondamentale distanziare i semi iniziali (es. di $\Delta = 10^6$) per le diverse repliche?</b></summary>
<br>
Per garantire l'indipendenza statistica tra le repliche. L'uso di semi adeguatamente distanziati nello stream del generatore assicura che le sequenze di numeri pseudocasuali non si sovrappongano durante l'esecuzione. Questa indipendenza campionaria è un requisito matematico fondamentale per poter calcolare correttamente gli Intervalli di Confidenza.
</details>

<details>
<summary><b>Cosa accade matematicamente all'Intervallo di Confidenza se tutte le repliche utilizzano lo stesso identico seme iniziale?</b></summary>
<br>
La deviazione standard campionaria $S$ si azzera e l'intervallo collassa in un singolo punto con ampiezza nulla. Poiché repliche identiche producono risultati identici, non vi è alcuno scostamento dalla media aritmetica, annullando di fatto la stima dell'errore e rendendo l'analisi statistica priva di significato.
</details>

---

## 🌳 Motore Event-Driven e Strutture Dati (FEL)

<details>
<summary><b>Qual è il vantaggio algoritmico dell'uso di uno Splay Tree per implementare la Future Event List (FEL)?</b></summary>
<br>
Lo Splay Tree garantisce prestazioni eccellenti (costo ammortizzato $O(\log n)$) spostando i nodi acceduti di recente verso la radice. Poiché in una simulazione event-driven i nuovi eventi (come i brevi tempi di servizio appena schedulati) sono spesso imminenti, questi tendono a concentrarsi nei primissimi livelli dell'albero, rendendo le successive estrazioni quasi istantanee.
</details>

<details>
<summary><b>Nel codice della FEL, perché l'operazione di `dequeue()` eseguita subito dopo un `getMin()` ha un costo $O(1)$?</b></summary>
<br>
In un Albero Binario di Ricerca (BST), il nodo con il valore minimo si trova sempre all'estrema sinistra e, per definizione, non possiede un figlio sinistro. Pertanto, la rimozione fisica di questo nodo (divenuto radice grazie allo Splay Tree) non richiede riorganizzazioni complesse: è sufficiente scollegarlo promuovendo il suo intero sottoalbero destro a nuova radice.
</details>

<details>
<summary><b>In quale preciso istante viene estratto e schedulato nella FEL il tempo di servizio per un cliente in attesa?</b></summary>
<br>
Solo nel momento esatto in cui il cliente esce dalla coda ed entra fisicamente nel server libero. Generare e schedulare le partenze di tutti i clienti mentre sono ancora in attesa (o addirittura all'inizio della simulazione) popolerebbe inutilmente la FEL, degradando drasticamente le prestazioni del motore a eventi.
</details>

---

## 📊 Teoria delle Code e Formule Matematiche

<details>
<summary><b>Secondo la formula di Pollaczek-Khinchine, qual è l'impatto fisico di una distribuzione dei servizi con alta varianza (a parità di $\rho$ e $E[S]$)?</b></summary>
<br>
Il tempo medio di attesa in coda aumenta drasticamente. Lavori eccezionalmente lunghi bloccano il server per molto tempo, causando un accumulo di clienti e generando ritardi a catena. I momenti in cui il server è vuoto (a causa di lavori molto corti) rappresentano invece capacità di calcolo persa, che non può essere "risparmiata" per sveltire i futuri picchi di lavoro.
</details>

<details>
<summary><b>Quale distribuzione implementata nel simulatore possiede un coefficiente di variazione $C_s^2 > 1$?</b></summary>
<br>
La distribuzione Iperesponenziale. Essa modella un mix di comportamenti eterogenei (ad esempio, alternando servizi brevissimi a servizi estremamente lunghi), generando un traffico molto irregolare che porta il coefficiente di variazione a superare il limite dell'unità (tipico invece della distribuzione esponenziale).
</details>

<details>
<summary><b>In un sistema aperto stabile e senza perdita di clienti, qual è la relazione tra il tasso medio di arrivo $\lambda$ e il throughput $X$?</b></summary>
<br>
$X = \lambda$. Per il principio di conservazione del flusso in un sistema a capacità infinita e in equilibrio stazionario, tutto ciò che entra deve prima o poi uscire. Quindi, il tasso di scorrimento in uscita equivale al tasso di ingresso.
</details>

<details>
<summary><b>Qual è l'espressione corretta della Legge di Little applicata all'intero sistema?</b></summary>
<br>
$E[N] = X \cdot E[T]$. La Legge di Little stabilisce che il numero medio di elementi accumulati nel sistema ($E[N]$) è pari al tasso con cui vi transitano (throughput $X$) moltiplicato per il tempo medio totale di permanenza nel sistema ($E[T]$).
</details>

---

## 🌐 Dinamiche di Rete (Sistemi Chiusi e Misti)

<details>
<summary><b>In una rete mista, cosa succede al nodo secondario $Q_2$ quando il nodo primario $Q_1$ viene totalmente saturato da una classe aperta iperesponenziale?</b></summary>
<br>
L'utilizzo e il throughput di $Q_2$ crollano a causa del fenomeno dello <i>starvation</i> (affamamento). Essendo la popolazione chiusa rigorosamente limitata a un numero finito $N$, se la gran parte dei clienti rimane intrappolata nel "collo di bottiglia" creatosi in $Q_1$, i nodi a valle riceveranno un flusso di lavoro insufficiente per mantenersi occupati.
</details>