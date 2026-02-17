package sim;

import libraries.Rngs;
import libraries.Rvgs;

/**
 * ServiceGenerator - Genera tempi di servizio e di interarrivo
 * usando diverse distribuzioni di probabilità.
 *
 * Usa ESCLUSIVAMENTE le librerie Leemis-Park (Rngs/Rvgs)
 * per la generazione di numeri pseudo-casuali.
 *
 * Distribuzioni supportate:
 * - Esponenziale: 1 parametro (media)
 * - Uniforme: 2 parametri (min, max)
 * - Erlang: 2 parametri (media totale, k stadi)
 * - Iperesponenziale: 3 parametri (p, media1, media2)
 */
public class ServiceGenerator {

    private final Rngs rngs;
    private final Rvgs rvgs;

    /**
     * @param rngs istanza Rngs già inizializzata con plantSeeds()
     * @param rvgs istanza Rvgs costruita con la stessa Rngs
     */
    public ServiceGenerator(Rngs rngs, Rvgs rvgs) {
        this.rngs = rngs;
        this.rvgs = rvgs;
    }

    // =========================================================================
    // Generazione Tempi di Servizio
    // =========================================================================

    /**
     * Genera un tempo di servizio distribuito esponenzialmente.
     * 
     * @param mean media della distribuzione (1/lambda)
     * @return tempo di servizio > 0
     */
    public double exponential(double mean) {
        return rvgs.exponential(mean);
    }

    /**
     * Genera un tempo di servizio distribuito uniformemente in [min, max].
     * 
     * @param min estremo inferiore
     * @param max estremo superiore
     * @return tempo di servizio in (min, max)
     */
    public double uniform(double min, double max) {
        return rvgs.uniform(min, max);
    }

    /**
     * Genera un tempo di servizio con distribuzione Erlang a k stadi.
     *
     * La Erlang ha coefficiente di variazione < 1.
     * Rvgs.erlang(n, b) somma n esponenziali con media b ciascuna,
     * quindi b = meanTotal / k.
     *
     * @param mean media totale della distribuzione
     * @param k    numero di stadi (k >= 1)
     * @return tempo di servizio > 0
     */
    public double erlang(double mean, long k) {
        double stageMean = mean / k; // media del singolo stadio
        return rvgs.erlang(k, stageMean);
    }

    /**
     * Genera un tempo di servizio con distribuzione Iperesponenziale.
     *
     * La Iperesponenziale ha coefficiente di variazione > 1.
     * Usa il metodo della composizione:
     * - Con probabilità p, campiona dalla prima esponenziale (media mean1)
     * - Con probabilità (1-p), campiona dalla seconda esponenziale (media mean2)
     *
     * @param p     probabilità di scegliere la prima esponenziale (0 < p < 1)
     * @param mean1 media della prima esponenziale
     * @param mean2 media della seconda esponenziale
     * @return tempo di servizio > 0
     */
    public double hyperExponential(double p, double mean1, double mean2) {
        // Genera U(0,1) usando Rngs (libreria Leemis), NON java.util.Random
        if (rngs.random() < p) {
            return rvgs.exponential(mean1);
        } else {
            return rvgs.exponential(mean2);
        }
    }

    // =========================================================================
    // Generazione Tempi di Interarrivo
    // =========================================================================

    /**
     * Genera un tempo di interarrivo distribuito esponenzialmente.
     * (Processo di Poisson con tasso lambda)
     *
     * @param mean media del tempo di interarrivo (1/lambda)
     * @return tempo di interarrivo > 0
     */
    public double getInterarrival(double mean) {
        return rvgs.exponential(mean);
    }

    /**
     * Genera un tempo di interarrivo con distribuzione Iperesponenziale.
     * Usato per la classe Batch nel sistema misto (punto 7 della consegna).
     *
     * @param p     probabilità di scegliere il primo ramo
     * @param mean1 media della prima esponenziale
     * @param mean2 media della seconda esponenziale
     * @return tempo di interarrivo > 0
     */
    public double getHyperExpInterarrival(double p, double mean1, double mean2) {
        return hyperExponential(p, mean1, mean2);
    }
}
