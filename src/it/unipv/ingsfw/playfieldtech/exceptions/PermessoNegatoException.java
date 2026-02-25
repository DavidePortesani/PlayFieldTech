package it.unipv.ingsfw.playfieldtech.exceptions;

// Ora estende RegolaDiBusinessException
public class PermessoNegatoException extends RegolaDiBusinessException {
    
    public PermessoNegatoException() {
        super("Non hai i permessi necessari per eseguire questa operazione.");
    }
    
    public PermessoNegatoException(String message) {
        super(message);
    }
}