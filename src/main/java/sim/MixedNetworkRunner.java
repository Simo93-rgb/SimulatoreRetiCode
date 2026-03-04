package sim;

import sim.SimulationRunner.ReplicationResults.ConfidenceInterval;

/**
 * Wrapper per R repliche indipendenti del sistema misto.
 * Calcola IC 95% per tutti gli indici separati per classe.
 */
public class MixedNetworkRunner {

    private final MixedNetworkConfig config;
    private final int  numReplicas;
    private final long baseSeed;

    public MixedNetworkRunner(MixedNetworkConfig config, int numReplicas, long baseSeed) {
        this.config      = config;
        this.numReplicas = numReplicas;
        this.baseSeed    = baseSeed;
    }

    public MixedNetworkRunner(MixedNetworkConfig config, int numReplicas) {
        this(config, numReplicas, SeedManager.DEFAULT_BASE_SEED);
    }

    public MixedNetworkResults runReplications() {
        long[] seeds = SeedManager.generateDistancedSeeds(baseSeed, numReplicas);

        MixedNetworkStatistics[] stats  = new MixedNetworkStatistics[numReplicas];
        double[]                 clocks = new double[numReplicas];

        for (int i = 0; i < numReplicas; i++) {
            MixedNetworkSimulator sim = new MixedNetworkSimulator(config, seeds[i]);
            stats[i]  = sim.run();
            clocks[i] = sim.getClock();
        }

        return new MixedNetworkResults(config, stats, clocks, numReplicas);
    }

    // =========================================================================

    public static class MixedNetworkResults {

        private final MixedNetworkConfig     config;
        private final MixedNetworkStatistics[] stats;
        private final double[]               clocks;
        private final int                    numReplicas;

        public MixedNetworkResults(MixedNetworkConfig config,
                                    MixedNetworkStatistics[] stats,
                                    double[] clocks, int numReplicas) {
            this.config      = config;
            this.stats       = stats;
            this.clocks      = clocks;
            this.numReplicas = numReplicas;
        }

        // ---- Statistiche di base ----

        private double mean(java.util.function.ToDoubleFunction<Integer> f) {
            double s = 0;
            for (int i = 0; i < numReplicas; i++) s += f.applyAsDouble(i);
            return s / numReplicas;
        }

        private double variance(java.util.function.ToDoubleFunction<Integer> f) {
            double m = mean(f), sq = 0;
            for (int i = 0; i < numReplicas; i++) { double d = f.applyAsDouble(i) - m; sq += d*d; }
            return numReplicas > 1 ? sq / (numReplicas - 1) : 0.0;
        }

        private double stddev(java.util.function.ToDoubleFunction<Integer> f) {
            return Math.sqrt(variance(f));
        }

        private ConfidenceInterval ci(java.util.function.ToDoubleFunction<Integer> f) {
            double m  = mean(f);
            double hw = tQuantile(numReplicas - 1) * stddev(f) / Math.sqrt(numReplicas);
            return new ConfidenceInterval(m, m - hw, m + hw);
        }

        // ---- Throughput ----
        public ConfidenceInterval ciThroughputSystem()   { return ci(i -> stats[i].getSystemThroughput(clocks[i])); }
        public ConfidenceInterval ciThroughputQ1()       { return ci(i -> stats[i].getThroughputQ1(clocks[i])); }
        public ConfidenceInterval ciThroughputQ1Closed() { return ci(i -> stats[i].getThroughputQ1Closed(clocks[i])); }
        public ConfidenceInterval ciThroughputQ1Open()   { return ci(i -> stats[i].getThroughputQ1Open(clocks[i])); }
        public ConfidenceInterval ciThroughputQ2()       { return ci(i -> stats[i].getThroughputQ2(clocks[i])); }

        // ---- Utilizzo ----
        public ConfidenceInterval ciUtilizationQ1()       { return ci(i -> stats[i].getUtilizationQ1(clocks[i])); }
        public ConfidenceInterval ciUtilizationQ1Closed() { return ci(i -> stats[i].getUtilizationQ1Closed(clocks[i])); }
        public ConfidenceInterval ciUtilizationQ1Open()   { return ci(i -> stats[i].getUtilizationQ1Open(clocks[i])); }
        public ConfidenceInterval ciUtilizationQ2()       { return ci(i -> stats[i].getUtilizationQ2(clocks[i])); }

        // ---- Response time ----
        public ConfidenceInterval ciResponseTimeQ1Closed()   { return ci(i -> stats[i].getMeanResponseTimeQ1Closed()); }
        public ConfidenceInterval ciResponseTimeQ1Open()     { return ci(i -> stats[i].getMeanResponseTimeQ1Open()); }
        public ConfidenceInterval ciResponseTimeQ2()         { return ci(i -> stats[i].getMeanResponseTimeQ2()); }
        public ConfidenceInterval ciResponseTimeSystemClosed() { return ci(i -> stats[i].getMeanResponseTimeSystemClosed()); }

        // ---- Code ----
        public ConfidenceInterval ciQueueLengthQ1() { return ci(i -> stats[i].getMeanQueueLengthQ1(clocks[i])); }
        public ConfidenceInterval ciQueueLengthQ2() { return ci(i -> stats[i].getMeanQueueLengthQ2(clocks[i])); }

        // ---- Getters raw ----
        public MixedNetworkConfig      getConfig()     { return config; }
        public int                     getNumReplicas() { return numReplicas; }
        public MixedNetworkStatistics  getStats(int i)  { return stats[i]; }
        public double                  getClock(int i)  { return clocks[i]; }

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

