package it.unipv.ingsfw.playfieldtech.model;

import java.time.DayOfWeek;
import java.time.LocalTime;

// Classe che rappresenta una fascia oraria
public class FasciaOraria {
    private int idFascia;
    private DayOfWeek giornoSettimana;
    private LocalTime oraInizio;
    private LocalTime oraFine;
    private double prezzoBase;

    public FasciaOraria(int idFascia, DayOfWeek giorno, LocalTime oraInizio, LocalTime oraFine, double prezzoBase) {
        this.idFascia = idFascia;
        this.giornoSettimana = giorno;
        this.oraInizio = oraInizio;
        this.oraFine = oraFine;
        this.prezzoBase = prezzoBase;
    }

    public int getIdFascia() { return idFascia; }
    public DayOfWeek getGiornoSettimana() { return giornoSettimana; } // Utile per filtrare le fasce in base al giorno del torneo
    public LocalTime getOraInizio() { return oraInizio; }
    public LocalTime getOraFine() { return oraFine; }
    public double getPrezzoBase() { return prezzoBase; }
}