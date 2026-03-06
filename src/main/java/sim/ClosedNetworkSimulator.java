package sim;

import libraries.Rngs;
import libraries.Rvgs;
import sim.core.*;
import sim.SeedManager.StreamType;

/**
 * Simulatore event-driven per sistema chiuso a tre centri.
 *
 * <h2>Topologia (Figura 1 consegna)</h2>
 * 
 * <pre>
 *              p1
 *   Q0  ─────────────►  Q1  ───┐
 *       └── (1-p1) ──►  Q2  ───┴──► Q0
 * </pre>
 *
 * <h2>Centri</h2>
 * <ul>
 * <li>Q0: Delay station (terminali). Server infiniti, think time esponenziale
 * con media Z.</li>
 * <li>Q1: Singolo server FCFS, servizio esponenziale con media S1.</li>
 * <li>Q2: Singolo server FCFS, servizio esponenziale con media S2.</li>
 * </ul>
 *
 * <h2>Routing</h2>
 * All'uscita da Q0, ogni cliente viene instradato a Q1 con probabilità p1
 * e a Q2 con probabilità (1-p1). Al termine del servizio in Q1 o Q2 torna a Q0.
 *
 * <h2>Stream RNG</h2>
 * <ul>
 * <li>Stream THINK_TIME (2): think time in Q0</li>
 * <li>Stream SERVICE (1): servizio in Q1</li>
 * <li>Stream ROUTING (3): servizio in Q2 + decisione routing Q0</li>
 * </ul>
 *
 * <h2>Condizione di stop</h2>
 * La simulazione termina quando
 * {@code completionsQ1 + completionsQ2 >= maxCompletions}.
 *
 * <h2>Stato del sistema</h2>
 * <ul>
 * <li>{@code inThinkQ0}: numero clienti in think time (Q0)</li>
 * <li>{@code serverBusyQ1}, {@code queueQ1}: stato Q1</li>
 * <li>{@code serverBusyQ2}, {@code queueQ2}: stato Q2</li>
 * <li>Invariante: inThinkQ0 + |queueQ1| + (busyQ1?1:0) + |queueQ2| +
 * (busyQ2?1:0) = N</li>
 * </ul>
 */
public class ClosedNetworkSimulator {

    private final ClosedNetworkConfig config;
    private final Rngs rngs;
    private final Rvgs rvgs;

    // Future Event List
    private final EventList fel;

    // Code di attesa
    private final CustomerQueue queueQ1;
    private final CustomerQueue queueQ2;

    // Stato server
    private Customer serverQ1; // null = idle
    private Customer serverQ2; // null = idle

    // Contatore clienti in think time (Q0 non ha coda esplicita, ha server
    // infiniti)
    private int inThinkQ0;

    // Statistiche
    private final ClosedNetworkStatistics stats;

    // Clock simulato
    private double clock;

    // ID progressivo clienti
    private long nextId;

    /**
     * Crea il simulatore con il seed dato.
     *
     * @param config configurazione del sistema chiuso
     * @param seed   seed iniziale per i generatori RNG
     */
    public ClosedNetworkSimulator(ClosedNetworkConfig config, long seed) {
        this.config = config;

        this.rngs = new Rngs();
        this.rngs.plantSeeds(seed);
        this.rvgs = new Rvgs(rngs);

        this.fel = new EventList();
        this.queueQ1 = new CustomerQueue();
        this.queueQ2 = new CustomerQueue();
        this.stats = new ClosedNetworkStatistics();

        this.clock = 0.0;
        this.nextId = 1;
        this.serverQ1 = null;
        this.serverQ2 = null;
        this.inThinkQ0 = 0;
    }

    /**
     * Esegue la simulazione fino a {@code maxCompletions} completamenti su Q1.
     *
     * @return statistiche finali della replica
     */
    public ClosedNetworkStatistics run() {
        initialize();

        while (stats.getCompletionsQ1() + stats.getCompletionsQ2() < config.getMaxCompletions()) {
            Event event = fel.getMin();
            fel.dequeue();

            stats.updateAreas(event.getTime());
            clock = event.getTime();

            switch (event.getType()) {
                case END_THINK_TIME -> processEndThinkTime(event);
                case DEPARTURE -> processDepartureQ1(event);
                case TIMEOUT -> processDepartureQ2(event);
                default -> throw new IllegalStateException("Evento sconosciuto: " + event.getType());
            }
        }

        return stats;
    }

    /**
     * Inizializzazione: tutti gli N clienti partono in Q0 (think time).
     *
     * Questa scelta minimizza il transitorio iniziale rispetto a partire con
     * tutti i clienti in Q1, dove si genererebbe un picco artificiale di carico.
     */
    private void initialize() {
        for (int i = 0; i < config.getNumCustomers(); i++) {
            Customer c = new Customer(nextId++, clock);
            scheduleEndThinkTime(c, clock);
            inThinkQ0++;
        }
    }

    // =========================================================================
    // Gestione eventi
    // =========================================================================

