package it.unipv.ingsfw.playfieldtech.controller;

import it.unipv.ingsfw.playfieldtech.exceptions.*;
import it.unipv.ingsfw.playfieldtech.facade.TorneoFacade;
import it.unipv.ingsfw.playfieldtech.model.*;
import it.unipv.ingsfw.playfieldtech.view.*;
import javax.swing.*;
import java.util.List;

/*
* Controller specializzato per la gestione dei tornei. Si occupa di orchestrare le operazioni legate ai tornei,
* delegando la logica al TorneoFacade e aggiornando le view specifiche dei tornei.
*/
public class TorneoController {

    private final AppController app;
    private final TorneoFacade facade;
    private GestioneTorneiDialog gestioneDialog;
    private VisualizzaTorneoDialog visualizzaDialog;

    public TorneoController(AppController app) {
        this.app = app;
        this.facade = app.getTorneoFacade();
    }

    public void mostraGestioneTornei() {
        try {
            List<Torneo> tornei = facade.getTuttiITornei();
            this.gestioneDialog = new GestioneTorneiDialog(app.getMainFrame(), tornei);

            gestioneDialog.addCreaTorneoListener(_ -> gestisciCreazioneTorneo());
            gestioneDialog.addGeneraCalendarioListener(_ -> generaCalendario());
            gestioneDialog.addVisualizzaPartiteListener(_ -> mostraVisualizzaTorneoDialog());
            gestioneDialog.addEliminaTorneoListener(_ -> eliminaTorneoSelezionato());            
            gestioneDialog.setVisible(true); // Apre la finestra di gestione tornei
        } catch (DataAccessException e) {
            gestioneDialog.mostraErroreNessuno("Errore caricamento tornei: " + e.getMessage());
        }
    }
    /*
    * Gestisce la creazione di un nuovo torneo. Apre un dialog per inserire il nome del torneo e selezionare le squadre partecipanti.
    * Dopo la conferma, chiama la facade per creare il torneo e aggiorna la lista dei tornei nella view. Gestisce anche i casi di errore 
    * e mostra messaggi appropriati all'utente.
    */
    private void gestisciCreazioneTorneo() {
        gestioneDialog.setVisible(false);
        try {
            List<Squadra> squadre = facade.getTutteLeSquadre();
            CreaTorneoDialog creaDialog = new CreaTorneoDialog(app.getMainFrame(), squadre);
            creaDialog.setVisible(true);
            
            if (creaDialog.isConfermato()) { // Se l'utente ha confermato la creazione del torneo
                String nome = creaDialog.getNomeTorneo(); // Recupera il nome del torneo inserito dall'utente
            
                if (nome.isEmpty()) {
                    gestioneDialog.mostraAttenzione("Devi inserire un nome per il torneo.");
                    return;
                }
                List<Squadra> selezionate = creaDialog.getSquadreSelezionate();

                if (selezionate.size() != 4) {
                    gestioneDialog.mostraAttenzione("Devi selezionare esattamente 4 squadre per iniziare un torneo.");
                    return;
                }
                
                facade.creaTorneo(nome, selezionate, app.getUtenteCorrente().getIdUtente());
                // Dopo la creazione, mostriamo un messaggio di successo per il torneo appena creato
                app.getMainFrame().mostraSuccesso("Torneo creato in BOZZA.");
            }
        } catch (RegolaDiBusinessException ex) {
            // Mostra il messaggio specifico dell'errore di univocità all'utente
            gestioneDialog.mostraErroreNessuno(ex.getMessage());
        } catch (DataAccessException ex) {
            gestioneDialog.mostraErroreNessuno("Errore tecnico creazione torneo: " + ex.getMessage());
            
        } finally {// Riapriamo la finestra di gestione tornei per mostrare l'elenco aggiornato, sia in caso di successo che di errore
            if (gestioneDialog != null) {
                gestioneDialog.dispose(); // Chiude la vecchia per ricaricare la lista
                mostraGestioneTornei();   // Riapre la lista aggiornata
            }
        }
    }

    /*
    * Genera il calendario di un torneo selezionato. Controlla che un torneo sia selezionato e che sia in stato "BOZZA" prima di procedere.
    * Chiama la facade per generare il calendario e aggiorna la view. Gestisce anche i casi di errore e mostra messaggi appropriati all'utente.
     * Se il calendario è già stato generato, mostra un messaggio di attenzione.
    */
    private void generaCalendario() {
        Torneo t = gestioneDialog.getTorneoSelezionato();
        // Controllo preventivo selezione
        if (t == null) { gestioneDialog.mostraAttenzione("Seleziona un torneo."); return; }
        
        // Controllo preventivo stato
        if (!"BOZZA".equals(t.getStato())) {
            gestioneDialog.mostraAttenzione("Il calendario è già stato generato per questo torneo.");
            return;
        }
        
        try {
            facade.generaCalendario(t, (Amministratore) app.getUtenteCorrente());
            gestioneDialog.mostraSuccesso("Calendario Generato con Successo!");
            gestioneDialog.dispose();
            mostraGestioneTornei();
        } catch (RegolaDiBusinessException e) {
            gestioneDialog.mostraAttenzione(e.getMessage());
        } catch (DataAccessException e) {
            gestioneDialog.mostraErroreNessuno(e.getMessage());
        }
    }

