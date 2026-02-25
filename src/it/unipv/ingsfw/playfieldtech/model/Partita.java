package it.unipv.ingsfw.playfieldtech.model;

// Classe che rappresenta una partita del torneo, con riferimenti alle squadre, prenotazione e risultati
public class Partita {
    private int idPartita;
    private int idTorneo;
    private Squadra squadraCasa; // Può essere null
    private Squadra squadraOspite; // Può essere null
    private Prenotazione prenotazione; // Può essere null
    private Integer risultatoCasa;
    private Integer risultatoOspite;
    
    private int turno; 
    
    // Riferimenti per costruire il tabellone
    private Partita partitaSuccessiva;
    private String slotVincitoreSuccessivo; // "CASA" o "OSPITE"

    public Partita(int idTorneo, int turno) {
        this.idTorneo = idTorneo;
        this.turno = turno;
    }
    
    // Metodo di utilità per verificare se la partita è pronta per essere prenotata 
    // (cioè ha entrambe le squadre assegnate e non ha ancora una prenotazione)
    public boolean isProntaPerPrenotazione() {
        return squadraCasa != null && squadraOspite != null && prenotazione == null;
    }
    
    /**
     * NUOVO METODO: Restituisce una stringa leggibile per il turno.
     * @return Stringa come "Finale", "Semifinale", ecc.
     */
    public String getTurnoString() {
        switch (this.turno) {
            case 1:
                return "Finale";
            case 2:
                return "Semifinale";
            default:
                return "Turno " + this.turno;
        }
    }

    public int getIdPartita() { return idPartita; }
    public void setIdPartita(int id) { this.idPartita = id; }
    public int getIdTorneo() { return idTorneo; }
    public Squadra getSquadraCasa() { return squadraCasa; }
    public void setSquadraCasa(Squadra s) { this.squadraCasa = s; }
    public Squadra getSquadraOspite() { return squadraOspite; }
    public void setSquadraOspite(Squadra s) { this.squadraOspite = s; }
    public Prenotazione getPrenotazione() { return prenotazione; }
    public void setPrenotazione(Prenotazione p) { this.prenotazione = p; }
    public Integer getRisultatoCasa() { return risultatoCasa; }
    public void setRisultatoCasa(Integer r) { this.risultatoCasa = r; }
    public Integer getRisultatoOspite() { return risultatoOspite; }
    public void setRisultatoOspite(Integer r) { this.risultatoOspite = r; }
    public int getTurno() { return turno; }
    public void setTurno(int turno) { this.turno = turno; }
    public Partita getPartitaSuccessiva() { return partitaSuccessiva; }
    public void setPartitaSuccessiva(Partita p) { this.partitaSuccessiva = p; }
    public String getSlotVincitoreSuccessivo() { return slotVincitoreSuccessivo; }
    public void setSlotVincitoreSuccessivo(String s) { this.slotVincitoreSuccessivo = s; }
    
}