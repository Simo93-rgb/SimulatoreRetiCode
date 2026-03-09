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

    private static final int     DEFAULT_REPLICAS    = 20;
    private static final long    DEFAULT_COMPLETIONS = 50_000L;
    private static final double  DEFAULT_Z           = 10.0;
    private static final double  DEFAULT_S1          = 1.0;
    private static final double  DEFAULT_S2          = 0.8;
    private static final double  DEFAULT_P1          = 0.3;
    private static final int[]   DEFAULT_N_VALUES    = { 5, 11, 17, 23, 30 };

    public static void main(String[] args) {
        int    numReplicas  = DEFAULT_REPLICAS;
        long   completions  = DEFAULT_COMPLETIONS;
        double thinkTime    = DEFAULT_Z;
        double serviceQ1    = DEFAULT_S1;
        double serviceQ2    = DEFAULT_S2;
        double routingP1    = DEFAULT_P1;
        int[]  nValues      = DEFAULT_N_VALUES;

        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "--replicas":
                    if (i + 1 < args.length) numReplicas = Integer.parseInt(args[++i]);
                    break;
                case "--completions":
                    if (i + 1 < args.length) completions = Long.parseLong(args[++i]);
                    break;
                case "--N":
                    if (i + 1 < args.length) {
                        String[] parts = args[++i].split(",");
                        nValues = new int[parts.length];
                        for (int j = 0; j < parts.length; j++) nValues[j] = Integer.parseInt(parts[j].trim());
                    }
                    break;
                case "--Z":
                    if (i + 1 < args.length) thinkTime = Double.parseDouble(args[++i]);
                    break;
                case "--S1":
                    if (i + 1 < args.length) serviceQ1 = Double.parseDouble(args[++i]);
                    break;
                case "--S2":
                    if (i + 1 < args.length) serviceQ2 = Double.parseDouble(args[++i]);
                    break;
                case "--p1":
                    if (i + 1 < args.length) routingP1 = Double.parseDouble(args[++i]);
                    break;
                case "--help":
                    System.out.println("Uso: Punto6Runner [opzioni]");
                    System.out.println("  --replicas N      Numero di repliche (default: " + DEFAULT_REPLICAS + ")");
                    System.out.println("  --completions N   Completamenti Q1 per replica (default: " + DEFAULT_COMPLETIONS + ")");
                    System.out.println("  --N n1,n2,...     Valori di N da simulare (default: 5,11,17,23,30)");
                    System.out.println("  --Z val           Think time medio (default: " + DEFAULT_Z + ")");
                    System.out.println("  --S1 val          Servizio medio Q1 (default: " + DEFAULT_S1 + ")");
                    System.out.println("  --S2 val          Servizio medio Q2 (default: " + DEFAULT_S2 + ")");
                    System.out.println("  --p1 val          Probabilità routing verso Q1 (default: " + DEFAULT_P1 + ")");
                    return;
                default:
                    System.err.println("Argomento sconosciuto: " + args[i]);
            }
        }

        run(numReplicas, completions, thinkTime, serviceQ1, serviceQ2, routingP1, nValues);
    }

    public static void run() {
        run(DEFAULT_REPLICAS, DEFAULT_COMPLETIONS, DEFAULT_Z, DEFAULT_S1, DEFAULT_S2, DEFAULT_P1, DEFAULT_N_VALUES);
    }

    public static void run(int numReplicas, long completions, double thinkTime,
                           double serviceQ1, double serviceQ2, double routingP1, int[] nValues) {
        System.out.println("# PUNTO 6 - SISTEMA CHIUSO CON Q0, Q1, Q2\n");
        System.out.println("- **Parametri**: `Z=" + thinkTime + "s`, `S1=" + serviceQ1
                + "s`, `S2=" + serviceQ2 + "s`, `p1=" + routingP1 + "`");
        System.out.println("- **Configurazione**: `R=" + numReplicas
                + "` repliche, `" + completions + "` completamenti/replica\n");

        for (int N : nValues) {
            runExperiment(N, numReplicas, completions, thinkTime, serviceQ1, serviceQ2, routingP1);
        }

        System.out.println("---\n");
        System.out.println("## RACCOLTA DATI COMPLETATA\n");
        System.out.println("I risultati sono pronti per essere inseriti in `results/punto6.md`.\n");
    }

    private static void runExperiment(int N, int numReplicas, long completions,
                                       double thinkTime, double serviceQ1, double serviceQ2, double routingP1) {
        // Etichetta carico
        String loadLabel = switch (N) {
            case 5 -> "LEGGERO";
            case 11 -> "MEDIO";
            case 17 -> "INTENSO";
            case 23 -> "VICINO SATURAZIONE";
            case 30 -> "SATURAZIONE";
            default -> "";
        };

        System.out.println("## ESPERIMENTO N=" + N + " (carico " + loadLabel + ")\n");

        ClosedNetworkConfig config = new ClosedNetworkConfig(
                N, thinkTime, serviceQ1, serviceQ2, routingP1, completions);

        System.out.printf("- **Throughput bound superiore X*(N)**: `%.4f job/s`%n\n",
                config.getSaturationThroughputBound());

        ClosedNetworkResults results = new ClosedNetworkRunner(config, numReplicas).runReplications();

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

        System.out.println("### Risultati Simulatore (IC 95%)\n");
        System.out.println("#### Throughput");
        System.out.println("- **X** (sistema): " + fmt(ciX));
        System.out.println("- **X1** (Q1): " + fmt(ciX1));
        System.out.println("- **X2** (Q2): " + fmt(ciX2));
        System.out.println();

        System.out.println("#### Utilizzo");
        System.out.println("- **$\\rho(Q_1)$**: " + fmt(ciU1));
        System.out.println("- **$\\rho(Q_2)$**: " + fmt(ciU2));
        System.out.println();

        System.out.println("#### Tempo Risposta");
        System.out.println("- **$E[T_1]$**: " + fmt(ciR1));
        System.out.println("- **$E[T_2]$**: " + fmt(ciR2));
        System.out.println("- **$E[T_{sys}]$**: " + fmt(ciRs));
        System.out.println();

        System.out.println("#### Lunghezza Code");
        System.out.println("- **$E[N_{q1}]$**: " + fmt(ciNq1));
        System.out.println("- **$E[N_{q2}]$**: " + fmt(ciNq2));
        System.out.println();

        System.out.println("#### Sintesi per Tabella\n");
        System.out.printf(
                "- `X = %.4f \\in [%.4f,%.4f]`\n- `\\rho_1 = %.4f`\n- `\\rho_2 = %.4f`\n- `E[T_{sys}] = %.4f \\in [%.4f,%.4f]`%n\n",
                ciX.getMean(), ciX.getLowerBound(), ciX.getUpperBound(),
                ciU1.getMean(), ciU2.getMean(),
                ciRs.getMean(), ciRs.getLowerBound(), ciRs.getUpperBound());
    }

    private static String fmt(ConfidenceInterval ci) {
        return String.format("`%.4f` | $\\in [%.4f, %.4f]$ *(RE: %.2f%%)*",
                ci.getMean(), ci.getLowerBound(), ci.getUpperBound(),
                ci.getRelativeError() * 100);
    }
}
