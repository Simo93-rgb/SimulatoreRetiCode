package sim.core;

/**
 * Rappresenta un customer (job) nel sistema di code.
 *
 * Separato da Event per mantenere Single Responsibility Principle:
 * - Event = messaggio/comando futuro ("cosa fare e quando")
 * - Customer = entità del dominio ("chi viene processato")
 */
public class Customer {
    private final long id;
    private final double arrivalTime;
    private double serviceTime;  // Tempo servizio (settato quando inizia servizio)

    /**
     * Crea un nuovo customer.
     *
     * @param id identificativo univoco del customer
     * @param arrivalTime tempo di arrivo al sistema (clock simulato)
     */
    public Customer(long id, double arrivalTime) {
        this.id = id;
        this.arrivalTime = arrivalTime;
        this.serviceTime = 0.0;
    }

    /**
     * @return ID univoco del customer
     */
    public long getId() {
        return id;
    }

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

