package sim.core;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

class CustomerQueueTest {

    private CustomerQueue queue;

    @BeforeEach
    void setUp() {
        queue = new CustomerQueue();
    }

    @Test
    @DisplayName("Coda inizialmente vuota")
    void testEmptyQueue() {
        assertTrue(queue.isEmpty());
        assertEquals(0, queue.size());
    }

    @Test
    @DisplayName("Enqueue aggiunge customer alla coda")
    void testEnqueue() {
        Customer c = new Customer(1, 10.0);
        queue.enqueue(c);

        assertFalse(queue.isEmpty());
        assertEquals(1, queue.size());
    }

    @Test
    @DisplayName("Dequeue rimuove e restituisce customer in ordine FIFO")
    void testFIFOOrder() {
        Customer c1 = new Customer(1, 1.0);
        Customer c2 = new Customer(2, 2.0);
        Customer c3 = new Customer(3, 3.0);

        queue.enqueue(c1);
        queue.enqueue(c2);
        queue.enqueue(c3);

        assertEquals(c1, queue.dequeue(), "Primo enqueued deve essere primo dequeued");
        assertEquals(c2, queue.dequeue(), "Secondo enqueued deve essere secondo dequeued");
        assertEquals(c3, queue.dequeue(), "Terzo enqueued deve essere terzo dequeued");

        assertTrue(queue.isEmpty());
    }

    @Test
    @DisplayName("Dequeue su coda vuota restituisce null")
    void testDequeueOnEmptyQueue() {
        assertNull(queue.dequeue());
    }

    @Test
    @DisplayName("Peek restituisce primo customer senza rimuoverlo")
    void testPeek() {
        Customer c1 = new Customer(1, 1.0);
        Customer c2 = new Customer(2, 2.0);

        queue.enqueue(c1);
        queue.enqueue(c2);

        assertEquals(c1, queue.peek(), "Peek deve restituire primo customer");
        assertEquals(2, queue.size(), "Peek non deve modificare size");
        assertEquals(c1, queue.peek(), "Peek ripetuto deve restituire stesso customer");
    }

    @Test
    @DisplayName("Peek su coda vuota restituisce null")
    void testPeekOnEmptyQueue() {
        assertNull(queue.peek());
    }

    @Test
    @DisplayName("Size aggiornato correttamente con enqueue/dequeue")
    void testSizeTracking() {
        assertEquals(0, queue.size());

        queue.enqueue(new Customer(1, 1.0));
        assertEquals(1, queue.size());

        queue.enqueue(new Customer(2, 2.0));
        assertEquals(2, queue.size());

        queue.dequeue();
        assertEquals(1, queue.size());

        queue.dequeue();
        assertEquals(0, queue.size());
    }

    @Test
    @DisplayName("Clear svuota la coda")
    void testClear() {
        queue.enqueue(new Customer(1, 1.0));
        queue.enqueue(new Customer(2, 2.0));
        queue.enqueue(new Customer(3, 3.0));

        assertFalse(queue.isEmpty());

        queue.clear();

        assertTrue(queue.isEmpty());
        assertEquals(0, queue.size());
        assertNull(queue.peek());
    }

    @Test
    @DisplayName("Array circolare si espande automaticamente")
    void testAutomaticGrowth() {
        int initialSize = CustomerQueue.INITIAL_SIZE;

        // Riempio oltre la capacità iniziale
        for (int i = 0; i < initialSize + 5; i++) {
            queue.enqueue(new Customer(i, i));
        }

        assertEquals(initialSize + 5, queue.size());

        // Verifico ordine FIFO dopo espansione
        for (int i = 0; i < initialSize + 5; i++) {
            Customer c = queue.dequeue();
            assertEquals(i, c.getId(), "Ordine FIFO deve essere preservato dopo grow");
        }
    }

    @Test
    @DisplayName("Wrapping dell'array circolare funziona correttamente")
    void testCircularWrapping() {
        CustomerQueue smallQueue = new CustomerQueue(5);

        // Enqueue 3 elementi
        smallQueue.enqueue(new Customer(1, 1.0));
        smallQueue.enqueue(new Customer(2, 2.0));
        smallQueue.enqueue(new Customer(3, 3.0));

        // Dequeue 2 elementi (front avanza)
        smallQueue.dequeue();
        smallQueue.dequeue();

        // Enqueue altri 3 (back wrappa)
        smallQueue.enqueue(new Customer(4, 4.0));
        smallQueue.enqueue(new Customer(5, 5.0));
        smallQueue.enqueue(new Customer(6, 6.0));

        // Verifico ordine: 3, 4, 5, 6
        assertEquals(3, smallQueue.dequeue().getId());
        assertEquals(4, smallQueue.dequeue().getId());
        assertEquals(5, smallQueue.dequeue().getId());
        assertEquals(6, smallQueue.dequeue().getId());

        assertTrue(smallQueue.isEmpty());
    }

    @Test
    @DisplayName("Stress test: 10000 enqueue/dequeue")
    void testStressTest() {
        int n = 10000;

        for (int i = 0; i < n; i++) {
            queue.enqueue(new Customer(i, i));
        }

        assertEquals(n, queue.size());

        for (int i = 0; i < n; i++) {
            Customer c = queue.dequeue();
            assertEquals(i, c.getId(), "Ordine FIFO deve essere preservato");
        }

        assertTrue(queue.isEmpty());
    }

    @Test
    @DisplayName("ToString fornisce informazioni utili")
    void testToString() {
        queue.enqueue(new Customer(1, 1.0));
        queue.enqueue(new Customer(2, 2.0));

        String str = queue.toString();
        assertTrue(str.contains("2"), "ToString deve contenere size");
    }

    @Test
    @DisplayName("ToString su coda vuota")
    void testToStringEmpty() {
        String str = queue.toString();
        assertTrue(str.contains("empty") || str.contains("0"),
            "ToString di coda vuota deve indicare stato vuoto");
    }
}

