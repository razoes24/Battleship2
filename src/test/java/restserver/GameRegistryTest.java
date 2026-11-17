package restserver;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes unitários da classe GameRegistry.
 * Author: 131834
 */
public class GameRegistryTest {

	private GameRegistry registry;

	@BeforeEach
	void setUp() {
		registry = new GameRegistry();
	}

	@Test
	@DisplayName("createSession devolve uma sessão com o nome e o callback dados")
	void createSession() {
		GameSession session = registry.createSession("Ana", "http://localhost:9090");

		assertNotNull(session);
		assertEquals("Ana", session.getPlayerName());
		assertEquals("http://localhost:9090", session.getCallbackUrl());
		assertFalse(session.isGameOver());
	}

	@Test
	@DisplayName("createSession gera um gameId que é um UUID válido")
	void createSessionGameIdEUuid() {
		GameSession session = registry.createSession("Ana", "http://localhost:9090");

		assertNotNull(session.getGameId());
		// se não for um UUID válido isto lança exceção
		assertEquals(session.getGameId(), UUID.fromString(session.getGameId()).toString());
	}

	@Test
	@DisplayName("duas sessões criadas têm gameIds diferentes")
	void createSessionIdsDiferentes() {
		GameSession s1 = registry.createSession("Ana", "http://a");
		GameSession s2 = registry.createSession("Rui", "http://b");

		assertNotEquals(s1.getGameId(), s2.getGameId());
	}

	@Test
	@DisplayName("getSession devolve a mesma sessão que foi criada")
	void getSession() {
		GameSession session = registry.createSession("Ana", "http://localhost:9090");

		assertSame(session, registry.getSession(session.getGameId()));
	}

	@Test
	@DisplayName("getSession devolve null quando o id não existe")
	void getSessionInexistente() {
		assertNull(registry.getSession("id-que-nao-existe"));
	}

	@Test
	@DisplayName("removeSession apaga a sessão do registo")
	void removeSession() {
		GameSession session = registry.createSession("Ana", "http://localhost:9090");
		String id = session.getGameId();

		registry.removeSession(id);

		assertNull(registry.getSession(id));
	}

	@Test
	@DisplayName("removeSession só apaga a sessão indicada")
	void removeSessionNaoAfetaOutras() {
		GameSession s1 = registry.createSession("Ana", "http://a");
		GameSession s2 = registry.createSession("Rui", "http://b");

		registry.removeSession(s1.getGameId());

		assertNull(registry.getSession(s1.getGameId()));
		assertSame(s2, registry.getSession(s2.getGameId()));
	}

	@Test
	@DisplayName("removeSession com id inexistente não faz nada")
	void removeSessionInexistente() {
		GameSession session = registry.createSession("Ana", "http://localhost:9090");

		assertDoesNotThrow(() -> registry.removeSession("id-que-nao-existe"));
		assertSame(session, registry.getSession(session.getGameId()));
	}
}
