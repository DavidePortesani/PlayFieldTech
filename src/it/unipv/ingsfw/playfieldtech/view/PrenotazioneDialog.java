package it.unipv.ingsfw.playfieldtech.view;

import it.unipv.ingsfw.playfieldtech.model.Squadra;
import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.Vector;

/*
* Finestra di dialogo per la prenotazione di un campo da parte del cliente.
* Permette di selezionare le squadre che giocheranno e mostra il prezzo totale.
* Accessibile solo dal cliente durante il processo di prenotazione.
*/
public class PrenotazioneDialog extends JDialog {
    private JComboBox<Squadra> squadraCasaBox;
    private JComboBox<Squadra> squadraOspiteBox;
    private JButton confermaButton;
    private JButton annullaButton;
    private JLabel prezzoLabel;
    private boolean confermato = false;

    // Costruttore che riceve il prezzo totale e la lista delle squadre disponibili per la prenotazione
    public PrenotazioneDialog(Frame owner, double prezzo, List<Squadra> squadreDisponibili) {
        super(owner, "Seleziona Squadre e Paga", true);
        setLayout(new BorderLayout(10, 10));
        setSize(450, 250); // Più piccola, non servono i 10 campi
        setLocationRelativeTo(owner);

        // Pannello Principale
        JPanel detailsPanel = new JPanel(new GridLayout(0, 2, 10, 10));
        detailsPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Prezzo
        prezzoLabel = new JLabel(String.format("%.2f €", prezzo));
        prezzoLabel.setFont(new Font("Arial", Font.BOLD, 16));
        prezzoLabel.setForeground(new Color(0, 100, 0));

        // ComboBox per le squadre (popolate con le squadre disponibili)
        Vector<Squadra> squadreCasaModel = new Vector<>(squadreDisponibili);
        Vector<Squadra> squadreOspiteModel = new Vector<>(squadreDisponibili);
        squadraCasaBox = new JComboBox<>(squadreCasaModel);
        squadraOspiteBox = new JComboBox<>(squadreOspiteModel);
        // Aggiunta dei componenti al pannello
        detailsPanel.add(new JLabel("Prezzo Totale:"));
        detailsPanel.add(prezzoLabel);
        detailsPanel.add(new JLabel("Seleziona Squadra di Casa:"));
        detailsPanel.add(squadraCasaBox);
        detailsPanel.add(new JLabel("Seleziona Squadra Ospite:"));
        detailsPanel.add(squadraOspiteBox);

        // Pannello Inferiore: Pulsanti di "Conferma e Paga" e "Annulla"
        JPanel buttonPanel = new JPanel();
        confermaButton = new JButton("Conferma e Paga");
        annullaButton = new JButton("Annulla");
        buttonPanel.add(confermaButton);
        buttonPanel.add(annullaButton);

        // Aggiunta dei Listener
        confermaButton.addActionListener(_ -> {
            confermato = true;
            setVisible(false);
        });
        annullaButton.addActionListener(_ -> setVisible(false));
        // Aggiunta dei pannelli alla finestra
        add(detailsPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    // Metodi Getter per il Controller per sapere se l'utente ha confermato e quali squadre ha selezionato
    public boolean isConfermato() {
        return confermato;
    }
    
    public Squadra getSquadraCasa() {
        return (Squadra) squadraCasaBox.getSelectedItem();
    }
    
    public Squadra getSquadraOspite() {
        return (Squadra) squadraOspiteBox.getSelectedItem();
    }
}