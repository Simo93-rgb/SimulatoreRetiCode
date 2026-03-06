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

    private static final int NUM_REPLICAS = 20;
    private static final long COMPLETIONS = 50_000L;

    private static final int N = 15;
    private static final double THINK_TIME = 10.0;
    private static final double SERVICE_Q1 = 1.0;
    private static final double SERVICE_Q2 = 0.8;
    private static final double HYPER_P = 0.5;

    // Tassi $\\lambda$ della classe aperta da esplorare.
    // Nota: la classe aperta occupa Q1 per 2·S1=2s → satura Q1 quando
    // $\\lambda_{open} \\cdot 2 \\to 1$,
    // cioè $\\lambda \\approx 0.45$.
    // Esploriamo fino a $\\lambda=0.40$ per mantenere il sistema in regime stabile
    // e
    // confrontabile.
    private static final double[] LAMBDA_OPEN = { 0.10, 0.20, 0.30, 0.40 };

    public static void main(String[] args) {
        run();
    }

    public static void run() {
        System.out.println("# PUNTO 7 - SISTEMA MISTO: CLASSE CHIUSA (N=15) + CLASSE APERTA\n");
        System.out.println("- **Parametri fissi**: `Z=" + THINK_TIME + "s`, `S1=" + SERVICE_Q1
                + "s`, `S2=" + SERVICE_Q2 + "s`, `S1_open=" + (2 * SERVICE_Q1) + "s`");
        System.out.println("- **Inter-arrivi aperti**: Hyperexp(p=" + HYPER_P
                + ", mean1=0.5 $\\cdot$ E[A], mean2=1.5 $\\cdot$ E[A])");
        System.out.println("- **Configurazione**: `R=" + NUM_REPLICAS
                + "` repliche, `" + COMPLETIONS + "` completamenti classe chiusa/replica\n");

        for (double lambda : LAMBDA_OPEN) {
            runExperiment(lambda);
        }

        System.out.println("---\n");
        System.out.println("## RACCOLTA DATI COMPLETATA\n");
        System.out.println("I risultati sono pronti per essere inseriti in `results/punto7.md`.\n");
    }

    private static void runExperiment(double lambda) {
        double meanInterarrival = 1.0 / lambda;
        double mean1 = 0.5 * meanInterarrival;
        double mean2 = 1.5 * meanInterarrival;

        // Verifica: E[A] = p·mean1 + (1-p)·mean2 = 0.5·0.5·E[A] + 0.5·1.5·E[A] = E[A] ✓
        System.out.printf("## ESPERIMENTO $\\lambda_{open}=%.2f$\n\n", lambda);
        System.out.printf("- **$E[A]$**: `%.3f s`\n", meanInterarrival);
        System.out.printf("- **mean1**: `%.3f s`\n", mean1);
        System.out.printf("- **mean2**: `%.3f s`\n\n", mean2);

        MixedNetworkConfig config = new MixedNetworkConfig(
                N, THINK_TIME, SERVICE_Q1, SERVICE_Q2, 0.3, COMPLETIONS,
                HYPER_P, mean1, mean2);

        MixedNetworkResults results = new MixedNetworkRunner(config, NUM_REPLICAS).runReplications();

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
