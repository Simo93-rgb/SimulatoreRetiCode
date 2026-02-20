Il capitolo 4 del libro in allegato descrive due macro-categorie di esperimenti progettati per dimostrare un concetto fondamentale nella teoria delle code: **a parità di medie e di utilizzo del sistema, cambiare la varianza delle distribuzioni di probabilità modifica drasticamente i tempi di attesa e di risposta**.

Ecco nel dettaglio i due set di esperimenti illustrati nel documento:

1. Impatto della variabilità dei tempi di Interarrivo (Sezione 4.2)

In questo primo gruppo di esperimenti, si studia cosa succede quando le richieste arrivano in modo più o meno irregolare, mantenendo costante il tempo di elaborazione del server:

*
**Servizio:** Mantenuto fisso a una distribuzione Esponenziale con media **S = 1 s** per tutti i modelli.


*
**Interarrivo:** Viene fatto variare testando 5 distribuzioni diverse, tutte con la stessa media ma con varianza (e coefficiente di variazione, indicato con **cv**) crescente:


* Costante (**cv = 0**).


* Ipo-esponenziale (**cv = 0.5**).


* Esponenziale (**cv = 1**).


* Iper-esponenziale (**cv = 5**).


* Iper-esponenziale (**cv = 10**).




*
**Obiettivo:** Valutare come cambia il tempo medio di risposta al variare del carico di arrivi (da **0.1** a **0.9** richieste al secondo).



2. Impatto della variabilità dei tempi di Servizio (Sezione 4.3)

Questo è considerato il caso "duale" del precedente.* **Interarrivo:** Fissato a una distribuzione Esponenziale per tutti i modelli.

*
**Servizio:** Viene fatto variare testando le stesse 5 distribuzioni viste sopra (Costante, Ipo-esponenziale, Esponenziale, Iper-esponenziale con **cv = 5** e **cv = 10**), mantenendo sempre la media a **1 s**.


*
**Obiettivo:** Questo set simula scenari reali in cui le richieste hanno pesi computazionali molto diversi tra loro (ad esempio, un server che calcola percorsi stradali, dove alcuni tragitti richiedono calcoli banali e altri sono estremamente complessi). Anche qui si misura il tempo di risposta al variare del tasso di arrivo.



---

### La conclusione chiave per il tuo progetto

Il risultato principale di questi esperimenti, che dovrai replicare, è che **conoscere solo la media degli arrivi o l'utilizzo del server non basta per prevedere i tempi di risposta**.

Ad esempio, nella tabella dei risultati della Sezione 4.3, con un carico altissimo del **90%** (arrivi a **0.9** req/s), il tempo di risposta passa da circa **6.51 s** (con servizio ipo-esponenziale) a oltre **453 s** (con servizio iper-esponenziale), pur avendo esattamente lo stesso utilizzo percentuale del server e la stessa media dei tempi di servizio!

Nel tuo "Punto 5", la consegna ti chiede di replicare questo approccio: testare cosa succede scambiando la classica distribuzione Esponenziale (sistema M/M/1) con distribuzioni a varianza maggiore o minore, per verificare che il tuo simulatore Java calcoli i tempi di risposta correttamente rispetto alla teoria dei modelli M/G/1.

