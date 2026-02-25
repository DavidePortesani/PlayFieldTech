// Dichiarazione del package corretta
package it.unipv.ingsfw.playfieldtech.view;

// Import necessari dai tuoi package
import it.unipv.ingsfw.playfieldtech.model.Partita;
import it.unipv.ingsfw.playfieldtech.model.Torneo;

// Import standard di Java Swing
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.List;

/**
 * Finestra di dialogo per l'amministratore per visualizzare il calendario
 * delle partite di un torneo e inserire i risultati.
 */
public class VisualizzaTorneoDialog extends JDialog {

    private JTable partiteTable;
    private DefaultTableModel tableModel;
    private JButton inserisciRisultatoButton;
    private JButton chiudiButton;

    public VisualizzaTorneoDialog(Frame owner, List<Partita> partite, Torneo torneo) {
        // Titolo della finestra aggiornato con il nome del torneo
        super(owner, "Calendario e Risultati: " + torneo.getNome(), true);
        setSize(800, 500); 
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout(10, 10));
        getRootPane().setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Tabella delle partite
        String[] columnNames = {"Turno", "Data", "Campo", "Orario", "Squadra Casa", "Squadra Ospite", "Risultato"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {//
                return false; // Rende la tabella non editabile
            }
        };

        // Popola la tabella con i dati delle partite
        for (Partita p : partite) {
            
            // Blocco di sicurezza per gestire i dati NULL
            String data = "Da definire";
            String campo = "Da assegnare";
            String orario = "-";
            
            // Controlla se la prenotazione esiste prima di accedervi
            if (p.getPrenotazione() != null) {
                data = p.getPrenotazione().getData().toString();
                campo = p.getPrenotazione().getImpianto().getNome();
                orario = p.getPrenotazione().getFasciaOraria().getOraInizio().toString();
            }

            // Controlla se le squadre esistono
            String squadraCasa;
            if (p.getSquadraCasa() != null) {
                squadraCasa = p.getSquadraCasa().getNomeSquadra();
            } else {
                squadraCasa = "Da definire";
            }

            String squadraOspite;
            if (p.getSquadraOspite() != null) {
                squadraOspite = p.getSquadraOspite().getNomeSquadra();
            } else {
                squadraOspite = "Da definire";
            }

            // Controlla se il risultato esiste
            String risultato = " - ";
            if (p.getRisultatoCasa() != null && p.getRisultatoOspite() != null) {
                risultato = p.getRisultatoCasa() + " - " + p.getRisultatoOspite();
            }
            // Fine blocco di sicurezza
            
            Object[] row = new Object[]{
                p.getTurnoString(), // Metodo che restituisce "Finale", "Semifinale", ecc.
                data,
                campo,
                orario,
                squadraCasa,
                squadraOspite,
                risultato
            };
            tableModel.addRow(row);
        }

        partiteTable = new JTable(tableModel);
        partiteTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        partiteTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        add(new JScrollPane(partiteTable), BorderLayout.CENTER);

        // Pannello Pulsanti
        JPanel buttonPanel = new JPanel();
        inserisciRisultatoButton = new JButton("Inserisci Risultato");
        chiudiButton = new JButton("Chiudi");
        
        buttonPanel.add(inserisciRisultatoButton);
        buttonPanel.add(chiudiButton);
        
        add(buttonPanel, BorderLayout.SOUTH);
        
        // Listener interno solo per il pulsante "Chiudi"
        chiudiButton.addActionListener(_ -> setVisible(false));
    }

    // Metodi Pubblici per il Controller
    
    /**
     * Restituisce l'indice della riga selezionata nella tabella.
     * @return l'indice della riga, o -1 se nessuna riga è selezionata.
     */
    public int getRigaPartitaSelezionata() {
        return partiteTable.getSelectedRow();
    }
    
    /**
     * Collega un'azione (definita nel Controller) al pulsante "Inserisci Risultato".
     * @param listener L'ActionListener da eseguire.
     */
    public void addInserisciRisultatoListener(ActionListener listener) {
        inserisciRisultatoButton.addActionListener(listener);
    }
 
    public void abilitaModalitaSolaLettura() {
        inserisciRisultatoButton.setVisible(false);
        //prenotaPartitaButton.setVisible(false);
        //this.setTitle(this.getTitle() + " [Vista Cliente]");
    }

    
    private String getStringaTurno(int turno) {
        if (turno == 1) return "Finale";
        if (turno == 2) return "Semifinale";
        return "Turno " + turno;
    }

    /**
     * Aggiorna la tabella con una nuova lista di partite.
     * Mantiene la finestra aperta.
     */
    public void aggiornaDati(List<Partita> nuovePartite) {
        // Svuota il modello attuale
        tableModel.setRowCount(0);

        // Ripopola
        for (Partita p : nuovePartite) {
            String data = "Da definire";
            String campo = "Da assegnare";
            String orario = "-";
            
            if (p.getPrenotazione() != null) {
                data = p.getPrenotazione().getData().toString();
                campo = p.getPrenotazione().getImpianto().getNome();
                orario = p.getPrenotazione().getFasciaOraria().getOraInizio().toString();
            }

            String squadraCasa = (p.getSquadraCasa() != null) ? p.getSquadraCasa().getNomeSquadra() : "TBD";
            String squadraOspite = (p.getSquadraOspite() != null) ? p.getSquadraOspite().getNomeSquadra() : "TBD";

            String risultato = " - ";
            if (p.getRisultatoCasa() != null && p.getRisultatoOspite() != null) {
                risultato = p.getRisultatoCasa() + " - " + p.getRisultatoOspite();
            }
            
            Object[] row = {
                getStringaTurno(p.getTurno()),
                data,
                campo,
                orario,
                squadraCasa,
                squadraOspite,
                risultato
            };
            tableModel.addRow(row);
        }
    }

    public String mostraGoal(String messaggio){
        return JOptionPane.showInputDialog(messaggio);
    }
    public void mostraErrore(String messaggio) {
        JOptionPane.showMessageDialog(this, messaggio, "Errore", JOptionPane.ERROR_MESSAGE);
    }
    public void mostraSuccesso(String messaggio) {
        JOptionPane.showMessageDialog(this, messaggio, "Successo", JOptionPane.INFORMATION_MESSAGE);
    }

    public String inputMessaggio(String messaggio) {
        return JOptionPane.showInputDialog(this, messaggio);
    }   
}