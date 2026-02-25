package it.unipv.ingsfw.playfieldtech.view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

/*
 * View per la dashboard del Cliente.
 * Fornisce l'accesso alle funzionalità di prenotazione, visualizzazione storico e gestione squadre.
 */
public class ClienteView extends JPanel{
    private JLabel welcomeLabel;
    private JTable calendarioTable;
    private JButton prenotaButton;
    private JButton logoutButton;
    private JButton storicoButton;
    private JLabel dataLabel;
    private JButton giornoPrecedenteButton;
    private JButton giornoSuccessivoButton;
    private JButton gestisciSquadreButton;
    
    // Dichiariamo il nuovo pulsante
    private JButton visualizzaTorneiButton;

    public ClienteView() {
        setLayout(new BorderLayout(10, 10));

        // Pannello Superiore
        JPanel topPanel = new JPanel(new BorderLayout());
        welcomeLabel = new JLabel("Benvenuto!", SwingConstants.CENTER);
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 16));
        logoutButton = new JButton("Logout");
        topPanel.add(welcomeLabel, BorderLayout.CENTER);
        topPanel.add(logoutButton, BorderLayout.EAST);
        
        // Pannello Data e Navigazione Giorni
        JPanel datePanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 5));
        giornoPrecedenteButton = new JButton("< Giorno Precedente");
        giornoSuccessivoButton = new JButton("Giorno Successivo >");
        dataLabel = new JLabel("Oggi");
        dataLabel.setFont(new Font("Arial", Font.BOLD, 14));
        datePanel.add(giornoPrecedenteButton);
        datePanel.add(dataLabel);
        datePanel.add(giornoSuccessivoButton);
        // Combiniamo i due pannelli in un unico contenitore per posizionarli entrambi nella parte superiore
        JPanel headerContainer = new JPanel(new BorderLayout());
        headerContainer.add(topPanel, BorderLayout.NORTH);
        headerContainer.add(datePanel, BorderLayout.CENTER);
        add(headerContainer, BorderLayout.NORTH);
        // Tabella del calendario
        calendarioTable = new JTable();
        add(new JScrollPane(calendarioTable), BorderLayout.CENTER);

        // Pannello Inferiore
        JPanel bottomPanel = new JPanel(new FlowLayout());
        prenotaButton = new JButton("Nuova Prenotazione");
        storicoButton = new JButton("Le mie Prenotazioni");
        gestisciSquadreButton = new JButton("Le mie Squadre");
        
        // Inizializziamo e aggiungiamo il pulsante
        visualizzaTorneiButton = new JButton("I miei Tornei");
        visualizzaTorneiButton.setBackground(new Color(220, 240, 255)); // Colore leggermente diverso

        bottomPanel.add(prenotaButton);
        bottomPanel.add(storicoButton);
        bottomPanel.add(gestisciSquadreButton);
        bottomPanel.add(visualizzaTorneiButton); 
        
        add(bottomPanel, BorderLayout.SOUTH);
    }

    public void setWelcomeMessage(String nome) { welcomeLabel.setText("Benvenuto, " + nome + "!"); }
    public JTable getCalendarioTable() { return calendarioTable; }
    public void setDataLabel(String testo) { dataLabel.setText(testo); }
    public void setGiornoPrecedenteButtonEnabled(boolean enabled) { giornoPrecedenteButton.setEnabled(enabled); }
    
    public void addLogoutListener(ActionListener l) { logoutButton.addActionListener(l); }
    public void addPrenotaListener(ActionListener l) { prenotaButton.addActionListener(l); }
    public void addStoricoListener(ActionListener l) { storicoButton.addActionListener(l); }
    public void addGiornoPrecedenteListener(ActionListener l) { giornoPrecedenteButton.addActionListener(l); }
    public void addGiornoSuccessivoListener(ActionListener l) { giornoSuccessivoButton.addActionListener(l); }
    public void addGestisciSquadreListener(ActionListener l) { gestisciSquadreButton.addActionListener(l); }
    
    // Listener per il Controller
    public void addVisualizzaTorneiListener(ActionListener l) { visualizzaTorneiButton.addActionListener(l); }
}