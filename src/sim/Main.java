package sim;

import libraries.*;

public class Main {
    public static void main(String[] args) {
        // Test per vedere se funziona
        Rngs r = new Rngs();
        r.plantSeeds(123456789);
        System.out.println("Setup completato. Seed piantati.");
    }
}