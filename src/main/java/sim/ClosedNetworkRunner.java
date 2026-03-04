package sim;

import sim.SimulationRunner.ReplicationResults.ConfidenceInterval;

/**
 * Wrapper per eseguire R repliche indipendenti del sistema chiuso.
 *
 * Segue lo stesso pattern di {@link SimulationRunner}:
 * - Semi distanziati per indipendenza statistica tra repliche
 * - Calcolo media, varianza, IC 95% (t-Student) per ogni indice
 */
public class ClosedNetworkRunner {

    private final ClosedNetworkConfig config;
    private final int numReplicas;
    private final long baseSeed;

    public ClosedNetworkRunner(ClosedNetworkConfig config, int numReplicas, long baseSeed) {
        this.config      = config;
        this.numReplicas = numReplicas;
        this.baseSeed    = baseSeed;
    }

    public ClosedNetworkRunner(ClosedNetworkConfig config, int numReplicas) {
        this(config, numReplicas, SeedManager.DEFAULT_BASE_SEED);
    }

    /**
     * Esegue R repliche e restituisce i risultati aggregati.
     *
     * @return risultati aggregati con IC per tutti gli indici
     */
    public ClosedNetworkResults runReplications() {
        long[] seeds = SeedManager.generateDistancedSeeds(baseSeed, numReplicas);

        ClosedNetworkStatistics[] stats = new ClosedNetworkStatistics[numReplicas];
        double[] clocks = new double[numReplicas];

        for (int i = 0; i < numReplicas; i++) {
            ClosedNetworkSimulator sim = new ClosedNetworkSimulator(config, seeds[i]);
            stats[i]  = sim.run();
            clocks[i] = sim.getClock();
        }

        return new ClosedNetworkResults(config, stats, clocks, numReplicas);
    }

    // =========================================================================
    // Risultati aggregati
    // =========================================================================

    /**
     * Risultati delle R repliche con statistiche e IC 95%.
     */
    public static class ClosedNetworkResults {

        private final ClosedNetworkConfig config;
        private final ClosedNetworkStatistics[] stats;
        private final double[] clocks;
        private final int numReplicas;

        public ClosedNetworkResults(ClosedNetworkConfig config,
                                     ClosedNetworkStatistics[] stats,
                                     double[] clocks,
                                     int numReplicas) {
            this.config      = config;
            this.stats       = stats;
            this.clocks      = clocks;
            this.numReplicas = numReplicas;
        }

        // ---- Statistiche di base ----

        private double getMean(java.util.function.ToDoubleFunction<Integer> f) {
            double sum = 0;
            for (int i = 0; i < numReplicas; i++) sum += f.applyAsDouble(i);
            return sum / numReplicas;
        }

        private double getVariance(java.util.function.ToDoubleFunction<Integer> f) {
            double mean = getMean(f);
            double sq   = 0;
            for (int i = 0; i < numReplicas; i++) {
                double d = f.applyAsDouble(i) - mean;
                sq += d * d;
            }
            return numReplicas > 1 ? sq / (numReplicas - 1) : 0.0;
        }

        private double getStdDev(java.util.function.ToDoubleFunction<Integer> f) {
            return Math.sqrt(getVariance(f));
        }

        private ConfidenceInterval ci(java.util.function.ToDoubleFunction<Integer> f) {
            double mean   = getMean(f);
            double stdDev = getStdDev(f);
            double t      = tQuantile(numReplicas - 1);
            double hw     = t * stdDev / Math.sqrt(numReplicas);
            return new ConfidenceInterval(mean, mean - hw, mean + hw);
        }

        // ---- IC per tutti gli indici ----

        public ConfidenceInterval ciThroughputSystem() {
            return ci(i -> stats[i].getSystemThroughput(clocks[i]));
        }

        public ConfidenceInterval ciThroughputQ1() {
            return ci(i -> stats[i].getThroughputQ1(clocks[i]));
        }

        public ConfidenceInterval ciThroughputQ2() {
            return ci(i -> stats[i].getThroughputQ2(clocks[i]));
        }

        public ConfidenceInterval ciUtilizationQ1() {
            return ci(i -> stats[i].getUtilizationQ1(clocks[i]));
        }

        public ConfidenceInterval ciUtilizationQ2() {
            return ci(i -> stats[i].getUtilizationQ2(clocks[i]));
        }

        public ConfidenceInterval ciResponseTimeQ1() {
            return ci(i -> stats[i].getMeanResponseTimeQ1());
        }

        public ConfidenceInterval ciResponseTimeQ2() {
            return ci(i -> stats[i].getMeanResponseTimeQ2());
        }

        public ConfidenceInterval ciResponseTimeSystem() {
            return ci(i -> stats[i].getMeanResponseTimeSystem());
        }

        public ConfidenceInterval ciQueueLengthQ1() {
            return ci(i -> stats[i].getMeanQueueLengthQ1(clocks[i]));
        }

        public ConfidenceInterval ciQueueLengthQ2() {
            return ci(i -> stats[i].getMeanQueueLengthQ2(clocks[i]));
        }

        public ConfidenceInterval ciSystemSizeQ1() {
            return ci(i -> stats[i].getMeanSystemSizeQ1(clocks[i]));
        }

        public ConfidenceInterval ciSystemSizeQ2() {
            return ci(i -> stats[i].getMeanSystemSizeQ2(clocks[i]));
        }

        // ---- Getters raw ----

        public ClosedNetworkConfig getConfig()                { return config; }
        public int getNumReplicas()                           { return numReplicas; }
        public ClosedNetworkStatistics getStats(int i)        { return stats[i]; }
        public double getClock(int i)                         { return clocks[i]; }

        // ---- t-Student (stesso metodo di SimulationRunner) ----

        private static double tQuantile(int df) {
            if (df > 30) return 1.96;
            double[] t = {
                0, 12.706, 4.303, 3.182, 2.776, 2.571,
                2.447, 2.365, 2.306, 2.262, 2.228,
                2.201, 2.179, 2.160, 2.145, 2.131,
                2.120, 2.110, 2.101, 2.093, 2.086,
                2.080, 2.074, 2.069, 2.064, 2.060,
                2.056, 2.052, 2.048, 2.045, 2.042, 2.040
            };
            return df < t.length ? t[df] : 1.96;
        }
    }
}

