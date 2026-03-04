Traccia per la relazione sulla parte pratica dell’esame di Valutazione delle Prestazioni e Simulazione:

Esercizio di estensione del simulatore di esempio

## Completamento del simulatore di coda singola

Questa sezione fa riferimento a quanto richiesto nei punti da 1 a 4 della consegna.

Descrizione delle principali nuove classi introdotte illustrando con un diagramma delle classi come sono state

integrate con quelle esistenti e con le librerie per la generazione di numeri casuali e di istanze di variabili

casuali (con random stream multiple) Rngs e Rvgs scaricate dal sito del prof. Leemis:

http://www.math.wm.edu/~leemis/ .

Non è necessario riportare il codice prodotto in questa relazione: si può fare riferimento direttamente ai file

del codice java, se serve a spiegare come è stato realizzato il simulatore esteso. È consigliabile prevedere la

possibilità di acquisire da file o da linea di comando parametri come le distribuzioni dei tempi di interarrivo

e di servizio e i loro parametri, il criterio di fine simulazione (es. un tempo massimo), il seme iniziale, il numero

di run previsti e il livello di confidenza. Attenzione a re-inizializzare stato, FEL, accumulatori ogni volta che si

avvia un nuovo run di simulazione. Prevedere la possibilità di incrementare il numero di run se l’accuratezza

(relativa) raggiunta non è soddisfacente.

## Calcolo degli indici del modello di coda singola e verifica del modello

In questa sezione si devono riportare gli indici calcolati e confrontare le stime ottenute tramite il simulatore

esteso con i valori esatti che si possono ottenere tramite le formule per la coda M/M/1 o M/G/1 oppure con

le stime ricavate su un modello analogo tramite Java Modelling Tools. Riportare in una tabella le stime

puntuali e intervallari dei principali indici richiesti nella consegna, indicando su una stessa riga i valori calcolati

con il simulatore esteso da voi, quelli calcolati con le formule (per le distribuzioni che lo consentono) oppure

quelli calcolati con il simulatore di Java Modelling Tools.

La tabella dovrà contenere varie parti riferite a valori diversi dei parametri delle distribuzioni previsti in input

(facendo variare sia il tipo di distribuzione sia i valori dei loro parametri, per mostrare l’andamento al variare

del carico ma anche al variare del coefficiente di variazione dei tempi di servizio – mantenendo quelli di

interarrivo esponenziali, oppure al variare del coefficiente di variazione dei tempi di interarrivo –

mantenendo quelli di servizio esponenziali). Si può prendere ispirazione dal capitolo 4 del testo (open access):

G. Serazzi, Performance Engineering - Learning Through Applications Using JMT, Springer 2024

## Costruzione della rete di code con una sola classe di clienti chiusa (sistema interattivo)

Riportare le principali scelte implementative descrivendo brevemente la struttura dati che rappresenta lo

stato del sistema in ogni istante, gli eventi previsti, gli indici di prestazione e i relativi accumulatori. Indicare

le classi da integrare/sostituire nello schema del primo simulatore per poter eseguire esperimenti sul modello

del sistema interattivo. Anche in questo caso è utile poter facilmente eseguire il modello al variare di alcuni

parametri. I metodi dedicati alla gestione degli eventi possono essere descritti nel report riportandoli in

forma di pseudo codice, oppure si può fare riferimento direttamente ai metodi contenuti nel codice java

prodotto (indicando nome file e numero di linea dove inizia il metodo): si raccomanda di commentare

adeguatamente queste parti del codice, in modo da renderle auto-esplicative.

## Calcolo degli indici del modello del sistema interattivo e verifica del modello

In questa sezione si devono riportare gli indici calcolati e confrontare le stime ottenute tramite il simulatore

di sistema interattivo (chiuso) con le stime ricavate su un modello analogo tramite Java Modelling Tools. Si

dovranno effettuare vari esperimenti facendo variare alcuni parametri (uno alla volta): il numero totale di job nel sistema (N), i parametri delle distribuzioni che caratterizzano i vari componenti del sistema interattivo.

Individuare almeno 3 diversi valori di N corrispondenti ad un carico molto leggero, uno medio e uno intenso

(per intenso si intende un valore di carico dove indici come throughput o tempo medio di risposta sono vicini

ai valori asintotici ricavabili con l’analisi operazionale).

Riportare in una tabella le stime puntuali e intervallari dei principali indici richiesti nella consegna, indicando

su una stessa riga i valori calcolati con il simulatore sviluppato da voi e quelli calcolati con il simulatore di Java

Modelling Tools.

## Costruzione della rete di code che rappresenta un sistema misto, con due classe di clienti: una chiusa

(clienti interattivi) e una aperta (clienti batch)

Riportare le principali scelte implementative descrivendo brevemente la struttura dati che rappresenta lo

stato del sistema in ogni istante, gli eventi previsti, gli indici di prestazione e i relativi accumulatori. Poiché si

tratta di una estensione rispetto al modello precedente descrivere solo le differenze rispetto al sistema

interattivo.

## Calcolo degli indici del modello del sistema misto (con job interattivi e batch), verifica del modello,

valutazione dell’impatto del carico batch sui tempi di risposta per i job interattivi.

In questa sezione si devono riportare gli indici calcolati e confrontare le stime ottenute tramite il simulatore

di sistema misto (interattivo e batch) con le stime ricavate su un modello analogo tramite Java Modelling

Tools. Si dovranno effettuare vari esperimenti facendo variare alcuni parametri (uno alla volta). In particolare

scegliere una configurazione già studiata nel modello del sistema interattivo prevedendo un carico interattivo

(valore di N) non particolarmente intenso in modo che ci sia ancora spazio per servire i job batch nel centro

di servizio Q1, e far crescere il carico batch (tasso di arrivi di job della classe batch) senza cambiare i parametri

delle distribuzioni che caratterizzano i vari componenti del sistema interattivo. Valutare l’impatto

dell’introduzione del carico batch su temi di risposta del sistema per i job interattivi.

Considerazioni finali

Eventuali considerazioni finali su problemi riscontrati (e come li avete risolti), scelte implementative,

osservazioni che ritenete interessate riportare.