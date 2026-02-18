package sim;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

import libraries.Rngs;
import libraries.Rvgs;

class SeedManagerTest {

    @Test
    @DisplayName("Generazione semi distanziati con base seed valido")
    void testGenerateDistancedSeeds() {
        long baseSeed = 123456789L;
        int numReplicas = 10;

        long[] seeds = SeedManager.generateDistancedSeeds(baseSeed, numReplicas);

        assertEquals(numReplicas, seeds.length);

        // Verifica spacing corretto
        for (int i = 0; i < numReplicas; i++) {
            long expected = baseSeed + (long) i * SeedManager.SEED_SPACING;
            assertEquals(expected, seeds[i],
                String.format("Seed %d non corretto", i));
        }
    }

    @Test
    @DisplayName("Generazione semi con default base seed")
    void testGenerateDistancedSeedsDefault() {
        int numReplicas = 5;
        long[] seeds = SeedManager.generateDistancedSeeds(numReplicas);

        assertEquals(numReplicas, seeds.length);
        assertEquals(SeedManager.DEFAULT_BASE_SEED, seeds[0]);
    }

    @Test
    @DisplayName("Validazione seed: rejects zero")
    void testRejectZeroSeed() {
        assertThrows(IllegalArgumentException.class, () -> {
            SeedManager.generateDistancedSeeds(0, 10);
        }, "Seed 0 deve essere rifiutato");
    }

    @Test
    @DisplayName("Validazione seed: rejects negative")
    void testRejectNegativeSeed() {
        assertThrows(IllegalArgumentException.class, () -> {
            SeedManager.generateDistancedSeeds(-100, 10);
        }, "Seed negativo deve essere rifiutato");
    }

    @Test
    @DisplayName("Validazione seed: rejects modulus")
    void testRejectModulusSeed() {
        assertThrows(IllegalArgumentException.class, () -> {
            SeedManager.generateDistancedSeeds(SeedManager.MODULUS, 10);
        }, "Seed >= MODULUS deve essere rifiutato");
    }

    @Test
    @DisplayName("Overflow detection con base seed troppo alto")
    void testOverflowDetection() {
        long baseSeed = SeedManager.MODULUS - 100;
        int numReplicas = 1000; // Causerà overflow

        assertThrows(IllegalArgumentException.class, () -> {
            SeedManager.generateDistancedSeeds(baseSeed, numReplicas);
        }, "Overflow deve essere rilevato");
    }

    @Test
    @DisplayName("Repliche con semi diversi producono sequenze diverse")
    void testDifferentSeedsProduceDifferentSequences() {
        long baseSeed = 100_000_000L;
        long[] seeds = SeedManager.generateDistancedSeeds(baseSeed, 3);

        // Genera primi 10 numeri da ogni seed
        double[][] sequences = new double[3][10];

        for (int replica = 0; replica < 3; replica++) {
            Rngs rngs = new Rngs();
            rngs.plantSeeds(seeds[replica]);
            rngs.selectStream(0);

            for (int i = 0; i < 10; i++) {
                sequences[replica][i] = rngs.random();
            }
        }

        // Verifica che le sequenze siano diverse
        for (int i = 0; i < 10; i++) {
            assertNotEquals(sequences[0][i], sequences[1][i], 1e-10,
                "Replica 0 e 1 devono differire");
            assertNotEquals(sequences[1][i], sequences[2][i], 1e-10,
                "Replica 1 e 2 devono differire");
        }
    }

    @Test
    @DisplayName("Stream indipendenti producono sequenze non correlate")
    void testStreamIndependence() {
        int n = 10000;
        double[] stream0 = new double[n];
        double[] stream1 = new double[n];

        Rngs rngs = new Rngs();
        rngs.plantSeeds(123456789L);
        Rvgs rvgs = new Rvgs(rngs);

        // Genera campioni da stream 0 (arrivi)
        for (int i = 0; i < n; i++) {
            rngs.selectStream(0);
            stream0[i] = rvgs.exponential(1.0);
        }

        // Reset seed e genera da stream 1 (servizi)
        rngs.plantSeeds(123456789L);
        for (int i = 0; i < n; i++) {
            rngs.selectStream(1);
            stream1[i] = rvgs.exponential(1.0);
        }

        // Calcola correlazione
        double correlation = SeedManager.calculateCorrelation(stream0, stream1);

        // Correlazione deve essere ~0 (tolleranza ±0.05 per n=10000)
        assertTrue(Math.abs(correlation) < 0.05,
            String.format("Correlazione tra stream = %.4f, attesa ≈ 0", correlation));
    }

    @Test
    @DisplayName("Stesso stream con stesso seed produce sequenza identica (riproducibilità)")
    void testReproducibility() {
        long seed = 987654321L;
        int n = 100;

        double[] sequence1 = new double[n];
        double[] sequence2 = new double[n];

        // Prima esecuzione
        Rngs rngs1 = new Rngs();
        rngs1.plantSeeds(seed);
        rngs1.selectStream(0);
        for (int i = 0; i < n; i++) {
            sequence1[i] = rngs1.random();
        }

        // Seconda esecuzione (stesso seed)
        Rngs rngs2 = new Rngs();
        rngs2.plantSeeds(seed);
        rngs2.selectStream(0);
        for (int i = 0; i < n; i++) {
            sequence2[i] = rngs2.random();
        }

        // Sequenze devono essere identiche
        assertArrayEquals(sequence1, sequence2, 1e-15,
            "Stesso seed deve produrre sequenza identica");
    }

    @Test
    @DisplayName("Correlazione di stream identico con se stesso = 1")
    void testSelfCorrelation() {
        double[] stream = {1.0, 2.0, 3.0, 4.0, 5.0};
        double corr = SeedManager.calculateCorrelation(stream, stream);

        assertEquals(1.0, corr, 1e-10, "Autocorrelazione deve essere 1");
    }

    @Test
    @DisplayName("Correlazione di stream anticorrelati = -1")
    void testNegativeCorrelation() {
        double[] stream1 = {1.0, 2.0, 3.0, 4.0, 5.0};
        double[] stream2 = {5.0, 4.0, 3.0, 2.0, 1.0};

        double corr = SeedManager.calculateCorrelation(stream1, stream2);

        assertEquals(-1.0, corr, 1e-10, "Anticorrelazione perfetta = -1");
    }

    @Test
    @DisplayName("Correlazione con array lunghezze diverse lancia eccezione")
    void testCorrelationDifferentLengths() {
        double[] stream1 = {1.0, 2.0, 3.0};
        double[] stream2 = {1.0, 2.0};

        assertThrows(IllegalArgumentException.class, () -> {
            SeedManager.calculateCorrelation(stream1, stream2);
        });
    }

    @Test
    @DisplayName("StreamType enum contiene stream standard")
    void testStreamTypeEnum() {
        assertEquals(0, SeedManager.StreamType.ARRIVALS.ordinal());
        assertEquals(1, SeedManager.StreamType.SERVICE.ordinal());
        assertEquals(2, SeedManager.StreamType.THINK_TIME.ordinal());
        assertEquals(3, SeedManager.StreamType.ROUTING.ordinal());
    }
}

