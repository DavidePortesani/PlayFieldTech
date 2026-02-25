package it.unipv.ingsfw.playfieldtech.manager;

import it.unipv.ingsfw.playfieldtech.dao.*;
import it.unipv.ingsfw.playfieldtech.exceptions.*;
import it.unipv.ingsfw.playfieldtech.factory.DaoFactory;
import it.unipv.ingsfw.playfieldtech.model.*;
import it.unipv.ingsfw.playfieldtech.observer.Subject;
import it.unipv.ingsfw.playfieldtech.observer.Observer;

import java.time.LocalDate;
import java.util.*;

// Classe che gestisce la logica dei tornei, inclusa la generazione del calendario, l'inserimento dei risultati e 
// l'avanzamento delle squadre nei tornei a eliminazione diretta
public class TorneoManager implements Subject {

    private final List<Observer> observers; 

    private final TorneoDAO torneoDAO;
    private final PartitaDAO partitaDAO;
    private final PrenotazioneDAO prenotazioneDAO;
    private final ImpiantiFasceDAO impiantiFasceDAO;
    private final FasciaOrariaDAO fasciaOrariaDAO;
    private final ImpiantoSportivoDAO impiantoDAO;
    private final SquadraDAO squadraDAO; 
    private final PrenotazioneManager prenotazioneManager;

    public TorneoManager(DaoFactory factory, PrenotazioneManager prenotazioneManager) {
       
        this.torneoDAO = factory.getTorneoDAO();
        this.partitaDAO =  factory.getPartitaDAO();
        this.prenotazioneDAO = factory.getPrenotazioneDAO();
        this.impiantiFasceDAO = factory.getImpiantiFasceDAO();
        this.fasciaOrariaDAO = factory.getFasciaOrariaDAO();
        this.impiantoDAO = factory.getImpiantoDAO();
        this.squadraDAO = factory.getSquadraDAO(); 

        this.prenotazioneManager = prenotazioneManager;

        this.observers = new ArrayList<>();
    }