    /*
    * Mostra il dialog per visualizzare le partite di un torneo selezionato. Controlla che un torneo sia selezionato prima di procedere.
    * Dopo che l'utente chiude il dialog di visualizzazione, aggiorna la lista dei tornei per riflettere eventuali modifiche ai risultati. 
    * Gestisce anche i casi di errore e mostra messaggi appropriati all'utente.
    * Se non ci sono partite associate al torneo, mostra un messaggio di attenzione.
    * Nota: questo metodo è chiamato quando l'utente clicca su "Visualizza Partite/Risultati" nella gestione tornei.
    */
    private void mostraVisualizzaTorneoDialog() {
        Torneo t = gestioneDialog.getTorneoSelezionato();
        if (t == null) { gestioneDialog.mostraAttenzione("Seleziona un torneo."); return; }

        try {
            List<Partita> partite = facade.getPartiteTorneo(t.getIdTorneo());
            this.visualizzaDialog = new VisualizzaTorneoDialog(app.getMainFrame(), partite, t);

            visualizzaDialog.addInserisciRisultatoListener(_ -> inserisciRisultato(partite));
            
            // Apre la finestra modale (BLOCCANTE)
            visualizzaDialog.setVisible(true); 

            // ESEGUITO QUANDO LA FINESTRA SI CHIUDE
            // Recupera la lista aggiornata dal DB e aggiorna la View in background
            List<Torneo> torneiAggiornati = facade.getTuttiITornei();
            gestioneDialog.aggiornaLista(torneiAggiornati);

        } catch (DataAccessException e) {
            gestioneDialog.mostraErroreNessuno("Errore caricamento partite: " + e.getMessage());
        }
    }

    /*
    * Gestisce l'inserimento del risultato di una partita selezionata. Controlla che una partita sia selezionata e che le 
    * squadre siano definite.
    * Apre un dialog per inserire i gol delle squadre, chiama la facade per salvare il risultato e aggiorna la view. 
    * Gestisce anche i casi di errore e mostra messaggi appropriati all'utente.
    * Se le squadre non sono ancora definite per la partita, mostra un messaggio di attenzione.
    */
    private void inserisciRisultato(List<Partita> partite) {
        int riga = visualizzaDialog.getRigaPartitaSelezionata();
        if (riga == -1) {
            gestioneDialog.mostraAttenzione("Seleziona una partita dalla tabella.");
            return;
        }
        Partita p = partite.get(riga);

        if (p.getSquadraCasa() == null || p.getSquadraOspite() == null) {
            gestioneDialog.mostraAttenzione("Squadre non ancora definite per questa partita.");
            return;
        }

        String golCasaStr = visualizzaDialog.mostraGoal("Gol " + p.getSquadraCasa().getNomeSquadra() + ":");
        if (golCasaStr == null) return;
        String golOspiteStr = visualizzaDialog.mostraGoal("Gol " + p.getSquadraOspite().getNomeSquadra() + ":");
        if (golOspiteStr == null) return;
        
        try {
            int gc = Integer.parseInt(golCasaStr);
            int go = Integer.parseInt(golOspiteStr);
            if (gc < 0 || go < 0) throw new NumberFormatException();

            facade.inserisciRisultato(p.getIdPartita(), gc, go, (Amministratore) app.getUtenteCorrente());
            
            visualizzaDialog.mostraSuccesso("Risultato salvato.");
            visualizzaDialog.dispose();
            mostraVisualizzaTorneoDialog();
        } catch (NumberFormatException e) {
            gestioneDialog.mostraAttenzione("Inserire numeri validi positivi.");
        } catch (RegolaDiBusinessException e) {
            gestioneDialog.mostraAttenzione(e.getMessage());
        } catch (Exception e) {
            gestioneDialog.mostraErroreNessuno(e.getMessage());
        }
    }

    // Metodo per eliminare un torneo selezionato. Chiede conferma all'utente prima di procedere. Se confermato, 
    // chiama la facade per eliminare il torneo e aggiorna la view. Gestisce anche i casi di errore e mostra messaggi appropriati all'utente.
    private void eliminaTorneoSelezionato() {
        Torneo t = gestioneDialog.getTorneoSelezionato();
        if (t == null) {
            gestioneDialog.mostraAttenzione("Seleziona un torneo da eliminare.");
            return;
        }

        // Chiediamo conferma perché è un'azione distruttiva
        int conferma = gestioneDialog.mostraConferma(
            "Sei sicuro di voler eliminare il torneo '" + t.getNome() + "'?\n" +
            "Verranno eliminate tutte le partite e i risultati associati.\n" +
            "Questa azione non può essere annullata.");

        if (conferma == JOptionPane.YES_OPTION) {
            try {
                facade.eliminaTorneo(t.getIdTorneo());
                gestioneDialog.mostraSuccesso("Torneo eliminato correttamente.");
                
                // Chiudiamo e riapriamo per aggiornare la lista
                gestioneDialog.dispose();
                mostraGestioneTornei();
                
            } catch (DataAccessException e) {
                gestioneDialog.mostraErroreNessuno("Impossibile eliminare il torneo: " + e.getMessage());
            }
        }
    }
}