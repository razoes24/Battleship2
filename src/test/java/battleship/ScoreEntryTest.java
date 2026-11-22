package battleship;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Testes unitários da classe {@link ScoreEntry}.
 *
 * @author 129854
 */
@DisplayName("Testes da classe ScoreEntry")
class ScoreEntryTest {

    @Test
    @DisplayName("getDate devolve a data passada ao construtor")
    void getDate() {
        ScoreEntry entry = new ScoreEntry("2026-11-17 18:00", 42);
        assertEquals("2026-11-17 18:00", entry.getDate());
    }

    @Test
    @DisplayName("getMoves devolve o número de jogadas passado ao construtor")
    void getMoves() {
        ScoreEntry entry = new ScoreEntry("2026-11-17 18:00", 42);
        assertEquals(42, entry.getMoves());
    }

    @Test
    @DisplayName("toString tem o formato 'data - N jogadas'")
    void testToString() {
        ScoreEntry entry = new ScoreEntry("2026-11-17 18:00", 7);
        assertEquals("2026-11-17 18:00 - 7 jogadas", entry.toString());
    }
}