    /*
    * Metodo che cerca di trovare uno slot libero per la partita p a partire da una data minima (di solito la data di inizio del torneo).
    */
    private void trovaEBookaSlot(Partita p, LocalDate dataMinima, Amministratore admin) 
            throws SlotNonTrovatoException, OperazioneFallitaException, DataAccessException {
        
        List<ImpiantoSportivo> campi = impiantoDAO.getTuttiImpianti();
        List<FasciaOraria> fasce = fasciaOrariaDAO.getTutteFasceOrarie();
        Map<Integer, List<Integer>> mappaDisponibilita = impiantiFasceDAO.getMappaDisponibilita();
        // carichiamo tutte le prenotazioni future una volta sola per evitare query ripetute durante la ricerca
        LocalDate dataCorrente = dataMinima;// Iniziamo la ricerca dalla data minima (di solito la data di inizio del torneo)
        int maxGiorniRicerca = 30; // Limitiamo la ricerca a un certo numero di giorni per evitare loop infiniti in caso di calendario pieno
        // Iteriamo giorno per giorno fino a trovare uno slot libero o esaurire il limite di ricerca
        for (int i = 0; i < maxGiorniRicerca; i++) {
            // carichiamo le prenotazioni del giorno una volta sola
            List<Prenotazione> prenotazioniGiorno = prenotazioneDAO.getPrenotazioniByDate(dataCorrente);
            // Per ogni combinazione di campo e fascia, controlliamo se è libera
            for (ImpiantoSportivo campo : campi) {
                for (FasciaOraria fascia : fasce) { //il primo ciclo lo salta sempre perchè la data del torneo è il giorno dopo
                    if (fascia.getGiornoSettimana() != dataCorrente.getDayOfWeek()) continue; // Saltiamo le fasce che non corrispondono al giorno della settimana del torneo
                    // Recuperiamo le fasce disponibili per questo campo dalla mappa
                    List<Integer> fasceDelCampo = mappaDisponibilita.get(campo.getIdCampo()); 
                    // Se il campo non ha fasce disponibili o se questa fascia specifica non è disponibile per questo campo, saltiamo direttamente al prossimo
                    if (fasceDelCampo == null || !fasceDelCampo.contains(fascia.getIdFascia())) continue; 

                    // CONTROLLO CAMPO: È libero fisicamente?
                    boolean isCampoOccupato = prenotazioniGiorno.stream().anyMatch(pr ->
                        pr.getFkCampo() == campo.getIdCampo() && pr.getFkFascia() == fascia.getIdFascia()
                    );
                    
                    if (!isCampoOccupato) {
                        // Il campo è libero. 
                        // Ora dobbiamo controllare se i giocatori coinvolti nella partita sono liberi in questo slot
                        //  (cioè non hanno altre prenotazioni o partite).
                        
                        // Recuperiamo tutti i membri delle due squadre
                        List<Cliente> giocatoriCasa = squadraDAO.getMembriBySquadra(p.getSquadraCasa().getIdSquadra());
                        List<Cliente> giocatoriOspite = squadraDAO.getMembriBySquadra(p.getSquadraOspite().getIdSquadra());
                        
                        List<Cliente> tuttiIGiocatori = new ArrayList<>();
                        tuttiIGiocatori.addAll(giocatoriCasa);
                        tuttiIGiocatori.addAll(giocatoriOspite);
                        
                        // Verifichiamo se qualcuno ha già un impegno (torneo o prenotazione privata)
                        boolean conflittoGiocatori = false;
                        for (Cliente c : tuttiIGiocatori) {
                            if (prenotazioneDAO.isUtenteOccupato(c.getIdUtente(), dataCorrente, fascia.getIdFascia())) {
                                conflittoGiocatori = true;
                                break; // Se anche uno solo è occupato, lo slot non va bene
                            }
                        }

                        if (conflittoGiocatori) {
                            // Se c'è conflitto, saltiamo questa fascia oraria e ne cerchiamo un'altra
                            continue; 
                        }

                        // se siamo qui, significa che sia il campo che i giocatori sono liberi: possiamo prenotare!
                        
                        Prenotazione nuova = new Prenotazione(0, dataCorrente, admin, campo, fascia);
                        nuova.setNomeSquadraA("TORNEO: " + p.getSquadraCasa().getNomeSquadra());
                        nuova.setNomeSquadraB("TORNEO: " + p.getSquadraOspite().getNomeSquadra());
                        nuova.setPagamentoConfermato(true);
                        
                        // Aggiungiamo i giocatori alla prenotazione per bloccarli per futuri controlli
                        nuova.setGiocatori(tuttiIGiocatori);
                        
                        try {
                            prenotazioneDAO.salvaPrenotazione(nuova); 
                            // Colleghiamo la prenotazione alla partita
                            partitaDAO.aggiornaConPrenotazione(p.getIdPartita(), nuova.getIdPrenotazione());  
                            return; // Successo! Usciamo dal metodo
                        } catch (Exception e) {
                            throw new OperazioneFallitaException("Errore tecnico salvataggio prenotazione.", e);
                        }
                    }
                }
            }
            dataCorrente = dataCorrente.plusDays(1); // Passiamo al giorno successivo e ripetiamo la ricerca
        }
        throw new SlotNonTrovatoException("Impossibile trovare slot libero (campo o giocatori) per la partita " + p.getIdPartita());
    }

    /*
    * Metodo che inserisce il risultato di una partita e, se la partita è parte di un torneo a eliminazione diretta, 
    * avanza automaticamente la squadra vincitrice al turno successivo prenotando uno slot per la nuova partita se necessario.
     * La logica è la seguente:
     * 1. Si aggiorna il risultato della partita nel database.
     * 2. Si determina la squadra vincitrice in base al numero di gol.
     * 3. Se la partita ha una partita successiva (cioè non è la finale), si avanza la squadra vincitrice alla partita successiva:
     *    - Si aggiorna la partita successiva assegnando la squadra vincitrice al posto giusto (casa o ospite).
     *    - Se la partita successiva è ora pronta per essere prenotata (cioè ha entrambe le squadre assegnate), si chiama il metodo per trovare e prenotare uno slot.
     * 4. Se la partita non ha una partita successiva, significa che era la finale: si aggiorna il vincitore del torneo e si conclude il torneo.
    */
    public void inserisciRisultato(int idPartita, int golCasa, int golOspite, Amministratore admin) 
            throws RisorsaNonTrovataException, DataAccessException, SlotNonTrovatoException, OperazioneFallitaException, RegolaDiBusinessException {
        
        //Controllo limite Gol (0-60)
        if (golCasa > 60 || golOspite > 60) {
            throw new RegolaDiBusinessException("Il numero di gol non può superare il limite di 60.");
        }
        
        if (golCasa == golOspite) throw new RegolaDiBusinessException("Il pareggio non è ammesso.");
        
        partitaDAO.aggiornaRisultato(idPartita, golCasa, golOspite);
        Partita p = partitaDAO.getPartitaById(idPartita);
        Squadra vincitore;

        if (golCasa > golOspite) {
            vincitore = p.getSquadraCasa();
        } else {
            vincitore = p.getSquadraOspite();
        }
        
        if (p.getPartitaSuccessiva() != null) {
            partitaDAO.avanzaVincitore(vincitore.getIdSquadra(), p.getPartitaSuccessiva().getIdPartita(), p.getSlotVincitoreSuccessivo());
            Partita next = partitaDAO.getPartitaById(p.getPartitaSuccessiva().getIdPartita());
            if (next.isProntaPerPrenotazione()) {
                trovaEBookaSlot(next, p.getPrenotazione().getData().plusDays(1), admin);
            }
        } else { // Se non c'è partita successiva, questa era la finale: aggiorniamo il vincitore del torneo e concludiamo il torneo
            torneoDAO.aggiornaVincitore(p.getIdTorneo(), vincitore.getIdSquadra());
            try {
                Torneo torneo = torneoDAO.getTorneoById(p.getIdTorneo());
                torneo.concludiTorneo();
                torneoDAO.aggiornaStato(p.getIdTorneo(), torneo.getStato());
            } catch (Exception e) {
                throw new RegolaDiBusinessException("Impossibile concludere il torneo: " + e.getMessage());
            }
            
        }
        notifyObservers();
    }

