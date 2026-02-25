package it.unipv.ingsfw.playfieldtech.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseManager {
    private static DatabaseManager instance;
    private Connection connection;
    private final String URL = "jdbc:mysql://localhost:3306/PlayFieldTechDB";
    private final String USER = "root"; 
    private final String PASSWORD = "password"; 

    private DatabaseManager() {
        try {
            this.connection = DriverManager.getConnection( URL, USER, PASSWORD);
        } catch (SQLException e) {
            throw new RuntimeException("Errore di connessione al database", e);
        }
    }

    public static DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }

    public Connection getConnection() {
        return connection;
    }
}
