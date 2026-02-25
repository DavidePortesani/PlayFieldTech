package it.unipv.ingsfw.playfieldtech.view;

import it.unipv.ingsfw.playfieldtech.model.Prenotazione;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
/**
 * Finestra di dialogo per visualizzare lo storico delle prenotazioni di un cliente.
 * Accessibile solo dal cliente stesso.
 */
public class StoricoPrenotazioniDialog extends JDialog {

    public StoricoPrenotazioniDialog(Frame owner, List<Prenotazione> storico) {
        super(owner, "Le tue Prenotazioni", true);
        setSize(600, 400);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout());

        String[] columnNames = {"Data", "Impianto", "Orario", "Squadra A", "Squadra B"};
        DefaultTableModel tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            // Rende la tabella non editabile
            public boolean isCellEditable(int row, int column) { return false; }
        };
        // Popola la tabella con i dati delle prenotazioni
        for (Prenotazione p : storico) {
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
        JTable storicoTable = new JTable(tableModel);
        add(new JScrollPane(storicoTable), BorderLayout.CENTER);

        JButton chiudiButton = new JButton("Chiudi");
        chiudiButton.addActionListener(_ -> setVisible(false));
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(chiudiButton);
        add(buttonPanel, BorderLayout.SOUTH);
    }
}