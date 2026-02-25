package it.unipv.ingsfw.playfieldtech.exceptions;

// Ora estende RegolaDiBusinessException
public class DatiConflittoException extends RegolaDiBusinessException {
    public DatiConflittoException(String message) {
        super(message);
    }
}