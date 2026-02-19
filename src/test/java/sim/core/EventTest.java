package sim.core;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

class EventTest {

    @Test
    @DisplayName("Creazione Event con customer")
    void testEventWithCustomer() {
        Customer c = new Customer(1, 5.0);
        Event e = new Event(EventType.DEPARTURE, 10.0, c);

        assertEquals(EventType.DEPARTURE, e.getType());
        assertEquals(10.0, e.getTime(), 1e-10);
        assertEquals(c, e.getCustomer());
    }

    @Test
    @DisplayName("Creazione Event senza customer")
    void testEventWithoutCustomer() {
        Event e = new Event(EventType.ARRIVAL, 5.0);

        assertEquals(EventType.ARRIVAL, e.getType());
        assertEquals(5.0, e.getTime(), 1e-10);
        assertNull(e.getCustomer(), "Customer deve essere null");
    }

    @Test
    @DisplayName("Ordinamento eventi per tempo (min-heap semantics)")
    void testEventOrdering() {
        Event e1 = new Event(EventType.ARRIVAL, 10.0);
        Event e2 = new Event(EventType.DEPARTURE, 5.0);
        Event e3 = new Event(EventType.ARRIVAL, 7.5);

        // e2 (time=5.0) < e3 (time=7.5) < e1 (time=10.0)
        assertTrue(e2.compareTo(e3) < 0, "e2 deve venire prima di e3");
        assertTrue(e3.compareTo(e1) < 0, "e3 deve venire prima di e1");
        assertTrue(e2.compareTo(e1) < 0, "e2 deve venire prima di e1");
    }

    @Test
    @DisplayName("Eventi con stesso tempo sono uguali in ordinamento")
    void testEqualTimestamps() {
        Event e1 = new Event(EventType.ARRIVAL, 10.0);
        Event e2 = new Event(EventType.DEPARTURE, 10.0);

        assertEquals(0, e1.compareTo(e2),
            "Eventi con stesso tempo devono avere compareTo = 0");
    }

    @Test
    @DisplayName("Type safety con EventType enum")
    void testEventTypeEnumSafety() {
        // Compila solo con valori validi di EventType
        Event arrival = new Event(EventType.ARRIVAL, 1.0);
        Event departure = new Event(EventType.DEPARTURE, 2.0);
        Event thinkEnd = new Event(EventType.END_THINK_TIME, 3.0);
        Event timeout = new Event(EventType.TIMEOUT, 4.0);

        assertEquals(EventType.ARRIVAL, arrival.getType());
        assertEquals(EventType.DEPARTURE, departure.getType());
        assertEquals(EventType.END_THINK_TIME, thinkEnd.getType());
        assertEquals(EventType.TIMEOUT, timeout.getType());
    }

    @Test
    @DisplayName("ToString fornisce informazioni utili per debug")
    void testToString() {
        Customer c = new Customer(42, 5.0);
        Event e = new Event(EventType.DEPARTURE, 10.0, c);
        String str = e.toString();

        assertTrue(str.contains("DEPARTURE"), "ToString deve contenere il tipo");
        assertTrue(str.contains("10.0"), "ToString deve contenere il tempo");
    }
}

