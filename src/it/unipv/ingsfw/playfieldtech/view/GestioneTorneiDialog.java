package it.unipv.ingsfw.playfieldtech.view;

import it.unipv.ingsfw.playfieldtech.model.Torneo;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.List;

/*
* Dialog per la gestione dei tornei, usato da AdminController e delegato a TorneoController.
* Permette di visualizzare i tornei esistenti, crearne di nuovi, generare il calendario, visualizzare partite/risultati e eliminare tornei.
*/
public class GestioneTorneiDialog extends JDialog {

    private JList<Torneo> torneiList;
    private DefaultListModel<Torneo> torneiListModel;
    private JButton creaTorneoButton;
    private JButton generaCalendarioButton;
    private JButton visualizzaPartiteButton;
    private JButton eliminaTorneoButton;

    public GestioneTorneiDialog(Frame owner, List<Torneo> tornei) {
        super(owner, "Gestione Tornei", true);
        setSize(600, 400);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout(10, 10));
        
        // Pannello Centrale: Lista dei tornei
        torneiListModel = new DefaultListModel<>();
        for (Torneo t : tornei) {
            torneiListModel.addElement(t); // Popola la lista con i tornei recuperati dalla Facade
        }
        torneiList = new JList<>(torneiListModel); // Usa un JList per mostrare i tornei in modo più chiaro e con supporto alla selezione
        torneiList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);// Permette di selezionare un solo torneo alla volta
        add(new JScrollPane(torneiList), BorderLayout.CENTER); // Aggiunge la lista con una barra di scorrimento al centro del dialog

        // Pannello Laterale: Pulsanti di azione
        JPanel actionPanel = new JPanel(new GridLayout(4, 1, 10, 10));
        actionPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        creaTorneoButton = new JButton("Crea Nuovo Torneo");
        generaCalendarioButton = new JButton("Genera Calendario");
        visualizzaPartiteButton = new JButton("Visualizza Partite/Risultati");
        eliminaTorneoButton = new JButton("Elimina Torneo");
        eliminaTorneoButton.setBackground(new Color(255, 200, 200)); // Rosso chiaro per attenzione
        
        actionPanel.add(creaTorneoButton);
        actionPanel.add(generaCalendarioButton); //una volta selzionato un torneo
        actionPanel.add(visualizzaPartiteButton);
        actionPanel.add(eliminaTorneoButton);

        add(actionPanel, BorderLayout.EAST);

        // Aggiungi un pulsante Chiudi
        JButton chiudiButton = new JButton("Chiudi");
        chiudiButton.addActionListener(_ -> setVisible(false));
        JPanel bottomPanel = new JPanel();
        bottomPanel.add(chiudiButton);
        add(bottomPanel, BorderLayout.SOUTH);
    }

  
    /*
    * Metodo per aggiornare la lista dei tornei quando necessario.
    */
    public void aggiornaLista(List<Torneo> tornei) {
        // Salviamo l'ID del torneo attualmente selezionato per non perdere la selezione
        Torneo selezionato = getTorneoSelezionato();
        int idSelezionato;
        if (selezionato != null) {
            idSelezionato = selezionato.getIdTorneo(); // Prende l'ID reale
        } else {
            idSelezionato = -1; // Usa un ID fittizio per indicare "nessuna selezione"
        }

        // Svuotiamo e ripopoliamo la lista con i tornei aggiornati
        torneiListModel.clear();
        for (Torneo t : tornei) {
            torneiListModel.addElement(t);
        }

        // Ripristiniamo la selezione se il torneo selezionato esiste ancora nella nuova lista
        if (idSelezionato != -1) {
            for (int i = 0; i < torneiListModel.size(); i++) {
                if (torneiListModel.get(i).getIdTorneo() == idSelezionato) {
                    torneiList.setSelectedIndex(i);
                    break;
                }
            }
        }
    }

    // Metodi per il Controller
    public Torneo getTorneoSelezionato() {
        return torneiList.getSelectedValue();
    }
    
    public void addCreaTorneoListener(ActionListener listener) {
        creaTorneoButton.addActionListener(listener);
    }
    
    public void addGeneraCalendarioListener(ActionListener listener) {
        generaCalendarioButton.addActionListener(listener);
    }
    
    public void addVisualizzaPartiteListener(ActionListener listener) {
        visualizzaPartiteButton.addActionListener(listener);
    }

    public void addEliminaTorneoListener(ActionListener listener) {
        eliminaTorneoButton.addActionListener(listener);
    }

    public void mostraErrore(String messaggio) {
        JOptionPane.showMessageDialog(this, messaggio, "Errore", JOptionPane.ERROR_MESSAGE);
    }
    public void mostraSuccesso(String messaggio) {
        JOptionPane.showMessageDialog(this, messaggio, "Successo", JOptionPane.INFORMATION_MESSAGE);
    }
    public int mostraConferma(String messaggio) {
        return JOptionPane.showConfirmDialog(this,messaggio, "Conferma Eliminazione",JOptionPane.YES_NO_OPTION,JOptionPane.WARNING_MESSAGE);

    }
    public void mostraErroreNessuno(String msg) { 
        JOptionPane.showMessageDialog(null, msg, "Errore", JOptionPane.ERROR_MESSAGE); 
    }
    public void mostraAttenzione(String msg) { 
        JOptionPane.showMessageDialog(null, msg, "Attenzione", JOptionPane.WARNING_MESSAGE);    
    }
}