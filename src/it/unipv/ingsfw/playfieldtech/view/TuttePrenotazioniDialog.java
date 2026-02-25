package it.unipv.ingsfw.playfieldtech.view;
import it.unipv.ingsfw.playfieldtech.model.Prenotazione;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Finestra di dialogo per visualizzare tutte le prenotazioni esistenti.
 * Accessibile solo dall'amministratore.
 */
public class TuttePrenotazioniDialog extends JDialog {

    public TuttePrenotazioniDialog(Frame owner, List<Prenotazione> prenotazioni) {
        super(owner, "Elenco di Tutte le Prenotazioni", true);
        setSize(800, 500);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout());

        String[] columnNames = {"Data", "Impianto", "Orario", "Squadra A", "Squadra B"};
        DefaultTableModel tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            // Rende la tabella non editabile
            public boolean isCellEditable(int row, int column) { return false; } 
        };
        // Popola la tabella con i dati delle prenotazioni
        for (Prenotazione p : prenotazioni) {
            Object[] row = new Object[]{
                p.getData(),
                p.getImpianto().getNome(),
                p.getFasciaOraria().getOraInizio() + " - " + p.getFasciaOraria().getOraFine(),
                p.getNomeSquadraA(),
                p.getNomeSquadraB()
            };
            tableModel.addRow(row);
        }
        // Crea la tabella e aggiungila alla finestra
        JTable prenotazioniTable = new JTable(tableModel);
        add(new JScrollPane(prenotazioniTable), BorderLayout.CENTER);

        JButton chiudiButton = new JButton("Chiudi");
        chiudiButton.addActionListener(_ -> setVisible(false));
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(chiudiButton);
        add(buttonPanel, BorderLayout.SOUTH);
    }
}