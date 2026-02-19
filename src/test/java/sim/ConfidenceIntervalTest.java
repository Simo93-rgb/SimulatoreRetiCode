package sim;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

import sim.SimulationRunner.ReplicationResults.ConfidenceInterval;

class ConfidenceIntervalTest {

    @Test
    @DisplayName("IC contiene media teorica M/M/1 per utilizzo")
    void testICContainsTheoreticalUtilization() {
        double lambda = 0.7;
        double mu = 1.0;
        double rhoTheory = lambda / mu;  // 0.7

        SimulationConfig config = new SimulationConfig(lambda, mu, 5000);
        SimulationRunner runner = new SimulationRunner(config, 20);

        SimulationRunner.ReplicationResults results = runner.runReplications();
        ConfidenceInterval ci = results.getConfidenceIntervalUtilization();

        assertTrue(ci.getLowerBound() <= rhoTheory && rhoTheory <= ci.getUpperBound(),
            String.format("IC [%.4f, %.4f] deve contenere ρ teorico %.4f",
                ci.getLowerBound(), ci.getUpperBound(), rhoTheory));
    }

    @Test
    @DisplayName("IC contiene media teorica per response time")
    void testICContainsTheoreticalResponseTime() {
        double lambda = 0.6;
        double mu = 1.0;
        double expectedRT = 1.0 / (mu - lambda);  // 2.5

        SimulationConfig config = new SimulationConfig(lambda, mu, 5000);
        SimulationRunner runner = new SimulationRunner(config, 20);

        SimulationRunner.ReplicationResults results = runner.runReplications();
        ConfidenceInterval ci = results.getConfidenceIntervalResponseTime();

        assertTrue(ci.getLowerBound() <= expectedRT && expectedRT <= ci.getUpperBound(),
            String.format("IC [%.4f, %.4f] deve contenere E[T] teorico %.4f",
                ci.getLowerBound(), ci.getUpperBound(), expectedRT));
    }

    @Test
    @DisplayName("Errore relativo diminuisce con più repliche")
    void testRelativeErrorDecreasesWithMoreReplicas() {
        SimulationConfig config = new SimulationConfig(0.7, 1.0, 3000);

        // 10 repliche
        SimulationRunner runner10 = new SimulationRunner(config, 10);
        ConfidenceInterval ci10 = runner10.runReplications().getConfidenceIntervalResponseTime();

        // 30 repliche
        SimulationRunner runner30 = new SimulationRunner(config, 30);
        ConfidenceInterval ci30 = runner30.runReplications().getConfidenceIntervalResponseTime();

        // Errore relativo dovrebbe diminuire (statisticamente, non sempre)
        // Uso semiampiezza assoluta che segue 1/√R
        assertTrue(ci30.getHalfWidth() < ci10.getHalfWidth(),
            String.format("Semiampiezza con 30 repliche (%.4f) dovrebbe essere < con 10 (%.4f)",
                ci30.getHalfWidth(), ci10.getHalfWidth()));
    }

    @Test
    @DisplayName("Errore relativo < 5% con repliche sufficienti")
    void testRelativeErrorBelow5Percent() {
        SimulationConfig config = new SimulationConfig(0.7, 1.0, 10000);
        SimulationRunner runner = new SimulationRunner(config, 30);

        SimulationRunner.ReplicationResults results = runner.runReplications();
        ConfidenceInterval ci = results.getConfidenceIntervalUtilization();

        assertTrue(ci.getRelativeError() < 0.05,
            String.format("Errore relativo (%.2f%%) dovrebbe essere < 5%% con 30 repliche",
                ci.getRelativeError() * 100));
    }

    @Test
    @DisplayName("ConfidenceInterval calcola correttamente half-width")
    void testHalfWidthCalculation() {
        ConfidenceInterval ci = new ConfidenceInterval(10.0, 9.0, 11.0);

        assertEquals(1.0, ci.getHalfWidth(), 1e-10);
        assertEquals(10.0, ci.getMean(), 1e-10);
    }

    @Test
    @DisplayName("ConfidenceInterval calcola correttamente errore relativo")
    void testRelativeErrorCalculation() {
        ConfidenceInterval ci = new ConfidenceInterval(10.0, 9.0, 11.0);

        // RE = |halfWidth / mean| = |1.0 / 10.0| = 0.1 = 10%
        assertEquals(0.1, ci.getRelativeError(), 1e-10);
    }

    @Test
    @DisplayName("ConfidenceInterval gestisce mean=0 senza divisione per zero")
    void testRelativeErrorWithZeroMean() {
        ConfidenceInterval ci = new ConfidenceInterval(0.0, -0.5, 0.5);

        assertEquals(0.0, ci.getRelativeError(), 1e-10,
            "RE con mean=0 deve essere 0 (convenzione)");
    }

    @Test
    @DisplayName("toString contiene informazioni chiave")
    void testToStringFormat() {
        ConfidenceInterval ci = new ConfidenceInterval(10.0, 9.5, 10.5);
        String str = ci.toString();

        assertTrue(str.contains("10.0") || str.contains("10.00"));
        assertTrue(str.contains("9.5") || str.contains("9.50"));
        assertTrue(str.contains("10.5") || str.contains("10.50"));
        assertTrue(str.contains("RE") || str.contains("%"));
    }

    @Test
    @DisplayName("Coverage IC al 95% è circa 95%")
    void testCoverageRate() {
        // Test statistico: eseguire N volte simulazione e contare quante volte
        // l'IC contiene la media teorica. Dovrebbe essere ~95%.
        double lambda = 0.8;
        double mu = 1.0;
        double rhoTheory = lambda / mu;

        SimulationConfig config = new SimulationConfig(lambda, mu, 2000);

        int numExperiments = 50;  // Eseguiamo 50 esperimenti
        int countContains = 0;

        for (int exp = 0; exp < numExperiments; exp++) {
            long seed = 1000000L + exp * 10000L;  // Semi diversi
            SimulationRunner runner = new SimulationRunner(config, 15, seed);
            ConfidenceInterval ci = runner.runReplications().getConfidenceIntervalUtilization();

            if (ci.getLowerBound() <= rhoTheory && rhoTheory <= ci.getUpperBound()) {
                countContains++;
            }
        }

        double coverage = (double) countContains / numExperiments;

        // Coverage dovrebbe essere tra 85% e 100% (95% ± margine)
        assertTrue(coverage >= 0.85 && coverage <= 1.0,
            String.format("Coverage IC 95%% osservato: %.1f%% (atteso ~95%%)", coverage * 100));
    }
}

