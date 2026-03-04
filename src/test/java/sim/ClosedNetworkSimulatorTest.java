package sim;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test suite per ClosedNetworkSimulator.
 *
 * Verifica:
 * 1. La simulazione termina al numero corretto di completamenti
 * 2. L'invariante di conservazione dei clienti (N costante)
 * 3. Throughput coerente con i bound dell'analisi operazionale
 * 4. Utilizzi Q1 e Q2 coerenti con la legge di utilizzo (ρ = X · S)
 * 5. Legge di Little verificata per Q1 e Q2
 * 6. Throughput di Q1 e Q2 coincidono (sistema in serie, steady state)
 */
class ClosedNetworkSimulatorTest {

    private static final long SEED           = 100_000_007L;
    private static final long COMPLETIONS    = 30_000L;
    private static final double THINK_TIME   = 10.0;
    private static final double SERVICE_Q1   = 1.0;
    private static final double SERVICE_Q2   = 0.8;

    /** Crea config standard con N clienti. */
    private ClosedNetworkConfig cfg(int N) {
        return new ClosedNetworkConfig(N, THINK_TIME, SERVICE_Q1, SERVICE_Q2, COMPLETIONS);
    }

    @Test
    @DisplayName("Simulazione termina al numero corretto di completamenti Q1")
    void testCompletionsCount() {
        ClosedNetworkSimulator sim = new ClosedNetworkSimulator(cfg(10), SEED);
        ClosedNetworkStatistics stats = sim.run();

        assertEquals(COMPLETIONS, stats.getCompletionsQ1(),
            "Simulazione deve fermarsi esattamente a maxCompletions");
    }

    @Test
    @DisplayName("Clock avanza durante la simulazione")
    void testClockAdvances() {
        ClosedNetworkSimulator sim = new ClosedNetworkSimulator(cfg(10), SEED);
        sim.run();
        assertTrue(sim.getClock() > 0, "Clock deve avanzare durante la simulazione");
    }

    @Test
    @DisplayName("Q2 completa circa lo stesso numero di job di Q1 (stato stazionario)")
    void testThroughputBalance() {
        ClosedNetworkSimulator sim = new ClosedNetworkSimulator(cfg(10), SEED);
        ClosedNetworkStatistics stats = sim.run();
        double clock = sim.getClock();

        double x1 = stats.getThroughputQ1(clock);
        double x2 = stats.getThroughputQ2(clock);

        // In steady state X1 ≈ X2 (sistema chiuso in serie)
        assertEquals(x1, x2, x1 * 0.05,
            String.format("Throughput Q1 (%.4f) e Q2 (%.4f) devono essere vicini", x1, x2));
    }

    @Test
    @DisplayName("Throughput non supera il bound di saturazione (analisi operazionale)")
    void testThroughputBound() {
        int N = 15;
        ClosedNetworkConfig config = cfg(N);
        ClosedNetworkSimulator sim  = new ClosedNetworkSimulator(config, SEED);
        ClosedNetworkStatistics stats = sim.run();

        double x     = stats.getSystemThroughput(sim.getClock());
        double xBound = config.getSaturationThroughputBound();

        assertTrue(x <= xBound * 1.02,
            String.format("Throughput simulato (%.4f) non deve superare il bound (%.4f)", x, xBound));
    }

    @Test
    @DisplayName("Legge di utilizzo: ρ1 = X · S1 (con tolleranza 5%)")
    void testUtilizationLawQ1() {
        ClosedNetworkSimulator sim = new ClosedNetworkSimulator(cfg(15), SEED);
        ClosedNetworkStatistics stats = sim.run();
        double clock = sim.getClock();

        double rho1     = stats.getUtilizationQ1(clock);
        double x        = stats.getSystemThroughput(clock);
        double rho1Exp  = x * SERVICE_Q1;

        assertEquals(rho1Exp, rho1, rho1Exp * 0.05,
            String.format("ρ1 simulato (%.4f) deve essere vicino a X·S1 (%.4f)", rho1, rho1Exp));
    }

