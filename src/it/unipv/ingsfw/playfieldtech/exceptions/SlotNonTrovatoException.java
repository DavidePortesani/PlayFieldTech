package it.unipv.ingsfw.playfieldtech.exceptions;

// Ora estende RegolaDiBusinessException
public class SlotNonTrovatoException extends RegolaDiBusinessException {

    public SlotNonTrovatoException() {
        super("Impossibile trovare uno slot (campo + orario) libero e compatibile per la prenotazione.");
    }

    public SlotNonTrovatoException(String message) {
        super(message);
    }
}