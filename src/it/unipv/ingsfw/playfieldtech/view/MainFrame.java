package it.unipv.ingsfw.playfieldtech.view;
import javax.swing.*;
import java.awt.*;

/*
* Finestra principale dell'applicazione, che contiene tutte le altre viste (login, registrazione, cliente, admin).
* Utilizza un CardLayout per gestire la navigazione tra le diverse viste.
*/
public class MainFrame extends JFrame {
    private CardLayout cardLayout;
    private JPanel mainPanel;
    private LoginView loginView;
    private RegistrazioneView registrazioneView;
    private ClienteView clienteView;
    private AdminView adminView;

    // Costruttore che inizializza tutte le viste e le aggiunge al CardLayout
    public MainFrame() {
        
        setTitle("PlayFieldTechApp");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);
        java.net.URL imgURL = getClass().getResource("/logo.png");
        ImageIcon icon = new ImageIcon(imgURL);
        setIconImage(icon.getImage());
        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);
        loginView = new LoginView();
        registrazioneView = new RegistrazioneView();
        clienteView = new ClienteView();
        adminView = new AdminView();
        mainPanel.add(loginView, "LOGIN");
        mainPanel.add(registrazioneView, "REGISTRAZIONE");
        mainPanel.add(clienteView, "CLIENTE");
        mainPanel.add(adminView, "ADMIN");
        add(mainPanel);
    }
    // Metodi per mostrare le diverse viste
    public void showLoginView() { cardLayout.show(mainPanel, "LOGIN"); }
    public void showRegistrazioneView() { cardLayout.show(mainPanel, "REGISTRAZIONE"); }
    public void showClienteView() { cardLayout.show(mainPanel, "CLIENTE"); }
    public void showAdminView() { cardLayout.show(mainPanel, "ADMIN"); }
    public LoginView getLoginView() { return loginView; }
    public RegistrazioneView getRegistrazioneView() { return registrazioneView; }
    public ClienteView getClienteView() { return clienteView; }
    public AdminView getAdminView() { return adminView; }
    // Metodi per mostrare messaggi di errore, successo o conferma
    public void mostraErrore(String messaggio) {
        JOptionPane.showMessageDialog(this, messaggio, "Errore", JOptionPane.ERROR_MESSAGE);
    }
    public void mostraSuccesso(String messaggio) {
        JOptionPane.showMessageDialog(this, messaggio, "Successo", JOptionPane.INFORMATION_MESSAGE);
    }
    public int mostraConferma(String messaggio) {
        return JOptionPane.showConfirmDialog(this, messaggio);
    }
}