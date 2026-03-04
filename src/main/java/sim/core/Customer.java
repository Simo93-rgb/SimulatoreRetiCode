package sim.core;

/**
 * Rappresenta un customer (job) nel sistema di code.
 *
 * Separato da Event per mantenere Single Responsibility Principle:
 * - Event = messaggio/comando futuro ("cosa fare e quando")
 * - Customer = entità del dominio ("chi viene processato")
 */
public class Customer {

    /**
     * Classe di appartenenza del customer.
     *
     * - CLOSED: cliente della classe chiusa (circola tra Q0→Q1→Q2→Q0)
     * - OPEN:   cliente della classe aperta (arriva dall'esterno, servito solo in Q1, poi esce)
     *
     * Default CLOSED per retrocompatibilità con tutti i simulatori precedenti.
     */
    public enum CustomerClass { CLOSED, OPEN }

    private final long id;
    private final double arrivalTime;
    private double serviceTime;      // Tempo servizio (settato quando inizia servizio)
    private final CustomerClass customerClass;  // Classe di appartenenza

    // Timestamp arrivi ai singoli centri (per sistema chiuso multi-centro)
    private double arrivalTimeAtQ1 = 0.0;
    private double arrivalTimeAtQ2 = 0.0;

    /**
     * Crea un customer della classe chiusa (default, retrocompatibile).
     *
     * @param id          identificativo univoco
     * @param arrivalTime tempo di arrivo al sistema
     */
    public Customer(long id, double arrivalTime) {
        this(id, arrivalTime, CustomerClass.CLOSED);
    }

    /**
     * Crea un customer con classe esplicita.
     *
     * @param id            identificativo univoco
     * @param arrivalTime   tempo di arrivo al sistema
     * @param customerClass classe di appartenenza (CLOSED o OPEN)
     */
    public Customer(long id, double arrivalTime, CustomerClass customerClass) {
        this.id            = id;
        this.arrivalTime   = arrivalTime;
        this.customerClass = customerClass;
        this.serviceTime   = 0.0;
    }

    /**
     * @return ID univoco del customer
     */
    public long getId() {
        return id;
    }

    /** @return classe di appartenenza del customer */
    public CustomerClass getCustomerClass() { return customerClass; }

    /** @return true se il customer appartiene alla classe aperta */
    public boolean isOpen() { return customerClass == CustomerClass.OPEN; }

    /**
     * @return tempo di arrivo al sistema
     */
    public double getArrivalTime() {
        return arrivalTime;
    }

    /**
     * Imposta tempo di servizio ricevuto.
     *
     * @param serviceTime durata servizio
     */
    public void setServiceTime(double serviceTime) {
        this.serviceTime = serviceTime;
    }

    /**
     * @return tempo di servizio ricevuto
     */
    public double getServiceTime() {
        return serviceTime;
    }

    /**
     * Calcola il tempo di risposta (response time) del customer.
     *
     * Response time = tempo di partenza - tempo di arrivo
     *
     * @param departureTime tempo in cui il customer lascia il sistema
     * @return tempo di risposta
     */
    public double getResponseTime(double departureTime) {
        return departureTime - arrivalTime;
    }

    // ---- Timestamp per sistema chiuso multi-centro ----

    /** Imposta il tempo di arrivo a Q1. */
    public void setArrivalTimeAtQ1(double t) { this.arrivalTimeAtQ1 = t; }

    /** @return tempo di arrivo a Q1 */
    public double getArrivalTimeAtQ1() { return arrivalTimeAtQ1; }

    /** Imposta il tempo di arrivo a Q2. */
    public void setArrivalTimeAtQ2(double t) { this.arrivalTimeAtQ2 = t; }

    /** @return tempo di arrivo a Q2 */
    public double getArrivalTimeAtQ2() { return arrivalTimeAtQ2; }

    @Override
    public String toString() {
        return String.format("Customer{id=%d, arrivalTime=%.4f}", id, arrivalTime);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Customer customer = (Customer) o;
        return id == customer.id;
    }

    @Override
    public int hashCode() {
        return Long.hashCode(id);
    }
}

