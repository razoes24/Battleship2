package restserver;

import battleship.IFleet;
import battleship.IGame;
import battleship.IPosition;
import battleship.IShip;
import battleship.Position;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import restserver.Dtos.*;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes unitários da classe GameController.
 *
 * O GameController é testado diretamente (sem arrancar o servidor web): os métodos
 * register e receiveShots são chamados como métodos normais de Java.
 * O CallbackClient é substituído por uma versão falsa (FakeCallbackClient) que não faz
 * chamadas HTTP e devolve o resultado que o teste escolher.
 *
 * Complexidade ciclomática:
 * - register: 5 (playerName null/vazio, callbackUrl null/vazio)
 * - receiveShots: 13 (sessão inexistente, jogo terminado, nº de tiros, posição inválida,
 *   estudante ganha, callback falha)
 * - evaluateShots: 7 (INVALID, REPEATED, MISS, SUNK, HIT)
 * - error: 1
 *
 * @author 129881
 */
@DisplayName("Testes da classe GameController")
class GameControllerTest {

    /** CallbackClient falso: não faz pedidos HTTP e regista se foi chamado. */
    static class FakeCallbackClient extends CallbackClient {
        boolean result = true;
        int calls = 0;
        List<IPosition> lastShots;

        @Override
        public boolean sendShots(GameSession session, List<IPosition> aiShots) {
            calls++;
            lastShots = aiShots;
            return result;
        }
    }

    private GameRegistry registry;
    private FakeCallbackClient callback;
    private GameController controller;

    @BeforeEach
    void setUp() {
        registry = new GameRegistry();
        callback = new FakeCallbackClient();
        controller = new GameController(registry, callback, new AiShotStrategy());
    }

    // ---------------------------------------------------------------------
    // Métodos auxiliares
    // ---------------------------------------------------------------------

    private RegistrationRequest registration(String name, String url) {
        RegistrationRequest r = new RegistrationRequest();
        r.playerName = name;
        r.callbackUrl = url;
        return r;
    }

    /** Regista um jogador e devolve o gameId. */
    private String registerPlayer() {
        ResponseEntity<?> resp = controller.register(registration("Jogador1", "http://localhost:9090"));
        return ((RegistrationResponse) resp.getBody()).gameId;
    }

    private ShotPosition shot(String row, int column) {
        ShotPosition sp = new ShotPosition();
        sp.row = row;
        sp.column = column;
        return sp;
    }

    private ShotPosition shot(IPosition pos) {
        return shot(String.valueOf(pos.getClassicRow()), pos.getClassicColumn());
    }

    private ShotRequest shots(ShotPosition... list) {
        ShotRequest req = new ShotRequest();
        req.shots = new ArrayList<>(List.of(list));
        return req;
    }

    private String errorMessage(ResponseEntity<?> resp) {
        return ((ErrorResponse) resp.getBody()).error;
    }

    /** Devolve uma posição do tabuleiro onde a frota da IA não tem nenhum navio. */
    private IPosition emptyPosition(IFleet fleet) {
        for (int r = 0; r < 10; r++)
            for (int c = 0; c < 10; c++) {
                IPosition p = new Position(r, c);
                if (fleet.shipAt(p) == null)
                    return p;
            }
        throw new IllegalStateException("No empty position found");
    }

    private IFleet aiFleet(String gameId) {
        return registry.getSession(gameId).getGame().getMyFleet();
    }

    // ---------------------------------------------------------------------
    // register (m0)
    // ---------------------------------------------------------------------

    @Test
    @DisplayName("register com dados válidos devolve 200 e cria a sessão")
    void registerValid() {
        ResponseEntity<?> resp = controller.register(registration("Jogador1", "http://localhost:9090"));
        assertEquals(200, resp.getStatusCode().value(), "Error: valid registration should return 200 OK.");

        RegistrationResponse body = (RegistrationResponse) resp.getBody();
        assertNotNull(body.gameId, "Error: a gameId should be returned.");
        assertEquals("REGISTERED", body.status, "Error: status should be REGISTERED.");
        assertEquals(3, body.shotsPerTurn, "Error: shotsPerTurn should be 3.");
        assertTrue(body.message.contains("Jogador1"), "Error: the welcome message should contain the player name.");
        assertNotNull(registry.getSession(body.gameId), "Error: the session should be stored in the registry.");
    }

