package sim.core;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

class CustomerTest {

    @Test
    @DisplayName("Creazione Customer con parametri validi")
    void testCustomerCreation() {
        Customer c = new Customer(42, 10.5);

        assertEquals(42, c.getId());
        assertEquals(10.5, c.getArrivalTime(), 1e-10);
    }

    @Test
    @DisplayName("Calcolo Response Time corretto")
    void testResponseTime() {
        Customer c = new Customer(1, 5.0);

        // Response time = departure - arrival
        assertEquals(10.0, c.getResponseTime(15.0), 1e-10);
        assertEquals(0.0, c.getResponseTime(5.0), 1e-10);
        assertEquals(2.5, c.getResponseTime(7.5), 1e-10);
    }

    @Test
    @DisplayName("Equality basata su ID")
    void testEquality() {
        Customer c1 = new Customer(1, 10.0);
        Customer c2 = new Customer(1, 20.0); // Stesso ID, arrival time diverso
        Customer c3 = new Customer(2, 10.0); // ID diverso

        assertEquals(c1, c2, "Customer con stesso ID devono essere uguali");
        assertNotEquals(c1, c3, "Customer con ID diverso devono essere diversi");
    }

    @Test
    @DisplayName("HashCode consistente con equals")
    void testHashCode() {
        Customer c1 = new Customer(1, 10.0);
        Customer c2 = new Customer(1, 20.0);

        assertEquals(c1.hashCode(), c2.hashCode(),
            "Customer uguali devono avere stesso hashCode");
    }

    @Test
    @DisplayName("ToString contiene informazioni essenziali")
    void testToString() {
        Customer c = new Customer(42, 10.5);
        String str = c.toString();

        assertTrue(str.contains("42"), "ToString deve contenere l'ID");
        assertTrue(str.contains("10.5"), "ToString deve contenere arrival time");
    }
}

