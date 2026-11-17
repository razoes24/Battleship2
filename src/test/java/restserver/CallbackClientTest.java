package restserver;

import battleship.IMove;
import battleship.IPosition;
import battleship.Position;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes unitários da classe CallbackClient.
 *
 * O CallbackClient faz um pedido HTTP POST ao servidor do estudante. Para o testar sem
 * depender de outro programa, cada teste arranca um pequeno servidor HTTP falso
 * (com.sun.net.httpserver.HttpServer, que vem com o JDK) numa porta livre do computador.
 * O teste escolhe o código HTTP e o JSON que esse servidor devolve.
 *
 * Complexidade ciclomática:
 * - sendShots: 9 (ciclo dos tiros, falha de serialização, falha HTTP, código HTTP fora de 2xx,
 *   JSON inválido, IA ganha por shipsRemaining == 0 ou por gameStatus == GAME_OVER)
 * - recordAiMove: 6 (results null, ciclo com duas condições de paragem)
 *
 * @author 129881
 */
@DisplayName("Testes da classe CallbackClient")
class CallbackClientTest {

    private HttpServer server;
    private CallbackClient client;
    private GameSession session;

    /** O que o servidor falso vai responder e o que recebeu. */
    private int responseStatus;
    private String responseBody;
    private String receivedMethod;
    private String receivedPath;
    private String receivedBody;

    private final List<IPosition> aiShots = List.of(new Position(0, 0), new Position(1, 1), new Position(2, 2));

