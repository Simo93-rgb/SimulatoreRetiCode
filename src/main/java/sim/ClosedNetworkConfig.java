package sim;

/**
 * Configurazione del sistema chiuso con tre centri (Q0, Q1, Q2).
 *
 * Topologia (da Figura 1 della consegna):
 * <pre>
 *              p1
 *   Q0  ─────────────►  Q1  ───┐
 *       └── (1-p1) ──►  Q2  ───┴──► Q0
 * </pre>
 *
 * - Q0: Terminali (delay station, servizio = think time, server infiniti)
 * - Q1: Primo centro di servizio (singolo server FCFS)
 * - Q2: Secondo centro di servizio (singolo server FCFS)
 * - p1: probabilità di routing da Q0 verso Q1 (1-p1 verso Q2)
 *
 * N clienti circolano indefinitamente nel sistema.
 * Tutti i tempi sono distribuiti secondo esponenziale (consegna punto 6).
 *
 * Immutabile per garantire riproducibilità tra repliche.
 */
public class ClosedNetworkConfig {

    /** Numero di clienti nel sistema (N, costante). */
    private final int numCustomers;

    /** Media think time in Q0 (Z, delay station). */
    private final double meanThinkTime;

    /** Media tempo di servizio in Q1. */
    private final double meanServiceQ1;

    /** Media tempo di servizio in Q2. */
    private final double meanServiceQ2;

    /** Probabilità di routing da Q0 verso Q1 (il complemento 1-p1 va verso Q2). */
    private final double routingProbabilityQ1;

    /** Numero di completamenti su Q1+Q2 da simulare (condizione di stop). */
    private final long maxCompletions;

    /**
     * Crea configurazione del sistema chiuso.
     *
     * @param numCustomers          numero di clienti nel sistema (N >= 1)
     * @param meanThinkTime         media del think time in Q0 (Z > 0)
     * @param meanServiceQ1         media del tempo di servizio in Q1 (> 0)
     * @param meanServiceQ2         media del tempo di servizio in Q2 (> 0)
     * @param routingProbabilityQ1  probabilità routing Q0→Q1 (0 < p1 < 1)
     * @param maxCompletions        numero completamenti totali (Q1+Q2) come criterio di stop
     */
    public ClosedNetworkConfig(int numCustomers,
                                double meanThinkTime,
                                double meanServiceQ1,
                                double meanServiceQ2,
                                double routingProbabilityQ1,
                                long maxCompletions) {
        if (numCustomers < 1) {
            throw new IllegalArgumentException("numCustomers must be >= 1, got: " + numCustomers);
        }
        if (meanThinkTime <= 0) {
            throw new IllegalArgumentException("meanThinkTime must be > 0, got: " + meanThinkTime);
        }
        if (meanServiceQ1 <= 0) {
            throw new IllegalArgumentException("meanServiceQ1 must be > 0, got: " + meanServiceQ1);
        }
        if (meanServiceQ2 <= 0) {
            throw new IllegalArgumentException("meanServiceQ2 must be > 0, got: " + meanServiceQ2);
        }
        if (routingProbabilityQ1 <= 0 || routingProbabilityQ1 >= 1) {
            throw new IllegalArgumentException("routingProbabilityQ1 must be in (0,1), got: " + routingProbabilityQ1);
        }
        if (maxCompletions < 1) {
            throw new IllegalArgumentException("maxCompletions must be >= 1, got: " + maxCompletions);
        }

        this.numCustomers          = numCustomers;
        this.meanThinkTime         = meanThinkTime;
        this.meanServiceQ1         = meanServiceQ1;
        this.meanServiceQ2         = meanServiceQ2;
        this.routingProbabilityQ1  = routingProbabilityQ1;
        this.maxCompletions        = maxCompletions;
    }

    public int getNumCustomers()             { return numCustomers; }
    public double getMeanThinkTime()          { return meanThinkTime; }
    public double getMeanServiceQ1()          { return meanServiceQ1; }
    public double getMeanServiceQ2()          { return meanServiceQ2; }
    public double getRoutingProbabilityQ1()   { return routingProbabilityQ1; }
    public long getMaxCompletions()           { return maxCompletions; }

    /**
     * Throughput asintotico superiore (bound di saturazione).
     *
     * Con routing probabilistico le domande ai centri sono:
     *   D1 = p1 * S1,  D2 = (1-p1) * S2
     * X*(N) = min(1/D_max, N/(Z + D1 + D2))
     *
     * @return throughput massimo teorico
     */
    public double getSaturationThroughputBound() {
        double d1   = routingProbabilityQ1 * meanServiceQ1;
        double d2   = (1.0 - routingProbabilityQ1) * meanServiceQ2;
        double dMax = Math.max(d1, d2);
        double dSum = meanThinkTime + d1 + d2;
        return Math.min(1.0 / dMax, (double) numCustomers / dSum);
    }

    @Override
    public String toString() {
        return String.format(
            "ClosedNetworkConfig{N=%d, Z=%.2f, S1=%.2f, S2=%.2f, p1=%.2f, maxComp=%d}",
            numCustomers, meanThinkTime, meanServiceQ1, meanServiceQ2,
            routingProbabilityQ1, maxCompletions);
    }
}