    @Test
    @DisplayName("register sem playerName (null) devolve 400")
    void registerNullName() {
        ResponseEntity<?> resp = controller.register(registration(null, "http://localhost:9090"));
        assertEquals(400, resp.getStatusCode().value(), "Error: null playerName should return 400.");
        assertEquals("playerName is required", errorMessage(resp), "Error: wrong error message.");
    }

    @Test
    @DisplayName("register com playerName em branco devolve 400")
    void registerBlankName() {
        ResponseEntity<?> resp = controller.register(registration("   ", "http://localhost:9090"));
        assertEquals(400, resp.getStatusCode().value(), "Error: blank playerName should return 400.");
        assertEquals("playerName is required", errorMessage(resp), "Error: wrong error message.");
    }

    @Test
    @DisplayName("register sem callbackUrl (null) devolve 400")
    void registerNullCallback() {
        ResponseEntity<?> resp = controller.register(registration("Jogador1", null));
        assertEquals(400, resp.getStatusCode().value(), "Error: null callbackUrl should return 400.");
        assertEquals("callbackUrl is required", errorMessage(resp), "Error: wrong error message.");
    }

    @Test
    @DisplayName("register com callbackUrl em branco devolve 400")
    void registerBlankCallback() {
        ResponseEntity<?> resp = controller.register(registration("Jogador1", ""));
        assertEquals(400, resp.getStatusCode().value(), "Error: blank callbackUrl should return 400.");
        assertEquals("callbackUrl is required", errorMessage(resp), "Error: wrong error message.");
    }

    // ---------------------------------------------------------------------
    // receiveShots (m1a / m1b) — validações
    // ---------------------------------------------------------------------

    @Test
    @DisplayName("receiveShots com gameId inexistente devolve 404")
    void receiveShotsUnknownGame() {
        ResponseEntity<?> resp = controller.receiveShots("nao-existe", shots(shot("A", 1), shot("A", 2), shot("A", 3)));
        assertEquals(404, resp.getStatusCode().value(), "Error: unknown game should return 404.");
        assertTrue(errorMessage(resp).contains("Game not found"), "Error: wrong error message.");
    }

    @Test
    @DisplayName("receiveShots num jogo já terminado devolve 410")
    void receiveShotsGameOver() {
        String gameId = registerPlayer();
        registry.getSession(gameId).markAiWins();
        ResponseEntity<?> resp = controller.receiveShots(gameId, shots(shot("A", 1), shot("A", 2), shot("A", 3)));
        assertEquals(410, resp.getStatusCode().value(), "Error: a finished game should return 410 GONE.");
        assertTrue(errorMessage(resp).contains("AI_WINS"), "Error: the message should contain the winner.");
    }

    @Test
    @DisplayName("receiveShots sem lista de tiros (null) devolve 400")
    void receiveShotsNullList() {
        String gameId = registerPlayer();
        ShotRequest req = new ShotRequest();
        req.shots = null;
        ResponseEntity<?> resp = controller.receiveShots(gameId, req);
        assertEquals(400, resp.getStatusCode().value(), "Error: null shots should return 400.");
    }

    @Test
    @DisplayName("receiveShots com número de tiros diferente de 3 devolve 400")
    void receiveShotsWrongNumber() {
        String gameId = registerPlayer();
        ResponseEntity<?> resp = controller.receiveShots(gameId, shots(shot("A", 1), shot("A", 2)));
        assertEquals(400, resp.getStatusCode().value(), "Error: 2 shots should return 400.");
        assertTrue(errorMessage(resp).contains("Exactly 3 shots"), "Error: wrong error message.");
    }