    @BeforeEach
    void setUp() throws IOException {
        server = HttpServer.create(new InetSocketAddress("localhost", 0), 0); // porta 0 = uma porta livre qualquer
        server.createContext("/", exchange -> {
            receivedMethod = exchange.getRequestMethod();
            receivedPath = exchange.getRequestURI().getPath();
            receivedBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            byte[] bytes = responseBody.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(responseStatus, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
        });
        server.start();

        client = new CallbackClient();
        String url = "http://localhost:" + server.getAddress().getPort();
        session = new GameSession("jogo-teste", "Jogador1", url);
    }

    @AfterEach
    void tearDown() {
        server.stop(0);
    }

    /** Prepara a resposta do servidor falso. */
    private void respond(int status, String body) {
        responseStatus = status;
        responseBody = body;
    }

    private String result(String row, int column, String outcome) {
        return "{\"row\":\"" + row + "\",\"column\":" + column + ",\"outcome\":\"" + outcome + "\",\"shipType\":null}";
    }

    private List<IMove> aiMoves() {
        return session.getGame().getAlienMoves();
    }

    // ---------------------------------------------------------------------

    @Test
    @DisplayName("Envia um POST para {callbackUrl}/game/{gameId}/shots com os 3 tiros em JSON")
    void sendsCorrectRequest() {
        respond(200, "{\"results\":[],\"shipsRemaining\":11,\"gameStatus\":\"ONGOING\",\"winner\":null}");
        assertTrue(client.sendShots(session, aiShots), "Error: a successful callback should return true.");

        assertEquals("POST", receivedMethod, "Error: the request should be a POST.");
        assertEquals("/game/jogo-teste/shots", receivedPath, "Error: wrong callback path.");
        assertTrue(receivedBody.contains("\"row\":\"A\""), "Error: the first shot should be in row A.");
        assertTrue(receivedBody.contains("\"column\":1"), "Error: the first shot should be in column 1.");
        assertTrue(receivedBody.contains("\"row\":\"C\""), "Error: the third shot should be in row C.");
        assertTrue(receivedBody.contains("\"column\":3"), "Error: the third shot should be in column 3.");
    }

    @Test
    @DisplayName("Resposta 200 com resultados: regista a jogada da IA e o jogo continua")
    void successRecordsMove() {
        respond(200, "{\"results\":[" + result("A", 1, "MISS") + "," + result("B", 2, "HIT") + ","
                + result("C", 3, "SUNK") + "],\"shipsRemaining\":10,\"gameStatus\":\"ONGOING\",\"winner\":null}");

        assertTrue(client.sendShots(session, aiShots), "Error: a successful callback should return true.");
        assertEquals(1, aiMoves().size(), "Error: the AI move should be recorded in the game.");
        assertEquals(3, aiMoves().get(0).getShots().size(), "Error: the recorded move should have 3 shots.");
        assertFalse(session.isGameOver(), "Error: the game should not be over.");
    }

    @Test
    @DisplayName("Resultados INVALID e REPEATED também são registados")
    void invalidAndRepeatedOutcomes() {
        respond(200, "{\"results\":[" + result("A", 1, "INVALID") + "," + result("B", 2, "REPEATED") + ","
                + result("C", 3, "MISS") + "],\"shipsRemaining\":11,\"gameStatus\":\"ONGOING\",\"winner\":null}");

        assertTrue(client.sendShots(session, aiShots), "Error: a successful callback should return true.");
        assertEquals(1, aiMoves().size(), "Error: the AI move should be recorded in the game.");
    }

    @Test
    @DisplayName("Jogadas seguidas ficam registadas com números sucessivos")
    void severalMovesAreRecorded() {
        respond(200, "{\"results\":[" + result("A", 1, "MISS") + "," + result("B", 2, "MISS") + ","
                + result("C", 3, "MISS") + "],\"shipsRemaining\":11,\"gameStatus\":\"ONGOING\",\"winner\":null}");
        client.sendShots(session, aiShots);
        client.sendShots(session, aiShots);
        assertEquals(2, aiMoves().size(), "Error: two AI moves should be recorded.");
    }

    @Test
    @DisplayName("Menos resultados do que tiros: o ciclo para no fim dos resultados")
    void fewerResultsThanShots() {
        respond(200, "{\"results\":[" + result("A", 1, "MISS")
                + "],\"shipsRemaining\":11,\"gameStatus\":\"ONGOING\",\"winner\":null}");
        assertTrue(client.sendShots(session, aiShots), "Error: a successful callback should return true.");
        assertEquals(1, aiMoves().size(), "Error: the AI move should still be recorded.");
    }

    @Test
    @DisplayName("Mais resultados do que tiros: o ciclo para no fim dos tiros")
    void moreResultsThanShots() {
        respond(200, "{\"results\":[" + result("A", 1, "MISS") + "," + result("B", 2, "MISS")
                + "],\"shipsRemaining\":11,\"gameStatus\":\"ONGOING\",\"winner\":null}");
        List<IPosition> oneShot = List.of(new Position(0, 0));
        assertTrue(client.sendShots(session, oneShot), "Error: a successful callback should return true.");
        assertEquals(1, aiMoves().size(), "Error: the AI move should be recorded.");
        assertEquals(1, aiMoves().get(0).getShots().size(), "Error: the recorded move should have 1 shot.");
    }

    @Test
    @DisplayName("Resposta sem lista de resultados (null): devolve true mas não regista jogada")
    void nullResults() {
        respond(200, "{\"results\":null,\"shipsRemaining\":11,\"gameStatus\":\"ONGOING\",\"winner\":null}");
        assertTrue(client.sendShots(session, aiShots), "Error: the callback itself succeeded, so it should return true.");
        assertEquals(0, aiMoves().size(), "Error: no move should be recorded without results.");
    }

    @Test
    @DisplayName("shipsRemaining = 0: a IA ganha")
    void aiWinsByShipsRemaining() {
        respond(200, "{\"results\":[],\"shipsRemaining\":0,\"gameStatus\":\"ONGOING\",\"winner\":null}");
        assertTrue(client.sendShots(session, aiShots), "Error: a successful callback should return true.");
        assertTrue(session.isGameOver(), "Error: the game should be over.");
        assertEquals("AI_WINS", session.getWinner(), "Error: the AI should be the winner.");
    }

    @Test
    @DisplayName("gameStatus = GAME_OVER: a IA ganha mesmo com navios restantes")
    void aiWinsByGameStatus() {
        respond(200, "{\"results\":[],\"shipsRemaining\":3,\"gameStatus\":\"GAME_OVER\",\"winner\":\"AI_WINS\"}");
        assertTrue(client.sendShots(session, aiShots), "Error: a successful callback should return true.");
        assertTrue(session.isGameOver(), "Error: the game should be over.");
        assertEquals("AI_WINS", session.getWinner(), "Error: the AI should be the winner.");
    }

    @Test
    @DisplayName("Servidor do estudante responde com erro HTTP 500: devolve false")
    void httpErrorStatus() {
        respond(500, "erro interno");
        assertFalse(client.sendShots(session, aiShots), "Error: an HTTP 500 should return false.");
        assertEquals(0, aiMoves().size(), "Error: no move should be recorded after an HTTP error.");
        assertFalse(session.isGameOver(), "Error: the game should not be over.");
    }

    @Test
    @DisplayName("Servidor do estudante responde 404: devolve false")
    void httpNotFoundStatus() {
        respond(404, "not found");
        assertFalse(client.sendShots(session, aiShots), "Error: an HTTP 404 should return false.");
    }

    @Test
    @DisplayName("Resposta 200 com JSON inválido: devolve false")
    void invalidJson() {
        respond(200, "isto não é JSON");
        assertFalse(client.sendShots(session, aiShots), "Error: an unparseable response should return false.");
        assertEquals(0, aiMoves().size(), "Error: no move should be recorded after a parse error.");
    }

    @Test
    @DisplayName("Servidor do estudante desligado (ligação recusada): devolve false")
    void serverDown() {
        server.stop(0); // a porta deixa de ter alguém à escuta
        assertFalse(client.sendShots(session, aiShots), "Error: a refused connection should return false.");
    }

    @Test
    @DisplayName("Lista de tiros vazia: envia o pedido na mesma")
    void emptyShotList() {
        respond(200, "{\"results\":[],\"shipsRemaining\":11,\"gameStatus\":\"ONGOING\",\"winner\":null}");
        assertTrue(client.sendShots(session, List.of()), "Error: an empty shot list should still be sent.");
        assertTrue(receivedBody.contains("\"shots\":[]"), "Error: the request should contain an empty shot list.");
    }
}