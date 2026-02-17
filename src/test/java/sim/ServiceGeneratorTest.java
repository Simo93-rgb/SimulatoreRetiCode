package sim;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;

import libraries.Rngs;
import libraries.Rvgs;

class ServiceGeneratorTest {

    private ServiceGenerator serviceGen;
    private static final int N = 100_000;
    private static final double TOLERANCE = 0.05; // 5%

    @BeforeEach
    void setUp() {
        // Viene eseguito prima di OGNI test
        Rngs rngs = new Rngs();
        rngs.plantSeeds(123456789);
        Rvgs rvgs = new Rvgs(rngs);
        serviceGen = new ServiceGenerator(rngs, rvgs);
    }

    @Test
    @DisplayName("Verifica Distribuzione Esponenziale")
    void testExponential() {
        double mean = 5.0;
        double sum = 0.0;

        for (int i = 0; i < N; i++) {
            sum += serviceGen.exponential(mean);
        }

        double actualMean = sum / N;
        assertEquals(mean, actualMean, mean * TOLERANCE, "La media esponenziale non è corretta");
    }

    @Test
    @DisplayName("Verifica Iper-Esponenziale")
    void testHyperExponential() {
        double p = 0.7; // 70% ramo 1
        double mean1 = 10.0;
        double mean2 = 2.0;
        double expectedMean = (p * mean1) + ((1 - p) * mean2); // E[X] teorico

        double sum = 0.0;
        for (int i = 0; i < N; i++) {
            sum += serviceGen.hyperExponential(p, mean1, mean2);
        }

        double actualMean = sum / N;
        assertEquals(expectedMean, actualMean, expectedMean * TOLERANCE, "La media iper-esponenziale non torna");
    }
}