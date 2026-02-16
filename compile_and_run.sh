#!/bin/bash

# 1. Pulisci la vecchia build (opzionale ma consigliato)
rm -rf bin/*

# 2. Compila TUTTO quello che trova in src mettendo i .class in bin
#    Il flag -d bin dice a Java di ricreare la struttura delle cartelle (sim/ e libraries/) dentro bin
echo "Compilazione in corso..."
javac -d bin -sourcepath src src/libraries/*.java src/sim/*.java

# Controllo errori
if [ $? -eq 0 ]; then
    echo "Compilazione riuscita!"
    echo "----------------------------------------"
    
    # 3. Esegui
    #    Nota: Devi usare il "Fully Qualified Name": nomepacchetto.NomeClasse
    java -cp bin sim.Main "$@"
else
    echo "Errore durante la compilazione."
fi