package sim;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test suite per ClosedNetworkConfig.
 * Verifica validazione parametri e calcolo del throughput bound.
 */
class ClosedNetworkConfigTest {

    @Test
    @DisplayName("Costruzione valida non lancia eccezioni")
    void testValidConfig() {
        assertDoesNotThrow(() -> new ClosedNetworkConfig(10, 5.0, 1.0, 0.8, 0.5, 10000));
    }

    @Test
    @DisplayName("numCustomers <= 0 lancia eccezione")
    void testInvalidNumCustomers() {
        assertThrows(IllegalArgumentException.class, () -> new ClosedNetworkConfig(0, 5.0, 1.0, 0.8, 0.5, 10000));
    }

    @Test
    @DisplayName("meanThinkTime <= 0 lancia eccezione")
    void testInvalidThinkTime() {
        assertThrows(IllegalArgumentException.class, () -> new ClosedNetworkConfig(10, 0.0, 1.0, 0.8, 0.5, 10000));
    }

    @Test
    @DisplayName("meanServiceQ1 <= 0 lancia eccezione")
    void testInvalidServiceQ1() {
        assertThrows(IllegalArgumentException.class, () -> new ClosedNetworkConfig(10, 5.0, -1.0, 0.8, 0.5, 10000));
    }

    @Test
    @DisplayName("meanServiceQ2 <= 0 lancia eccezione")
    void testInvalidServiceQ2() {
        assertThrows(IllegalArgumentException.class, () -> new ClosedNetworkConfig(10, 5.0, 1.0, 0.0, 0.5, 10000));
    }

    @Test
    @DisplayName("maxCompletions <= 0 lancia eccezione")
    void testInvalidMaxCompletions() {
        assertThrows(IllegalArgumentException.class, () -> new ClosedNetworkConfig(10, 5.0, 1.0, 0.8, 0.5, 0));
    }

    @Test
    @DisplayName("Getters restituiscono i valori corretti")
    void testGetters() {
        ClosedNetworkConfig c = new ClosedNetworkConfig(10, 5.0, 1.0, 0.8, 0.5, 20000);
        assertEquals(10, c.getNumCustomers());
        assertEquals(5.0, c.getMeanThinkTime(), 1e-9);
        assertEquals(1.0, c.getMeanServiceQ1(), 1e-9);
        assertEquals(0.8, c.getMeanServiceQ2(), 1e-9);
        assertEquals(20000, c.getMaxCompletions());
    }

    @Test
    @DisplayName("Throughput bound correttamente calcolato: collo di bottiglia Q1")
    void testSaturationBoundBottleneckQ1() {
        // Z=10, S1=1.0, S2=0.8 → D_max = 1.0, D_sum = 11.8
        // X*(N) = min(1/0.5, N/11.8)
        // Per N=5: min(2.0, 5/11.8) = 5/11.8 ≈ 0.4237 → domina D_sum
        // Per N=30: min(2.0, 30/11.8) = 2.0 → domina bottleneck
        ClosedNetworkConfig c5 = new ClosedNetworkConfig(5, 10.0, 1.0, 0.8, 0.5, 10000);
        ClosedNetworkConfig c30 = new ClosedNetworkConfig(30, 10.0, 1.0, 0.8, 0.5, 10000);

        assertEquals(5.0 / (10.0 + 0.5 + 0.4), c5.getSaturationThroughputBound(), 1e-6);
        assertEquals(1.0 / 0.5, c30.getSaturationThroughputBound(), 1e-6);
    }
}
