package sim;

import sim.SimulationRunner.ReplicationResults;
import sim.SimulationRunner.ReplicationResults.ConfidenceInterval;

/**
 * Raccolta dati per validazione vs JMT (Punto 5).
 *
 * Esegue i 6 esperimenti pianificati e stampa risultati in formato tabellare.
 */
public class Punto5DataCollection {

    private static final int NUM_REPLICAS = 20;
    private static final int CUSTOMERS_PER_REPLICA = 100000;  // 2M customers totali (20×100k)

    public static void main(String[] args) {
        System.out.println("=".repeat(80));
        System.out.println("PUNTO 5 - RACCOLTA DATI SIMULATORE");
        System.out.println("Configurazione: R=" + NUM_REPLICAS + " repliche, N=" + CUSTOMERS_PER_REPLICA + " customer/replica");
        System.out.println("=".repeat(80));
        System.out.println();

        // Esperimento 1: M/M/1 Standard (baseline)
        runExperiment1_MM1_Baseline();

        // Esperimento 2: M/M/1 Basso carico
        runExperiment2_MM1_LowLoad();

        // Esperimento 3: M/M/1 Alto carico
        runExperiment3_MM1_HighLoad();

        // Esperimento 4: M/D/1 (servizio deterministico)
        runExperiment4_MD1();

        // Esperimento 5: M/H₂/1 (servizio iperesponenziale)
        runExperiment5_MH2();

        // Esperimento 6: M/Ek/1 (servizio Erlang)
        runExperiment6_MEk();

        System.out.println("\n" + "=".repeat(80));
        System.out.println("RACCOLTA DATI COMPLETATA");
        System.out.println("Copia questi risultati in docs/punto5.md nelle tabelle corrispondenti");
        System.out.println("=".repeat(80));
    }

    private static void runExperiment1_MM1_Baseline() {
        System.out.println("--- ESPERIMENTO 1: M/M/1 Standard (ρ=0.8) ---");
        System.out.println("Parametri: λ=0.8, μ=1.0");
        System.out.println("Teoria: E[T]=5.0, E[N]=4.0, E[Nq]=3.2, ρ=0.8\n");

        SimulationConfig config = new SimulationConfig(0.8, 1.0, CUSTOMERS_PER_REPLICA);
        ReplicationResults results = new SimulationRunner(config, NUM_REPLICAS).runReplications();

        printResults(results);
        System.out.println();
    }

    private static void runExperiment2_MM1_LowLoad() {
        System.out.println("--- ESPERIMENTO 2: M/M/1 Basso Carico (ρ=0.5) ---");
        System.out.println("Parametri: λ=0.5, μ=1.0");
        System.out.println("Teoria: E[T]=2.0, E[N]=1.0, ρ=0.5\n");

        SimulationConfig config = new SimulationConfig(0.5, 1.0, CUSTOMERS_PER_REPLICA);
        ReplicationResults results = new SimulationRunner(config, NUM_REPLICAS).runReplications();

        printResults(results);
        System.out.println();
    }

    private static void runExperiment3_MM1_HighLoad() {
        System.out.println("--- ESPERIMENTO 3: M/M/1 Alto Carico (ρ=0.9) ---");
        System.out.println("Parametri: λ=0.9, μ=1.0");
        System.out.println("Teoria: E[T]=10.0, E[N]=9.0, ρ=0.9\n");

        SimulationConfig config = new SimulationConfig(0.9, 1.0, CUSTOMERS_PER_REPLICA);
        ReplicationResults results = new SimulationRunner(config, NUM_REPLICAS).runReplications();

        printResults(results);
        System.out.println();
    }

    private static void runExperiment4_MD1() {
        System.out.println("--- ESPERIMENTO 4: M/D/1 (Servizio Deterministico) ---");
        System.out.println("Parametri: λ=0.8, μ=1.0 (servizio costante D=1.0)");
        System.out.println("Teoria M/D/1: E[T]=3.0, E[Nq]=1.6");
        System.out.println("NOTA: Implementazione M/D/1 richiede modifica ServiceGenerator");
        System.out.println("      (usare distribuzione uniforme U[0.999, 1.001] come approssimazione)");
        System.out.println("SKIP per ora - implementare se necessario\n");
    }

