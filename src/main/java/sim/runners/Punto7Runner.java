package sim.runners;

import sim.MixedNetworkConfig;
import sim.MixedNetworkRunner;
import sim.MixedNetworkRunner.MixedNetworkResults;
import sim.SimulationRunner.ReplicationResults.ConfidenceInterval;

/**
 * Raccolta dati per il sistema misto classe chiusa + classe aperta (Punto 7).
 *
 * Parametri fissi (stessi del punto 6):
 * - N = 15 clienti chiusi
 * - Z = 10.0 s (think time Q0)
 * - S1 = 1.0 s (servizio Q1 classe chiusa)
 * - S2 = 0.8 s (servizio Q2)
 * - S1_open = 2·S1 = 2.0 s (servizio Q1 classe aperta, da consegna)
 *
 * Inter-arrivi classe aperta: iperesponenziale (p=0.5, mean1, mean2)
 * La media totale E[A] = p·mean1 + (1-p)·mean2 decresce al crescere del tasso
 * λ.
 *
 * Valori crescenti di $\\lambda_{open} = 1/E[A]$ esplorati:
 * - $\\lambda=0.10$ → carico leggero
 * - $\\lambda=0.30$ → carico medio
 * - $\\lambda=0.50$ → carico intenso
 * - $\\lambda=0.70$ → vicino alla saturazione
 *
 * Per ogni $\\lambda$ fissiamo $E[A]$ e poniamo mean1 = 0.5·E[A], mean2 =
 * 1.5·E[A]
 * (stessa proporzione usata nel punto 5 per M/H₂/1, C²_v > 1 garantita).
 */
public class Punto7Runner {

    private static final int      DEFAULT_REPLICAS    = 20;
    private static final long     DEFAULT_COMPLETIONS = 50_000L;
    private static final int      DEFAULT_N           = 15;
    private static final double   DEFAULT_Z           = 10.0;
    private static final double   DEFAULT_S1          = 1.0;
    private static final double   DEFAULT_S2          = 0.8;
    private static final double   DEFAULT_P1          = 0.3;
    private static final double   DEFAULT_HYPER_P     = 0.5;
    private static final double[] DEFAULT_LAMBDA_OPEN = { 0.10, 0.20, 0.30, 0.40 };

