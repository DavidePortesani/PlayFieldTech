package it.unipv.ingsfw.playfieldtech.view;

import it.unipv.ingsfw.playfieldtech.model.Squadra;
import it.unipv.ingsfw.playfieldtech.controller.AdminController;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/*
 * Dialog per la gestione delle squadre da parte dell'amministratore.
 * Permette di visualizzare tutte le squadre esistenti e rimuoverle.
 */
public class GestioneSquadreAdminDialog extends JDialog {
    private JTable table;
    private DefaultTableModel tableModel;
    private AdminController controller;

    public GestioneSquadreAdminDialog(Frame owner, List<Squadra> squadre, AdminController controller) {
        super(owner, "Visualizza e Rimuovi Squadre", true);
        this.controller = controller;
        setLayout(new BorderLayout());

        tableModel = new DefaultTableModel(new Object[]{"ID", "Nome Squadra"}, 0);
        for (Squadra s : squadre) {
            tableModel.addRow(new Object[]{s.getIdSquadra(), s.getNomeSquadra()}); // Popola la tabella con le squadre recuperate dalla Facade
        }
        table = new JTable(tableModel);
        
        JButton btnDelete = new JButton("Rimuovi Squadra Selezionata");
        // Aggiungi l'ActionListener per il pulsante di eliminazione
        btnDelete.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row != -1) {
                int id = (int) tableModel.getValueAt(row, 0);
                int confirm = JOptionPane.showConfirmDialog(this, "Sei sicuro di voler eliminare la squadra?");
                if (confirm == JOptionPane.YES_OPTION) {
                    controller.rimuoviSquadra(id);
                    tableModel.removeRow(row);
                }
            }
        });

        add(new JScrollPane(table), BorderLayout.CENTER);
        add(btnDelete, BorderLayout.SOUTH);
        
        setSize(500, 400);
        setLocationRelativeTo(owner);
    }
}