    @Test
    @DisplayName("receiveShots com linha null, linha em branco ou coluna fora de 1..10 devolve 400")
    void receiveShotsInvalidPositions() {
        String gameId = registerPlayer();
        assertEquals(400, controller.receiveShots(gameId, shots(shot(null, 1), shot("A", 2), shot("A", 3)))
                .getStatusCode().value(), "Error: null row should return 400.");
        assertEquals(400, controller.receiveShots(gameId, shots(shot(" ", 1), shot("A", 2), shot("A", 3)))
                .getStatusCode().value(), "Error: blank row should return 400.");
        assertEquals(400, controller.receiveShots(gameId, shots(shot("A", 0), shot("A", 2), shot("A", 3)))
                .getStatusCode().value(), "Error: column 0 should return 400.");
        ResponseEntity<?> resp = controller.receiveShots(gameId, shots(shot("A", 11), shot("A", 2), shot("A", 3)));
        assertEquals(400, resp.getStatusCode().value(), "Error: column 11 should return 400.");
        assertTrue(errorMessage(resp).contains("Invalid position"), "Error: wrong error message.");
        assertEquals(0, callback.calls, "Error: the AI should not fire back when the request is invalid.");
    }

    // ---------------------------------------------------------------------
    // receiveShots — avaliação dos tiros (evaluateShots)
    // ---------------------------------------------------------------------

    @Test
    @DisplayName("Tiros na água dão MISS, jogo continua e a IA responde pelo callback")
    void receiveShotsMiss() {
        String gameId = registerPlayer();
        IPosition empty = emptyPosition(aiFleet(gameId));
        // o mesmo tiro 3 vezes: o 1.º é MISS e os outros são REPEATED
        ResponseEntity<?> resp = controller.receiveShots(gameId, shots(shot(empty), shot(empty), shot(empty)));

        assertEquals(200, resp.getStatusCode().value(), "Error: a valid turn should return 200.");
        ShotResponse body = (ShotResponse) resp.getBody();
        assertEquals(3, body.results.size(), "Error: there should be 3 results.");
        assertEquals("MISS", body.results.get(0).outcome, "Error: a shot in the water should be MISS.");
        assertNull(body.results.get(0).shipType, "Error: a MISS should not have a ship type.");
        assertEquals("REPEATED", body.results.get(1).outcome, "Error: a repeated shot in the same turn should be REPEATED.");
        assertEquals("REPEATED", body.results.get(2).outcome, "Error: a repeated shot in the same turn should be REPEATED.");
        assertEquals("ONGOING", body.gameStatus, "Error: the game should still be ongoing.");
        assertNull(body.winner, "Error: there should be no winner yet.");
        assertEquals(11, body.shipsRemaining, "Error: all 11 AI ships should still be floating.");
        assertEquals(1, callback.calls, "Error: the AI should fire back through the callback.");
        assertEquals(3, callback.lastShots.size(), "Error: the AI should fire 3 shots.");
    }

    @Test
    @DisplayName("Linha em minúsculas é aceite e convertida (ex.: 'a')")
    void receiveShotsLowercaseRow() {
        String gameId = registerPlayer();
        ResponseEntity<?> resp = controller.receiveShots(gameId, shots(shot("a", 1), shot("b", 2), shot("c", 3)));
        assertEquals(200, resp.getStatusCode().value(), "Error: lowercase rows should be accepted.");
        ShotResponse body = (ShotResponse) resp.getBody();
        assertEquals("A", body.results.get(0).row, "Error: the row should be returned in uppercase.");
        assertEquals(1, body.results.get(0).column, "Error: the column should be 1.");
    }

