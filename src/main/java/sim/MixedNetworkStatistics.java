package sim;

import sim.core.Customer.CustomerClass;

/**
 * Statistiche del sistema misto (classe chiusa + classe aperta).
 *
 * Raccoglie accumulatori separati per classe dove richiesto dalla consegna:
 * - Q1: utilizzo totale + dettaglio per classe (chiusa / aperta)
 * - Q2: utilizzo totale (solo classe chiusa transita per Q2)
 * - Throughput e E[T] per classe
 *
 * Indici calcolati:
 * <ul>
 * <li>X_closed, X_open: throughput per classe</li>
 * <li>ρ_Q1_total, ρ_Q1_closed, ρ_Q1_open: utilizzo Q1 totale e per classe</li>
 * <li>ρ_Q2: utilizzo Q2 (solo classe chiusa)</li>
 * <li>E[T1_closed], E[T1_open]: response time Q1 per classe</li>
 * <li>E[T2], E[T_sys]: response time Q2 e sistema centrale (solo chiusa)</li>
 * <li>E[Nq1], E[Nq2]: lunghezza media code</li>
 * </ul>
 */
public class MixedNetworkStatistics {

    // ---- Accumulatori Q1 (area totale = closed + open) ----
    private double areaBusyQ1Closed = 0.0; // area server Q1 occupato da classe chiusa
    private double areaBusyQ1Open = 0.0; // area server Q1 occupato da classe aperta
    private double areaQueueQ1 = 0.0; // integrale lunghezza coda Q1 (tutte le classi)
    private double areaSystemQ1 = 0.0; // integrale n1(t)

    // ---- Accumulatori Q2 ----
    private double areaBusyQ2 = 0.0;
    private double areaQueueQ2 = 0.0;
    private double areaSystemQ2 = 0.0;

    // ---- Completamenti e somma response time ----
    private long completionsQ1Closed = 0;
    private long completionsQ1Open = 0;
    private long completionsQ2 = 0;
    private double sumRespQ1Closed = 0.0;
    private double sumRespQ1Open = 0.0;
    private double sumRespQ2 = 0.0;

    // ---- Stato corrente ----
    private int queueLengthQ1 = 0;
    private int queueLengthQ2 = 0;
    private boolean serverBusyQ1 = false;
    private boolean serverBusyQ1Open = false; // true se il cliente in Q1 è OPEN
    private boolean serverBusyQ2 = false;

    private double lastEventTime = 0.0;

    /**
     * Aggiorna le aree temporali prima di ogni cambio di stato.
     */
    public void updateAreas(double currentTime) {
        double dt = currentTime - lastEventTime;

        // Q1: distingo la classe del cliente in servizio
        if (serverBusyQ1) {
            if (serverBusyQ1Open)
                areaBusyQ1Open += dt;
            else
                areaBusyQ1Closed += dt;
        }
        areaQueueQ1 += queueLengthQ1 * dt;
        areaSystemQ1 += (queueLengthQ1 + (serverBusyQ1 ? 1 : 0)) * dt;

        // Q2
        if (serverBusyQ2)
            areaBusyQ2 += dt;
        areaQueueQ2 += queueLengthQ2 * dt;
        areaSystemQ2 += (queueLengthQ2 + (serverBusyQ2 ? 1 : 0)) * dt;

        lastEventTime = currentTime;
    }

    // ---- Registrazione completamenti ----

    public void recordDepartureQ1(double arrivalAtQ1, double departureTime, CustomerClass cls) {
        double resp = departureTime - arrivalAtQ1;
        if (cls == CustomerClass.OPEN) {
            completionsQ1Open++;
            sumRespQ1Open += resp;
        } else {
            completionsQ1Closed++;
            sumRespQ1Closed += resp;
        }
    }

    public void recordDepartureQ2(double arrivalAtQ2, double departureTime) {
        completionsQ2++;
        sumRespQ2 += (departureTime - arrivalAtQ2);
    }

    // ---- Aggiornamento stato ----

    public void setQueueLengthQ1(int n) {
        this.queueLengthQ1 = n;
    }

    public void setQueueLengthQ2(int n) {
        this.queueLengthQ2 = n;
    }

