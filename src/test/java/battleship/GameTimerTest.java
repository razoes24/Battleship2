package battleship;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes unitários da classe GameTimer.
 * Como a GameTimer não tem getters, os testes verificam o que ela escreve no System.out.
 *
 * Complexidade ciclomática:
 * - startGame: 1
 * - endMove: 2 (relógio parado / relógio a contar)
 * - printTotal: 3 (sem jogadas / com jogadas + ciclo for)
 * - restart: 1
 * - format: 1
 *
 * @author 129881
 */
@DisplayName("Testes da classe GameTimer")
class GameTimerTest {

    private GameTimer timer;
    private final ByteArrayOutputStream output = new ByteArrayOutputStream();
    private PrintStream originalOut;

    @BeforeEach
    void setUp() {
        timer = new GameTimer();
        originalOut = System.out;
        System.setOut(new PrintStream(output));
    }

    @AfterEach
    void tearDown() {
        System.setOut(originalOut);
        timer = null;
    }

    /** Devolve o que foi escrito no System.out até agora. */
    private String printed() {
        return output.toString();
    }

    @Test
    @DisplayName("endMove sem startGame não faz nada (relógio parado)")
    void endMoveWithoutStart() {
        timer.endMove();
        assertEquals("", printed(), "Error: endMove should not print anything if the game was not started.");
    }

    @Test
    @DisplayName("endMove depois de startGame regista e mostra o tempo da jogada 1")
    void endMoveAfterStart() {
        timer.startGame();
        timer.endMove();
        assertTrue(printed().contains("Tempo da jogada 1: "),
                "Error: endMove should print the time of move 1.");
        assertTrue(printed().matches("(?s).*Tempo da jogada 1: \\d+[.,]\\d s.*"),
                "Error: the time should be formatted in seconds with one decimal place.");
    }

    @Test
    @DisplayName("endMove numera as jogadas seguidas (1, 2, 3)")
    void endMoveSeveralMoves() {
        timer.startGame();
        timer.endMove();
        timer.endMove();
        timer.endMove();
        String out = printed();
        assertTrue(out.contains("Tempo da jogada 1: "), "Error: move 1 should be printed.");
        assertTrue(out.contains("Tempo da jogada 2: "), "Error: move 2 should be printed.");
        assertTrue(out.contains("Tempo da jogada 3: "), "Error: move 3 should be printed.");
    }

    @Test
    @DisplayName("printTotal sem jogadas não mostra nada")
    void printTotalWithoutMoves() {
        timer.printTotal();
        assertEquals("", printed(), "Error: printTotal should not print anything if there were no moves.");
    }

    @Test
    @DisplayName("printTotal depois de startGame mas sem jogadas não mostra nada")
    void printTotalAfterStartWithoutMoves() {
        timer.startGame();
        timer.printTotal();
        assertEquals("", printed(), "Error: printTotal should not print anything if no move was finished.");
    }

    @Test
    @DisplayName("printTotal com jogadas mostra o tempo total e o número de jogadas")
    void printTotalWithMoves() {
        timer.startGame();
        timer.endMove();
        timer.endMove();
        timer.printTotal();
        String out = printed();
        assertTrue(out.contains("Tempo total do jogo: "), "Error: printTotal should print the total time.");
        assertTrue(out.contains("em 2 jogadas"), "Error: printTotal should print the number of moves (2).");
    }

    @Test
    @DisplayName("startGame apaga os tempos do jogo anterior")
    void startGameResetsMoves() {
        timer.startGame();
        timer.endMove();
        timer.endMove();

        output.reset();
        timer.startGame();
        timer.endMove();
        timer.printTotal();

        String out = printed();
        assertTrue(out.contains("Tempo da jogada 1: "), "Error: the move count should restart at 1 in a new game.");
        assertFalse(out.contains("Tempo da jogada 3: "), "Error: moves from the previous game should have been cleared.");
        assertTrue(out.contains("em 1 jogadas"), "Error: the total should only count the moves of the new game.");
    }

    @Test
    @DisplayName("O tempo medido de uma jogada reflete o tempo realmente passado")
    void endMoveMeasuresElapsedTime() throws InterruptedException {
        timer.startGame();
        Thread.sleep(150);
        timer.endMove();
        // 150 ms -> pelo menos "0,1 s" / "0.1 s"
        assertTrue(printed().matches("(?s).*Tempo da jogada 1: (0[.,][1-9]|[1-9]\\d*[.,]\\d) s.*"),
                "Error: a move of ~150 ms should be shown as at least 0.1 s.");
    }
}