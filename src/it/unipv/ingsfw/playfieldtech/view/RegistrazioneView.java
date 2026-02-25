package it.unipv.ingsfw.playfieldtech.view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

public class RegistrazioneView extends JPanel {
    private JTextField nomeField;
    private JTextField cognomeField;
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JPasswordField confermaPasswordField;
    private JButton registratiButton;
    private JButton tornaAlLoginButton;
    /*
    * Pannello per la registrazione di un nuovo cliente. Accessibile solo dal login.
    */
    public RegistrazioneView() {
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.gridx = 0; gbc.gridy = 0;
        add(new JLabel("Nome:"), gbc); gbc.gridy++;
        add(new JLabel("Cognome:"), gbc); gbc.gridy++;
        add(new JLabel("Username:"), gbc); gbc.gridy++;
        add(new JLabel("Password:"), gbc); gbc.gridy++;
        add(new JLabel("Conferma Password:"), gbc);
        gbc.gridx = 1; gbc.gridy = 0; gbc.fill = GridBagConstraints.HORIZONTAL;
        nomeField = new JTextField(20); add(nomeField, gbc); gbc.gridy++;
        cognomeField = new JTextField(20); add(cognomeField, gbc); gbc.gridy++;
        usernameField = new JTextField(20); add(usernameField, gbc); gbc.gridy++;
        passwordField = new JPasswordField(20); add(passwordField, gbc); gbc.gridy++;
        confermaPasswordField = new JPasswordField(20); add(confermaPasswordField, gbc);
        gbc.gridx = 0; gbc.gridy++; gbc.gridwidth = 2;
        JPanel buttonPanel = new JPanel();
        registratiButton = new JButton("Registrati");
        tornaAlLoginButton = new JButton("Torna al Login");
        buttonPanel.add(registratiButton);
        buttonPanel.add(tornaAlLoginButton);
        add(buttonPanel, gbc);
    }

    public String getNome() { return nomeField.getText(); }
    public String getCognome() { return cognomeField.getText(); }
    public String getUsername() { return usernameField.getText(); }
    public char[] getPassword() { return passwordField.getPassword(); }
    public char[] getConfermaPassword() { return confermaPasswordField.getPassword(); }
    public void addRegistrazioneListener(ActionListener l) { registratiButton.addActionListener(l); }
    public void addTornaAlLoginListener(ActionListener l) { tornaAlLoginButton.addActionListener(l); }
    // Metodo per pulire i campi dopo una registrazione riuscita o in caso di errori
    public void clearFields() {
        nomeField.setText(""); cognomeField.setText(""); usernameField.setText("");
        passwordField.setText(""); confermaPasswordField.setText("");
    }
}
