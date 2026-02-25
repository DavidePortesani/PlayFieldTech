package test.it.unipv.ingsfw.playfieldtech.registrazione;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class RegistrationValidationTest {

    // Simulazione della logica presente in AppController (logica pura)
    private boolean validaLunghezza(String testo) {
        return testo.length() <= 30;
    }

    private boolean validaSoloLettere(String testo) {
        return testo.matches("[a-zA-Z]+");
    }

    @Test
    void testLunghezzaCampi() {
        System.out.println("--- TEST VALIDAZIONE LUNGHEZZA (MAX 30) ---");
        assertTrue(validaLunghezza("Marco"), "Nome breve OK");
        assertFalse(validaLunghezza("QuestoNomeÈDecisamenteTroppoLungoPerIlDatabase"), "Nome > 30 KO");
    }

    @Test
    void testCaratteriAlfabetici() {
        System.out.println("--- TEST SOLO LETTERE ---");
        assertTrue(validaSoloLettere("Mario"), "Mario OK");
        assertFalse(validaSoloLettere("Mario123"), "Numeri KO");
        assertFalse(validaSoloLettere("M@rio"), "Simboli KO");
    }
}