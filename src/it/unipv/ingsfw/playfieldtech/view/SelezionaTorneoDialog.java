package it.unipv.ingsfw.playfieldtech.view;

import it.unipv.ingsfw.playfieldtech.model.Torneo;
import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * Finestra di dialogo per permettere al cliente di selezionare quale torneo visualizzare
 * nel caso in cui sia iscritto a più tornei contemporaneamente.
 */
public class SelezionaTorneoDialog extends JDialog {
    private JComboBox<Torneo> torneiBox;
    private JButton confermaButton;
    private boolean confermato = false;
    // Costruttore che riceve la lista dei tornei a cui il cliente è iscritto
    public SelezionaTorneoDialog(Frame owner, List<Torneo> tornei) {
        super(owner, "Seleziona Torneo", true);
        setSize(400, 200);
        setLocationRelativeTo(owner);
        setLayout(new GridLayout(3, 1, 10, 10));

        add(new JLabel("Sei iscritto a più tornei. Quale vuoi vedere?", SwingConstants.CENTER));

        torneiBox = new JComboBox<>(tornei.toArray(new Torneo[0]));
        add(torneiBox);

        JPanel btnPanel = new JPanel();
        confermaButton = new JButton("Visualizza");
        JButton annullaButton = new JButton("Annulla");
        
        btnPanel.add(confermaButton);
        btnPanel.add(annullaButton);
        add(btnPanel);

        confermaButton.addActionListener(_ -> {
            confermato = true;
            setVisible(false);
        });
        annullaButton.addActionListener(_ -> setVisible(false));
    }

    public boolean isConfermato() { return confermato; }
    public Torneo getTorneoSelezionato() { return (Torneo) torneiBox.getSelectedItem(); }
}