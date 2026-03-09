package sim.runners;

import sim.SimulationConfig;
import sim.SimulationRunner;
import sim.SimulationRunner.ReplicationResults;
import sim.SimulationRunner.ReplicationResults.ConfidenceInterval;

/**
 * Raccolta dati per validazione vs JMT (Punto 5).
 *
 * Esegue i 6 esperimenti pianificati e stampa risultati in formato tabellare.
 * Può essere lanciato direttamente oppure tramite Main con argomento "punto5".
 */
public class Punto5Runner {

    private static final int DEFAULT_REPLICAS = 20;
    private static final int DEFAULT_CUSTOMERS = 100000; // 2M customers totali (20×100k)

    public static void main(String[] args) {
        int numReplicas = DEFAULT_REPLICAS;
        int customersPerReplica = DEFAULT_CUSTOMERS;

        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "--replicas":
                    if (i + 1 < args.length) numReplicas = Integer.parseInt(args[++i]);
                    break;
                case "--customers":
                    if (i + 1 < args.length) customersPerReplica = Integer.parseInt(args[++i]);
                    break;
                case "--help":
                    System.out.println("Uso: Punto5Runner [--replicas N] [--customers N]");
                    System.out.println("  --replicas N    Numero di repliche (default: " + DEFAULT_REPLICAS + ")");
                    System.out.println("  --customers N   Completamenti per replica (default: " + DEFAULT_CUSTOMERS + ")");
                    return;
                default:
                    System.err.println("Argomento sconosciuto: " + args[i]);
            }
        }

        run(numReplicas, customersPerReplica);
    }

    public static void run() {
        run(DEFAULT_REPLICAS, DEFAULT_CUSTOMERS);
    }

    public static void run(int numReplicas, int customersPerReplica) {
        System.out.println("# PUNTO 5 - RACCOLTA DATI SIMULATORE\n");
        System.out.println("**Configurazione:** `R=" + numReplicas + "` repliche, `N=" + customersPerReplica
                + "` customer/replica\n");

        // Esperimento 1: M/M/1 Standard (baseline)
        runExperiment1_MM1_Baseline(numReplicas, customersPerReplica);

        // Esperimento 2: M/M/1 Basso carico
        runExperiment2_MM1_LowLoad(numReplicas, customersPerReplica);

        // Esperimento 3: M/M/1 Alto carico
        runExperiment3_MM1_HighLoad(numReplicas, customersPerReplica);

        // Esperimento 4: M/D/1 (servizio deterministico)
        runExperiment4_MD1(numReplicas, customersPerReplica);

        // Esperimento 5: M/H₂/1 (servizio iperesponenziale)
        runExperiment5_MH2(numReplicas, customersPerReplica);

        // Esperimento 6: M/Ek/1 (servizio Erlang)
        runExperiment6_MEk(numReplicas, customersPerReplica);

        System.out.println("---\n");
        System.out.println("## RACCOLTA DATI COMPLETATA\n");
        System.out.println("I risultati sono pronti per essere inseriti in `results/punto5.md`.\n");
    }

    private static void runExperiment1_MM1_Baseline(int numReplicas, int customersPerReplica) {
        System.out.println("## ESPERIMENTO 1: M/M/1 Standard ($\\rho=0.8$)\n");
        System.out.println("- **Parametri:** $\\lambda=0.8$, $\\mu=1.0$");
        System.out.println("- **Teoria:** $E[T]=5.0$, $E[N]=4.0$, $E[N_q]=3.2$, $\\rho=0.8$\n");

        SimulationConfig config = new SimulationConfig(0.8, 1.0, customersPerReplica);
        ReplicationResults results = new SimulationRunner(config, numReplicas).runReplications();

        printResults(results);
        System.out.println();
    }

    private static void runExperiment2_MM1_LowLoad(int numReplicas, int customersPerReplica) {
        System.out.println("## ESPERIMENTO 2: M/M/1 Basso Carico ($\\rho=0.5$)\n");
        System.out.println("- **Parametri:** $\\lambda=0.5$, $\\mu=1.0$");
        System.out.println("- **Teoria:** $E[T]=2.0$, $E[N]=1.0$, $\\rho=0.5$\n");

        SimulationConfig config = new SimulationConfig(0.5, 1.0, customersPerReplica);
        ReplicationResults results = new SimulationRunner(config, numReplicas).runReplications();

        printResults(results);
        System.out.println();
    }

    private static void runExperiment3_MM1_HighLoad(int numReplicas, int customersPerReplica) {
        System.out.println("## ESPERIMENTO 3: M/M/1 Alto Carico ($\\rho=0.9$)\n");
        System.out.println("- **Parametri:** $\\lambda=0.9$, $\\mu=1.0$");
        System.out.println("- **Teoria:** $E[T]=10.0$, $E[N]=9.0$, $\\rho=0.9$\n");

        SimulationConfig config = new SimulationConfig(0.9, 1.0, customersPerReplica);
        ReplicationResults results = new SimulationRunner(config, numReplicas).runReplications();

        printResults(results);
        System.out.println();
    }

    private static void runExperiment4_MD1(int numReplicas, int customersPerReplica) {
        System.out.println("## ESPERIMENTO 4: M/D/1 (Servizio Deterministico)\n");
        System.out.println("- **Parametri:** $\\lambda=0.8$, servizio D=1.0 ($cv^2=0$)");
        System.out.println("- **Teoria M/D/1:** $E[T]=3.0$, $E[N_q]=1.6$, $E[N]=2.4$\n");

        SimulationConfig config = new SimulationConfig(
                0.8, 1.0, customersPerReplica,
                SimulationConfig.ServiceDistribution.DETERMINISTIC,
                0, 0, 0, 0 // parametri inutilizzati per DETERMINISTIC
        );
        ReplicationResults results = new SimulationRunner(config, numReplicas).runReplications();

        printResults(results);
        System.out.println();
    }

    private static void runExperiment5_MH2(int numReplicas, int customersPerReplica) {
        System.out.println("## ESPERIMENTO 5: M/H$_2$/1 (Servizio Iperesponenziale)\n");
        System.out.println("- **Parametri:** $\\lambda=0.8$, servizio Hyperexp(p=0.5, mean1=0.5, mean2=1.5)");
        System.out.println("- **Media servizio:** $E[S]=1.0$, $cv^2>1$");
        System.out.println("- **Previsione:** $E[T] > 5.0$ (M/M/1)\n");

        SimulationConfig config = new SimulationConfig(
                0.8, 1.0, customersPerReplica,
                SimulationConfig.ServiceDistribution.HYPEREXPONENTIAL,
                0, // erlangK (inutilizzato)
                0.5, // hyperP
                0.5, // hyperMean1
                1.5 // hyperMean2 → E[S] = 0.5*0.5 + 0.5*1.5 = 1.0
        );
        ReplicationResults results = new SimulationRunner(config, numReplicas).runReplications();

        printResults(results);
        System.out.println();
    }

    private static void runExperiment6_MEk(int numReplicas, int customersPerReplica) {
        System.out.println("## ESPERIMENTO 6: M/E$_k$/1 (Servizio Erlang)\n");
        System.out.println("- **Parametri:** $\\lambda=0.8$, servizio Erlang(k=4, mean=1.0)");
        System.out.println("- **cv$^2$:** $1/k=0.25$");
        System.out.println("- **Previsione:** $E[T] < 5.0$ (M/M/1)\n");

        SimulationConfig config = new SimulationConfig(
                0.8, 1.0, customersPerReplica,
                SimulationConfig.ServiceDistribution.ERLANG,
                4, // erlangK
                0, 0, 0 // hyper params (inutilizzati)
        );
        ReplicationResults results = new SimulationRunner(config, numReplicas).runReplications();

        printResults(results);
        System.out.println();
    }

    private static void printResults(ReplicationResults results) {
        ConfidenceInterval ciThroughput = results.getConfidenceIntervalThroughput();
        ConfidenceInterval ciUtilization = results.getConfidenceIntervalUtilization();
        ConfidenceInterval ciResponseTime = results.getConfidenceIntervalResponseTime();
        ConfidenceInterval ciQueueLength = results.getConfidenceIntervalQueueLength();
        ConfidenceInterval ciSystemSize = results.getConfidenceIntervalSystemSize();

        System.out.println("### Risultati Simulatore (IC 95%)\n");
        System.out.println("| Metrica | Media | Intervallo di Confidenza | Errore Relativo |");
        System.out.println("|---|---|---|---|");
        System.out.println("| **X** (Throughput) | " + formatCIRow(ciThroughput));
        System.out.println("| **$\\rho$** (Utilizzo) | " + formatCIRow(ciUtilization));
        System.out.println("| **$E[T]$** (Response Time) | " + formatCIRow(ciResponseTime));
        System.out.println("| **$E[N_q]$** (Queue Length) | " + formatCIRow(ciQueueLength));
        System.out.println("| **$E[N]$** (System Size) | " + formatCIRow(ciSystemSize));
    }

    private static String formatCIRow(ConfidenceInterval ci) {
        return String.format("`%.4f` | $\\in [%.4f, %.4f]$ | `%.2f%%` |",
                ci.getMean(), ci.getLowerBound(), ci.getUpperBound(), ci.getRelativeError() * 100);
    }
}
