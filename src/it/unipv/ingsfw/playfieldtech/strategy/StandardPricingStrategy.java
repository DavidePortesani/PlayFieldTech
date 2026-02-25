package it.unipv.ingsfw.playfieldtech.strategy;


import it.unipv.ingsfw.playfieldtech.model.Prenotazione;

// Strategia di prezzo standard che restituisce semplicemente il prezzo base della fascia oraria.
public class StandardPricingStrategy implements PricingStrategy {
    @Override
    public double calcolaPrezzo(Prenotazione prenotazione) {
        // La strategia standard restituisce semplicemente il prezzo base della fascia oraria.
        return prenotazione.getFasciaOraria().getPrezzoBase();
    }
}