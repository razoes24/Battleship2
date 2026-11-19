package restserver;

import battleship.Game;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes unitários da classe GameSession.
 * Author: 131834
 */
public class GameSessionTest {

	private GameSession session;

	@BeforeEach
	void setUp() {
		session = new GameSession("abc-123", "Ana", "http://localhost:9090");
	}

	@Test
	@DisplayName("o construtor guarda o id, o nome e o callback")
	void construtor() {
		assertEquals("abc-123", session.getGameId());
		assertEquals("Ana", session.getPlayerName());
		assertEquals("http://localhost:9090", session.getCallbackUrl());
	}

	@Test
	@DisplayName("o jogo começa sem terminar e sem vencedor")
	void estadoInicial() {
		assertFalse(session.isGameOver());
		assertNull(session.getWinner());
	}

	@Test
	@DisplayName("shotsPerTurn é igual a Game.NUMBER_SHOTS")
	void shotsPerTurn() {
		assertEquals(Game.NUMBER_SHOTS, session.getShotsPerTurn());
	}

	@Test
	@DisplayName("getGame devolve um jogo com a frota da IA já posicionada")
	void getGame() {
		assertNotNull(session.getGame());
		assertInstanceOf(Game.class, session.getGame());
		assertEquals(11, session.getGame().getRemainingShips());
	}

	@Test
	@DisplayName("markAiWins termina o jogo e o vencedor é a IA")
	void markAiWins() {
		session.markAiWins();

		assertTrue(session.isGameOver());
		assertEquals("AI_WINS", session.getWinner());
	}

	@Test
	@DisplayName("markStudentWins termina o jogo e o vencedor é o estudante")
	void markStudentWins() {
		session.markStudentWins();

		assertTrue(session.isGameOver());
		assertEquals("STUDENT_WINS", session.getWinner());
	}
}
