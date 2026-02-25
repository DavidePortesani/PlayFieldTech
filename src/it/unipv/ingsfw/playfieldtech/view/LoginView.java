package it.unipv.ingsfw.playfieldtech.view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

/*
* Pannello di Login, prima schermata che l'utente vede all'avvio dell'applicazione. Permette di inserire username e password 
* per accedere al proprio account, oppure di accedere alla schermata di registrazione se non si ha ancora un account.
*/
public class LoginView extends JPanel {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JButton registratiButton;
    // Costruttore che inizializza i componenti grafici e li posiziona usando GridBagLayout
    public LoginView() {
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.gridx = 0; gbc.gridy = 0;
        add(new JLabel("Username:"), gbc);
        gbc.gridy++;
        add(new JLabel("Password:"), gbc);
        gbc.gridx = 1; gbc.gridy = 0; gbc.fill = GridBagConstraints.HORIZONTAL;
        usernameField = new JTextField(20);
        add(usernameField, gbc);
        gbc.gridy++;
        passwordField = new JPasswordField(20);
        add(passwordField, gbc);
        gbc.gridx = 0; gbc.gridy++; gbc.gridwidth = 2; gbc.fill = GridBagConstraints.NONE;
        JPanel buttonPanel = new JPanel();
        loginButton = new JButton("Login");
        registratiButton = new JButton("Non hai un account? Registrati");
        buttonPanel.add(loginButton);
        buttonPanel.add(registratiButton);
        add(buttonPanel, gbc);
    }
    // Metodi Getter per il Controller per ottenere i dati inseriti dall'utente e per aggiungere i listener ai pulsanti
    public String getUsername() { return usernameField.getText(); }
    public char[] getPassword() { return passwordField.getPassword(); }
    public void addLoginListener(ActionListener l) { loginButton.addActionListener(l); }
    public void addRegistratiListener(ActionListener l) { registratiButton.addActionListener(l); }
    public void clearFields() { usernameField.setText(""); passwordField.setText(""); }
}