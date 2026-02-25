package it.unipv.ingsfw.playfieldtech.strategy;

import it.unipv.ingsfw.playfieldtech.model.Prenotazione;

/**
 * L'interfaccia Strategy. Definisce il contratto per tutte le
 * possibili strategie di calcolo del prezzo.
 */
public interface PricingStrategy {
    double calcolaPrezzo(Prenotazione prenotazione);
}