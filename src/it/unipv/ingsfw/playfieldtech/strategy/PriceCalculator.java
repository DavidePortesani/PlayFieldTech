package it.unipv.ingsfw.playfieldtech.strategy;


import it.unipv.ingsfw.playfieldtech.model.Prenotazione;
import java.time.DayOfWeek;

// Classe che utilizza la strategia di prezzo per calcolare il prezzo finale di una prenotazione
public class PriceCalculator {
    
    private PricingStrategy strategy;

    public double calcolaPrezzoFinale(Prenotazione prenotazione) {
        // Seleziona la strategia basandosi sul giorno della settimana
        DayOfWeek giorno = prenotazione.getData().getDayOfWeek();
        if (giorno == DayOfWeek.SATURDAY || giorno == DayOfWeek.SUNDAY) {
            this.strategy = new WeekendPricingStrategy();
        } else {
            this.strategy = new StandardPricingStrategy();
        }

        // Delega il calcolo alla strategia selezionata
        return strategy.calcolaPrezzo(prenotazione);
    }
}