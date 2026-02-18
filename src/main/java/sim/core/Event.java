package sim.core;

/**
 * Rappresenta un evento futuro nel simulatore event-driven.
 *
 * Gli eventi sono ordinati per tempo (FEL = Future Event List).
 * Ogni evento ha un tipo (ARRIVAL, DEPARTURE, ...) e un timestamp.
 *
 * Può trasportare un Customer come payload (opzionale).
 */
public class Event implements Comparable<Event> {
    private final EventType type;
    final double time; // Package-private per accesso da EventList
    private final Customer customer; // Può essere null per alcuni tipi di evento

    // Campi per Splay Tree (usati da EventList)
    // Package-private per permettere accesso da EventList nello stesso package
    Event leftlink, rightlink, uplink;

    /**
     * Costruttore completo con customer associato.
     *
     * @param type tipo di evento
     * @param time tempo in cui l'evento deve essere processato (clock simulato)
     * @param customer customer associato (può essere null)
     */
    public Event(EventType type, double time, Customer customer) {
        this.type = type;
        this.time = time;
        this.customer = customer;
    }

    /**
     * Costruttore per eventi senza customer associato.
     *
     * Usato tipicamente per ARRIVAL (il customer viene creato al momento del processing).
     *
     * @param type tipo di evento
     * @param time tempo in cui l'evento deve essere processato
     */
    public Event(EventType type, double time) {
        this(type, time, null);
    }

    /**
     * @return tipo di evento
     */
    public EventType getType() {
        return type;
    }

    /**
     * @return tempo dell'evento (clock simulato)
     */
    public double getTime() {
        return time;
    }

    /**
     * @return customer associato (può essere null)
     */
    public Customer getCustomer() {
        return customer;
    }

    /**
     * Confronta eventi per tempo (per ordinamento in FEL).
     *
     * Eventi con tempo minore vengono prima (min-heap semantics).
     */
    @Override
    public int compareTo(Event other) {
        return Double.compare(this.time, other.time);
    }

    @Override
    public String toString() {
        return String.format("Event{type=%s, time=%.4f, customer=%s}",
            type, time, customer != null ? customer.toString() : "null");
    }
}

