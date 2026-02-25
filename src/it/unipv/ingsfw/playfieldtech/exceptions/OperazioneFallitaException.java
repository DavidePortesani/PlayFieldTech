package it.unipv.ingsfw.playfieldtech.exceptions;

/**
 * Eccezione controllata per errori di business generici che
 * causano il fallimento di un'operazione complessa (es. generazione calendario).
 * Estende RegolaDiBusinessException per essere catturata polimorficamente.
 */
public class OperazioneFallitaException extends RegolaDiBusinessException {
    
    public OperazioneFallitaException(String message) {
        super(message);
    }

    public OperazioneFallitaException(String message, Throwable cause) {
        super(message + " Causa: " + cause.getMessage());
    }
}