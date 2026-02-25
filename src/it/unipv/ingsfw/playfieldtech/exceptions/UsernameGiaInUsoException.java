package it.unipv.ingsfw.playfieldtech.exceptions;

public class UsernameGiaInUsoException extends Exception {
    public UsernameGiaInUsoException(String username) {
        super("L'username '" + username + "' è già in uso. Scegline un altro.");
    }
}