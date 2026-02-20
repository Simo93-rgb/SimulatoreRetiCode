package sim;

/**
 * Configurazione parametri per simulazione M/M/1.
 *
 * Immutabile per garantire riproducibilità tra repliche.
 */
public class SimulationConfig {

    /**
     * Tipo di distribuzione servizio.
     */
    public enum ServiceDistribution {
        EXPONENTIAL,      // M/M/1 (cv=1, default)
        DETERMINISTIC,    // M/D/1 (cv=0, approx con Uniform)
        ERLANG,           // M/Ek/1 (cv=1/sqrt(k))
        HYPEREXPONENTIAL  // M/H₂/1 (cv>1)
    }

    /** Tasso medio di arrivo (λ, arrivi/unità tempo) */
    private final double arrivalRate;

    /** Tasso medio di servizio (μ, servizi/unità tempo) */
    private final double serviceRate;

    /** Numero di customer da simulare (condizione di stop) */
    private final long maxCustomers;

    /** Tipo distribuzione servizio (default: EXPONENTIAL) */
    private final ServiceDistribution serviceDistribution;

    /** Parametro k per Erlang (solo se serviceDistribution == ERLANG) */
    private final int erlangK;

    /** Parametro p per Hyperexponential (solo se serviceDistribution == HYPEREXPONENTIAL) */
    private final double hyperP;

    /** Parametro mean1 per Hyperexponential */
    private final double hyperMean1;

    /** Parametro mean2 per Hyperexponential */
    private final double hyperMean2;

    /**
     * Crea configurazione per coda M/M/1 (default: servizio Exponential).
     *
     * @param arrivalRate tasso arrivo λ (arrivi/unità tempo)
     * @param serviceRate tasso servizio μ (servizi/unità tempo)
     * @param maxCustomers numero customer da simulare
     * @throws IllegalArgumentException se parametri invalidi
     */
    public SimulationConfig(double arrivalRate, double serviceRate, long maxCustomers) {
        this(arrivalRate, serviceRate, maxCustomers, ServiceDistribution.EXPONENTIAL, 0, 0, 0, 0);
    }

    /**
     * Crea configurazione con distribuzione servizio custom.
     *
     * @param arrivalRate tasso arrivo λ
     * @param serviceRate tasso servizio μ (o 1/mean per calcolare mean)
     * @param maxCustomers numero customer
     * @param serviceDistribution tipo distribuzione servizio
     * @param erlangK parametro k (solo per ERLANG)
     * @param hyperP parametro p (solo per HYPEREXPONENTIAL)
     * @param hyperMean1 media componente 1 (solo per HYPEREXPONENTIAL)
     * @param hyperMean2 media componente 2 (solo per HYPEREXPONENTIAL)
     */
    public SimulationConfig(double arrivalRate, double serviceRate, long maxCustomers,
                             ServiceDistribution serviceDistribution,
                             int erlangK, double hyperP, double hyperMean1, double hyperMean2) {
        validateParameters(arrivalRate, serviceRate, maxCustomers);

        this.arrivalRate = arrivalRate;
        this.serviceRate = serviceRate;
        this.maxCustomers = maxCustomers;
        this.serviceDistribution = serviceDistribution;
        this.erlangK = erlangK;
        this.hyperP = hyperP;
        this.hyperMean1 = hyperMean1;
        this.hyperMean2 = hyperMean2;
    }

    /**
     * Validazione parametri M/M/1.
     *
     * Vincoli:
     * - λ > 0 (deve esserci traffico)
     * - μ > 0 (server deve processare)
     * - ρ = λ/μ < 1 (sistema deve essere stabile)
     * - maxCustomers > 0
     */
    private void validateParameters(double lambda, double mu, long n) {
        if (lambda <= 0) {
            throw new IllegalArgumentException("Arrival rate must be positive: " + lambda);
        }
        if (mu <= 0) {
            throw new IllegalArgumentException("Service rate must be positive: " + mu);
        }

        double rho = lambda / mu;
        if (rho >= 1.0) {
            throw new IllegalArgumentException(
                String.format("System unstable: ρ = λ/μ = %.3f >= 1 (λ=%f, μ=%f)",
                    rho, lambda, mu));
        }

        if (n <= 0) {
            throw new IllegalArgumentException("Max customers must be positive: " + n);
        }
    }

    /** @return tasso arrivo λ */
    public double getArrivalRate() {
        return arrivalRate;
    }

    /** @return tasso servizio μ */
    public double getServiceRate() {
        return serviceRate;
    }

    /** @return media tempo interarrivo E[A] = 1/λ */
    public double getMeanInterarrival() {
        return 1.0 / arrivalRate;
    }

    /** @return media tempo servizio E[S] = 1/μ */
    public double getMeanService() {
        return 1.0 / serviceRate;
    }

    /** @return utilizzo server ρ = λ/μ */
    public double getUtilization() {
        return arrivalRate / serviceRate;
    }

    /** @return numero max customer */
    public long getMaxCustomers() {
        return maxCustomers;
    }

    public ServiceDistribution getServiceDistribution() {
        return serviceDistribution;
    }

    public int getErlangK() {
        return erlangK;
    }

    public double getHyperP() {
        return hyperP;
    }

    public double getHyperMean1() {
        return hyperMean1;
    }

    public double getHyperMean2() {
        return hyperMean2;
    }

    @Override
    public String toString() {
        return String.format("M/M/1(λ=%.2f, μ=%.2f, ρ=%.3f, N=%d)",
            arrivalRate, serviceRate, getUtilization(), maxCustomers);
    }
}
