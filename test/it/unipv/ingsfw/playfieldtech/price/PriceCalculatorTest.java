package test.it.unipv.ingsfw.playfieldtech.price;

import it.unipv.ingsfw.playfieldtech.model.*;
import it.unipv.ingsfw.playfieldtech.strategy.PriceCalculator;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;

//Verifica che il calcolo del prezzo (Strategy Pattern) 
//funzioni correttamente per i giorni feriali e i weekend.
class PriceCalculatorTest {

    @Test
    void testPrezzoFeriale() {
        // Setup: Lunedì (Feriale)
        // Nota: Assicurati che la data sia effettivamente un lunedì
        LocalDate lunedi = LocalDate.of(2023, 10, 23); 
        assertEquals(DayOfWeek.MONDAY, lunedi.getDayOfWeek());

        FasciaOraria fascia = new FasciaOraria(1, DayOfWeek.MONDAY, LocalTime.of(10, 0), LocalTime.of(11, 0), 50.0);
        ImpiantoSportivo campo = new ImpiantoSportivo(1, "Campo Test", "Calcetto");
        Utente user = new Cliente(1, "test", "pass", "Nome", "Cognome");
        
        Prenotazione p = new Prenotazione(0, lunedi, user, campo, fascia);
        
        PriceCalculator calculator = new PriceCalculator();
        double prezzo = calculator.calcolaPrezzoFinale(p);

        // Assert: Il prezzo deve essere quello base (50.0)
        assertEquals(50.0, prezzo, 0.01, "Il prezzo feriale dovrebbe essere uguale al prezzo base");
    }

    @Test
    void testPrezzoWeekend() {
        // Setup: Sabato (Weekend)
        LocalDate sabato = LocalDate.of(2023, 10, 28);
        assertEquals(DayOfWeek.SATURDAY, sabato.getDayOfWeek());

        double prezzoBase = 50.0;
        FasciaOraria fascia = new FasciaOraria(1, DayOfWeek.SATURDAY, LocalTime.of(10, 0), LocalTime.of(11, 0), prezzoBase);
        ImpiantoSportivo campo = new ImpiantoSportivo(1, "Campo Test", "Calcetto");
        Utente user = new Cliente(1, "test", "pass", "Nome", "Cognome");
        
        Prenotazione p = new Prenotazione(0, sabato, user, campo, fascia);
        
        PriceCalculator calculator = new PriceCalculator();
        double prezzo = calculator.calcolaPrezzoFinale(p);

        // Assert: Il prezzo deve essere maggiorato del 15%
        double prezzoAtteso = prezzoBase * 1.15;
        assertEquals(prezzoAtteso, prezzo, 0.01, "Il prezzo nel weekend dovrebbe essere maggiorato del 15%");
    }
}