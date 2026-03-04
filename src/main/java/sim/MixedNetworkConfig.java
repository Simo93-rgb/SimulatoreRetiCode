package sim;

/**
 * Configurazione del sistema misto: classe chiusa (N clienti) + classe aperta.
 *
 * Estende la topologia del punto 6 aggiungendo un flusso esterno che
 * entra direttamente in Q1 e ne esce dopo il servizio (non passa per Q2 né Q0).
 *
 * <h2>Classe aperta</h2>
 * <ul>
 *   <li>Inter-arrivi: iperesponenziale con parametri (p, λ₁, λ₂)</li>
 *   <li>Servizio in Q1: esponenziale con media {@code 2 * meanServiceQ1Closed}</li>
 *   <li>Routing: Q1 esclusivamente, poi esce dal sistema</li>
 * </ul>
 *
 * Immutabile per garantire riproducibilità tra repliche.
 */
public class MixedNetworkConfig {

    // ---- Parametri ereditati dalla classe chiusa ----
    private final int    numCustomers;
    private final double meanThinkTime;
    private final double meanServiceQ1Closed;   // S1 per classe chiusa
    private final double meanServiceQ2;
    private final long   maxCompletions;        // completamenti totali Q1 (chiusi+aperti) come stop

    // ---- Parametri classe aperta ----
    private final double openHyperP;       // probabilità di scegliere λ₁
    private final double openMeanArr1;     // 1/λ₁ (media inter-arrivo componente 1)
    private final double openMeanArr2;     // 1/λ₂ (media inter-arrivo componente 2)

    /**
     * @param numCustomers       N clienti classe chiusa
     * @param meanThinkTime      media think time Q0 (Z)
     * @param meanServiceQ1Closed media servizio Q1 classe chiusa (S1)
     * @param meanServiceQ2      media servizio Q2 (S2)
     * @param maxCompletions     completamenti totali Q1 (chiusi+aperti) come criterio di stop
     * @param openHyperP         parametro p dell'iperesponenziale inter-arrivo (0 < p < 1)
     * @param openMeanArr1       media inter-arrivo componente 1 (1/λ₁)
     * @param openMeanArr2       media inter-arrivo componente 2 (1/λ₂)
     */
    public MixedNetworkConfig(int numCustomers,
                               double meanThinkTime,
                               double meanServiceQ1Closed,
                               double meanServiceQ2,
                               long maxCompletions,
                               double openHyperP,
                               double openMeanArr1,
                               double openMeanArr2) {
        if (numCustomers < 1)
            throw new IllegalArgumentException("numCustomers must be >= 1");
        if (meanThinkTime <= 0)
            throw new IllegalArgumentException("meanThinkTime must be > 0");
        if (meanServiceQ1Closed <= 0)
            throw new IllegalArgumentException("meanServiceQ1Closed must be > 0");
        if (meanServiceQ2 <= 0)
            throw new IllegalArgumentException("meanServiceQ2 must be > 0");
        if (maxCompletions < 1)
            throw new IllegalArgumentException("maxCompletions must be >= 1");
        if (openHyperP <= 0 || openHyperP >= 1)
            throw new IllegalArgumentException("openHyperP must be in (0,1)");
        if (openMeanArr1 <= 0)
            throw new IllegalArgumentException("openMeanArr1 must be > 0");
        if (openMeanArr2 <= 0)
            throw new IllegalArgumentException("openMeanArr2 must be > 0");

        this.numCustomers        = numCustomers;
        this.meanThinkTime       = meanThinkTime;
        this.meanServiceQ1Closed = meanServiceQ1Closed;
        this.meanServiceQ2       = meanServiceQ2;
        this.maxCompletions      = maxCompletions;
        this.openHyperP          = openHyperP;
        this.openMeanArr1        = openMeanArr1;
        this.openMeanArr2        = openMeanArr2;
    }

    // ---- Getters classe chiusa ----
    public int    getNumCustomers()        { return numCustomers; }
    public double getMeanThinkTime()       { return meanThinkTime; }
    public double getMeanServiceQ1Closed() { return meanServiceQ1Closed; }
    public double getMeanServiceQ2()       { return meanServiceQ2; }
    public long   getMaxCompletions()      { return maxCompletions; }

    // ---- Getters classe aperta ----
    public double getOpenHyperP()      { return openHyperP; }
    public double getOpenMeanArr1()    { return openMeanArr1; }
    public double getOpenMeanArr2()    { return openMeanArr2; }

    /**
     * Media servizio Q1 per la classe aperta = 2 * S1_chiusa (da consegna).
     */
    public double getMeanServiceQ1Open() {
        return 2.0 * meanServiceQ1Closed;
    }

    /**
     * Tasso medio di inter-arrivo della classe aperta.
     * E[interarrival] = p/λ₁ + (1-p)/λ₂  (media dell'iperesponenziale)
     * → λ_open = 1 / E[interarrival]
     */
    public double getOpenMeanInterarrival() {
        return openHyperP * openMeanArr1 + (1.0 - openHyperP) * openMeanArr2;
    }

    @Override
    public String toString() {
        return String.format(
            "MixedNetworkConfig{N=%d, Z=%.2f, S1=%.2f, S2=%.2f, " +
            "open: p=%.2f, E[A1]=%.2f, E[A2]=%.2f, E[A]=%.2f}",
            numCustomers, meanThinkTime, meanServiceQ1Closed, meanServiceQ2,
            openHyperP, openMeanArr1, openMeanArr2, getOpenMeanInterarrival());
    }
}



