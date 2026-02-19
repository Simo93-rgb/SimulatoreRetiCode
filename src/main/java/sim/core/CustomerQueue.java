package sim.core;

/**
 * Coda FIFO (First-In-First-Out) per gestire i customer in attesa.
 *
 * Implementata come array circolare dinamico che si espande automaticamente
 * quando necessario (raddoppia la dimensione).
 *
 * Adattata da SimUtils/Queue.java del simulatore proposto.
 *
 * @author Adattato per il progetto SimulatoreRetiCode
 */
public class CustomerQueue {
    /**
     * Dimensione iniziale della coda.
     */
    public static final int INITIAL_SIZE = 10;

    /**
     * Array circolare che contiene i customer.
     */
    private Customer[] queue;

    /**
     * Indice del primo elemento (front) e del primo spazio libero (back).
     */
    private int front;
    private int back;

    /**
     * Crea una coda vuota con dimensione iniziale di default.
     */
    public CustomerQueue() {
        this(INITIAL_SIZE);
    }

    /**
     * Crea una coda vuota con dimensione iniziale specificata.
     *
     * @param initialSize dimensione iniziale dell'array
     */
    public CustomerQueue(int initialSize) {
        this.queue = new Customer[initialSize];
        this.front = 0;
        this.back = 0;
    }

    /**
     * Raddoppia la dimensione della coda quando necessario.
     *
     * Gestisce correttamente il caso dell'array circolare.
     */
    private synchronized void grow() {
        Customer[] newQueue = new Customer[2 * queue.length];
        int size = size();

        if (back >= front) {
            // Caso semplice: elementi contigui
            System.arraycopy(queue, front, newQueue, 0, size);
        } else {
            // Caso circolare: elementi wrappati
            int firstPart = queue.length - front;
            System.arraycopy(queue, front, newQueue, 0, firstPart);
            System.arraycopy(queue, 0, newQueue, firstPart, back);
        }

        // Reset front e back
        front = 0;
        back = size;
        queue = newQueue;
    }

    /**
     * @return true se la coda è vuota
     */
    public synchronized boolean isEmpty() {
        return front == back;
    }

    /**
     * Aggiunge un customer alla fine della coda.
     *
     * @param customer customer da aggiungere
     */
    public synchronized void enqueue(Customer customer) {
        if (((back + 1) % queue.length) == front) {
            grow();
        }

        queue[back] = customer;
        back = (back + 1) % queue.length;
    }

    /**
     * Rimuove e restituisce il customer all'inizio della coda.
     *
     * @return customer rimosso, o null se la coda è vuota
     */
    public synchronized Customer dequeue() {
        if (isEmpty()) {
            return null;
        }

        Customer customer = queue[front];
        queue[front] = null; // Evita memory leak
        front = (front + 1) % queue.length;
        return customer;
    }

    /**
     * Restituisce il customer all'inizio della coda senza rimuoverlo.
     *
     * @return customer in testa, o null se la coda è vuota
     */
    public synchronized Customer peek() {
        if (isEmpty()) {
            return null;
        }
        return queue[front];
    }

    /**
     * @return numero di customer attualmente in coda
     */
    public synchronized int size() {
        if (front <= back) {
            return back - front;
        } else {
            return (back + queue.length - front);
        }
    }

    /**
     * Rimuove tutti i customer dalla coda.
     */
    public synchronized void clear() {
        // Nullifica riferimenti per GC
        for (int i = front; i != back; i = (i + 1) % queue.length) {
            queue[i] = null;
        }
        front = 0;
        back = 0;
    }

    @Override
    public synchronized String toString() {
        if (isEmpty()) {
            return "CustomerQueue{empty}";
        }

        StringBuilder sb = new StringBuilder("CustomerQueue{size=");
        sb.append(size()).append(", customers=[");

        boolean first = true;
        for (int i = front; i != back; i = (i + 1) % queue.length) {
            if (!first) sb.append(", ");
            sb.append(queue[i].getId());
            first = false;
        }
        sb.append("]}");

        return sb.toString();
    }
}

