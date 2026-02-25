package it.unipv.ingsfw.playfieldtech.model;

// Classe che rappresenta un impianto sportivo
public class ImpiantoSportivo {
    private int idCampo;
    private String nome;
    private String tipoSport;

    public ImpiantoSportivo(int idCampo, String nome, String tipoSport) {
        this.idCampo = idCampo;
        this.nome = nome;
        this.tipoSport = tipoSport;
    }

    public int getIdCampo() { return idCampo; }
    public String getNome() { return nome; }
    public String getTipoSport() { return tipoSport; }
}