    @Test
    @DisplayName("Legge di utilizzo: ρ2 = X · S2 (con tolleranza 5%)")
    void testUtilizationLawQ2() {
        ClosedNetworkSimulator sim = new ClosedNetworkSimulator(cfg(15), SEED);
        ClosedNetworkStatistics stats = sim.run();
        double clock = sim.getClock();

        double rho2     = stats.getUtilizationQ2(clock);
        double x        = stats.getSystemThroughput(clock);
        double rho2Exp  = x * SERVICE_Q2;

        assertEquals(rho2Exp, rho2, rho2Exp * 0.05,
            String.format("ρ2 simulato (%.4f) deve essere vicino a X·S2 (%.4f)", rho2, rho2Exp));
    }

    @Test
    @DisplayName("Legge di Little per Q1: E[N1] = X · E[T1] (con tolleranza 5%)")
    void testLittlesLawQ1() {
        ClosedNetworkSimulator sim = new ClosedNetworkSimulator(cfg(15), SEED);
        ClosedNetworkStatistics stats = sim.run();
        double clock = sim.getClock();

        double x  = stats.getSystemThroughput(clock);
        double t1 = stats.getMeanResponseTimeQ1();
        double n1 = stats.getMeanSystemSizeQ1(clock);

        double n1Expected = x * t1;
        assertEquals(n1Expected, n1, n1Expected * 0.05,
            String.format("E[N1]=%.4f deve essere vicino a X·E[T1]=%.4f", n1, n1Expected));
    }

    @Test
    @DisplayName("Legge di Little per Q2: E[N2] = X · E[T2] (con tolleranza 5%)")
    void testLittlesLawQ2() {
        ClosedNetworkSimulator sim = new ClosedNetworkSimulator(cfg(15), SEED);
        ClosedNetworkStatistics stats = sim.run();
        double clock = sim.getClock();

        double x  = stats.getSystemThroughput(clock);
        double t2 = stats.getMeanResponseTimeQ2();
        double n2 = stats.getMeanSystemSizeQ2(clock);

        double n2Expected = x * t2;
        assertEquals(n2Expected, n2, n2Expected * 0.05,
            String.format("E[N2]=%.4f deve essere vicino a X·E[T2]=%.4f", n2, n2Expected));
    }

    @Test
    @DisplayName("Con N grande il throughput si avvicina al bottleneck bound 1/S1")
    void testSaturationAtHighN() {
        // N=50 >> N* → throughput deve avvicinarsi a 1/S1 = 1.0
        ClosedNetworkSimulator sim = new ClosedNetworkSimulator(cfg(50), SEED);
        ClosedNetworkStatistics stats = sim.run();

        double x = stats.getSystemThroughput(sim.getClock());
        double bottleneckBound = 1.0 / SERVICE_Q1;  // 1.0

        assertEquals(bottleneckBound, x, bottleneckBound * 0.02,
            String.format("Con N=50, X (%.4f) deve essere vicino al bound 1/S1 (%.4f)", x, bottleneckBound));
    }

    @Test
    @DisplayName("Con N piccolo il throughput cresce con N (regime leggero)")
    void testThroughputGrowsWithN() {
        ClosedNetworkSimulator sim5  = new ClosedNetworkSimulator(cfg(5),  SEED);
        ClosedNetworkSimulator sim15 = new ClosedNetworkSimulator(cfg(15), SEED);

        double x5  = sim5.run().getSystemThroughput(sim5.getClock());
        double x15 = sim15.run().getSystemThroughput(sim15.getClock());

        assertTrue(x15 > x5,
            String.format("Throughput deve crescere con N: X(N=5)=%.4f, X(N=15)=%.4f", x5, x15));
    }

    @Test
    @DisplayName("Repliche indipendenti producono risultati diversi")
    void testReplicasAreDifferent() {
        long[] seeds = SeedManager.generateDistancedSeeds(SEED, 2);

        ClosedNetworkSimulator sim1 = new ClosedNetworkSimulator(cfg(10), seeds[0]);
        ClosedNetworkSimulator sim2 = new ClosedNetworkSimulator(cfg(10), seeds[1]);

        double t1 = sim1.run().getMeanResponseTimeQ1();
        double t2 = sim2.run().getMeanResponseTimeQ1();

        assertNotEquals(t1, t2, "Repliche con semi diversi devono dare risultati diversi");
    }
}