    /*
    * Metodo che verifica che non ci siano giocatori doppi tra le squadre selezionate per un torneo.
     * Se un giocatore è presente in più di una squadra, viene lanciata una RegolaDiBusinessException con un messaggio dettagliato che 
     * indica il conflitto.
     * Questo metodo viene chiamato durante la creazione del torneo per assicurarsi che ogni giocatore partecipi a una sola squadra 
     * all'interno dello stesso torneo.
     * La logica è la seguente:
     * 1. Si crea una mappa per tenere traccia dei giocatori già visti e a quale squadra appartengono.
     * 2. Si iterano tutte le squadre selezionate e si recuperano i loro membri.
     * 3. Per ogni membro, si controlla se è già presente nella mappa:
     *    - Se sì, significa che c'è un conflitto: il giocatore appartiene a più di una squadra. Si lancia l'eccezione con un
     *      messaggio dettagliato.
     *    - Se no, si aggiunge il giocatore alla mappa associandolo alla sua squadra.
    */
    public void validaGiocatoriUnivoci(List<Squadra> squadreSelezionate) throws RegolaDiBusinessException, DataAccessException {
        Map<Integer, String> giocatoriGiaPresenti = new HashMap<>(); // Mappa per tenere traccia dei giocatori già visti e a quale squadra appartengono
        // Per ogni squadra selezionata, recuperiamo i suoi membri e verifichiamo se qualcuno è già presente in un'altra squadra
        for (Squadra s : squadreSelezionate) {
            List<Cliente> membri = squadraDAO.getMembriBySquadra(s.getIdSquadra());
            for (Cliente c : membri) {
                if (giocatoriGiaPresenti.containsKey(c.getIdUtente())) { // Se il giocatore è già presente, c'è un conflitto
                    String squadraPrecedente = giocatoriGiaPresenti.get(c.getIdUtente());
                    throw new RegolaDiBusinessException(
                        "Conflitto Giocatore: L'utente '" + c.getUsername() + "' (" + c.getNome() + " " + c.getCognome() + ")" +
                        "\nfa parte di due squadre selezionate:\n" +
                        "1. " + squadraPrecedente + "\n" +
                        "2. " + s.getNomeSquadra() + "\n\n" +
                        "Impossibile creare il torneo."
                    );
                }
                giocatoriGiaPresenti.put(c.getIdUtente(), s.getNomeSquadra());
            }
        }
    }

    /*
    * Metodo che elimina completamente un torneo, tutte le partite associate, le prenotazioni e i risultati.
     * Viene usato quando un amministratore decide di cancellare un torneo esistente.
     * La logica è la seguente:
     * 1. Recupera tutte le partite del torneo.
     * 2. Per ogni partita, se ha una prenotazione associata, elimina la prenotazione (che a sua volta libera i giocatori).
     * 3. Elimina tutte le partite del torneo.
     * 4. Elimina il torneo stesso.
    */
    public void eliminaTorneoCompleto(int idTorneo) throws DataAccessException {
        try {
            prenotazioneDAO.eliminaPrenotazioniDiTorneo(idTorneo);
            torneoDAO.eliminaTorneo(idTorneo);
            prenotazioneManager.notifyObservers();
            notifyObservers();
        } catch (Exception e) {
            throw new DataAccessException("Errore durante l'eliminazione completa del torneo: " + e.getMessage(), e);
        }
    }

