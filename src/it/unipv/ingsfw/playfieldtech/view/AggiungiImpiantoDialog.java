package it.unipv.ingsfw.playfieldtech.view;

import it.unipv.ingsfw.playfieldtech.model.FasciaOraria;
import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Finestra di dialogo per l'inserimento di un nuovo impianto e la selezione
 * delle fasce orarie disponibili.
 */
public class AggiungiImpiantoDialog extends JDialog {
    private JTextField nomeField;
    private JButton confermaButton;
    private JButton annullaButton;
    private boolean confermato = false;

    // Lista di checkbox per le fasce orarie
    private List<JCheckBox> fasceCheckBoxes;
    private List<FasciaOraria> fasceDisponibili;

    public AggiungiImpiantoDialog(Frame owner, List<FasciaOraria> fasce) {
        super(owner, "Aggiungi Nuovo Impianto", true);
        setLayout(new BorderLayout(10, 10));
        setSize(400, 500);
        setLocationRelativeTo(owner);

        this.fasceDisponibili = fasce;
        this.fasceCheckBoxes = new ArrayList<>();

        // Pannello superiore per il nome dell'impianto
        JPanel nomePanel = new JPanel(new BorderLayout(5, 5));
        nomePanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        nomePanel.add(new JLabel("Nome del nuovo campo:"), BorderLayout.WEST);
        nomeField = new JTextField();
        nomePanel.add(nomeField, BorderLayout.CENTER);

        // Pannello centrale con la lista di checkbox per le fasce orarie
        JPanel fascePanel = new JPanel(new GridLayout(0, 2, 5, 5)); // 2 colonne
        fascePanel.setBorder(BorderFactory.createTitledBorder("Seleziona Fasce Orarie Disponibili"));
        for (FasciaOraria fascia : fasceDisponibili) {
            String label = fascia.getGiornoSettimana() + " " + fascia.getOraInizio() + "-" + fascia.getOraFine();
            JCheckBox checkBox = new JCheckBox(label);
            fasceCheckBoxes.add(checkBox);
            fascePanel.add(checkBox);
        }

        // Pannello inferiore con i pulsanti
        JPanel buttonPanel = new JPanel();
        confermaButton = new JButton("Conferma");
        annullaButton = new JButton("Annulla");
        buttonPanel.add(confermaButton);
        buttonPanel.add(annullaButton);

        confermaButton.addActionListener(_ -> {
            confermato = true;
            setVisible(false);
        });
        annullaButton.addActionListener(_ -> setVisible(false));

        add(nomePanel, BorderLayout.NORTH);
        add(new JScrollPane(fascePanel), BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    public String getNomeImpianto() {
        return nomeField.getText();
    }

    public boolean isConfermato() {
        return confermato;
    }

    /**
     * Restituisce la lista degli oggetti FasciaOraria selezionati dall'utente.
     */
    public List<FasciaOraria> getSelectedFasce() {
        List<FasciaOraria> selected = new ArrayList<>();
        for (int i = 0; i < fasceCheckBoxes.size(); i++) {
            if (fasceCheckBoxes.get(i).isSelected()) {
                selected.add(fasceDisponibili.get(i));
            }
        }
        return selected;
    }
}