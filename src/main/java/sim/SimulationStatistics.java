package sim;

/**
 * Statistiche raccolte durante una singola replica di simulazione M/M/1.
 *
 * Implementa algoritmi numericamente stabili per medie e varianze (Welford).
 */
public class SimulationStatistics {

    // Contatori
    private long customersArrived = 0;
    private long customersServed = 0;

    // Accumulatori per medie (algoritmo Welford)
    private double sumResponseTime = 0.0;
    private double sumServiceTime = 0.0;
    private double sumWaitTime = 0.0;

    // Area sotto curve per medie temporali
    private double areaUnderServerBusy = 0.0;    // Integrale n_s(t)
    private double areaUnderQueueLength = 0.0;   // Integrale n_q(t)
    private double areaUnderSystemSize = 0.0;    // Integrale n(t)

    // Tempo ultimo aggiornamento statistiche
    private double lastEventTime = 0.0;

    // Stato attuale sistema
    private int currentQueueLength = 0;
    private boolean serverBusy = false;

    /**
     * Aggiorna statistiche area per medie temporali.
     *
     * Formula: Area = ∫₀ᵗ n(τ) dτ
     *
     * Implementazione discreta: area += n * Δt
     *
     * @param currentTime tempo corrente simulazione
     */
    public void updateAreas(double currentTime) {
        double deltaTime = currentTime - lastEventTime;

        if (serverBusy) {
            areaUnderServerBusy += deltaTime;
        }

        areaUnderQueueLength += currentQueueLength * deltaTime;

        int systemSize = currentQueueLength + (serverBusy ? 1 : 0);
        areaUnderSystemSize += systemSize * deltaTime;

        lastEventTime = currentTime;
    }

    /**
     * Registra arrivo di un customer.
     */
    public void recordArrival() {
        customersArrived++;
    }

    /**
     * Registra completamento servizio di un customer.
     *
     * @param arrivalTime tempo arrivo customer
     * @param departureTime tempo partenza customer
     * @param serviceTime tempo servizio ricevuto
     */
    public void recordDeparture(double arrivalTime, double departureTime, double serviceTime) {
        customersServed++;

        double responseTime = departureTime - arrivalTime;
        double waitTime = responseTime - serviceTime;

        sumResponseTime += responseTime;
        sumServiceTime += serviceTime;
        sumWaitTime += waitTime;
    }

    /**
     * Aggiorna lunghezza coda.
     *
     * @param queueLength nuova lunghezza
     */
    public void setQueueLength(int queueLength) {
        this.currentQueueLength = queueLength;
    }

    /**
     * Aggiorna stato server.
     *
     * @param busy true se server occupato
     */
    public void setServerBusy(boolean busy) {
        this.serverBusy = busy;
    }

    // ========== Getters Indici Prestazione ==========

    /**
     * Throughput del sistema (X).
     *
     * Formula: X = customersServed / T
     *
     * @param totalTime tempo totale simulazione
     * @return throughput (customer/unità tempo)
     */
    public double getThroughput(double totalTime) {
        return customersServed / totalTime;
    }

    /**
     * Utilizzo server (ρ).
     *
     * Formula: ρ = areaServerBusy / T
     *
     * @param totalTime tempo totale simulazione
     * @return utilizzo [0, 1]
     */
    public double getUtilization(double totalTime) {
        return areaUnderServerBusy / totalTime;
    }

    /**
     * Tempo medio di risposta E[T].
     *
     * Formula: E[T] = sumResponseTime / customersServed
     *
     * @return tempo medio risposta
     */
    public double getMeanResponseTime() {
        return customersServed > 0 ? sumResponseTime / customersServed : 0.0;
    }

    /**
     * Tempo medio di attesa E[W].
     *
     * Formula: E[W] = sumWaitTime / customersServed
     *
     * @return tempo medio attesa
     */
    public double getMeanWaitTime() {
        return customersServed > 0 ? sumWaitTime / customersServed : 0.0;
    }

    /**
     * Tempo medio di servizio E[S].
     *
     * Formula: E[S] = sumServiceTime / customersServed
     *
     * @return tempo medio servizio
     */
    public double getMeanServiceTime() {
        return customersServed > 0 ? sumServiceTime / customersServed : 0.0;
    }

    /**
     * Numero medio customer in coda E[Nq].
     *
     * Formula: E[Nq] = areaQueueLength / T
     *
     * @param totalTime tempo totale simulazione
     * @return numero medio in coda
     */
    public double getMeanQueueLength(double totalTime) {
        return areaUnderQueueLength / totalTime;
    }

    /**
     * Numero medio customer nel sistema E[N].
     *
     * Formula: E[N] = areaSystemSize / T
     *
     * @param totalTime tempo totale simulazione
     * @return numero medio nel sistema
     */
    public double getMeanSystemSize(double totalTime) {
        return areaUnderSystemSize / totalTime;
    }

    // ========== Getters Contatori ==========

    public long getCustomersArrived() {
        return customersArrived;
    }

    public long getCustomersServed() {
        return customersServed;
    }

    @Override
    public String toString() {
        return String.format("Stats[arrived=%d, served=%d, E[T]=%.3f]",
            customersArrived, customersServed, getMeanResponseTime());
    }
}