    @Test
    @DisplayName("Linha fora do tabuleiro (ex.: 'Z') dá INVALID")
    void receiveShotsInvalidOutcome() {
        String gameId = registerPlayer();
        IPosition empty = emptyPosition(aiFleet(gameId));
        ResponseEntity<?> resp = controller.receiveShots(gameId, shots(shot("Z", 5), shot(empty), shot("Z", 6)));
        ShotResponse body = (ShotResponse) resp.getBody();
        assertEquals("INVALID", body.results.get(0).outcome, "Error: row Z is outside the board, should be INVALID.");
        assertEquals("MISS", body.results.get(1).outcome, "Error: a shot in the water should be MISS.");
        assertEquals("INVALID", body.results.get(2).outcome, "Error: row Z is outside the board, should be INVALID.");
    }

    @Test
    @DisplayName("Tiro num navio grande dá HIT e numa barca dá SUNK, com o tipo de navio")
    void receiveShotsHitAndSunk() {
        String gameId = registerPlayer();
        IFleet fleet = aiFleet(gameId);
        IShip galleon = fleet.getShipsLike("Galeao").get(0);
        IShip barge = fleet.getShipsLike("Barca").get(0);
        IPosition empty = emptyPosition(fleet);

        ResponseEntity<?> resp = controller.receiveShots(gameId,
                shots(shot(galleon.getPositions().get(0)), shot(barge.getPositions().get(0)), shot(empty)));
        ShotResponse body = (ShotResponse) resp.getBody();

        assertEquals("HIT", body.results.get(0).outcome, "Error: one shot in a galleon should be HIT.");
        assertEquals("Galeao", body.results.get(0).shipType, "Error: the ship type should be Galeao.");
        assertEquals("SUNK", body.results.get(1).outcome, "Error: one shot in a barge should be SUNK.");
        assertEquals("Barca", body.results.get(1).shipType, "Error: the ship type should be Barca.");
        assertEquals("MISS", body.results.get(2).outcome, "Error: a shot in the water should be MISS.");
        assertEquals(10, body.shipsRemaining, "Error: after sinking a barge, 10 ships should remain.");
    }

    @Test
    @DisplayName("Se o callback falhar, o turno do estudante continua a ser devolvido (200)")
    void receiveShotsCallbackFails() {
        callback.result = false;
        String gameId = registerPlayer();
        ResponseEntity<?> resp = controller.receiveShots(gameId, shots(shot("A", 1), shot("A", 2), shot("A", 3)));
        assertEquals(200, resp.getStatusCode().value(), "Error: a callback failure should not break the student's turn.");
        assertEquals("ONGOING", ((ShotResponse) resp.getBody()).gameStatus, "Error: the game should still be ongoing.");
        assertEquals(1, callback.calls, "Error: the callback should have been attempted.");
    }

    @Test
    @DisplayName("Quando todos os navios da IA afundam, o estudante ganha (GAME_OVER) e a IA não dispara")
    void receiveShotsStudentWins() {
        String gameId = registerPlayer();
        IGame game = registry.getSession(gameId).getGame();
        // afundar todos os navios da IA menos uma barca, que vai ser afundada pelos tiros
        IShip lastBarge = game.getMyFleet().getShipsLike("Barca").get(0);
        for (IShip s : game.getMyFleet().getShips())
            if (s != lastBarge)
                s.sink();
        IPosition empty = emptyPosition(game.getMyFleet());

        ResponseEntity<?> resp = controller.receiveShots(gameId,
                shots(shot(lastBarge.getPositions().get(0)), shot(empty), shot(empty)));
        ShotResponse body = (ShotResponse) resp.getBody();

        assertEquals(200, resp.getStatusCode().value(), "Error: the winning turn should return 200.");
        assertEquals("GAME_OVER", body.gameStatus, "Error: the game should be over.");
        assertEquals("STUDENT_WINS", body.winner, "Error: the student should win.");
        assertEquals(0, body.shipsRemaining, "Error: no AI ships should remain.");
        assertTrue(registry.getSession(gameId).isGameOver(), "Error: the session should be marked as over.");
        assertEquals(0, callback.calls, "Error: the AI should not fire back after losing.");
    }
}