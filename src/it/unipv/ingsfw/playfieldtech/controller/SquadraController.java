package it.unipv.ingsfw.playfieldtech.controller;

import it.unipv.ingsfw.playfieldtech.exceptions.DataAccessException;
import it.unipv.ingsfw.playfieldtech.exceptions.RegolaDiBusinessException;
import it.unipv.ingsfw.playfieldtech.facade.SquadraFacade;
import it.unipv.ingsfw.playfieldtech.model.Cliente;
import it.unipv.ingsfw.playfieldtech.model.Squadra;
import it.unipv.ingsfw.playfieldtech.view.GestioneSquadreDialog;

import java.util.ArrayList;
import java.util.List;

/*
* Controller specializzato per la gestione delle squadre. Si occupa di orchestrare le operazioni legate alle squadre,
* delegando la logica al SquadraFacade e aggiornando le view specifiche delle squadre. Gestisce la creazione di squadre, 
* l'aggiunta e la rimozione di membri, e l'aggiornamento delle liste dei membri nelle view.
* Utilizza la PrenotazioneFacade per recuperare i dati necessari alla visualizzazione e alla gestione delle squadre, 
* mantenendo così una separazione chiara tra la logica di business (gestita dalle Facade) e la logica di presentazione (gestita dai Controller).
*/
public class SquadraController {
    private final AppController app;
    private final SquadraFacade squadraFacade; 
    private GestioneSquadreDialog dialog;

