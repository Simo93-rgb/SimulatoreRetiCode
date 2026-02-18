package sim;

import libraries.Rngs;

/**
 * Gestisce la generazione di semi distanziati per repliche indipendenti
 * e la configurazione di stream RNG separati per diverse sorgenti stocastiche.
 *
 * Basato su Leemis-Park Cap. 2 "Random Number Generation".
 */
public class SeedManager {

    /**
     * Modulus del generatore LCG (Lehmer generator).
     *
     * Valore letto da Rngs per evitare hardcoding duplicato.
     *
     * Riferimento: Park & Miller (1988), "Random Number Generators: Good Ones Are Hard To Find"
     */
    public static final long MODULUS = new Rngs().MODULUS;

    /**
     * Spacing minimo tra semi per garantire indipendenza statistica.
     *
     * Valore: 10^6 (Leemis p.512: "seeds should be spaced at least 100,000 apart").
     *
     * Razionale: Il generatore LCG ha periodo MODULUS - 1 ≈ 2.1 × 10^9.
     * Con spacing 10^6, possiamo generare fino a ~2100 repliche indipendenti.
     */
    public static final long SEED_SPACING = 1_000_000L;

    /**
     * Seed di default per inizializzazione deterministica.
     *
     * Valore scelto: primo numero primo > 10^8 (evita pattern a bassa entropia).
     */
    public static final long DEFAULT_BASE_SEED = 100_000_007L;

    /**
     * Genera array di semi distanziati per repliche indipendenti.
     *
     * Formula: seed[i] = baseSeed + i * SEED_SPACING
     *
     * Garantisce che ogni replica usi una sequenza pseudo-casuale non sovrapposta.
     * Critico per validità statistica del metodo delle repliche indipendenti.
     *
     * @param baseSeed seme iniziale (deve essere in [1, 2^31-2])
     * @param numReplicas numero di repliche da eseguire
     * @return array di semi distanziati
     * @throws IllegalArgumentException se baseSeed invalido o overflow
     */
    public static long[] generateDistancedSeeds(long baseSeed, int numReplicas) {
        validateBaseSeed(baseSeed);

        if (numReplicas <= 0) {
            throw new IllegalArgumentException("Number of replicas must be positive");
        }

        long[] seeds = new long[numReplicas];

        for (int i = 0; i < numReplicas; i++) {
            long seed = baseSeed + (long) i * SEED_SPACING;

            // Check overflow (LCG modulus: 2^31 - 1)
            if (seed > MODULUS - 1 || seed < 1) {
                throw new IllegalArgumentException(
                    String.format("Seed overflow at replica %d. Base=%d too large for %d replicas",
                        i, baseSeed, numReplicas));
            }

            seeds[i] = seed;
        }

        return seeds;
    }

    /**
     * Genera semi con seed base di default.
     *
     * @param numReplicas numero di repliche
     * @return array di semi distanziati
     */
    public static long[] generateDistancedSeeds(int numReplicas) {
        return generateDistancedSeeds(DEFAULT_BASE_SEED, numReplicas);
    }

    /**
     * Valida seed per generatore LCG.
     *
     * Vincoli Rngs (Leemis):
     * - 1 ≤ seed ≤ MODULUS - 1
     * - seed = 0 causa sequenza degenerata
     * - seed = MODULUS causa wrap immediato
     *
     * @param seed seed da validare
     * @throws IllegalArgumentException se seed fuori range
     */
    private static void validateBaseSeed(long seed) {
        if (seed < 1 || seed >= MODULUS) {
            throw new IllegalArgumentException(
                String.format("Seed must be in [1, %d]. Got: %d",
                    MODULUS - 1, seed));
        }
    }

    /**
     * Configura stream indipendenti per diverse sorgenti stocastiche.
     *
     * Pattern standard per simulazione DES:
     * - Stream 0: Tempi di interarrivo
     * - Stream 1: Tempi di servizio
     * - Stream 2+: Altri processi stocastici (think time, routing, ecc.)
     *
     * Esempio utilizzo:
     * <pre>
     * Rngs rngs = new Rngs();
     * rngs.plantSeeds(seed);
     *
     * // Genera arrivo
     * rngs.selectStream(StreamType.ARRIVALS.ordinal());
     * double interarrival = rvgs.exponential(lambda);
     *
     * // Genera servizio (indipendente!)
     * rngs.selectStream(StreamType.SERVICE.ordinal());
     * double service = rvgs.exponential(mu);
     * </pre>
     */
    public enum StreamType {
        /**
         * Stream 0: Processo di arrivo (tempi di interarrivo).
         */
        ARRIVALS,

        /**
         * Stream 1: Processo di servizio (tempi di servizio).
         */
        SERVICE,

        /**
         * Stream 2: Think time (per classe Interactive).
         */
        THINK_TIME,

        /**
         * Stream 3: Routing decisions (per reti di code).
         */
        ROUTING
    }

    /**
     * Calcola correlazione empirica tra due stream RNG.
     *
     * Usato per verificare indipendenza statistica.
     *
     * Formula Pearson: ρ = Cov(X,Y) / (σ_X σ_Y)
     *
     * @param stream1 array campioni stream 1
     * @param stream2 array campioni stream 2
     * @return coefficiente di correlazione [-1, 1]
     * @throws IllegalArgumentException se array lunghezze diverse
     */
    public static double calculateCorrelation(double[] stream1, double[] stream2) {
        if (stream1.length != stream2.length) {
            throw new IllegalArgumentException("Arrays must have same length");
        }

        int n = stream1.length;
        if (n < 2) {
            throw new IllegalArgumentException("Need at least 2 samples");
        }

        // Calcola medie
        double mean1 = 0, mean2 = 0;
        for (int i = 0; i < n; i++) {
            mean1 += stream1[i];
            mean2 += stream2[i];
        }
        mean1 /= n;
        mean2 /= n;

        // Calcola covarianza e deviazioni standard
        double cov = 0, var1 = 0, var2 = 0;
        for (int i = 0; i < n; i++) {
            double diff1 = stream1[i] - mean1;
            double diff2 = stream2[i] - mean2;
            cov += diff1 * diff2;
            var1 += diff1 * diff1;
            var2 += diff2 * diff2;
        }

        cov /= (n - 1);
        var1 /= (n - 1);
        var2 /= (n - 1);

        double std1 = Math.sqrt(var1);
        double std2 = Math.sqrt(var2);

        // Evita divisione per zero
        if (std1 < 1e-10 || std2 < 1e-10) {
            return 0.0;
        }

        return cov / (std1 * std2);
    }
}