    private static void runExperiment5_MH2() {
        System.out.println("--- ESPERIMENTO 5: M/H₂/1 (Servizio Iperesponenziale) ---");
        System.out.println("Parametri: λ=0.8, servizio Hyperexp(p=0.8, μ1=0.8333, μ2=5.0)");
        System.out.println("Media servizio: E[S]=1.0, Cs²>1");
        System.out.println("Previsione: E[T] > 5.0 (M/M/1)\n");
        System.out.println("NOTA: Implementazione M/H₂/1 richiede integrazione Hyperexponential in simulatore");
        System.out.println("      ServiceGenerator.hyperexponential() già disponibile (Punto 1)");
        System.out.println("SKIP per ora - implementare se necessario\n");
    }

    private static void runExperiment6_MEk() {
        System.out.println("--- ESPERIMENTO 6: M/Ek/1 (Servizio Erlang) ---");
        System.out.println("Parametri: λ=0.8, servizio Erlang(k=4, mean=1.0)");
        System.out.println("Cs²=1/4=0.25");
        System.out.println("Previsione: E[T] < 5.0 (M/M/1)\n");
        System.out.println("NOTA: Implementazione M/Ek/1 richiede integrazione Erlang in simulatore");
        System.out.println("      ServiceGenerator.erlang() già disponibile (Punto 1)");
        System.out.println("SKIP per ora - implementare se necessario\n");
    }

    private static void printResults(ReplicationResults results) {
        ConfidenceInterval ciThroughput = results.getConfidenceIntervalThroughput();
        ConfidenceInterval ciUtilization = results.getConfidenceIntervalUtilization();
        ConfidenceInterval ciResponseTime = results.getConfidenceIntervalResponseTime();
        ConfidenceInterval ciQueueLength = results.getConfidenceIntervalQueueLength();
        ConfidenceInterval ciSystemSize = results.getConfidenceIntervalSystemSize();

        System.out.println("RISULTATI SIMULATORE (IC 95%):");
        System.out.println("  Throughput (X):     " + formatCI(ciThroughput));
        System.out.println("  Utilizzo (ρ):       " + formatCI(ciUtilization));
        System.out.println("  E[T] (Response):    " + formatCI(ciResponseTime));
        System.out.println("  E[Nq] (Queue Len):  " + formatCI(ciQueueLength));
        System.out.println("  E[N] (System Size): " + formatCI(ciSystemSize));

        System.out.println("\nFORMATO PER TABELLA punto5.md:");
        System.out.printf("  X:    %.4f ∈ [%.4f, %.4f]\n",
            ciThroughput.getMean(), ciThroughput.getLowerBound(), ciThroughput.getUpperBound());
        System.out.printf("  ρ:    %.4f ∈ [%.4f, %.4f]\n",
            ciUtilization.getMean(), ciUtilization.getLowerBound(), ciUtilization.getUpperBound());
        System.out.printf("  E[T]: %.4f ∈ [%.4f, %.4f]\n",
            ciResponseTime.getMean(), ciResponseTime.getLowerBound(), ciResponseTime.getUpperBound());
        System.out.printf("  E[Nq]: %.4f ∈ [%.4f, %.4f]\n",
            ciQueueLength.getMean(), ciQueueLength.getLowerBound(), ciQueueLength.getUpperBound());
        System.out.printf("  E[N]: %.4f ∈ [%.4f, %.4f]\n",
            ciSystemSize.getMean(), ciSystemSize.getLowerBound(), ciSystemSize.getUpperBound());
    }

    private static String formatCI(ConfidenceInterval ci) {
        return String.format("%.4f ∈ [%.4f, %.4f] (RE=%.2f%%)",
            ci.getMean(), ci.getLowerBound(), ci.getUpperBound(), ci.getRelativeError() * 100);
    }
}


