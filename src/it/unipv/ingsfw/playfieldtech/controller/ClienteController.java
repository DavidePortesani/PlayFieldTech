package it.unipv.ingsfw.playfieldtech.controller;

import it.unipv.ingsfw.playfieldtech.exceptions.*;
import it.unipv.ingsfw.playfieldtech.facade.PrenotazioneFacade;
import it.unipv.ingsfw.playfieldtech.facade.SquadraFacade;
import it.unipv.ingsfw.playfieldtech.facade.TorneoFacade;
import it.unipv.ingsfw.playfieldtech.model.*;
import it.unipv.ingsfw.playfieldtech.view.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.*;
import java.util.stream.Collectors;

public class ClienteController {
    private final AppController appController;
    private final MainFrame mainFrame;
    private final ClienteView view;
    private final PrenotazioneFacade prenotazioneFacade; 
    private final SquadraFacade squadraFacade;
    private final TorneoFacade torneoFacade;
    private final SquadraController squadraController;
    
    
    private LocalDate dataVisualizzata;
    private List<ImpiantoSportivo> campiCache;
    private List<FasciaOraria> fasceCache;

    public ClienteController(AppController app, MainFrame frame, ClienteView view) {
        this.appController = app;
        this.mainFrame = frame;
        this.view = view;
        this.prenotazioneFacade = app.getPrenotazioneFacade();
        this.torneoFacade = app.getTorneoFacade();
        this.squadraFacade = app.getSquadraFacade();
        this.squadraController = new SquadraController(app);
        initListeners();
    }

    private void initListeners() {
        view.addLogoutListener(_ -> appController.eseguiLogout());
        view.addPrenotaListener(_ -> eseguiPrenotazione());
        view.addStoricoListener(_ -> mostraStorico());
        view.addGiornoPrecedenteListener(_ -> { dataVisualizzata = dataVisualizzata.minusDays(1); aggiornaCalendarioView(); });
        view.addGiornoSuccessivoListener(_ -> { dataVisualizzata = dataVisualizzata.plusDays(1); aggiornaCalendarioView(); });
        view.addGestisciSquadreListener(_ -> squadraController.mostraGestioneSquadre());
        view.addVisualizzaTorneiListener(_ -> visualizzaMieiTornei());

        // Il controller si mette in ascolto tramite una lambda.
        // Quando il PrenotazioneFacade lancia "notifyObservers()", questo blocco viene eseguito
        appController.getPrenotazioneFacade().addObserver(() -> {
            // SwingUtilities assicura che l'aggiornamento visivo avvenga 
            SwingUtilities.invokeLater(() -> {
                aggiornaCalendarioView();
            });
        });
    }

    public void onLogin() {
        view.setWelcomeMessage(appController.getUtenteCorrente().getNome());
        dataVisualizzata = LocalDate.now();
        aggiornaCalendarioView();
    }

    public void aggiornaCalendarioView() {
        if (dataVisualizzata == null || !(appController.getUtenteCorrente() instanceof Cliente)) {
            return;
        }
        try {
            campiCache = prenotazioneFacade.getTuttiImpianti();
            List<FasciaOraria> tutteFasce = prenotazioneFacade.getTutteFasceOrarie();
            List<Prenotazione> prenotazioni = prenotazioneFacade.getPrenotazioniGiorno(dataVisualizzata);
            Map<Integer, List<Integer>> disponibilita = prenotazioneFacade.getMappaDisponibilita();

            DayOfWeek giorno = dataVisualizzata.getDayOfWeek();
            fasceCache = tutteFasce.stream().filter(f -> f.getGiornoSettimana() == giorno).collect(Collectors.toList());

            view.setDataLabel(dataVisualizzata.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.FULL)));
            view.setGiornoPrecedenteButtonEnabled(!dataVisualizzata.isEqual(LocalDate.now()));

            Vector<String> colNames = new Vector<>();
            colNames.add("Orario");
            for (ImpiantoSportivo c : campiCache) colNames.add(c.getNome());

            DefaultTableModel model = new DefaultTableModel(colNames, 0) {
                 @Override public boolean isCellEditable(int r, int c) { return false; }
            };

