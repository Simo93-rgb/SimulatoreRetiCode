package sim.core;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

class EventListTest {

    private EventList eventList;

    @BeforeEach
    void setUp() {
        eventList = new EventList();
    }

    @Test
    @DisplayName("EventList inizialmente vuota")
    void testEmptyList() {
        assertTrue(eventList.isEmpty());
        assertEquals(0, eventList.size());
    }

    @Test
    @DisplayName("Enqueue singolo evento")
    void testEnqueueSingleEvent() {
        Event e = new Event(EventType.ARRIVAL, 10.0);
        eventList.enqueue(e);

        assertFalse(eventList.isEmpty());
        assertEquals(1, eventList.size());
    }

    @Test
    @DisplayName("GetMin restituisce evento con tempo minimo")
    void testGetMinReturnsEarliestEvent() {
        Event e1 = new Event(EventType.ARRIVAL, 10.0);
        Event e2 = new Event(EventType.DEPARTURE, 5.0);
        Event e3 = new Event(EventType.ARRIVAL, 7.5);

        eventList.enqueue(e1);
        eventList.enqueue(e2);
        eventList.enqueue(e3);

        Event min = eventList.getMin();
        assertEquals(5.0, min.getTime(), 1e-10, "GetMin deve restituire evento con tempo 5.0");
        assertEquals(EventType.DEPARTURE, min.getType());
    }

    @Test
    @DisplayName("GetMin non rimuove l'evento (size rimane costante)")
    void testGetMinDoesNotRemove() {
        Event e = new Event(EventType.ARRIVAL, 10.0);
        eventList.enqueue(e);

        assertEquals(1, eventList.size());
        eventList.getMin();
        assertEquals(1, eventList.size(), "GetMin non deve modificare size");
    }

    @Test
    @DisplayName("Dequeue rimuove l'evento ottenuto con getMin")
    void testDequeueRemovesEvent() {
        Event e = new Event(EventType.ARRIVAL, 10.0);
        eventList.enqueue(e);

        eventList.getMin();
        eventList.dequeue();

        assertTrue(eventList.isEmpty());
        assertEquals(0, eventList.size());
    }

    @Test
    @DisplayName("GetMin + Dequeue in sequenza mantengono ordine temporale")
    void testSequentialGetMinDequeue() {
        // Inserisco eventi fuori ordine
        eventList.enqueue(new Event(EventType.ARRIVAL, 10.0));
        eventList.enqueue(new Event(EventType.DEPARTURE, 3.0));
        eventList.enqueue(new Event(EventType.ARRIVAL, 7.0));
        eventList.enqueue(new Event(EventType.DEPARTURE, 1.0));
        eventList.enqueue(new Event(EventType.ARRIVAL, 5.0));

        // Estraggo in ordine crescente di tempo
        double[] expectedTimes = {1.0, 3.0, 5.0, 7.0, 10.0};

        for (double expectedTime : expectedTimes) {
            Event e = eventList.getMin();
            eventList.dequeue();
            assertEquals(expectedTime, e.getTime(), 1e-10,
                "Eventi devono essere estratti in ordine temporale");
        }

        assertTrue(eventList.isEmpty());
    }

    @Test
    @DisplayName("GetMin su lista vuota lancia eccezione")
    void testGetMinOnEmptyListThrows() {
        assertThrows(IllegalStateException.class, () -> {
            eventList.getMin();
        });
    }

    @Test
    @DisplayName("Dequeue su lista vuota lancia eccezione")
    void testDequeueOnEmptyListThrows() {
        assertThrows(IllegalStateException.class, () -> {
            eventList.dequeue();
        });
    }

    @Test
    @DisplayName("Stress test: inserimento e rimozione 1000 eventi")
    void testLargeNumberOfEvents() {
        int n = 1000;

        // Inserisco eventi con tempo casuale
        for (int i = 0; i < n; i++) {
            double time = Math.random() * 10000;
            eventList.enqueue(new Event(EventType.ARRIVAL, time));
        }

        assertEquals(n, eventList.size());

        // Estraggo tutti in ordine
        double previousTime = -1.0;
        for (int i = 0; i < n; i++) {
            Event e = eventList.getMin();
            eventList.dequeue();

            assertTrue(e.getTime() >= previousTime,
                "Eventi devono essere estratti in ordine non decrescente");
            previousTime = e.getTime();
        }

        assertTrue(eventList.isEmpty());
    }

    @Test
    @DisplayName("Eventi con stesso timestamp gestiti correttamente")
    void testEventsWithSameTimestamp() {
        eventList.enqueue(new Event(EventType.ARRIVAL, 5.0));
        eventList.enqueue(new Event(EventType.DEPARTURE, 5.0));
        eventList.enqueue(new Event(EventType.ARRIVAL, 5.0));

        assertEquals(3, eventList.size());

        // Tutti devono avere time = 5.0
        for (int i = 0; i < 3; i++) {
            Event e = eventList.getMin();
            eventList.dequeue();
            assertEquals(5.0, e.getTime(), 1e-10);
        }
    }
}