    public SquadraController(AppController app) {
        this.app = app;
        this.squadraFacade = app.getSquadraFacade();  
    }
    /*
    * Mostra il dialog per la gestione delle squadre dell'utente corrente. Recupera le squadre di cui l'utente 
    * è membro tramite la PrenotazioneFacade,
    * e aggiorna la view con le squadre e i membri. Gestisce anche i casi di errore e mostra messaggi appropriati all'utente. 
    * Permette di creare nuove squadre, aggiungere membri e rimuovere membri, delegando la logica alla SquadraFacade.
    * Se non ci sono squadre, mostra un messaggio di attenzione.
    * Nota: questo metodo è chiamato quando l'utente clicca su "Gestione Squadre" nella view dell'Admin.
    */
    public void mostraGestioneSquadre() {
        try {
            List<Squadra> squadre = squadraFacade.getSquadreDiUtente(app.getUtenteCorrente().getIdUtente());
            
            this.dialog = new GestioneSquadreDialog(app.getMainFrame(), squadre);
            // Definizione di un Runnable per aggiornare la lista dei membri quando si seleziona una squadra diversa
            // Questo Runnable viene chiamato ogni volta che l'utente cambia la selezione della squadra nella combo box
            Runnable updateMembriList = () -> { 
                try {
                    Squadra s = (Squadra) dialog.getSquadreComboBox().getSelectedItem(); 
                    if (s != null) {
                        
                        List<Cliente> membri = squadraFacade.getMembriSquadra(s.getIdSquadra());
                        
                        dialog.refreshMembri(membri);
                        boolean isCapitano = app.getUtenteCorrente().getIdUtente() == s.getIdCapitano();
                        dialog.setAzioniCapitanoEnabled(isCapitano);
                    } else {
                        dialog.refreshMembri(new ArrayList<>());
                        dialog.setAzioniCapitanoEnabled(false);
                    }
                } catch (DataAccessException e) {
                    dialog.mostraErrore("Errore DB: " + e.getMessage());
                }
            };

            dialog.getSquadreComboBox().addActionListener(_ -> updateMembriList.run());
            dialog.addCreaSquadraListener(_ -> creaSquadra());
            dialog.addAggiungiMembroListener(_ -> aggiungiMembro());
            dialog.addRimuoviMembroListener(_ -> rimuoviMembro());

            updateMembriList.run();
            dialog.setVisible(true);
        } catch (DataAccessException e) {
            app.getMainFrame().mostraErrore( "Errore DB: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /*
    * Recupera i membri di una squadra tramite la SquadraFacade. Gestisce i casi di errore e 
    * lancia un'eccezione se si verifica un problema di accesso ai dati.
    * Nota: questo metodo è utilizzato internamente per aggiornare la lista dei membri nella 
    * view dopo operazioni di aggiunta o rimozione, e per visualizzare i membri quando si seleziona una squadra. 
    */
    private void creaSquadra() {
        String nomeSquadra = dialog.inputMessaggio("Nome della nuova squadra:");
        if (nomeSquadra == null) return; 
        
        try {
            // Usiamo ancora la facade per la logica di business (scrittura)
            squadraFacade.creaSquadra(nomeSquadra, (Cliente) app.getUtenteCorrente());
            
            dialog.mostraSuccesso("Squadra creata!");
            // Chiude e riapre la finestra per aggiornare la lista delle squadre, così da mostrare subito la nuova squadra creata
            dialog.dispose();
            mostraGestioneSquadre(); 
        } catch (RegolaDiBusinessException e) { 
            dialog.mostraErrore(e.getMessage());
        } catch (DataAccessException e) {
            dialog.mostraErrore("Errore Database: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /*
    * Gestisce l'aggiunta di un membro a una squadra selezionata. Controlla che una squadra sia selezionata e che il nome utente 
    * del nuovo membro non sia vuoto.
    * Chiama la SquadraFacade per aggiungere il membro e aggiorna la lista dei membri nella view. Gestisce anche i casi di 
    * errore e mostra messaggi appropriati all'utente.
    * Nota: questo metodo è chiamato quando l'utente clicca su "Aggiungi Membro" nella gestione squadre. Dopo l'aggiunta, 
    * aggiorna la lista dei membri per riflettere la modifica.
    * Se non è selezionata una squadra o se il nome utente è vuoto, non fa nulla.
    * Se l'aggiunta ha successo, mostra un messaggio di successo e aggiorna la lista dei membri. 
    * Se si verifica un errore di business (es. utente già membro, utente non esistente, ecc.), mostra il messaggio 
    * specifico dell'errore. 
    */
    private void aggiungiMembro() {
        Squadra s = (Squadra) dialog.getSquadreComboBox().getSelectedItem();
        String username = dialog.getNuovoMembroUsername();
        // Controlla che sia selezionata una squadra e che il nome utente non sia vuoto
        if (s == null || username.trim().isEmpty()) return; 

        try {
            squadraFacade.aggiungiMembro(s, username, (Cliente) app.getUtenteCorrente());
            dialog.mostraSuccesso("Membro aggiunto!");
            dialog.clearNuovoMembroField();
            
            // Uso facade per aggiornare la lista
            List<Cliente> membri = squadraFacade.getMembriSquadra(s.getIdSquadra());
            dialog.refreshMembri(membri);
            
        } catch (RegolaDiBusinessException e) {
            dialog.mostraErrore(e.getMessage());
        } catch (DataAccessException e) {
            dialog.mostraErrore("Errore Database: " + e.getMessage());
            
        }
    }

    /*
    * Gestisce la rimozione di un membro da una squadra selezionata. Controlla che una squadra sia selezionata e che un membro 
    * sia selezionato nella lista.
    * Chiama la SquadraFacade per rimuovere il membro e aggiorna la lista dei membri nella view. 
    * Gestisce anche i casi di errore e mostra messaggi appropriati all'utente.
    * Nota: questo metodo è chiamato quando l'utente clicca su "Rimuovi Membro" nella gestione squadre. 
    * Dopo la rimozione, aggiorna la lista dei membri per riflettere la modifica.
    * Se non è selezionata una squadra o se nessun membro è selezionato, non fa nulla.
    */
    private void rimuoviMembro() {
        Squadra s = (Squadra) dialog.getSquadreComboBox().getSelectedItem();
        Cliente c = dialog.getMembriList().getSelectedValue();
        if (s == null || c == null) return;

        try {
            squadraFacade.rimuoviMembro(s, c, (Cliente) app.getUtenteCorrente());
            dialog.mostraSuccesso("Membro rimosso!");
            
            // Uso facade per aggiornare la lista
            List<Cliente> membri = squadraFacade.getMembriSquadra(s.getIdSquadra());
            dialog.refreshMembri(membri);
            
        } catch (RegolaDiBusinessException e) {
            dialog.mostraErrore(e.getMessage());
        } catch (DataAccessException e) {
            dialog.mostraErrore("Errore Database: " + e.getMessage());
            e.printStackTrace();
        }
    }
}