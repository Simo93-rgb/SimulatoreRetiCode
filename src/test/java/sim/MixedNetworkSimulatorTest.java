package sim;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import sim.core.Customer;
import sim.core.Customer.CustomerClass;
import static org.junit.jupiter.api.Assertions.*;

class MixedNetworkSimulatorTest {

    private static final long SEED = 100_000_007L;
    private static final long COMPLETIONS = 20_000L;
    // Parametri base: N=15, Z=10, S1=1.0, S2=0.8, λ_open=0.3
    private static final double MEAN_ARR = 1.0 / 0.3; // ≈ 3.333 s
    private static final double MEAN1 = 0.5 * MEAN_ARR;
    private static final double MEAN2 = 1.5 * MEAN_ARR;

    private MixedNetworkConfig cfg() {
        return new MixedNetworkConfig(15, 10.0, 1.0, 0.8, 0.5, COMPLETIONS, 0.5, MEAN1, MEAN2);
    }

    @Test
    @DisplayName("Simulazione termina al numero corretto di completamenti totali su Q1")
    void testCompletionsCount() {
        MixedNetworkSimulator sim = new MixedNetworkSimulator(cfg(), SEED);
        MixedNetworkStatistics stats = sim.run();
        long total = stats.getCompletionsQ1Closed() + stats.getCompletionsQ1Open();
        assertEquals(COMPLETIONS, total,
                "La simulazione deve fermarsi a maxCompletions completamenti totali su Q1");
    }

    @Test
    @DisplayName("Clock avanza durante la simulazione")
    void testClockAdvances() {
        MixedNetworkSimulator sim = new MixedNetworkSimulator(cfg(), SEED);
        sim.run();
        assertTrue(sim.getClock() > 0);
    }

    @Test
    @DisplayName("La classe aperta genera completamenti su Q1 (flusso esterno attivo)")
    void testOpenClassCompletions() {
        MixedNetworkSimulator sim = new MixedNetworkSimulator(cfg(), SEED);
        MixedNetworkStatistics stats = sim.run();
        assertTrue(stats.getCompletionsQ1Open() > 0,
                "La classe aperta deve aver completato almeno un servizio");
    }

    @Test
    @DisplayName("La classe aperta NON transita per Q2 (completamenti Q2 ≈ completamenti chiusi)")
    void testOpenClassDoesNotVisitQ2() {
        MixedNetworkSimulator sim = new MixedNetworkSimulator(cfg(), SEED);
        MixedNetworkStatistics stats = sim.run();
        // In steady state completamenti Q2 ≈ completamenti chiusi su Q1
        long diff = Math.abs(stats.getCompletionsQ2() - stats.getCompletionsQ1Closed());
        // Tolleranza: al massimo 1% di scarto per effetti di bordo
        assertTrue(diff < COMPLETIONS * 0.01,
                String.format("Q2 completamenti (%d) deve essere vicino a chiusi Q1 (%d)",
                        stats.getCompletionsQ2(), stats.getCompletionsQ1Closed()));
    }

    @Test
    @DisplayName("Legge di utilizzo Q1 totale: ρ1 ≈ X1_closed·S1 + X1_open·2·S1")
    void testUtilizationLawQ1() {
        MixedNetworkSimulator sim = new MixedNetworkSimulator(cfg(), SEED);
        MixedNetworkStatistics stats = sim.run();
        double clock = sim.getClock();

        double rho1 = stats.getUtilizationQ1(clock);
        double expected = stats.getThroughputQ1Closed(clock) * 1.0
                + stats.getThroughputQ1Open(clock) * 2.0; // 2·S1

        assertEquals(expected, rho1, expected * 0.05,
                String.format("ρ1=%.4f deve essere ≈ X_closed·S1 + X_open·2S1=%.4f", rho1, expected));
    }

    @Test
    @DisplayName("Legge di utilizzo Q2: ρ2 ≈ X_closed · S2")
    void testUtilizationLawQ2() {
        MixedNetworkSimulator sim = new MixedNetworkSimulator(cfg(), SEED);
        MixedNetworkStatistics stats = sim.run();
        double clock = sim.getClock();

        double rho2 = stats.getUtilizationQ2(clock);
        double expected = stats.getSystemThroughput(clock) * 0.8;

        assertEquals(expected, rho2, expected * 0.05,
                String.format("ρ2=%.4f deve essere ≈ X·S2=%.4f", rho2, expected));
    }