    /**
     * Evento: cliente termina il think time in Q0 e viene instradato
     * probabilisticamente.
     *
     * Pseudo-codice:
     * 
     * <pre>
     * inThinkQ0--
     * u ~ Uniform(0,1)  via stream ROUTING
     * SE u < p1:
     *     customer.arrivalTimeAtQ1 ← clock
     *     se Q1 libero → startServiceQ1(customer) altrimenti queueQ1.enqueue
     * ALTRIMENTI:
     *     customer.arrivalTimeAtQ2 ← clock
     *     se Q2 libero → startServiceQ2(customer) altrimenti queueQ2.enqueue
     * </pre>
     *
     * @param event evento END_THINK_TIME
     */
    private void processEndThinkTime(Event event) {
        Customer c = event.getCustomer();
        inThinkQ0--;

        // Decisione di routing: p1 → Q1, (1-p1) → Q2
        rngs.selectStream(StreamType.ROUTING.ordinal());
        double u = rngs.random();

        if (u < config.getRoutingProbabilityQ1()) {
            c.setArrivalTimeAtQ1(clock);
            if (serverQ1 == null) {
                startServiceQ1(c);
            } else {
                queueQ1.enqueue(c);
                stats.setQueueLengthQ1(queueQ1.size());
            }
        } else {
            c.setArrivalTimeAtQ2(clock);
            if (serverQ2 == null) {
                startServiceQ2(c);
            } else {
                queueQ2.enqueue(c);
                stats.setQueueLengthQ2(queueQ2.size());
            }
        }
    }

    /**
     * Evento: cliente completa il servizio in Q1 e si presenta a Q2.
     *
     * Pseudo-codice:
     * 
     * <pre>
     * registra stats Q1
     * se queueQ1 non vuota → startServiceQ1(next)
     * altrimenti           → serverQ1 = idle
     * imposta arrivalTimeAtQ2(customer)
     * se Q2 libero → startServiceQ2(customer)
     * altrimenti   → queueQ2.enqueue(customer)
     * </pre>
     *
     * @param event evento DEPARTURE (usato per Q1)
     */
    private void processDepartureQ1(Event event) {
        Customer departing = event.getCustomer();

        // Registra completamento Q1
        stats.recordDepartureQ1(departing.getArrivalTimeAtQ1(), clock);
        stats.setServerBusyQ1(false);
        serverQ1 = null;

        // Avanza coda Q1
        if (!queueQ1.isEmpty()) {
            Customer next = queueQ1.dequeue();
            stats.setQueueLengthQ1(queueQ1.size());
            startServiceQ1(next);
        }

        // Il cliente torna a Q0 (pensa)
        scheduleEndThinkTime(departing, clock);
        inThinkQ0++;
    }

    /**
     * Evento: cliente completa il servizio in Q2 e torna a Q0 (think time).
     *
     * Pseudo-codice:
     * 
     * <pre>
     * registra stats Q2
     * se queueQ2 non vuota → startServiceQ2(next)
     * altrimenti           → serverQ2 = idle
     * scheduleEndThinkTime(customer)  ← il cliente ricomincia il ciclo
     * inThinkQ0++
     * </pre>
     *
     * @param event evento TIMEOUT (usato per Q2, nome tecnico riutilizzato)
     */
    private void processDepartureQ2(Event event) {
        Customer departing = event.getCustomer();

        // Registra completamento Q2
        stats.recordDepartureQ2(departing.getArrivalTimeAtQ2(), clock);
        stats.setServerBusyQ2(false);
        serverQ2 = null;

        // Avanza coda Q2
        if (!queueQ2.isEmpty()) {
            Customer next = queueQ2.dequeue();
            stats.setQueueLengthQ2(queueQ2.size());
            startServiceQ2(next);
        }

        // Il cliente torna a Q0 (pensa)
        scheduleEndThinkTime(departing, clock);
        inThinkQ0++;
    }

    // =========================================================================
    // Avvio servizi e scheduling
    // =========================================================================

    /** Inizia il servizio di {@code c} in Q1 e schedula la sua partenza. */
    private void startServiceQ1(Customer c) {
        serverQ1 = c;
        stats.setServerBusyQ1(true);

        rngs.selectStream(StreamType.SERVICE.ordinal());
        double serviceTime = rvgs.exponential(config.getMeanServiceQ1());
        c.setServiceTime(serviceTime);

        // EventType.DEPARTURE = partenza da Q1
        fel.enqueue(new Event(EventType.DEPARTURE, clock + serviceTime, c));
    }

    /** Inizia il servizio di {@code c} in Q2 e schedula la sua partenza. */
    private void startServiceQ2(Customer c) {
        serverQ2 = c;
        stats.setServerBusyQ2(true);

        rngs.selectStream(StreamType.ROUTING.ordinal());
        double serviceTime = rvgs.exponential(config.getMeanServiceQ2());

        // EventType.TIMEOUT = partenza da Q2 (riuso enum esistente, semantica diversa)
        fel.enqueue(new Event(EventType.TIMEOUT, clock + serviceTime, c));
    }

    /** Schedula la fine del think time per {@code c} a partire da {@code from}. */
    private void scheduleEndThinkTime(Customer c, double from) {
        rngs.selectStream(StreamType.THINK_TIME.ordinal());
        double thinkTime = rvgs.exponential(config.getMeanThinkTime());

        // Resetta l'ID per il prossimo ciclo (stesso oggetto riusato)
        fel.enqueue(new Event(EventType.END_THINK_TIME, from + thinkTime, c));
    }

    // =========================================================================
    // Getters
    // =========================================================================

    /** @return clock simulato al termine della simulazione */
    public double getClock() {
        return clock;
    }

    /** @return configurazione utilizzata */
    public ClosedNetworkConfig getConfig() {
        return config;
    }

    /** @return clienti attualmente in think time (Q0) */
    public int getInThinkQ0() {
        return inThinkQ0;
    }
}