    public void setServerBusyQ1(boolean b, boolean isOpen) {
        this.serverBusyQ1 = b;
        this.serverBusyQ1Open = b && isOpen;
    }

    public void setServerBusyQ2(boolean b) {
        this.serverBusyQ2 = b;
    }

    // =========================================================================
    // Indici Q1
    // =========================================================================

    /** Throughput totale Q1 (entrambe le classi). */
    public double getThroughputQ1(double T) {
        return (completionsQ1Closed + completionsQ1Open) / T;
    }

    /** Throughput Q1 classe chiusa. */
    public double getThroughputQ1Closed(double T) {
        return completionsQ1Closed / T;
    }

    /** Throughput Q1 classe aperta. */
    public double getThroughputQ1Open(double T) {
        return completionsQ1Open / T;
    }

    /** Utilizzo totale Q1: ρ1 = (areaClosed + areaOpen) / T. */
    public double getUtilizationQ1(double T) {
        return (areaBusyQ1Closed + areaBusyQ1Open) / T;
    }

    /** Utilizzo Q1 imputabile alla classe chiusa. */
    public double getUtilizationQ1Closed(double T) {
        return areaBusyQ1Closed / T;
    }

    /** Utilizzo Q1 imputabile alla classe aperta. */
    public double getUtilizationQ1Open(double T) {
        return areaBusyQ1Open / T;
    }

    /** E[Nq1] — lunghezza media coda Q1. */
    public double getMeanQueueLengthQ1(double T) {
        return areaQueueQ1 / T;
    }

    /** E[N1] — numero medio nel centro Q1. */
    public double getMeanSystemSizeQ1(double T) {
        return areaSystemQ1 / T;
    }

    /** E[T1] classe chiusa. */
    public double getMeanResponseTimeQ1Closed() {
        return completionsQ1Closed > 0 ? sumRespQ1Closed / completionsQ1Closed : 0.0;
    }

    /** E[T1] classe aperta. */
    public double getMeanResponseTimeQ1Open() {
        return completionsQ1Open > 0 ? sumRespQ1Open / completionsQ1Open : 0.0;
    }

    // =========================================================================
    // Indici Q2
    // =========================================================================

    /** Throughput Q2 (solo classe chiusa). */
    public double getThroughputQ2(double T) {
        return completionsQ2 / T;
    }

    /** Utilizzo Q2. */
    public double getUtilizationQ2(double T) {
        return areaBusyQ2 / T;
    }

    /** E[Nq2]. */
    public double getMeanQueueLengthQ2(double T) {
        return areaQueueQ2 / T;
    }

    /** E[N2]. */
    public double getMeanSystemSizeQ2(double T) {
        return areaSystemQ2 / T;
    }

    /** E[T2] (classe chiusa). */
    public double getMeanResponseTimeQ2() {
        return completionsQ2 > 0 ? sumRespQ2 / completionsQ2 : 0.0;
    }

    // =========================================================================
    // Indici sistema
    // =========================================================================

    /**
     * Throughput sistema visto da Q0 = completamenti che tornano a Q0.
     * Con routing parallelo (p1 -> Q1, 1-p1 -> Q2), tornano a Q0 sia da Q1 sia da
     * Q2.
     * Quindi X_sistema = X_Q1_closed + X_Q2.
     */
    public double getSystemThroughput(double T) {
        return (completionsQ1Closed + completionsQ2) / T;
    }

    /**
     * E[T_sys] sistema centrale classe chiusa.
     * Media pesata: (completamenti_Q1·E[T1] + completamenti_Q2·E[T2]) / totale.
     */
    public double getMeanResponseTimeSystemClosed() {
        long total = completionsQ1Closed + completionsQ2;
        if (total == 0)
            return 0.0;
        return (sumRespQ1Closed + sumRespQ2) / total;
    }

    // ---- Getters raw ----
    public long getCompletionsQ1Closed() {
        return completionsQ1Closed;
    }

    public long getCompletionsQ1Open() {
        return completionsQ1Open;
    }

    public long getCompletionsQ2() {
        return completionsQ2;
    }
}