    @Test
    @DisplayName("E[T1_open] >= 2·S1 (almeno il tempo di servizio puro della classe aperta)")
    void testOpenServiceTimeDouble() {
        // E[T1_open] = tempo attesa in coda + tempo servizio
        // Il tempo di servizio della classe aperta è 2·S1 = 2.0 s
        // Quindi E[T1_open] >= 2.0 in qualsiasi condizione di carico
        MixedNetworkSimulator sim = new MixedNetworkSimulator(cfg(), SEED);
        MixedNetworkStatistics stats = sim.run();

        double t1o = stats.getMeanResponseTimeQ1Open();
        double minExp = 2.0 * 1.0; // 2 · S1

        assertTrue(t1o >= minExp,
                String.format("E[T1_open]=%.4f deve essere >= 2·S1=%.4f", t1o, minExp));
    }

    @Test
    @DisplayName("Al crescere di λ_open, ρ_Q1 totale cresce")
    void testUtilizationIncreasesWithLambda() {
        MixedNetworkConfig cfgLow = makeCfg(0.10);
        MixedNetworkConfig cfgHigh = makeCfg(0.50);

        MixedNetworkSimulator simLow = new MixedNetworkSimulator(cfgLow, SEED);
        MixedNetworkSimulator simHigh = new MixedNetworkSimulator(cfgHigh, SEED);

        double rhoLow = simLow.run().getUtilizationQ1(simLow.getClock());
        double rhoHigh = simHigh.run().getUtilizationQ1(simHigh.getClock());

        assertTrue(rhoHigh > rhoLow,
                String.format("ρ1(λ=0.5)=%.4f deve essere > ρ1(λ=0.1)=%.4f", rhoHigh, rhoLow));
    }

    @Test
    @DisplayName("Al crescere di λ_open, E[T1_closed] cresce (interferenza della classe aperta)")
    void testClosedResponseTimeIncreasesWithLambda() {
        MixedNetworkConfig cfgLow = makeCfg(0.10);
        MixedNetworkConfig cfgHigh = makeCfg(0.50);

        MixedNetworkSimulator simLow = new MixedNetworkSimulator(cfgLow, SEED);
        MixedNetworkSimulator simHigh = new MixedNetworkSimulator(cfgHigh, SEED);

        double t1Low = simLow.run().getMeanResponseTimeQ1Closed();
        double t1High = simHigh.run().getMeanResponseTimeQ1Closed();

        assertTrue(t1High > t1Low,
                String.format("E[T1_closed](λ=0.5)=%.4f deve essere > E[T1_closed](λ=0.1)=%.4f",
                        t1High, t1Low));
    }

    @Test
    @DisplayName("Repliche indipendenti producono risultati diversi")
    void testReplicasAreDifferent() {
        long[] seeds = SeedManager.generateDistancedSeeds(SEED, 2);
        MixedNetworkSimulator s1 = new MixedNetworkSimulator(cfg(), seeds[0]);
        MixedNetworkSimulator s2 = new MixedNetworkSimulator(cfg(), seeds[1]);
        assertNotEquals(s1.run().getMeanResponseTimeQ1Closed(),
                s2.run().getMeanResponseTimeQ1Closed());
    }

    @Test
    @DisplayName("CustomerClass di default è CLOSED (retrocompatibilità)")
    void testCustomerDefaultClass() {
        Customer c = new Customer(1, 0.0);
        assertEquals(CustomerClass.CLOSED, c.getCustomerClass());
        assertFalse(c.isOpen());
    }

    @Test
    @DisplayName("Customer OPEN è riconosciuto correttamente")
    void testCustomerOpenClass() {
        Customer c = new Customer(1, 0.0, CustomerClass.OPEN);
        assertEquals(CustomerClass.OPEN, c.getCustomerClass());
        assertTrue(c.isOpen());
    }

    // ---- Helper ----
    private MixedNetworkConfig makeCfg(double lambda) {
        double meanArr = 1.0 / lambda;
        return new MixedNetworkConfig(
                15, 10.0, 1.0, 0.8, 0.5, COMPLETIONS,
                0.5, 0.5 * meanArr, 1.5 * meanArr);
    }
}
