package it.unipv.ingsfw.playfieldtech.app;

import it.unipv.ingsfw.playfieldtech.controller.AppController;
import it.unipv.ingsfw.playfieldtech.view.MainFrame;
import javax.swing.SwingUtilities;

public class App {

public static void main(String[] args){        SwingUtilities.invokeLater(() -> {
            MainFrame frame = new MainFrame();
            new AppController(frame);
            frame.showLoginView();
            frame.setVisible(true);
        });
    }
}