    public void creaTorneo(String nome, List<Squadra> squadre, int idAdmin)
        throws DataAccessException, RegolaDiBusinessException {
        // La data di inizio viene impostata automaticamente a domani (o a una data futura predefinita)
        LocalDate dataInizio = LocalDate.now().plusDays(1);
        // Validazione: Controlla se ci sono giocatori doppi
        validaGiocatoriUnivoci(squadre);
        
        // Creazione oggetto e interazione con il DAO
        // L'ID viene assegnato dal database con AUTOINCREMENT, quindi inizializziamo a 0 o un valore fittizio 
        // e lasciamo che sia il DAO a restituire l'ID reale dopo la creazione
        Torneo nuovo = new Torneo(0, nome, dataInizio); //in torneo.java il costruttore assegna lo stato a BOZZA di default
        torneoDAO.creaTorneo(nuovo, squadre, idAdmin);
    }

    // mi permette di generare il calendario di un torneo già esistente
    public void generaCalendario(Torneo torneo, Amministratore admin) 
            throws RegolaDiBusinessException {
        
        // MODIFICA PER I TEST: Prendiamo le squadre già presenti nell'oggetto
        List<Squadra> squadreAggiornate = torneo.getSquadrePartecipanti();
        
        // Se l'oggetto è vuoto (es. nell'uso reale), le andiamo a pescare dal DB
        if (squadreAggiornate == null || squadreAggiornate.isEmpty()) {
            try {
                squadreAggiornate = torneoDAO.getSquadreByTorneo(torneo.getIdTorneo()); 
                torneo.setSquadrePartecipanti(squadreAggiornate);
            } catch (Exception e) {
                throw new RegolaDiBusinessException("Errore recupero squadre per generazione calendario: " + e.getMessage());
            }
        }

        // Generazione dell'Albero del Torneo (Delegata a metodo privato)
        List<Partita> partitePrimoTurno = generaStrutturaAlbero(torneo, squadreAggiornate.size());

        // Assegnazione Casuale delle Squadre
        Collections.shuffle(squadreAggiornate);
        // Assegna le squadre alle partite del primo turno e aggiorna il database
        for (int i = 0; i < partitePrimoTurno.size(); i++) {
            Partita p = partitePrimoTurno.get(i);
            Squadra casa = squadreAggiornate.get(i * 2);
            Squadra ospite = squadreAggiornate.get(i * 2 + 1);
            
            p.setSquadraCasa(casa);
            p.setSquadraOspite(ospite);
            // Aggiorna le squadre della partita nel database
            partitaDAO.aggiornaSquadre(p.getIdPartita(), casa.getIdSquadra(), ospite.getIdSquadra()); 
            
            // Prenotazione Fisica dei Campi
            trovaEBookaSlot(p, torneo.getDataInizio(), admin);
        }
        
        // Aggiornamento Stato Finale
        try {
            torneo.generaCalendario();
        } catch (Exception e) {
            throw new RegolaDiBusinessException("Errore durante la generazione del calendario: " + e.getMessage());
        }
        torneoDAO.aggiornaStato(torneo.getIdTorneo(), torneo.getStato());
        
        // Notifica l'interfaccia grafica
        notifyObservers();
    }


