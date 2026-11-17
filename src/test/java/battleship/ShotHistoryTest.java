package battleship;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes unitários da classe ShotHistory.
 *
 * Complexidade ciclomática:
 * - register: 1
 * - count: 1
 *
 * @author 129881
 */
@DisplayName("Testes da classe ShotHistory")
class ShotHistoryTest {

    private ShotHistory history;

    @BeforeEach
    void setUp() {
        history = new ShotHistory();
    }

    @AfterEach
    void tearDown() {
        history = null;
    }

    @Test
    @DisplayName("Histórico novo começa sem tiros (count = 0)")
    void countEmpty() {
        assertEquals(0, history.count(), "Error: a new ShotHistory should have no shots.");
    }

    @Test
    @DisplayName("register de um tiro aumenta o count para 1")
    void registerOneShot() {
        history.register("A1");
        assertEquals(1, history.count(), "Error: after one register, count should be 1.");
    }

    @Test
    @DisplayName("register de vários tiros conta todos")
    void registerSeveralShots() {
        history.register("A1");
        history.register("B5");
        history.register("J10");
        assertEquals(3, history.count(), "Error: after three registers, count should be 3.");
    }

    @Test
    @DisplayName("register do mesmo tiro repetido também é contado")
    void registerRepeatedShot() {
        history.register("C3");
        history.register("C3");
        assertEquals(2, history.count(), "Error: repeated shots should also be counted.");
    }
}