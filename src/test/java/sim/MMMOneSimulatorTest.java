package sim;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

class MMMOneSimulatorTest {

    @Test
    @DisplayName("Simulazione termina correttamente")
    void testSimulationCompletes() {
        SimulationConfig config = new SimulationConfig(0.8, 1.0, 1000);
        MMMOneSimulator sim = new MMMOneSimulator(config, 123456789L);

        SimulationStatistics stats = sim.run();

        assertEquals(1000, stats.getCustomersServed());
        assertTrue(sim.getClock() > 0, "Clock deve avanzare");
    }

    @Test
    @DisplayName("Utilizzo server vicino a ρ teorico")
    void testUtilizationMatchesTheory() {
        double lambda = 0.7;
        double mu = 1.0;
        double rhoTheory = lambda / mu;  // 0.7

        SimulationConfig config = new SimulationConfig(lambda, mu, 10000);
        MMMOneSimulator sim = new MMMOneSimulator(config, 987654321L);

        SimulationStatistics stats = sim.run();
        double rhoSimulated = stats.getUtilization(sim.getClock());

        // Tolleranza 10% (con 10k customer dovrebbe essere vicino)
        assertEquals(rhoTheory, rhoSimulated, 0.1,
            String.format("Utilizzo simulato (%.3f) deve essere vicino a ρ=%.3f",
                rhoSimulated, rhoTheory));
    }

    @Test
    @DisplayName("Response time consistente con teoria M/M/1")
    void testResponseTimeMatchesTheory() {
        double lambda = 0.6;
        double mu = 1.0;

        // Teoria M/M/1: E[T] = 1/(μ - λ)
        double expectedResponseTime = 1.0 / (mu - lambda);  // 1/(1.0 - 0.6) = 2.5

        SimulationConfig config = new SimulationConfig(lambda, mu, 5000);
        MMMOneSimulator sim = new MMMOneSimulator(config, 111111111L);

        SimulationStatistics stats = sim.run();
        double simulatedResponseTime = stats.getMeanResponseTime();

        // Tolleranza 20% (variabilità simulazione)
        assertEquals(expectedResponseTime, simulatedResponseTime,
            expectedResponseTime * 0.2,
            String.format("E[T] simulato (%.3f) deve essere vicino a teoria (%.3f)",
                simulatedResponseTime, expectedResponseTime));
    }

    @Test
    @DisplayName("Numero medio sistema consistente con teoria (Little)")
    void testSystemSizeMatchesLittle() {
        double lambda = 0.5;
        double mu = 1.0;

        SimulationConfig config = new SimulationConfig(lambda, mu, 8000);
        MMMOneSimulator sim = new MMMOneSimulator(config, 222222222L);

        SimulationStatistics stats = sim.run();

        double throughput = stats.getThroughput(sim.getClock());
        double meanResponseTime = stats.getMeanResponseTime();
        double meanSystemSize = stats.getMeanSystemSize(sim.getClock());

        // Teorema di Little: E[N] = λ * E[T]
        double expectedN = throughput * meanResponseTime;

        assertEquals(expectedN, meanSystemSize, 0.1,
            "E[N] deve rispettare teorema di Little");
    }

    @Test
    @DisplayName("Simulazioni con semi diversi producono risultati diversi")
    void testDifferentSeedsProduceDifferentResults() {
        SimulationConfig config = new SimulationConfig(0.7, 1.0, 1000);

        MMMOneSimulator sim1 = new MMMOneSimulator(config, 100000001L);
        MMMOneSimulator sim2 = new MMMOneSimulator(config, 200000001L);

        SimulationStatistics stats1 = sim1.run();
        SimulationStatistics stats2 = sim2.run();

        double rt1 = stats1.getMeanResponseTime();
        double rt2 = stats2.getMeanResponseTime();

        assertNotEquals(rt1, rt2, 1e-6,
            "Semi diversi devono produrre risultati diversi");
    }

    @Test
    @DisplayName("Stessa simulazione con stesso seed è riproducibile")
    void testReproducibilityWithSameSeed() {
        long seed = 333333333L;
        SimulationConfig config = new SimulationConfig(0.8, 1.0, 500);

        MMMOneSimulator sim1 = new MMMOneSimulator(config, seed);
        MMMOneSimulator sim2 = new MMMOneSimulator(config, seed);

        SimulationStatistics stats1 = sim1.run();
        SimulationStatistics stats2 = sim2.run();

        assertEquals(stats1.getMeanResponseTime(), stats2.getMeanResponseTime(), 1e-10,
            "Stesso seed deve produrre risultati identici");
        assertEquals(stats1.getUtilization(sim1.getClock()),
                     stats2.getUtilization(sim2.getClock()), 1e-10);
    }
}

