package sim;

import sim.runners.Punto5Runner;
import sim.runners.Punto6Runner;
import sim.runners.Punto7Runner;

/**
 * Entry point principale del simulatore.
 *
 * Dispatcher CLI: lancia il runner del punto desiderato.
 *
 * Uso:
 *   java sim.Main punto5
 *   java sim.Main punto6
 *
 * Senza argomenti: stampa l'help.
 */
public class Main {

    public static void main(String[] args) {
        if (args.length == 0) {
            printHelp();
            return;
        }

        switch (args[0].toLowerCase()) {
            case "punto5" -> Punto5Runner.run();
            case "punto6" -> Punto6Runner.run();
            case "punto7" -> Punto7Runner.run();
            default -> {
                System.err.println("Punto non riconosciuto: " + args[0]);
                printHelp();
                System.exit(1);
            }
        }
    }

    private static void printHelp() {
        System.out.println("Uso: java sim.Main <punto>");
        System.out.println("Punti disponibili:");
        System.out.println("  punto5  - Validazione M/M/1 e impatto variabilità servizio");
        System.out.println("  punto6  - Sistema chiuso con Q0, Q1, Q2 (N variabile)");
        System.out.println("  punto7  - Sistema misto: classe chiusa + classe aperta su Q1");
    }
}