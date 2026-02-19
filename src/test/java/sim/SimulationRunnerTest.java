package sim;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

class SimulationRunnerTest {

    @Test
    @DisplayName("Esegue R repliche con successo")
    void testRunMultipleReplicas() {
        SimulationConfig config = new SimulationConfig(0.7, 1.0, 1000);
        SimulationRunner runner = new SimulationRunner(config, 5);

        SimulationRunner.ReplicationResults results = runner.runReplications();

        assertEquals(5, results.getNumReplicas());
        assertNotNull(results.getReport());
    }

    @Test
    @DisplayName("Repliche indipendenti producono variabilità")
    void testReplicasProduceVariability() {
        SimulationConfig config = new SimulationConfig(0.8, 1.0, 500);
        SimulationRunner runner = new SimulationRunner(config, 10);

        SimulationRunner.ReplicationResults results = runner.runReplications();

        // Deve esserci variabilità tra repliche
        assertTrue(results.getStdDevResponseTime() > 0,
            "Deviazione standard response time deve essere > 0");
        assertTrue(results.getStdDevUtilization() > 0,
            "Deviazione standard utilization deve essere > 0");
    }

    @Test
    @DisplayName("Media tra repliche vicina a valore teorico M/M/1")
    void testMeanAcrossReplicasMatchesTheory() {
        double lambda = 0.6;
        double mu = 1.0;
        double expectedRT = 1.0 / (mu - lambda);  // 2.5

        SimulationConfig config = new SimulationConfig(lambda, mu, 5000);
        SimulationRunner runner = new SimulationRunner(config, 20);

        SimulationRunner.ReplicationResults results = runner.runReplications();
        double meanRT = results.getMeanResponseTime();

        // Con 20 repliche e 5000 customer, media deve essere molto vicina
        assertEquals(expectedRT, meanRT, 0.15,
            String.format("Media E[T] su 20 repliche (%.3f) deve essere vicina a teoria (%.3f)",
                meanRT, expectedRT));
    }

    @Test
    @DisplayName("Utilizzo medio vicino a ρ teorico")
    void testMeanUtilizationMatchesRho() {
        double lambda = 0.75;
        double mu = 1.0;
        double rhoTheory = lambda / mu;

        SimulationConfig config = new SimulationConfig(lambda, mu, 3000);
        SimulationRunner runner = new SimulationRunner(config, 15);

        SimulationRunner.ReplicationResults results = runner.runReplications();
        double meanRho = results.getMeanUtilization();

        assertEquals(rhoTheory, meanRho, 0.05,
            String.format("Utilizzo medio (%.3f) deve essere vicino a ρ=%.3f",
                meanRho, rhoTheory));
    }

    @Test
    @DisplayName("Report contiene informazioni chiave")
    void testReportContainsKeyMetrics() {
        SimulationConfig config = new SimulationConfig(0.7, 1.0, 1000);
        SimulationRunner runner = new SimulationRunner(config, 3);

        SimulationRunner.ReplicationResults results = runner.runReplications();
        String report = results.getReport();

        assertTrue(report.contains("Throughput"));
        assertTrue(report.contains("Utilization"));
        assertTrue(report.contains("Response"));
        assertTrue(report.contains("Queue"));
    }
}

