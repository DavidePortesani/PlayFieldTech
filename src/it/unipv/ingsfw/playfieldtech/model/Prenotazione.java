package it.unipv.ingsfw.playfieldtech.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Classe centrale che rappresenta una prenotazione
 */
public class Prenotazione {

    private int idPrenotazione;
    private LocalDate data;
    private String nomeSquadraA;
    private String nomeSquadraB;
    private boolean pagamentoConfermato;

    private Utente creatore;

    private ImpiantoSportivo impianto;
    private FasciaOraria fasciaOraria;
    private List<Cliente> giocatori; // I giocatori rimangono sempre 'Clienti'

    // Campi ausiliari per DAO (per caricare dati parziali)
    private int fkCampo;
    private int fkFascia;

    public Prenotazione(int idPrenotazione, LocalDate data, Utente creatore, ImpiantoSportivo impianto, FasciaOraria fascia) {
        this.idPrenotazione = idPrenotazione;
        this.data = data;
        this.creatore = creatore; 
        this.impianto = impianto;
        this.fasciaOraria = fascia;
        this.pagamentoConfermato = false;
        this.giocatori = new ArrayList<>();
    }

    public int getIdPrenotazione() { return idPrenotazione; }
    public void setIdPrenotazione(int idPrenotazione) { this.idPrenotazione = idPrenotazione; }

    public LocalDate getData() { return data; }
    public void setData(LocalDate data) { this.data = data; }

    public String getNomeSquadraA() { return nomeSquadraA; }
    public void setNomeSquadraA(String nomeSquadraA) { this.nomeSquadraA = nomeSquadraA; }

    public String getNomeSquadraB() { return nomeSquadraB; }
    public void setNomeSquadraB(String nomeSquadraB) { this.nomeSquadraB = nomeSquadraB; }

    public boolean isPagamentoConfermato() { return pagamentoConfermato; }
    public void setPagamentoConfermato(boolean pagamentoConfermato) { this.pagamentoConfermato = pagamentoConfermato; }

    public Utente getCreatore() { return creatore; } 
    public void setCreatore(Utente creatore) { this.creatore = creatore; } 

    public ImpiantoSportivo getImpianto() { return impianto; }
    public void setImpianto(ImpiantoSportivo impianto) { this.impianto = impianto; }

    public FasciaOraria getFasciaOraria() { return fasciaOraria; }
    public void setFasciaOraria(FasciaOraria fasciaOraria) { this.fasciaOraria = fasciaOraria; }

    public List<Cliente> getGiocatori() { return giocatori; }
    public void setGiocatori(List<Cliente> giocatori) { this.giocatori = giocatori; }

    public int getFkCampo() { return fkCampo; }
    public void setFkCampo(int fkCampo) { this.fkCampo = fkCampo; }
    public int getFkFascia() { return fkFascia; }
    public void setFkFascia(int fkFascia) { this.fkFascia = fkFascia; }
}