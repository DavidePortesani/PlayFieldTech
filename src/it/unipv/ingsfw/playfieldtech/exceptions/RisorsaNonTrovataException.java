package it.unipv.ingsfw.playfieldtech.exceptions;

// Ora estende RegolaDiBusinessException (è una Exception, non RuntimeException)
public class RisorsaNonTrovataException extends RegolaDiBusinessException {
    public RisorsaNonTrovataException(String message) {
        super(message);
    }
}