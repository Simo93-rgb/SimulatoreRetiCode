package sim;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

class SimulationConfigTest {

    @Test
    @DisplayName("Creazione config con parametri validi")
    void testValidConfig() {
        SimulationConfig config = new SimulationConfig(0.8, 1.0, 10000);

        assertEquals(0.8, config.getArrivalRate(), 1e-10);
        assertEquals(1.0, config.getServiceRate(), 1e-10);
        assertEquals(10000, config.getMaxCustomers());
        assertEquals(0.8, config.getUtilization(), 1e-10);
    }

    @Test
    @DisplayName("Calcolo medie da tassi")
    void testMeanCalculations() {
        SimulationConfig config = new SimulationConfig(0.5, 2.0, 1000);

        assertEquals(2.0, config.getMeanInterarrival(), 1e-10);  // 1/0.5
        assertEquals(0.5, config.getMeanService(), 1e-10);       // 1/2.0
        assertEquals(0.25, config.getUtilization(), 1e-10);      // 0.5/2.0
    }

    @Test
    @DisplayName("Rifiuta tasso arrivo negativo")
    void testRejectNegativeArrivalRate() {
        assertThrows(IllegalArgumentException.class, () -> {
            new SimulationConfig(-0.5, 1.0, 1000);
        });
    }

    @Test
    @DisplayName("Rifiuta tasso servizio zero")
    void testRejectZeroServiceRate() {
        assertThrows(IllegalArgumentException.class, () -> {
            new SimulationConfig(0.5, 0.0, 1000);
        });
    }

    @Test
    @DisplayName("Rifiuta sistema instabile (ρ >= 1)")
    void testRejectUnstableSystem() {
        // ρ = 1.0 / 1.0 = 1.0 >= 1 → instabile
        assertThrows(IllegalArgumentException.class, () -> {
            new SimulationConfig(1.0, 1.0, 1000);
        });

        // ρ = 1.5 / 1.0 = 1.5 > 1 → instabile
        assertThrows(IllegalArgumentException.class, () -> {
            new SimulationConfig(1.5, 1.0, 1000);
        });
    }

    @Test
    @DisplayName("Accetta sistema stabile (ρ < 1)")
    void testAcceptStableSystem() {
        // ρ = 0.9 / 1.0 = 0.9 < 1 → stabile
        assertDoesNotThrow(() -> {
            new SimulationConfig(0.9, 1.0, 1000);
        });
    }

    @Test
    @DisplayName("Rifiuta max customers negativo")
    void testRejectNegativeMaxCustomers() {
        assertThrows(IllegalArgumentException.class, () -> {
            new SimulationConfig(0.5, 1.0, -100);
        });
    }

    @Test
    @DisplayName("ToString contiene parametri chiave")
    void testToString() {
        SimulationConfig config = new SimulationConfig(0.8, 1.0, 5000);
        String str = config.toString();

        assertTrue(str.contains("0.8") || str.contains("0.80"));
        assertTrue(str.contains("1.0") || str.contains("1.00"));
        assertTrue(str.contains("5000"));
    }
}

