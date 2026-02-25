package it.unipv.ingsfw.playfieldtech.observer;

// Interfaccia che permette di aggiungere, rimuovere e notificare gli osservatori
public interface Subject {
    void addObserver(Observer o);
    void removeObserver(Observer o);
    void notifyObservers();
}
