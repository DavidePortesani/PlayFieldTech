package it.unipv.ingsfw.playfieldtech.exceptions;

/**
 * Eccezione "madre" controllata (checked exception) per tutti gli errori
 * di business che possono essere gestiti e mostrati all'utente.
 */
public class RegolaDiBusinessException extends Exception {
    public RegolaDiBusinessException(String message) {
        super(message);
    }
}