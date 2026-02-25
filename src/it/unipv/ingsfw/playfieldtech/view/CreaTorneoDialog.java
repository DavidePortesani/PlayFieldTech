package it.unipv.ingsfw.playfieldtech.view;

import it.unipv.ingsfw.playfieldtech.model.Squadra;
import javax.swing.*;
import java.awt.*;
import java.util.List;

/*
 * Dialog per la creazione di un nuovo torneo.
 * Permette di inserire il nome del torneo e selezionare le squadre partecipanti.
 */
public class CreaTorneoDialog extends JDialog {

    private JTextField nomeTorneoField;
    private JList<Squadra> squadreList;
    private DefaultListModel<Squadra> squadreListModel;
    private JButton confermaButton;
    private JButton annullaButton;
    private boolean confermato = false;

    public CreaTorneoDialog(Frame owner, List<Squadra> tutteLeSquadre) {
        super(owner, "Crea Nuovo Torneo", true);
        setSize(500, 600);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout(10, 10));

        // Pannello Superiore: Dati del torneo
        JPanel detailsPanel = new JPanel(new GridLayout(1, 2, 10, 10)); // 1 riga, 2 colonne con spazio tra i componenti
        detailsPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));// Aggiunge padding al pannello dei dettagli
        nomeTorneoField = new JTextField();
        
        detailsPanel.add(new JLabel("Nome Torneo:"));
        detailsPanel.add(nomeTorneoField);
   
        add(detailsPanel, BorderLayout.NORTH);

        // Pannello Centrale: Selezione Squadre
        squadreListModel = new DefaultListModel<>();
        for (Squadra s : tutteLeSquadre) {
            squadreListModel.addElement(s);
        }
        squadreList = new JList<>(squadreListModel); // Popola la JList con le squadre recuperate dalla Facade
        squadreList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION); // Permette di selezionare più squadre
        JScrollPane scrollPane = new JScrollPane(squadreList); // Aggiungiamo un bordo con titolo al pannello di selezione
        scrollPane.setBorder(BorderFactory.createTitledBorder("Seleziona Squadre Partecipanti (4)")); // Titolo del bordo
        add(scrollPane, BorderLayout.CENTER);

        // Pannello Inferiore: Pulsanti
        JPanel buttonPanel = new JPanel();
        confermaButton = new JButton("Conferma");
        annullaButton = new JButton("Annulla");
        buttonPanel.add(confermaButton);
        buttonPanel.add(annullaButton);
        add(buttonPanel, BorderLayout.SOUTH);

        // Listener
        confermaButton.addActionListener(_ -> {
            confermato = true;
            setVisible(false);
        });
        annullaButton.addActionListener(_ -> setVisible(false));
    }
    
    public boolean isConfermato() {
        return confermato;
    }
    
    public String getNomeTorneo() {
        return nomeTorneoField.getText();
    }
    
    public List<Squadra> getSquadreSelezionate() {
        return squadreList.getSelectedValuesList(); // Restituisce la lista delle squadre selezionate
    }
}