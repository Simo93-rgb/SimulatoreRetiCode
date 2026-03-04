package sim;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

class MixedNetworkConfigTest {

    private MixedNetworkConfig valid() {
        return new MixedNetworkConfig(15, 10.0, 1.0, 0.8, 10000, 0.5, 5.0, 15.0);
    }

    @Test @DisplayName("Costruzione valida non lancia eccezioni")
    void testValidConfig() {
        assertDoesNotThrow(this::valid);
    }

    @Test @DisplayName("openHyperP fuori (0,1) lancia eccezione")
    void testInvalidHyperP() {
        assertThrows(IllegalArgumentException.class, () ->
            new MixedNetworkConfig(15, 10.0, 1.0, 0.8, 10000, 0.0, 5.0, 15.0));
        assertThrows(IllegalArgumentException.class, () ->
            new MixedNetworkConfig(15, 10.0, 1.0, 0.8, 10000, 1.0, 5.0, 15.0));
    }

    @Test @DisplayName("meanServiceQ1Open = 2 * S1")
    void testOpenServiceDouble() {
        MixedNetworkConfig c = valid();
        assertEquals(2.0 * c.getMeanServiceQ1Closed(), c.getMeanServiceQ1Open(), 1e-9);
    }

    @Test @DisplayName("getOpenMeanInterarrival coerente con parametri iperesponenziale")
    void testMeanInterarrival() {
        // p=0.5, mean1=5.0, mean2=15.0 → E[A] = 0.5*5 + 0.5*15 = 10.0
        MixedNetworkConfig c = valid();
        assertEquals(10.0, c.getOpenMeanInterarrival(), 1e-9);
    }

    @Test @DisplayName("Parametri negativi o zero lanciano eccezione")
    void testInvalidParams() {
        assertThrows(IllegalArgumentException.class, () ->
            new MixedNetworkConfig(0, 10.0, 1.0, 0.8, 10000, 0.5, 5.0, 15.0));
        assertThrows(IllegalArgumentException.class, () ->
            new MixedNetworkConfig(15, 0.0, 1.0, 0.8, 10000, 0.5, 5.0, 15.0));
        assertThrows(IllegalArgumentException.class, () ->
            new MixedNetworkConfig(15, 10.0, 1.0, 0.8, 10000, 0.5, 0.0, 15.0));
    }
}

