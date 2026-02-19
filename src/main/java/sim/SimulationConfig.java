package sim;

/**
 * Configurazione parametri per simulazione M/M/1.
 *
 * Immutabile per garantire riproducibilità tra repliche.
 */
public class SimulationConfig {

    /** Tasso medio di arrivo (λ, arrivi/unità tempo) */
    private final double arrivalRate;

    /** Tasso medio di servizio (μ, servizi/unità tempo) */
    private final double serviceRate;

    /** Numero di customer da simulare (condizione di stop) */
    private final long maxCustomers;

    /**
     * Crea configurazione per coda M/M/1.
     *
     * @param arrivalRate tasso arrivo λ (arrivi/unità tempo)
     * @param serviceRate tasso servizio μ (servizi/unità tempo)
     * @param maxCustomers numero customer da simulare
     * @throws IllegalArgumentException se parametri invalidi
     */
    public SimulationConfig(double arrivalRate, double serviceRate, long maxCustomers) {
        validateParameters(arrivalRate, serviceRate, maxCustomers);

        this.arrivalRate = arrivalRate;
        this.serviceRate = serviceRate;
        this.maxCustomers = maxCustomers;
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

    @Override
    public String toString() {
        return String.format("M/M/1(λ=%.2f, μ=%.2f, ρ=%.3f, N=%d)",
            arrivalRate, serviceRate, getUtilization(), maxCustomers);
    }
}

