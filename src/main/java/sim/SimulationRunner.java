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

        /**
         * Risultato intervallo di confidenza.
         */
        public static class ConfidenceInterval {
            private final double mean;
            private final double lowerBound;
            private final double upperBound;
            private final double halfWidth;
            private final double relativeError;

            public ConfidenceInterval(double mean, double lowerBound, double upperBound) {
                this.mean = mean;
                this.lowerBound = lowerBound;
                this.upperBound = upperBound;
                this.halfWidth = (upperBound - lowerBound) / 2.0;
                this.relativeError = mean != 0 ? Math.abs(halfWidth / mean) : 0.0;
            }

            public double getMean() { return mean; }
            public double getLowerBound() { return lowerBound; }
            public double getUpperBound() { return upperBound; }
            public double getHalfWidth() { return halfWidth; }

            /**
             * Errore relativo: |semiampiezza / media|
             *
             * Indica precisione relativa della stima.
             * Tipicamente si richiede < 0.05 (5%) per buona precisione.
             */
            public double getRelativeError() { return relativeError; }

            @Override
            public String toString() {
                return String.format("%.4f ∈ [%.4f, %.4f] (RE=%.2f%%)",
                    mean, lowerBound, upperBound, relativeError * 100);
            }
        }

        /**
         * Calcola quantile distribuzione t-Student.
         *
         * Tabella per α=0.05 (IC 95%). Per df > 30 usa approssimazione normale.
         */
        private static double getTStudentQuantile(double alpha, int degreesOfFreedom) {
            if (alpha != 0.05) {
                throw new UnsupportedOperationException(
                    "Solo IC 95% (alpha=0.05) supportato");
            }

            if (degreesOfFreedom > 30) {
                return 1.96; // z per grandi campioni
            }

            // Tabella t-Student (α/2 = 0.025, two-tailed)
            double[] tTable = {
                0, 12.706, 4.303, 3.182, 2.776, 2.571,
                2.447, 2.365, 2.306, 2.262, 2.228,
                2.201, 2.179, 2.160, 2.145, 2.131,
                2.120, 2.110, 2.101, 2.093, 2.086,
                2.080, 2.074, 2.069, 2.064, 2.060,
                2.056, 2.052, 2.048, 2.045, 2.042, 2.040
            };

            return degreesOfFreedom < tTable.length ? tTable[degreesOfFreedom] : 1.96;
        }

        /**
         * Calcola IC per un indice generico.
         *
         * Formula: X̄ ± t_{α/2,R-1} · (S/√R)
         */
        private ConfidenceInterval getConfidenceInterval(
                java.util.function.ToDoubleFunction<Integer> extractor) {

            double mean = getMean(extractor);
            double stdDev = getStdDev(extractor);

            int df = numReplicas - 1;
            double tValue = getTStudentQuantile(0.05, df);
            double standardError = stdDev / Math.sqrt(numReplicas);
            double halfWidth = tValue * standardError;

            return new ConfidenceInterval(mean, mean - halfWidth, mean + halfWidth);
        }

        // ========== Intervalli di Confidenza (IC 95%) ==========

        public ConfidenceInterval getConfidenceIntervalThroughput() {
            return getConfidenceInterval(i -> replicaStats[i].getThroughput(clockTimes[i]));
        }

        public ConfidenceInterval getConfidenceIntervalUtilization() {
            return getConfidenceInterval(i -> replicaStats[i].getUtilization(clockTimes[i]));
        }

        public ConfidenceInterval getConfidenceIntervalResponseTime() {
            return getConfidenceInterval(i -> replicaStats[i].getMeanResponseTime());
        }

        public ConfidenceInterval getConfidenceIntervalQueueLength() {
            return getConfidenceInterval(i -> replicaStats[i].getMeanQueueLength(clockTimes[i]));
        }

        public ConfidenceInterval getConfidenceIntervalSystemSize() {
            return getConfidenceInterval(i -> replicaStats[i].getMeanSystemSize(clockTimes[i]));
        }
    }
}
