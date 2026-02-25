// Assicurati che questo package corrisponda alla tua struttura di cartelle
package it.unipv.ingsfw.playfieldtech.view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

/**
 * View per la dashboard dell'Amministratore.
 * Fornisce l'accesso a tutte le funzionalità di gestione del sistema.
 */
public class AdminView extends JPanel {

    // Componenti dell'interfaccia
    private JLabel welcomeLabel;
    private JButton aggiungiImpiantoButton;
    private JButton visualizzaPrenotazioniButton;
    private JButton gestisciTorneiButton;
    private JButton logoutButton;
    private JButton btnGestioneSquadre;

    public AdminView() {
        // Layout principale del pannello
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); // Aggiunge padding
        // Pannello Superiore (Titolo e Logout)
        JPanel topPanel = new JPanel(new BorderLayout());
        welcomeLabel = new JLabel("Pannello Amministratore", SwingConstants.CENTER);
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 16));
        logoutButton = new JButton("Logout");
        
        topPanel.add(welcomeLabel, BorderLayout.CENTER);
        topPanel.add(logoutButton, BorderLayout.EAST);
        
        add(topPanel, BorderLayout.NORTH);
        
        // Pannello Centrale (Pulsanti Funzionalità)
        // Usiamo un GridLayout per allineare i pulsanti verticalmente
        JPanel centerPanel = new JPanel(new GridLayout(5, 1, 10, 10)); // 4 righe, 1 colonna
        
        btnGestioneSquadre = new JButton("Gestione Squadre");
        aggiungiImpiantoButton = new JButton("Aggiungi Nuovo Impianto");
        visualizzaPrenotazioniButton = new JButton("Visualizza Tutte le Prenotazioni");
        gestisciTorneiButton = new JButton("Gestisci Tornei");
        
        // Aggiunge i pulsanti al pannello centrale
        centerPanel.add(aggiungiImpiantoButton);
        centerPanel.add(visualizzaPrenotazioniButton);
        centerPanel.add(gestisciTorneiButton);
        // Aggiungi il pulsante al pannello
        centerPanel.add(btnGestioneSquadre);
        add(centerPanel, BorderLayout.CENTER);
    }

    /**
     * Imposta il messaggio di benvenuto personalizzato per l'admin.
     * @param nome Il nome dell'amministratore loggato.
     */
    public void setWelcomeMessage(String nome) {
        welcomeLabel.setText("Benvenuto, Amministratore " + nome + "!");
    }

    /**
     * Collega un'azione (definita nel Controller) al pulsante "Aggiungi Impianto".
     * @param listener L'ActionListener da eseguire.
     */
    public void addAggiungiImpiantoListener(ActionListener listener) {
        aggiungiImpiantoButton.addActionListener(listener);
    }

    /**
     * Collega un'azione al pulsante "Visualizza Prenotazioni".
     * @param listener L'ActionListener da eseguire.
     */
    public void addVisualizzaPrenotazioniListener(ActionListener listener) {
        visualizzaPrenotazioniButton.addActionListener(listener);
    }

    /**
     * Collega un'azione al pulsante "Gestisci Tornei".
     * @param listener L'ActionListener da eseguire.
     */
    public void addGestisciTorneiListener(ActionListener listener) {
        gestisciTorneiButton.addActionListener(listener);
    }

    /**
     * Collega un'azione al pulsante "Logout".
     * @param listener L'ActionListener da eseguire.
     */
    public void addLogoutListener(ActionListener listener) {
        logoutButton.addActionListener(listener);
    }

    // Metodo per permettere al Controller di ascoltare il click
    public void addGestioneSquadreListener(ActionListener listener) {
        btnGestioneSquadre.addActionListener(listener);
    }

    // Metodo per ottenere il JFrame padre, utile per centrare dialog o passare come owner
    public Frame getFrame() {
        // Se AdminView estende JPanel, risale la gerarchia fino al JFrame
        return (Frame) SwingUtilities.getWindowAncestor(this);
    }   
}