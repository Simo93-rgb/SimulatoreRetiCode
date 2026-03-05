package sim.runners;

import sim.ClosedNetworkConfig;
import sim.ClosedNetworkRunner;
import sim.ClosedNetworkRunner.ClosedNetworkResults;
import sim.SimulationRunner.ReplicationResults.ConfidenceInterval;

/**
 * Raccolta dati per il sistema chiuso a tre centri (Punto 6).
 *
 * Esegue esperimenti al variare di N (numero di clienti):
 * - N=5 → carico leggero
 * - N=15 → carico medio
 * - N=30 → carico intenso (vicino alla saturazione)
 * - N=50 → saturazione
 *
 * Parametri fissi:
 * - Z = 10.0 s (think time medio in Q0)
 * - S1 = 1.0 s (servizio medio in Q1)
 * - S2 = 0.8 s (servizio medio in Q2)
 * - Collo di bottiglia: Q1 (S1 > S2)
 *
 * Può essere lanciato direttamente oppure tramite Main con argomento "punto6".
 */
public class Punto6Runner {

    private static final int NUM_REPLICAS = 20;
    private static final long COMPLETIONS = 50_000L; // completamenti Q1 per replica

    // Parametri sistema (fissi tra tutti gli esperimenti)
    private static final double THINK_TIME = 10.0;
    private static final double SERVICE_Q1 = 1.0;
    private static final double SERVICE_Q2 = 0.8;

    // Valori di N da simulare
    private static final int[] N_VALUES = { 5, 15, 30, 50 };

    public static void main(String[] args) {
        run();
    }

    public static void run() {
        System.out.println("=".repeat(80));
        System.out.println("PUNTO 6 - SISTEMA CHIUSO CON Q0, Q1, Q2");
        System.out.println("Parametri: Z=" + THINK_TIME + "s, S1=" + SERVICE_Q1
                + "s, S2=" + SERVICE_Q2 + "s");
        System.out.println("Configurazione: R=" + NUM_REPLICAS
                + " repliche, " + COMPLETIONS + " completamenti/replica");
        System.out.println("=".repeat(80));
        System.out.println();

        for (int N : N_VALUES) {
            runExperiment(N);
        }

        System.out.println("=".repeat(80));
        System.out.println("RACCOLTA DATI COMPLETATA");
        System.out.println("Copia i risultati in docs/punto6.md");
        System.out.println("=".repeat(80));
    }

    private static void runExperiment(int N) {
        // Etichetta carico
        String loadLabel = switch (N) {
            case 5 -> "LEGGERO";
            case 15 -> "MEDIO";
            case 30 -> "INTENSO";
            case 50 -> "SATURAZIONE";
            default -> "";
        };

        System.out.println("--- ESPERIMENTO N=" + N + " (carico " + loadLabel + ") ---");

        ClosedNetworkConfig config = new ClosedNetworkConfig(
                N, THINK_TIME, SERVICE_Q1, SERVICE_Q2, 0.3, COMPLETIONS);

        System.out.printf("  Throughput bound superiore X*(N) = %.4f job/s%n",
                config.getSaturationThroughputBound());

        ClosedNetworkResults results = new ClosedNetworkRunner(config, NUM_REPLICAS).runReplications();

        printResults(results);
        System.out.println();
    }

    private static void printResults(ClosedNetworkResults r) {
        ConfidenceInterval ciX = r.ciThroughputSystem();
        ConfidenceInterval ciX1 = r.ciThroughputQ1();
        ConfidenceInterval ciX2 = r.ciThroughputQ2();
        ConfidenceInterval ciR1 = r.ciResponseTimeQ1();
        ConfidenceInterval ciR2 = r.ciResponseTimeQ2();
        ConfidenceInterval ciRs = r.ciResponseTimeSystem();
        ConfidenceInterval ciU1 = r.ciUtilizationQ1();
        ConfidenceInterval ciU2 = r.ciUtilizationQ2();
        ConfidenceInterval ciNq1 = r.ciQueueLengthQ1();
        ConfidenceInterval ciNq2 = r.ciQueueLengthQ2();

        System.out.println("  THROUGHPUT:");
        System.out.println("    X (sistema): " + fmt(ciX));
        System.out.println("    X1 (Q1):     " + fmt(ciX1));
        System.out.println("    X2 (Q2):     " + fmt(ciX2));

        System.out.println("  UTILIZZO:");
        System.out.println("    ρ(Q1):      " + fmt(ciU1));
        System.out.println("    ρ(Q2):      " + fmt(ciU2));

        System.out.println("  TEMPO RISPOSTA:");
        System.out.println("    E[T1]:      " + fmt(ciR1));
        System.out.println("    E[T2]:      " + fmt(ciR2));
        System.out.println("    E[T_sys]:   " + fmt(ciRs));

        System.out.println("  LUNGHEZZA CODE:");
        System.out.println("    E[Nq1]:     " + fmt(ciNq1));
        System.out.println("    E[Nq2]:     " + fmt(ciNq2));

        System.out.println("\n  FORMATO TABELLA punto6.md:");
        System.out.printf("    X=%.4f ∈ [%.4f,%.4f] | ρ1=%.4f | ρ2=%.4f | E[T_sys]=%.4f ∈ [%.4f,%.4f]%n",
                ciX.getMean(), ciX.getLowerBound(), ciX.getUpperBound(),
                ciU1.getMean(), ciU2.getMean(),
                ciRs.getMean(), ciRs.getLowerBound(), ciRs.getUpperBound());
    }

    private static String fmt(ConfidenceInterval ci) {
        return String.format("%.4f ∈ [%.4f, %.4f] (RE=%.2f%%)",
                ci.getMean(), ci.getLowerBound(), ci.getUpperBound(),
                ci.getRelativeError() * 100);
    }
}
