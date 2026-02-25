package it.unipv.ingsfw.playfieldtech.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Rappresenta una squadra di giocatori.
 */
public class Squadra {

    private int idSquadra;
    private String nomeSquadra;
    private Cliente capitano;
    private List<Cliente> membri;
    private int idCapitano; // ID del capitano per controlli senza caricare l'intero oggetto

    public Squadra(int idSquadra, String nomeSquadra, Cliente capitano, int idCapitano) {
        this.idSquadra = idSquadra;
        this.nomeSquadra = nomeSquadra;
        this.capitano = capitano;
        this.idCapitano = idCapitano;
        this.membri = new ArrayList<>();
    }

    public Squadra(){
        // Costruttore vuoto per utilizzo test
    }

    public int getIdSquadra() { return idSquadra; }
    public void setIdSquadra(int id) { this.idSquadra = id; }
    public String getNomeSquadra() { return nomeSquadra; }
    public void setNomeSquadra(String nome) { this.nomeSquadra = nome; }
    public Cliente getCapitano() { return capitano; }
    public List<Cliente> getMembri() { return membri; }
    public void setMembri(List<Cliente> membri) { this.membri = membri; }
    public int getIdCapitano() { return idCapitano; }

    @Override
    public String toString() {
        return nomeSquadra;
    }
}