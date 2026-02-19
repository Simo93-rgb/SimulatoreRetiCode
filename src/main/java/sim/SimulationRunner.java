package sim;

/**
 * Wrapper per eseguire R repliche indipendenti di simulazione M/M/1.
 *
 * Implementa metodo delle prove ripetute con semi distanziati.
 */
public class SimulationRunner {

    private final SimulationConfig config;
    private final int numReplicas;
    private final long baseSeed;

    /**
     * Crea runner per repliche multiple.
     *
     * @param config parametri simulazione
     * @param numReplicas numero di repliche da eseguire
     * @param baseSeed seed base (semi distanziati generati automaticamente)
     */
    public SimulationRunner(SimulationConfig config, int numReplicas, long baseSeed) {
        this.config = config;
        this.numReplicas = numReplicas;
        this.baseSeed = baseSeed;
    }

    /**
     * Crea runner con seed base di default.
     */
    public SimulationRunner(SimulationConfig config, int numReplicas) {
        this(config, numReplicas, SeedManager.DEFAULT_BASE_SEED);
    }

    /**
     * Esegue R repliche e restituisce risultati aggregati.
     *
     * @return risultati aggregati delle repliche
     */
    public ReplicationResults runReplications() {
        // Genera semi distanziati
        long[] seeds = SeedManager.generateDistancedSeeds(baseSeed, numReplicas);

        SimulationStatistics[] replicaStats = new SimulationStatistics[numReplicas];
        double[] clockTimes = new double[numReplicas];

        // Esegui repliche
        for (int i = 0; i < numReplicas; i++) {
            MMMOneSimulator simulator = new MMMOneSimulator(config, seeds[i]);
            replicaStats[i] = simulator.run();
            clockTimes[i] = simulator.getClock();
        }

        return new ReplicationResults(config, replicaStats, clockTimes);
    }

    /**
     * Risultati aggregati da R repliche indipendenti.
     */
    public static class ReplicationResults {

        private final SimulationConfig config;
        private final SimulationStatistics[] replicaStats;
        private final double[] clockTimes;
        private final int numReplicas;

        public ReplicationResults(SimulationConfig config,
                                   SimulationStatistics[] stats,
                                   double[] times) {
            this.config = config;
            this.replicaStats = stats;
            this.clockTimes = times;
            this.numReplicas = stats.length;
        }

        /**
         * Calcola media di un indice tra le repliche.
         *
         * @param extractor funzione che estrae indice da SimulationStatistics
         * @return media tra repliche
         */
        private double getMean(java.util.function.ToDoubleFunction<Integer> extractor) {
            double sum = 0.0;
            for (int i = 0; i < numReplicas; i++) {
                sum += extractor.applyAsDouble(i);
            }
            return sum / numReplicas;
        }

        /**
         * Calcola varianza campionaria di un indice tra le repliche.
         *
         * Formula: S² = (1/(R-1)) * Σ(Xi - X̄)²
         *
         * @param extractor funzione che estrae indice
         * @return varianza campionaria
         */
        private double getVariance(java.util.function.ToDoubleFunction<Integer> extractor) {
            double mean = getMean(extractor);
            double sumSquaredDiff = 0.0;

            for (int i = 0; i < numReplicas; i++) {
                double diff = extractor.applyAsDouble(i) - mean;
                sumSquaredDiff += diff * diff;
            }

            return numReplicas > 1 ? sumSquaredDiff / (numReplicas - 1) : 0.0;
        }

        /**
         * Calcola deviazione standard di un indice.
         *
         * @param extractor funzione che estrae indice
         * @return deviazione standard
         */
        private double getStdDev(java.util.function.ToDoubleFunction<Integer> extractor) {
            return Math.sqrt(getVariance(extractor));
        }

        // ========== Throughput ==========

        public double getMeanThroughput() {
            return getMean(i -> replicaStats[i].getThroughput(clockTimes[i]));
        }

        public double getStdDevThroughput() {
            return getStdDev(i -> replicaStats[i].getThroughput(clockTimes[i]));
        }

        // ========== Utilization ==========

        public double getMeanUtilization() {
            return getMean(i -> replicaStats[i].getUtilization(clockTimes[i]));
        }

        public double getStdDevUtilization() {
            return getStdDev(i -> replicaStats[i].getUtilization(clockTimes[i]));
        }

        // ========== Response Time ==========

        public double getMeanResponseTime() {
            return getMean(i -> replicaStats[i].getMeanResponseTime());
        }

        public double getStdDevResponseTime() {
            return getStdDev(i -> replicaStats[i].getMeanResponseTime());
        }

        // ========== Queue Length ==========

        public double getMeanQueueLength() {
            return getMean(i -> replicaStats[i].getMeanQueueLength(clockTimes[i]));
        }

        public double getStdDevQueueLength() {
            return getStdDev(i -> replicaStats[i].getMeanQueueLength(clockTimes[i]));
        }

        // ========== System Size ==========

        public double getMeanSystemSize() {
            return getMean(i -> replicaStats[i].getMeanSystemSize(clockTimes[i]));
        }

        public double getStdDevSystemSize() {
            return getStdDev(i -> replicaStats[i].getMeanSystemSize(clockTimes[i]));
        }

        // ========== Getters ==========

        public int getNumReplicas() {
            return numReplicas;
        }

        public SimulationConfig getConfig() {
            return config;
        }

        public SimulationStatistics getReplicaStats(int index) {
            return replicaStats[index];
        }

        /**
         * Report testuale risultati.
         */
        public String getReport() {
            StringBuilder sb = new StringBuilder();
            sb.append(String.format("=== Simulation Results (%d replicas) ===\n", numReplicas));
            sb.append(String.format("Config: %s\n\n", config));

            sb.append(String.format("Throughput:     %.4f ± %.4f\n",
                getMeanThroughput(), getStdDevThroughput()));
            sb.append(String.format("Utilization:    %.4f ± %.4f\n",
                getMeanUtilization(), getStdDevUtilization()));
            sb.append(String.format("E[T] (Response): %.4f ± %.4f\n",
                getMeanResponseTime(), getStdDevResponseTime()));
            sb.append(String.format("E[Nq] (Queue):  %.4f ± %.4f\n",
                getMeanQueueLength(), getStdDevQueueLength()));
            sb.append(String.format("E[N] (System):  %.4f ± %.4f\n",
                getMeanSystemSize(), getStdDevSystemSize()));

            return sb.toString();
        }

        @Override
        public String toString() {
            return getReport();
        }
    }
}

