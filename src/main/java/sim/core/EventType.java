package sim.core;

/**
 * Tipi di eventi per il simulatore event-driven.
 *
 * Enum invece di int per type-safety e leggibilità.
 */
public enum EventType {
    /**
     * Arrivo di un nuovo customer al sistema.
     */
    ARRIVAL,

    /**
     * Completamento del servizio (customer lascia il server).
     */
    DEPARTURE,

    /**
     * Fine del think time per customer Interactive (Step 3+).
     * Il customer torna dalla rete di terminali alla CPU.
     */
    END_THINK_TIME,

    /**
     * Timeout o altri eventi futuri (estensioni).
     */
    TIMEOUT
}

