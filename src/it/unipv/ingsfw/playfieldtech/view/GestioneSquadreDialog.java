package it.unipv.ingsfw.playfieldtech.view;

import it.unipv.ingsfw.playfieldtech.model.Cliente;
import it.unipv.ingsfw.playfieldtech.model.Squadra;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.List;

/**
 * Finestra di dialogo per la gestione completa delle squadre di un utente:
 * creazione, selezione e gestione dei membri.
 */
public class GestioneSquadreDialog extends JDialog {
    private JComboBox<Squadra> squadreComboBox;
    private JList<Cliente> membriList;
    private DefaultListModel<Cliente> membriListModel;
    private JButton creaSquadraButton;
    private JButton aggiungiMembroButton;
    private JButton rimuoviMembroButton;
    private JTextField nuovoMembroField;

    // Il costruttore che il controller sta cercando
    public GestioneSquadreDialog(Frame owner, List<Squadra> squadre) {
        super(owner, "Gestione Squadre", true);
        setSize(600, 400);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout(10, 10));

        // Pannello Superiore: Selezione squadra e creazione
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        topPanel.add(new JLabel("Seleziona una squadra:"));
        squadreComboBox = new JComboBox<>(squadre.toArray(new Squadra[0]));
        creaSquadraButton = new JButton("Crea Nuova Squadra");
        topPanel.add(squadreComboBox);
        topPanel.add(creaSquadraButton);

        // Pannello Centrale: Elenco membri
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBorder(BorderFactory.createTitledBorder("Membri della Squadra"));
        membriListModel = new DefaultListModel<>();
        membriList = new JList<>(membriListModel);
        centerPanel.add(new JScrollPane(membriList), BorderLayout.CENTER);

        // Pannello Inferiore: Azioni sui membri
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        actionPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        nuovoMembroField = new JTextField(15);
        aggiungiMembroButton = new JButton("Aggiungi Membro");
        rimuoviMembroButton = new JButton("Rimuovi Selezionato");
        actionPanel.add(new JLabel("Username:"));
        actionPanel.add(nuovoMembroField);
        actionPanel.add(aggiungiMembroButton);
        actionPanel.add(rimuoviMembroButton);

        add(topPanel, BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);
        add(actionPanel, BorderLayout.SOUTH);
    }

    public JComboBox<Squadra> getSquadreComboBox() {
        return squadreComboBox;
    }

    public JList<Cliente> getMembriList() {
        return membriList;
    }

    public String getNuovoMembroUsername() {
        return nuovoMembroField.getText();
    }

    public void clearNuovoMembroField() {
        nuovoMembroField.setText("");
    }
    
    public void addCreaSquadraListener(ActionListener listener) {
        creaSquadraButton.addActionListener(listener);
    }

    public void addAggiungiMembroListener(ActionListener listener) {
        aggiungiMembroButton.addActionListener(listener);
    }

    public void addRimuoviMembroListener(ActionListener listener) {
        rimuoviMembroButton.addActionListener(listener);
    }
    
    public void refreshMembri(List<Cliente> membri) {
        membriListModel.clear();
        for (Cliente c : membri) {
            membriListModel.addElement(c);
        }
    }
 
    /**
     * Abilita o disabilita i controlli di gestione della squadra (aggiunta/rimozione).
     * Da chiamare quando l'utente non è il capitano della squadra selezionata.
     * @param enabled true per abilitare, false per disabilitare.
     */
    public void setAzioniCapitanoEnabled(boolean enabled) {
        nuovoMembroField.setEditable(enabled);
        aggiungiMembroButton.setEnabled(enabled);
        rimuoviMembroButton.setEnabled(enabled);
        // Se disabilitiamo, puliamo il campo di input per evitare confusione all'utente così da non lasciare un username 
        // scritto senza possibilità di usarlo. Se invece è abilitato, l'utente può scrivere un nuovo username.
        if (!enabled) {
            nuovoMembroField.setText(""); // Pulisce il campo per evitare confusione
        }
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