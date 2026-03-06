package sim;

import libraries.Rngs;
import libraries.Rvgs;
import sim.core.*;
import sim.core.Customer.CustomerClass;
import sim.SeedManager.StreamType;

/**
 * Simulatore event-driven per sistema misto: classe chiusa + classe aperta.
 *
 * <h2>Topologia</h2>
 * 
 * <pre>
 *        [Classe aperta: arrivi iperesponenziali]
 *                         │
 *   ┌─────────────────────▼───────────────────────┐
 *   │                     │                       │
 *   ▼                     ▼                       │
 *  Q0 (Delay) ──► Q1 (Server FCFS, 2 classi) ──► Q2 (Server FCFS)
 *                         │
 *                  [Classe aperta: esce]
 * </pre>
 *
 * <h2>Logica di routing a Q1</h2>
 * <ul>
 * <li>Classe chiusa → dopo Q1 va a Q2, poi torna a Q0</li>
 * <li>Classe aperta → dopo Q1 esce dal sistema (non va a Q2)</li>
 * </ul>
 *
 * <h2>Tempi di servizio Q1</h2>
 * <ul>
 * <li>Classe chiusa: esponenziale con media {@code S1}</li>
 * <li>Classe aperta: esponenziale con media {@code 2·S1} (da consegna)</li>
 * </ul>
 *
 * <h2>Stream RNG (Leemis-Park, nessun java.util.Random)</h2>
 * <ul>
 * <li>THINK_TIME (2): think time Q0 classe chiusa</li>
 * <li>SERVICE (1): servizio Q1 classe chiusa</li>
 * <li>ROUTING (3): servizio Q2</li>
 * <li>OPEN_ARRIVALS (4): inter-arrivi classe aperta (componente
 * iperesponenziale)</li>
 * <li>OPEN_SERVICE (5): servizio Q1 classe aperta</li>
 * </ul>
 *
 * <h2>Condizione di stop</h2>
 * {@code completionsQ1Closed >= maxCompletions} — usa la classe chiusa per
 * garantire un orizzonte di simulazione stabile indipendente dal carico aperto.
 */
public class MixedNetworkSimulator {

    private final MixedNetworkConfig config;
    private final Rngs rngs;
    private final Rvgs rvgs;

    private final EventList fel;
    private final CustomerQueue queueQ1;
    private final CustomerQueue queueQ2;

    private Customer serverQ1; // null = idle
    private Customer serverQ2; // null = idle

    private int inThinkQ0;

    private final MixedNetworkStatistics stats;
    private double clock;
    private long nextId;

    public MixedNetworkSimulator(MixedNetworkConfig config, long seed) {
        this.config = config;

        this.rngs = new Rngs();
        this.rngs.plantSeeds(seed);
        this.rvgs = new Rvgs(rngs);

        this.fel = new EventList();
        this.queueQ1 = new CustomerQueue();
        this.queueQ2 = new CustomerQueue();
        this.stats = new MixedNetworkStatistics();

        this.clock = 0.0;
        this.nextId = 1;
        this.serverQ1 = null;
        this.serverQ2 = null;
        this.inThinkQ0 = 0;
    }

    /**
     * Esegue la simulazione fino a {@code maxCompletions} completamenti totali su
     * Q1
     * (classe chiusa + classe aperta).
     *
     * <p>
     * Si usa il totale Q1 e non solo i completamenti chiusi per evitare deadlock:
     * ad alto carico aperto i clienti chiusi si accodano dietro la valanga di
     * aperti
     * e i loro completamenti diventano così rari da non raggiungere mai la soglia.
     * Il totale Q1 avanza sempre a ritmo ~1/S1_eff indipendentemente dal mix di
     * classi.
     * </p>
     */
    public MixedNetworkStatistics run() {
        initialize();

        while (stats.getCompletionsQ1Closed() + stats.getCompletionsQ1Open() < config.getMaxCompletions()) {
            Event event = fel.getMin();
            fel.dequeue();

            stats.updateAreas(event.getTime());
            clock = event.getTime();

            switch (event.getType()) {
                case END_THINK_TIME -> processEndThinkTime(event);
                case ARRIVAL -> processArrivalOpen(event);
                case DEPARTURE -> processDepartureQ1(event);
                case TIMEOUT -> processDepartureQ2(event);
                default -> throw new IllegalStateException("Evento sconosciuto: " + event.getType());
            }
        }

        return stats;
    }

    // =========================================================================
    // Inizializzazione
    // =========================================================================

    /**
     * Tutti gli N clienti chiusi partono in Q0.
     * Il primo arrivo della classe aperta viene schedulato al tempo 0.
     */
    private void initialize() {
        // Clienti classe chiusa → Q0
        for (int i = 0; i < config.getNumCustomers(); i++) {
            Customer c = new Customer(nextId++, clock, CustomerClass.CLOSED);
            scheduleEndThinkTime(c, clock);
            inThinkQ0++;
        }
        // Primo arrivo classe aperta
        scheduleNextOpenArrival(clock);
    }

    // =========================================================================
    // Gestione eventi
    // =========================================================================

    /**
     * Fine think time: cliente chiuso lascia Q0. Routing probabilistico: p1 -> Q1,
     * (1-p1) -> Q2.
     */
    private void processEndThinkTime(Event event) {
        Customer c = event.getCustomer();
        inThinkQ0--;

        rngs.selectStream(StreamType.ROUTING.ordinal());
        double u = rngs.random();

        if (u < config.getRoutingProbabilityQ1()) {
            c.setArrivalTimeAtQ1(clock);
            enqueueOrServeQ1(c);
        } else {
            c.setArrivalTimeAtQ2(clock);
            enqueueOrServeQ2(c);
        }
    }

