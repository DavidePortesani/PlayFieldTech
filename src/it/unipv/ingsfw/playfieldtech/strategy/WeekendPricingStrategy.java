package it.unipv.ingsfw.playfieldtech.strategy;

import it.unipv.ingsfw.playfieldtech.model.Prenotazione;
import java.time.DayOfWeek;


// Strategia di prezzo che applica una maggiorazione nei weekend
public class WeekendPricingStrategy implements PricingStrategy {
    private static final double MAGGIORAZIONE_WEEKEND = 1.15; // Maggiorazione del 15%

    @Override
    public double calcolaPrezzo(Prenotazione prenotazione) {
        double prezzoBase = prenotazione.getFasciaOraria().getPrezzoBase();
        DayOfWeek giorno = prenotazione.getData().getDayOfWeek();

        if (giorno == DayOfWeek.SATURDAY || giorno == DayOfWeek.SUNDAY) {
            return prezzoBase * MAGGIORAZIONE_WEEKEND;
        }
        // Se non è weekend, si comporta come la strategia standard.
        return prezzoBase;
    }
}