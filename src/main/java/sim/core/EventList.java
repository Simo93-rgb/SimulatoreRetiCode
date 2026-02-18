package sim.core;

/**
 * Future Event List (FEL) implementata come Splay Tree.
 *
 * La Splay Tree è una struttura auto-bilanciante particolarmente efficiente
 * per accessi sequenziali agli elementi minimi, come nel caso di una simulazione
 * event-driven dove si estrae sempre l'evento imminente.
 *
 * Complessità ammortizzata: O(log n) per enqueue, getMin, dequeue.
 *
 * Riferimento: Sleator & Tarjan (1985), "Self-Adjusting Binary Search Trees"
 *
 * Codice adattato dal simulatore proposto (SimUtils/EventList.java).
 */
public class EventList {
    private Event root;
    private Event updatePointer;
    private Event left, right, next, temp, farleft, farfarleft;
    private double ntime;
    private boolean updateCode;
    private int size = 0;

    /**
     * Crea una FEL vuota.
     */
    public EventList() {
        this.root = null;
        this.size = 0;
    }

    /**
     * @return true se la FEL è vuota
     */
    public boolean isEmpty() {
        return this.root == null;
    }

    /**
     * @return numero di eventi nella FEL
     */
    public int size() {
        return size;
    }

    /**
     * Inserisce un nuovo evento nella FEL.
     *
     * @param event evento da inserire
     */
    public void enqueue(Event event) {
        size++;
        event.uplink = null;
        next = this.root;
        this.root = event;

        if (next == null) {
            event.leftlink = null;
            event.rightlink = null;
        } else {
            mainblock: {
                ntime = event.time;
                left = event;
                right = event;

                if (next.time > ntime) {
                    // Caso 2: next.time > ntime
                    do {
                        temp = next.leftlink;
                        if (temp == null) {
                            right.leftlink = next;
                            next.uplink = right;
                            left.rightlink = null;
                            break mainblock;
                        }
                        if (temp.time <= ntime) {
                            right.leftlink = next;
                            next.uplink = right;
                            right = next;
                            next = temp;
                            break;
                        }
                        next.leftlink = temp.rightlink;
                        if (temp.rightlink != null)
                            temp.rightlink.uplink = next;
                        right.leftlink = temp;
                        temp.uplink = right;
                        temp.rightlink = next;
                        next.uplink = temp;
                        right = temp;
                        next = temp.leftlink;
                        if (next == null) {
                            left.rightlink = null;
                            break mainblock;
                        }
                    } while (next.time > ntime);
                }

                forblock:
                for (; ; ) {
                    // Caso 1: next.time <= ntime
                    do {
                        temp = next.rightlink;
                        if (temp == null) {
                            left.rightlink = next;
                            next.uplink = left;
                            right.leftlink = null;
                            break forblock;
                        }
                        if (temp.time > ntime) {
                            left.rightlink = next;
                            next.uplink = left;
                            left = next;
                            next = temp;
                            break;
                        }
                        next.rightlink = temp.leftlink;
                        if (temp.leftlink != null)
                            temp.leftlink.uplink = next;
                        left.rightlink = temp;
                        temp.uplink = left;
                        temp.leftlink = next;
                        next.uplink = temp;
                        left = temp;
                        next = temp.rightlink;
                        if (next == null) {
                            right.leftlink = null;
                            break forblock;
                        }
                    } while (next.time <= ntime);

                    // Caso 2: next.time > ntime
                    do {
                        temp = next.leftlink;
                        if (temp == null) {
                            right.leftlink = next;
                            next.uplink = right;
                            left.rightlink = null;
                            break forblock;
                        }
                        if (temp.time <= ntime) {
                            right.leftlink = next;
                            next.uplink = right;
                            right = next;
                            next = temp;
                            break;
                        }
                        next.leftlink = temp.rightlink;
                        if (temp.rightlink != null)
                            temp.rightlink.uplink = next;
                        right.leftlink = temp;
                        temp.uplink = right;
                        temp.rightlink = next;
                        next.uplink = temp;
                        right = temp;
                        next = temp.leftlink;
                        if (next == null) {
                            left.rightlink = null;
                            break forblock;
                        }
                    } while (next.time > ntime);
                }
            }

            // Swap left/right links
            temp = event.leftlink;
            event.leftlink = event.rightlink;
            event.rightlink = temp;
        }
    }

    /**
     * Restituisce l'evento con tempo minimo (evento imminente)
     * SENZA rimuoverlo dalla FEL.
     *
     * Deve essere seguito da dequeue() per rimuovere l'evento.
     *
     * @return evento con tempo minimo
     * @throws IllegalStateException se la FEL è vuota
     */
    public Event getMin() {
        if (isEmpty()) {
            throw new IllegalStateException("EventList is empty");
        }

        next = this.root;
        left = next.leftlink;

        if (left == null) {
            updateCode = true;
            return next;
        } else {
            for (; ; ) {
                farleft = left.leftlink;
                if (farleft == null) {
                    updateCode = false;
                    updatePointer = next;
                    return left;
                }
                farfarleft = farleft.leftlink;
                if (farfarleft == null) {
                    updateCode = false;
                    updatePointer = left;
                    return farleft;
                }
                next.leftlink = farleft;
                farleft.uplink = next;
                left.leftlink = farleft.rightlink;
                if (farleft.rightlink != null)
                    farleft.rightlink.uplink = left;
                farleft.rightlink = left;
                left.uplink = farleft;
                next = farleft;
                left = farfarleft;
            }
        }
    }

    /**
     * Rimuove dalla FEL l'evento restituito dall'ultima chiamata a getMin().
     *
     * DEVE essere chiamato immediatamente dopo getMin().
     *
     * @throws IllegalStateException se chiamato senza getMin() precedente
     */
    public void dequeue() {
        if (size == 0) {
            throw new IllegalStateException("Cannot dequeue from empty EventList");
        }

        size--;

        if (updateCode) {
            next = null; // Rimuovi riferimento
            root = root.rightlink;
            if (root != null)
                root.uplink = null;
        } else {
            left = farleft = null; // Rimuovi riferimenti
            if (updatePointer.leftlink.rightlink != null)
                updatePointer.leftlink.rightlink.uplink = updatePointer;
            updatePointer.leftlink = updatePointer.leftlink.rightlink;
        }
    }

    @Override
    public String toString() {
        return String.format("EventList{size=%d, isEmpty=%b}", size, isEmpty());
    }
}