    /**
     * Arrivo cliente aperto: entra direttamente in Q1.
     * Schedula subito il prossimo arrivo aperto (processo di Poisson non si ferma).
     */
    private void processArrivalOpen(Event event) {
        Customer c = event.getCustomer();
        c.setArrivalTimeAtQ1(clock);

        // Schedula prossimo arrivo aperto
        scheduleNextOpenArrival(clock);

        enqueueOrServeQ1(c);
    }

    /**
     * Partenza da Q1.
     *
     * - Registra stats Q1 per la classe corretta
     * - Avanza coda Q1
     * - Routing: chiuso → Q2 ; aperto → esce dal sistema
     */
    private void processDepartureQ1(Event event) {
        Customer departing = event.getCustomer();

        stats.recordDepartureQ1(departing.getArrivalTimeAtQ1(), clock, departing.getCustomerClass());
        stats.setServerBusyQ1(false, false);
        serverQ1 = null;

        if (!queueQ1.isEmpty()) {
            Customer next = queueQ1.dequeue();
            stats.setQueueLengthQ1(queueQ1.size());
            startServiceQ1(next);
        }

        if (departing.isOpen()) {
            // Classe aperta: esce dal sistema, nessuna azione aggiuntiva
        } else {
            // Classe chiusa: torna a Q0
            scheduleEndThinkTime(departing, clock);
            inThinkQ0++;
        }
    }

    /**
     * Partenza da Q2: cliente chiuso torna a Q0.
     */
    private void processDepartureQ2(Event event) {
        Customer departing = event.getCustomer();

        stats.recordDepartureQ2(departing.getArrivalTimeAtQ2(), clock);
        stats.setServerBusyQ2(false);
        serverQ2 = null;

        if (!queueQ2.isEmpty()) {
            Customer next = queueQ2.dequeue();
            stats.setQueueLengthQ2(queueQ2.size());
            startServiceQ2(next);
        }

        scheduleEndThinkTime(departing, clock);
        inThinkQ0++;
    }

    // =========================================================================
    // Helpers: accodamento e avvio servizio
    // =========================================================================

    private void enqueueOrServeQ1(Customer c) {
        if (serverQ1 == null) {
            startServiceQ1(c);
        } else {
            queueQ1.enqueue(c);
            stats.setQueueLengthQ1(queueQ1.size());
        }
    }

    private void enqueueOrServeQ2(Customer c) {
        if (serverQ2 == null) {
            startServiceQ2(c);
        } else {
            queueQ2.enqueue(c);
            stats.setQueueLengthQ2(queueQ2.size());
        }
    }

    // =========================================================================
    // Avvio servizi e scheduling (solo Leemis-Park, mai java.util.Random)
    // =========================================================================

    private void startServiceQ1(Customer c) {
        serverQ1 = c;
        stats.setServerBusyQ1(true, c.isOpen());

        double serviceTime;
        if (c.isOpen()) {
            // Classe aperta: media doppia — stream dedicato OPEN_SERVICE
            rngs.selectStream(StreamType.OPEN_SERVICE.ordinal());
            serviceTime = rvgs.exponential(config.getMeanServiceQ1Open());
        } else {
            // Classe chiusa — stream SERVICE
            rngs.selectStream(StreamType.SERVICE.ordinal());
            serviceTime = rvgs.exponential(config.getMeanServiceQ1Closed());
        }
        c.setServiceTime(serviceTime);
        fel.enqueue(new Event(EventType.DEPARTURE, clock + serviceTime, c));
    }

    private void startServiceQ2(Customer c) {
        serverQ2 = c;
        stats.setServerBusyQ2(true);

        rngs.selectStream(StreamType.ROUTING.ordinal());
        double serviceTime = rvgs.exponential(config.getMeanServiceQ2());
        fel.enqueue(new Event(EventType.TIMEOUT, clock + serviceTime, c));
    }

    private void scheduleEndThinkTime(Customer c, double from) {
        rngs.selectStream(StreamType.THINK_TIME.ordinal());
        double thinkTime = rvgs.exponential(config.getMeanThinkTime());
        fel.enqueue(new Event(EventType.END_THINK_TIME, from + thinkTime, c));
    }

    /**
     * Schedula il prossimo arrivo della classe aperta.
     *
     * Inter-arrivo iperesponenziale (composizione):
     * - con prob p → esponenziale(mean1) [stream OPEN_ARRIVALS]
     * - con prob 1-p → esponenziale(mean2) [stream OPEN_ARRIVALS]
     *
     * La scelta della componente usa rngs.random() sullo stesso stream,
     * mantenendo tutto su Leemis-Park.
     */
    private void scheduleNextOpenArrival(double from) {
        rngs.selectStream(StreamType.OPEN_ARRIVALS.ordinal());
        double u = rngs.random(); // U(0,1) Leemis-Park, NON java.util.Random
        double interarrival = (u < config.getOpenHyperP())
                ? rvgs.exponential(config.getOpenMeanArr1())
                : rvgs.exponential(config.getOpenMeanArr2());

        Customer c = new Customer(nextId++, from + interarrival, CustomerClass.OPEN);
        fel.enqueue(new Event(EventType.ARRIVAL, from + interarrival, c));
    }

    // =========================================================================
    // Getters
    // =========================================================================

    public double getClock() {
        return clock;
    }

    public MixedNetworkConfig getConfig() {
        return config;
    }

    public int getInThinkQ0() {
        return inThinkQ0;
    }
}
