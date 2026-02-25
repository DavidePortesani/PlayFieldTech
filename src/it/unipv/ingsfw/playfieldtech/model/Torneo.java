package it.unipv.ingsfw.playfieldtech.model;

import it.unipv.ingsfw.playfieldtech.state.*; // Importa gli stati
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

// Classe che rappresenta un torneo
public class Torneo {
    private int idTorneo;
    private String nome;
    private LocalDate dataInizio;
    
    // STATE PATTERN INTEGRATO 
    private StatoTorneo statoCorrente; 
    
    private Squadra vincitore;
    private List<Squadra> squadrePartecipanti;
    private List<Partita> partite; 

    public Torneo(int idTorneo, String nome, LocalDate dataInizio) {
        this.idTorneo = idTorneo;
        this.nome = nome;
        this.dataInizio = dataInizio;
        
        // Di default, un torneo nasce in BOZZA
        this.statoCorrente = new StatoBozza(); 
        
        this.squadrePartecipanti = new ArrayList<>();
        this.partite = new ArrayList<>();
    }
    
    // Deleghiamo i controlli all'oggetto Stato
    public void generaCalendario() throws Exception { statoCorrente.generaCalendario(this); }
    public void inserisciRisultato() throws Exception { statoCorrente.inserisciRisultato(this); }
    public void concludiTorneo() throws Exception { statoCorrente.concludiTorneo(this); }

    // Ritorna la stringa per compatibilità col DB
    public String getStato() { return statoCorrente.getNomeStato(); }
    
    // Imposta lo stato a partire dalla stringa letta dal DB (TorneoDAO)
    public void setStato(String statoDalDB) { 
        if ("ATTIVO".equalsIgnoreCase(statoDalDB)) this.statoCorrente = new StatoAttivo();
        else if ("CONCLUSO".equalsIgnoreCase(statoDalDB)) this.statoCorrente = new StatoConcluso();
        else ;
    }

    // Usato internamente dalle classi di Stato per scambiarsi di posto
    public void setStatoObj(StatoTorneo nuovoStato) { this.statoCorrente = nuovoStato; }

    public int getIdTorneo() { return idTorneo; }
    public void setIdTorneo(int id) { this.idTorneo = id; }
    public String getNome() { return nome; }
    public LocalDate getDataInizio() { return dataInizio; }
    public List<Squadra> getSquadrePartecipanti() { return squadrePartecipanti; }
    public void setSquadrePartecipanti(List<Squadra> squadre) { this.squadrePartecipanti = squadre; }
    public List<Partita> getPartite() { return partite; }
    public void setPartite(List<Partita> partite) { this.partite = partite; }
    public Squadra getVincitore() { return vincitore; }
    public void setVincitore(Squadra vincitore) { this.vincitore = vincitore; }
    
    @Override
    public String toString() { return nome + " (" + getStato() + ")"; }
}