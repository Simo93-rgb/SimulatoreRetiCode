package sim;

import libraries.Rngs;
import libraries.Rvgs;
import sim.core.*;
import sim.SeedManager.StreamType;

/**
 * Simulatore M/M/1 event-driven.
 *
 * Sistema:
 * - Arrivi: Processo di Poisson con tasso λ
 * - Servizio: Distribuzione esponenziale con tasso μ
 * - Server singolo, disciplina FCFS
 * - Capacità infinita (no blocking)
 *
 * Pattern: Next-Event Simulation con FEL (Future Event List).
 */
public class MMMOneSimulator {

    private final SimulationConfig config;
    private final Rngs rngs;
    private final Rvgs rvgs;
    private final ServiceGenerator serviceGen;

    // Strutture dati event-driven
    private final EventList fel;
    private final CustomerQueue queue;

    // Statistiche
    private final SimulationStatistics stats;

    // Stato simulazione
    private double clock;
    private long nextCustomerId;
    private Customer currentCustomer;  // Customer in servizio

    /**
     * Crea simulatore M/M/1 con seed specificato.
     *
     * @param config parametri simulazione
     * @param seed seed iniziale per RNG
     */
    public MMMOneSimulator(SimulationConfig config, long seed) {
        this.config = config;

        // Inizializza RNG con stream separati
        this.rngs = new Rngs();
        this.rngs.plantSeeds(seed);
        this.rvgs = new Rvgs(rngs);
        this.serviceGen = new ServiceGenerator(rngs, rvgs);

        // Inizializza strutture dati
        this.fel = new EventList();
        this.queue = new CustomerQueue();
        this.stats = new SimulationStatistics();

        // Stato iniziale
        this.clock = 0.0;
        this.nextCustomerId = 1;
        this.currentCustomer = null;
    }

    /**
     * Esegue simulazione completa.
     *
     * @return statistiche finali
     */
    public SimulationStatistics run() {
        initialize();

        while (stats.getCustomersServed() < config.getMaxCustomers()) {
            Event event = fel.getMin();
            fel.dequeue();

            // Aggiorna aree prima di modificare stato
            stats.updateAreas(event.getTime());
            clock = event.getTime();

            // Process event
            switch (event.getType()) {
                case ARRIVAL -> processArrival(event);
                case DEPARTURE -> processDeparture(event);
                default -> throw new IllegalStateException("Unknown event type: " + event.getType());
            }
        }

        return stats;
    }

    /**
     * Inizializza simulazione schedulando primo arrivo.
     */
    private void initialize() {
        // Schedule primo arrivo al tempo 0
        scheduleNextArrival(0.0);
    }

    /**
     * Processa evento ARRIVAL.
     *
     * Logica:
     * 1. Crea customer con timestamp arrivo
     * 2. Schedule prossimo arrivo (processo indipendente)
     * 3. Se server libero → inizia servizio
     *    Altrimenti → customer entra in coda
     */
    private void processArrival(Event event) {
        // Crea customer
        Customer customer = new Customer(nextCustomerId++, clock);
        stats.recordArrival();

        // Schedule prossimo arrivo (stream ARRIVALS)
        scheduleNextArrival(clock);

        // Gestione customer
        if (currentCustomer == null) {
            // Server libero: inizia servizio immediatamente
            startService(customer);
        } else {
            // Server occupato: entra in coda
            queue.enqueue(customer);
            stats.setQueueLength(queue.size());
        }
    }

    /**
     * Processa evento DEPARTURE.
     *
     * Logica:
     * 1. Customer corrente lascia il sistema
     * 2. Registra statistiche (response time, service time)
     * 3. Se coda non vuota → prendi prossimo e inizia servizio
     *    Altrimenti → server diventa idle
     */
    private void processDeparture(Event event) {
        Customer departingCustomer = event.getCustomer();

        // Service time è già salvato nel customer
        stats.recordDeparture(
            departingCustomer.getArrivalTime(),
            clock,
            departingCustomer.getServiceTime()
        );

        // Prossimo customer
        if (!queue.isEmpty()) {
            Customer nextCustomer = queue.dequeue();
            if (nextCustomer == null) {
                throw new IllegalStateException(
                    String.format("BUG: dequeue() returned null but isEmpty()=false. Queue size=%d", queue.size()));
            }
            stats.setQueueLength(queue.size());
            startService(nextCustomer);
        } else {
            // Server diventa idle
            currentCustomer = null;
            stats.setServerBusy(false);
        }
    }

    /**
     * Inizia servizio per un customer.
     *
     * @param customer customer da servire
     */
    private void startService(Customer customer) {
        currentCustomer = customer;
        stats.setServerBusy(true);

        // Genera tempo servizio (stream SERVICE)
        rngs.selectStream(StreamType.SERVICE.ordinal());
        double serviceTime = serviceGen.exponential(config.getMeanService());
        customer.setServiceTime(serviceTime);  // Salva nel customer

        // Schedule departure
        Event departure = new Event(EventType.DEPARTURE, clock + serviceTime, customer);
        fel.enqueue(departure);
    }

    /**
     * Schedule prossimo arrivo.
     *
     * @param currentTime tempo corrente
     */
    private void scheduleNextArrival(double currentTime) {
        // Genera interarrival time (stream ARRIVALS)
        rngs.selectStream(StreamType.ARRIVALS.ordinal());
        double interarrival = serviceGen.exponential(config.getMeanInterarrival());

        Event arrival = new Event(EventType.ARRIVAL, currentTime + interarrival);
        fel.enqueue(arrival);
    }


    /**
     * @return tempo simulato corrente
     */
    public double getClock() {
        return clock;
    }

    /**
     * @return configurazione utilizzata
     */
    public SimulationConfig getConfig() {
        return config;
    }
}

