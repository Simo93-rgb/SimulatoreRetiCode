package sim;

/**
 * Statistiche raccolte durante una singola replica del sistema chiuso.
 *
 * Tre centri:
 * - Q0: Delay station (think time) — nessuna coda, server infiniti
 * - Q1: Singolo server FCFS
 * - Q2: Singolo server FCFS
 *
 * Indici raccolti per Q1 e Q2:
 * - Throughput X_i = completamenti / T
 * - Utilizzo ρ_i = area_busy_i / T
 * - E[Nq_i] = area_queue_i / T
 * - E[N_i] = area_system_i / T
 * - E[T_i] = sum_response_i / completamenti (via Little: E[N_i] / X_i)
 *
 * Per il sistema centrale (Q1+Q2):
 * - E[T_sys] = E[T1] + E[T2] (per Little, sistema in serie)
 *
 * Throughput del sistema visto da Q0 = X_system = completamenti Q1 / T
 * (in steady state tutti i centri hanno lo stesso throughput).
 */
public class ClosedNetworkStatistics {

    // ---- Accumulatori Q1 ----
    private double areaServerBusyQ1 = 0.0;
    private double areaQueueLengthQ1 = 0.0;
    private double areaSystemSizeQ1 = 0.0;
    private double sumResponseTimeQ1 = 0.0;
    private long completionsQ1 = 0;

    // ---- Accumulatori Q2 ----
    private double areaServerBusyQ2 = 0.0;
    private double areaQueueLengthQ2 = 0.0;
    private double areaSystemSizeQ2 = 0.0;
    private double sumResponseTimeQ2 = 0.0;
    private long completionsQ2 = 0;

    // ---- Stato corrente ----
    private int queueLengthQ1 = 0;
    private int queueLengthQ2 = 0;
    private boolean serverBusyQ1 = false;
    private boolean serverBusyQ2 = false;

    // ---- Tempo ultimo aggiornamento ----
    private double lastEventTime = 0.0;

    /**
     * Aggiorna le aree temporali prima di ogni cambio di stato.
     *
     * Formula discreta: area += n(t) * Δt
     *
     * @param currentTime tempo corrente della simulazione
     */
    public void updateAreas(double currentTime) {
        double dt = currentTime - lastEventTime;

        // Q1
        if (serverBusyQ1)
            areaServerBusyQ1 += dt;
        areaQueueLengthQ1 += queueLengthQ1 * dt;
        areaSystemSizeQ1 += (queueLengthQ1 + (serverBusyQ1 ? 1 : 0)) * dt;

        // Q2
        if (serverBusyQ2)
            areaServerBusyQ2 += dt;
        areaQueueLengthQ2 += queueLengthQ2 * dt;
        areaSystemSizeQ2 += (queueLengthQ2 + (serverBusyQ2 ? 1 : 0)) * dt;

        lastEventTime = currentTime;
    }

    // ---- Registrazione completamenti ----

    /** Registra completamento servizio in Q1. */
    public void recordDepartureQ1(double arrivalTimeAtQ1, double departureTime) {
        completionsQ1++;
        sumResponseTimeQ1 += (departureTime - arrivalTimeAtQ1);
    }

    /** Registra completamento servizio in Q2. */
    public void recordDepartureQ2(double arrivalTimeAtQ2, double departureTime) {
        completionsQ2++;
        sumResponseTimeQ2 += (departureTime - arrivalTimeAtQ2);
    }

    // ---- Aggiornamento stato ----

    public void setQueueLengthQ1(int n) {
        this.queueLengthQ1 = n;
    }

    public void setQueueLengthQ2(int n) {
        this.queueLengthQ2 = n;
    }

    public void setServerBusyQ1(boolean b) {
        this.serverBusyQ1 = b;
    }

    public void setServerBusyQ2(boolean b) {
        this.serverBusyQ2 = b;
    }

    // ========== Indici Q1 ==========

    /** Throughput di Q1: X1 = completamenti / T. */
    public double getThroughputQ1(double totalTime) {
        return completionsQ1 / totalTime;
    }

    /** Utilizzo di Q1: ρ1 = area_busy / T. */
    public double getUtilizationQ1(double totalTime) {
        return areaServerBusyQ1 / totalTime;
    }

    /** E[Nq1] — numero medio in coda a Q1. */
    public double getMeanQueueLengthQ1(double totalTime) {
        return areaQueueLengthQ1 / totalTime;
    }

    /** E[N1] — numero medio nel centro Q1 (coda + servizio). */
    public double getMeanSystemSizeQ1(double totalTime) {
        return areaSystemSizeQ1 / totalTime;
    }

    /** E[T1] — tempo medio di risposta Q1 (per customer completato). */
    public double getMeanResponseTimeQ1() {
        return completionsQ1 > 0 ? sumResponseTimeQ1 / completionsQ1 : 0.0;
    }

    // ========== Indici Q2 ==========

    /** Throughput di Q2: X2 = completamenti / T. */
    public double getThroughputQ2(double totalTime) {
        return completionsQ2 / totalTime;
    }

    /** Utilizzo di Q2: ρ2 = area_busy / T. */
    public double getUtilizationQ2(double totalTime) {
        return areaServerBusyQ2 / totalTime;
    }

    /** E[Nq2] — numero medio in coda a Q2. */
    public double getMeanQueueLengthQ2(double totalTime) {
        return areaQueueLengthQ2 / totalTime;
    }

    /** E[N2] — numero medio nel centro Q2 (coda + servizio). */
    public double getMeanSystemSizeQ2(double totalTime) {
        return areaSystemSizeQ2 / totalTime;
    }

    /** E[T2] — tempo medio di risposta Q2 (per customer completato). */
    public double getMeanResponseTimeQ2() {
        return completionsQ2 > 0 ? sumResponseTimeQ2 / completionsQ2 : 0.0;
    }

    // ========== Indici Sistema Centrale (Q1+Q2) ==========

    /**
     * Throughput del sistema (visto da Q0): X = (completamenti Q1 + completamenti
     * Q2) / T.
     */
    public double getSystemThroughput(double totalTime) {
        return getThroughputQ1(totalTime) + getThroughputQ2(totalTime);
    }

    /**
     * Tempo medio di risposta del sistema centrale (Q1 || Q2).
     * Poiché è parallelo, E[T_sys] è la media pesata sui completamenti.
     */
    public double getMeanResponseTimeSystem() {
        long totalCompletions = completionsQ1 + completionsQ2;
        if (totalCompletions == 0)
            return 0.0;
        return (sumResponseTimeQ1 + sumResponseTimeQ2) / totalCompletions;
    }

    // ========== Getters raw ==========

    public long getCompletionsQ1() {
        return completionsQ1;
    }

    public long getCompletionsQ2() {
        return completionsQ2;
    }

    @Override
    public String toString() {
        return String.format(
                "ClosedNetStats[completQ1=%d, E[T1]=%.3f, E[T2]=%.3f]",
                completionsQ1, getMeanResponseTimeQ1(), getMeanResponseTimeQ2());
    }
}