    /*
    * Metodo che genera la struttura ad albero delle partite per un torneo a eliminazione diretta, partendo dalla finale e risalendo fino al primo turno.
     * Restituisce la lista di partite del primo turno (quelle che devono essere giocate per prime).
     * La logica è la seguente:
     * - Si crea prima la partita della finale (turno 1).
     * - Poi si crea il turno precedente (es. semifinali, turno 2) collegando le partite alla finale.
     * - Poi si crea il turno precedente ancora (es. quarti, turno 4) collegando le partite alle semifinali.
     * - Si continua fino a coprire tutte le squadre (numSquadre/2 partite al primo turno).
     * nel nostro caso avremo al massimo 4 squadre, quindi al massimo 2 partite (semifinali) più la finale, 
     * ma il metodo è generico per qualsiasi numero di squadre che sia potenza di 2 (4, 8, 16, ecc.).
    */
    private List<Partita> generaStrutturaAlbero(Torneo torneo, int numSquadre) throws DataAccessException {
        Map<Integer, List<Partita>> strutturaTurni = new HashMap<>();
        
        Partita finale = new Partita(torneo.getIdTorneo(), 1); // nel switch dei turni, 1 è sempre la finale
        partitaDAO.creaPartitaVuota(finale);

        // 1 è il turno della finale, 2 delle semifinali
        List<Partita> listaPerLaFinale = new ArrayList<>();
        listaPerLaFinale.add(finale);
        strutturaTurni.put(1, listaPerLaFinale); // Iniziamo con la finale, che è l'unica partita del turno 1

        int turnoCorrente = 2;
        // Finché non abbiamo creato abbastanza partite per coprire tutte le squadre (numSquadre/2 partite al primo turno)
        while (turnoCorrente <= numSquadre / 2) {
            // Prendiamo le partite del turno successivo (es. per 4 squadre, prendiamo la finale)
            List<Partita> partiteTurnoSuccessivo = strutturaTurni.get(turnoCorrente / 2); 
            // Per ogni partita del turno successivo, creiamo 2 partite del turno corrente e le colleghiamo
            List<Partita> partiteQuestoTurno = new ArrayList<>();
            // Creiamo 2 partite per ogni partita del turno successivo (es. per ogni finale, creiamo 2 semifinali)
            for (Partita matchSuccessivo : partiteTurnoSuccessivo) {
                Partita p1 = new Partita(torneo.getIdTorneo(), turnoCorrente); 
                Partita p2 = new Partita(torneo.getIdTorneo(), turnoCorrente);
                // Creiamo le partite vuote nel database per ottenere gli ID necessari al collegamento
                partitaDAO.creaPartitaVuota(p1);
                partitaDAO.creaPartitaVuota(p2);
                // Colleghiamo le partite appena create alla partita del turno successivo (es. semifinale -> finale)
                partitaDAO.collegaPartitaSuccessiva(p1.getIdPartita(), matchSuccessivo.getIdPartita(), "CASA"); 
                partitaDAO.collegaPartitaSuccessiva(p2.getIdPartita(), matchSuccessivo.getIdPartita(), "OSPITE");
                // Aggiungiamo le partite del turno corrente alla lista che poi salveremo nella mappa
                partiteQuestoTurno.add(p1);
                partiteQuestoTurno.add(p2);
            }
            strutturaTurni.put(turnoCorrente, partiteQuestoTurno);
            turnoCorrente *= 2;
        }
        
        // Ritorna le partite del primo turno (es. le 2 semifinali se ci sono 4 squadre)
        return strutturaTurni.get(numSquadre / 2);  // mi torna l'id 2 del hashmap, che contiene le partite del primo turno da giocare (es. semifinali)
    }

    @Override
    public void addObserver(Observer o) {
        observers.add(o);
    }

    @Override
    public void removeObserver(Observer o) {
        observers.remove(o);
    }

    @Override
    // Quando c'è un cambiamento significativo (es. risultato inserito, torneo eliminato, calendario generato), 
    // chiamiamo questo metodo per notificare tutti gli observer registrati.
    // Vedi ClienteController riga 210
    public void notifyObservers() {
        for (Observer observer : observers) {
            observer.update();
        }
    }

    // Metodo per recuperare tutti i tornei, usato dalla Facade per mostrare la lista completa dei tornei nella sezione "Esplora"
    public List<Torneo> getTuttiITornei() throws DataAccessException {
        return torneoDAO.getTuttiITornei();
    }

    // Metodo per recuperare tutte le squadre, usato dalla Facade per mostrare la lista di squadre durante la creazione del torneo
    public List<Squadra> getTutteLeSquadre() throws DataAccessException {
        return squadraDAO.getTutteLeSquadre();
    }

    // Metodo per recuperare le partite di un torneo specifico, usato dalla Facade per mostrare il calendario
    public List<Partita> getPartiteTorneo(int idTorneo) throws DataAccessException {
        return partitaDAO.getPartiteByTorneo(idTorneo);
    }

    // Metodo per recuperare i tornei di un utente specifico, usato dalla Facade per mostrare la dashboard personale
    public List<Torneo> getTorneiDiUtente(int idUtente) throws DataAccessException {
        return torneoDAO.getTorneiPerUtente(idUtente);
    }
}