            for (FasciaOraria f : fasceCache) {
                Vector<Object> row = new Vector<>();
                row.add(f.getOraInizio() + " - " + f.getOraFine());
                for (ImpiantoSportivo c : campiCache) {
                    String val = "";
                    List<Integer> validi = disponibilita.get(c.getIdCampo());
                    if (validi != null && validi.contains(f.getIdFascia())) {
                        boolean occupato = prenotazioni.stream().anyMatch(p -> p.getFkCampo() == c.getIdCampo() && p.getFkFascia() == f.getIdFascia());
                        val = occupato ? "OCCUPATO" : "Libero";
                    }
                    row.add(val);
                }
                model.addRow(row);
            }
            view.getCalendarioTable().setModel(model);
        } catch (DataAccessException e) {
            mainFrame.mostraErrore("Errore caricamento calendario: " + e.getMessage());
        }
    }

    private void eseguiPrenotazione() {
        JTable table = view.getCalendarioTable();
        int r = table.getSelectedRow();
        int c = table.getSelectedColumn();

        if (r == -1 || c < 1 || !"Libero".equals(table.getValueAt(r, c))) {
            mainFrame.mostraErrore("Seleziona uno slot libero.");
            return;
        }

        try {
            FasciaOraria fascia = fasceCache.get(r);
            ImpiantoSportivo campo = campiCache.get(c - 1);
            
            Prenotazione bozza = new Prenotazione(0, dataVisualizzata, appController.getUtenteCorrente(), campo, fascia);
            double prezzo = prenotazioneFacade.calcolaPrezzo(bozza); // Calcolo prezzo via Facade

            List<Squadra> squadre = squadraFacade.getTutteSquadre();
            if (squadre.size() < 2) {
                mainFrame.mostraErrore("Servono almeno 2 squadre nel sistema.");
                return;
            }

            PrenotazioneDialog dialog = new PrenotazioneDialog(mainFrame, prezzo, squadre);
            dialog.setVisible(true);

            if (dialog.isConfermato()) {
                Squadra casa = dialog.getSquadraCasa();
                Squadra ospite = dialog.getSquadraOspite();
                if (casa.equals(ospite)) throw new RegolaDiBusinessException("Le squadre devono essere diverse.");

                // Controllo membri (semplificato, la logica complessa potrebbe andare nella Facade se necessario)
                List<Cliente> mCasa = squadraFacade.getMembriSquadra(casa.getIdSquadra());
                List<Cliente> mOspite = squadraFacade.getMembriSquadra(ospite.getIdSquadra());
                if (mCasa.size() < 5 || mOspite.size() < 5) throw new RegolaDiBusinessException("Squadre incomplete (min 5 giocatori).");

                int pay = mainFrame.mostraConferma("Confermi pagamento di " + prezzo + "€?");
                if (pay == JOptionPane.YES_OPTION) {
                    List<Cliente> giocatoriPartita = new ArrayList<>();
                    giocatoriPartita.addAll(mCasa.subList(0, 5));
                    giocatoriPartita.addAll(mOspite.subList(0, 5));
                    bozza.setGiocatori(giocatoriPartita);
                    
                    prenotazioneFacade.confermaPrenotazione(bozza, casa, ospite, appController.getUtenteCorrente());
            
                    aggiornaCalendarioView(); // Aggiorna la vista dopo la prenotazione
                    // L'aggiornamento avviene via Observer
                }
            }
        }catch (PermessoNegatoException ex) {
            mainFrame.mostraErrore(ex.getMessage());
        
        } catch (RegolaDiBusinessException e) {
            mainFrame.mostraErrore(e.getMessage());
        } catch (DataAccessException e) {
            mainFrame.mostraErrore("Errore tecnico: " + e.getMessage());
        }
        
    }

    private void mostraStorico() {
        try {
            List<Prenotazione> storico = prenotazioneFacade.getStoricoUtente(appController.getUtenteCorrente().getIdUtente());
            new StoricoPrenotazioniDialog(mainFrame, storico).setVisible(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void visualizzaMieiTornei() {
        try {
            int idUtente = appController.getUtenteCorrente().getIdUtente();
            List<Torneo> mieiTornei = torneoFacade.getTorneiDiUtente(idUtente);

            if (mieiTornei.isEmpty()) {
                // Messaggio aggiornato
                mainFrame.mostraErrore("Al momento non partecipi a nessun torneo ATTIVO.\n(I tornei in bozza o conclusi non vengono mostrati).");
                return;
            }
    
            Torneo torneoDaVisualizzare = null;

            if (mieiTornei.size() == 1) {
                torneoDaVisualizzare = mieiTornei.get(0);
            } else {
                SelezionaTorneoDialog dialog = new SelezionaTorneoDialog(mainFrame, mieiTornei);
                dialog.setVisible(true);
                if (dialog.isConfermato()) {
                    torneoDaVisualizzare = dialog.getTorneoSelezionato();
                }
            }

            if (torneoDaVisualizzare != null) {
                final int idTorneo = torneoDaVisualizzare.getIdTorneo(); // Final per uso nella lambda
                List<Partita> partite = torneoFacade.getPartiteTorneo(torneoDaVisualizzare.getIdTorneo());
                VisualizzaTorneoDialog viewDialog = new VisualizzaTorneoDialog(mainFrame, partite, torneoDaVisualizzare);
                viewDialog.abilitaModalitaSolaLettura();
                // Definizione dell'Observer per aggiornamento in tempo reale
                it.unipv.ingsfw.playfieldtech.observer.Observer torneoObserver = () -> {
                    //update() viene chiamato quando il TorneoFacade chiama notifyObservers() dopo una modifica al torneo 
                    // (es. risultato partita inserito)
                    SwingUtilities.invokeLater(() -> {
                        try {
                            List<Partita> nuovePartite = torneoFacade.getPartiteTorneo(idTorneo);
                            viewDialog.aggiornaDati(nuovePartite);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    });
                };

                // Registrazione Observer
                torneoFacade.addObserver(torneoObserver);

                // Gestione chiusura finestra (importante rimuovere l'observer!)
                viewDialog.addWindowListener(new java.awt.event.WindowAdapter() {
                    @Override
                    public void windowClosed(java.awt.event.WindowEvent windowEvent) {
                        torneoFacade.removeObserver(torneoObserver);
                    }
                });
                viewDialog.setVisible(true);
            }

        } catch (DataAccessException e) {
            mainFrame.mostraErrore("Errore recupero tornei: " + e.getMessage());
        }
    }
}