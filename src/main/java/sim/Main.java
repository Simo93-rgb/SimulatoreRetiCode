package sim;

import libraries.*;

public class Main {
    public static void main(String[] args) {
        // Test per vedere se funziona
        Rngs r = new Rngs();
        r.testRandom(); // should print: The implementation of Rngs.java is correct
        r.plantSeeds(123456789);

        System.out.println("Setup completato. Seed piantati.");
    }
}