    public static void main(String[] args) {
        int      numReplicas  = DEFAULT_REPLICAS;
        long     completions  = DEFAULT_COMPLETIONS;
        int      N            = DEFAULT_N;
        double   thinkTime    = DEFAULT_Z;
        double   serviceQ1    = DEFAULT_S1;
        double   serviceQ2    = DEFAULT_S2;
        double   routingP1    = DEFAULT_P1;
        double   hyperP       = DEFAULT_HYPER_P;
        double[] lambdaOpen   = DEFAULT_LAMBDA_OPEN;

        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "--replicas":
                    if (i + 1 < args.length) numReplicas = Integer.parseInt(args[++i]);
                    break;
                case "--completions":
                    if (i + 1 < args.length) completions = Long.parseLong(args[++i]);
                    break;
                case "--N":
                    if (i + 1 < args.length) N = Integer.parseInt(args[++i]);
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
                case "--lambda":
                    if (i + 1 < args.length) {
                        String[] parts = args[++i].split(",");
                        lambdaOpen = new double[parts.length];
                        for (int j = 0; j < parts.length; j++) lambdaOpen[j] = Double.parseDouble(parts[j].trim());
                    }
                    break;
                case "--help":
                    System.out.println("Uso: Punto7Runner [opzioni]");
                    System.out.println("  --replicas N      Numero di repliche (default: " + DEFAULT_REPLICAS + ")");
                    System.out.println("  --completions N   Completamenti classe chiusa per replica (default: " + DEFAULT_COMPLETIONS + ")");
                    System.out.println("  --N val           Clienti chiusi (default: " + DEFAULT_N + ")");
                    System.out.println("  --Z val           Think time medio (default: " + DEFAULT_Z + ")");
                    System.out.println("  --S1 val          Servizio medio Q1 classe chiusa (default: " + DEFAULT_S1 + ")");
                    System.out.println("  --S2 val          Servizio medio Q2 (default: " + DEFAULT_S2 + ")");
                    System.out.println("  --p1 val          Probabilità routing verso Q1 (default: " + DEFAULT_P1 + ")");
                    System.out.println("  --lambda l1,l2,.  Tassi lambda classe aperta (default: 0.10,0.20,0.30,0.40)");
                    return;
                default:
                    System.err.println("Argomento sconosciuto: " + args[i]);
            }
        }

        run(numReplicas, completions, N, thinkTime, serviceQ1, serviceQ2, routingP1, hyperP, lambdaOpen);
    }

    public static void run() {
        run(DEFAULT_REPLICAS, DEFAULT_COMPLETIONS, DEFAULT_N, DEFAULT_Z, DEFAULT_S1, DEFAULT_S2,
                DEFAULT_P1, DEFAULT_HYPER_P, DEFAULT_LAMBDA_OPEN);
    }

    public static void run(int numReplicas, long completions, int N,
                           double thinkTime, double serviceQ1, double serviceQ2,
                           double routingP1, double hyperP, double[] lambdaOpen) {
        System.out.println("# PUNTO 7 - SISTEMA MISTO: CLASSE CHIUSA (N=" + N + ") + CLASSE APERTA\n");
        System.out.println("- **Parametri fissi**: `Z=" + thinkTime + "s`, `S1=" + serviceQ1
                + "s`, `S2=" + serviceQ2 + "s`, `S1_open=" + (2 * serviceQ1) + "s`");
        System.out.println("- **Inter-arrivi aperti**: Hyperexp(p=" + hyperP
                + ", mean1=0.5 $\\cdot$ E[A], mean2=1.5 $\\cdot$ E[A])");
        System.out.println("- **Configurazione**: `R=" + numReplicas
                + "` repliche, `" + completions + "` completamenti classe chiusa/replica\n");

        for (double lambda : lambdaOpen) {
            runExperiment(lambda, numReplicas, completions, N, thinkTime, serviceQ1, serviceQ2, routingP1, hyperP);
        }

        System.out.println("---\n");
        System.out.println("## RACCOLTA DATI COMPLETATA\n");
        System.out.println("I risultati sono pronti per essere inseriti in `results/punto7.md`.\n");
    }

    private static void runExperiment(double lambda, int numReplicas, long completions,
                                       int N, double thinkTime, double serviceQ1, double serviceQ2,
                                       double routingP1, double hyperP) {
        double meanInterarrival = 1.0 / lambda;
        double mean1 = 0.5 * meanInterarrival;
        double mean2 = 1.5 * meanInterarrival;

        // Verifica: E[A] = p·mean1 + (1-p)·mean2 = 0.5·0.5·E[A] + 0.5·1.5·E[A] = E[A] ✓
        System.out.printf("## ESPERIMENTO $\\lambda_{open}=%.2f$\n\n", lambda);
        System.out.printf("- **$E[A]$**: `%.3f s`\n", meanInterarrival);
        System.out.printf("- **mean1**: `%.3f s`\n", mean1);
        System.out.printf("- **mean2**: `%.3f s`\n\n", mean2);

        MixedNetworkConfig config = new MixedNetworkConfig(
                N, thinkTime, serviceQ1, serviceQ2, routingP1, completions,
                hyperP, mean1, mean2);

        MixedNetworkResults results = new MixedNetworkRunner(config, numReplicas).runReplications();

        printResults(results, lambda);
        System.out.println();
    }

    private static void printResults(MixedNetworkResults r, double lambda) {
        System.out.println("### Risultati Simulatore (IC 95%)\n");
        System.out.println("#### Throughput");
        System.out.printf("- **X_sistema** (chiuso): %s%n", fmt(r.ciThroughputSystem()));
        System.out.printf("- **X_Q1 totale**: %s%n", fmt(r.ciThroughputQ1()));
        System.out.printf("- **X_Q1 chiuso**: %s%n", fmt(r.ciThroughputQ1Closed()));
        System.out.printf("- **X_Q1 aperto**: %s%n", fmt(r.ciThroughputQ1Open()));
        System.out.printf("- **X_Q2**: %s%n\n", fmt(r.ciThroughputQ2()));

        System.out.println("#### Utilizzo");
        System.out.printf("- **$\\rho(Q_1)$ totale**: %s%n", fmt(r.ciUtilizationQ1()));
        System.out.printf("- **$\\rho(Q_1)$** (chiuso): %s%n", fmt(r.ciUtilizationQ1Closed()));
        System.out.printf("- **$\\rho(Q_1)$** (aperto): %s%n", fmt(r.ciUtilizationQ1Open()));
        System.out.printf("- **$\\rho(Q_2)$**: %s%n\n", fmt(r.ciUtilizationQ2()));

        System.out.println("#### Tempo Risposta");
        System.out.printf("- **$E[T_1]$ chiuso**: %s%n", fmt(r.ciResponseTimeQ1Closed()));
        System.out.printf("- **$E[T_1]$ aperto**: %s%n", fmt(r.ciResponseTimeQ1Open()));
        System.out.printf("- **$E[T_2]$**: %s%n", fmt(r.ciResponseTimeQ2()));
        System.out.printf("- **$E[T_{sys}]$ chiuso**: %s%n\n", fmt(r.ciResponseTimeSystemClosed()));

        System.out.println("#### Lunghezza Code");
        System.out.printf("- **$E[N_{q1}]$**: %s%n", fmt(r.ciQueueLengthQ1()));
        System.out.printf("- **$E[N_{q2}]$**: %s%n\n", fmt(r.ciQueueLengthQ2()));
    }

    private static String fmt(ConfidenceInterval ci) {
        return String.format("`%.4f` | $\\in [%.4f, %.4f]$ *(RE: %.2f%%)*",
                ci.getMean(), ci.getLowerBound(), ci.getUpperBound(),
                ci.getRelativeError() * 100);
    }
}
