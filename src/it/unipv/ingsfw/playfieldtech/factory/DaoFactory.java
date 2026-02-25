package it.unipv.ingsfw.playfieldtech.factory;

import it.unipv.ingsfw.playfieldtech.dao.*;

public class DaoFactory {
    
    private static DaoFactory instance;

    // Singleton della Factory
    private DaoFactory() {}

    public static DaoFactory getInstance() {
        if (instance == null) instance = new DaoFactory();
        return instance;
    }

    // Metodi per ottenere i DAO
    public TorneoDAO getTorneoDAO() { return new TorneoDAO(); }
    public PartitaDAO getPartitaDAO() { return new PartitaDAO(); }
    public PrenotazioneDAO getPrenotazioneDAO() { return new PrenotazioneDAO(); }
    public SquadraDAO getSquadraDAO() { return new SquadraDAO(); }
    public ImpiantoSportivoDAO getImpiantoDAO() { return new ImpiantoSportivoDAO(); }
    public FasciaOrariaDAO getFasciaOrariaDAO() { return new FasciaOrariaDAO(); }
    public ImpiantiFasceDAO getImpiantiFasceDAO() { return new ImpiantiFasceDAO(); }
    public UtenteDAO getUtenteDAO() { return new UtenteDAO(); }
}