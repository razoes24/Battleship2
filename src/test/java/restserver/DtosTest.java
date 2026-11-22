package restserver;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for class Dtos.
 * Author: Bruno Frasco - 129856
 * Date: 2026-11-21 21:15
 *
 * Cyclomatic Complexity:
 * Dtos(): 1
 * RegistrationRequest(): 1
 * RegistrationResponse(): 1
 * ShotRequest(): 1
 * ShotPosition(): 1
 * ShotResponse(): 1
 * ShotResult(): 1
 * ErrorResponse(String): 1
 */
class DtosTest {

    private Dtos dtos;

    @BeforeEach
    void setUp() {
        dtos = new Dtos();
    }

    @AfterEach
    void tearDown() {
        dtos = null;
    }

    @Test
    @DisplayName("Dtos(): Deve instanciar a classe contentora Dtos corretamente")
    void Dtos() {
        assertNotNull(dtos, "Erro: esperava-se uma instância não nula de Dtos, mas obteve-se null");
    }

    @Test
    @DisplayName("RegistrationRequest(): Deve instanciar e permitir atribuir playerName e callbackUrl")
    void RegistrationRequest() {
        Dtos.RegistrationRequest req = new Dtos.RegistrationRequest();
        req.playerName = "Alice";
        req.callbackUrl = "http://localhost:9090";

        assertAll("Validação de RegistrationRequest",
                () -> assertEquals("Alice", req.playerName,
                        "Erro: esperava-se playerName 'Alice', mas obteve-se " + req.playerName),
                () -> assertEquals("http://localhost:9090", req.callbackUrl,
                        "Erro: esperava-se callbackUrl 'http://localhost:9090', mas obteve-se " + req.callbackUrl)
        );
    }

    @Test
    @DisplayName("RegistrationResponse(): Deve instanciar e permitir atribuir todos os campos da resposta")
    void RegistrationResponse() {
        Dtos.RegistrationResponse res = new Dtos.RegistrationResponse();
        res.gameId = "550e8400-e29b-41d4-a716-446655440000";
        res.status = "REGISTERED";
        res.shotsPerTurn = 3;
        res.message = "Game ready. You shoot first.";

        assertAll("Validação de RegistrationResponse",
                () -> assertEquals("550e8400-e29b-41d4-a716-446655440000", res.gameId,
                        "Erro: esperava-se gameId correspondente, mas obteve-se " + res.gameId),
                () -> assertEquals("REGISTERED", res.status,
                        "Erro: esperava-se status 'REGISTERED', mas obteve-se " + res.status),
                () -> assertEquals(3, res.shotsPerTurn,
                        "Erro: esperava-se shotsPerTurn 3, mas obteve-se " + res.shotsPerTurn),
                () -> assertEquals("Game ready. You shoot first.", res.message,
                        "Erro: esperava-se mensagem esperada, mas obteve-se " + res.message)
        );
    }

    @Test
    @DisplayName("ShotPosition(): Deve instanciar e permitir atribuir row e column")
    void ShotPosition() {
        Dtos.ShotPosition pos = new Dtos.ShotPosition();
        pos.row = "B";
        pos.column = 3;

        assertAll("Validação de ShotPosition",
                () -> assertEquals("B", pos.row,
                        "Erro: esperava-se row 'B', mas obteve-se " + pos.row),
                () -> assertEquals(3, pos.column,
                        "Erro: esperava-se column 3, mas obteve-se " + pos.column)
        );
    }

    @Test
    @DisplayName("ShotRequest(): Deve instanciar e permitir atribuir a lista de ShotPosition")
    void ShotRequest() {
        Dtos.ShotPosition pos = new Dtos.ShotPosition();
        pos.row = "A";
        pos.column = 1;

        Dtos.ShotRequest req = new Dtos.ShotRequest();
        req.shots = List.of(pos);

        assertAll("Validação de ShotRequest",
                () -> assertNotNull(req.shots,
                        "Erro: esperava-se lista de shots não nula, mas obteve-se null"),
                () -> assertEquals(1, req.shots.size(),
                        "Erro: esperava-se 1 shot na lista, mas obteve-se " + req.shots.size())
        );
    }

    @Test
    @DisplayName("ShotResult(): Deve instanciar e permitir atribuir row, column, outcome e shipType")
    void ShotResult() {
        Dtos.ShotResult result = new Dtos.ShotResult();
        result.row = "G";
        result.column = 7;
        result.outcome = "HIT";
        result.shipType = "caravela";

        assertAll("Validação de ShotResult",
                () -> assertEquals("G", result.row,
                        "Erro: esperava-se row 'G', mas obteve-se " + result.row),
                () -> assertEquals(7, result.column,
                        "Erro: esperava-se column 7, mas obteve-se " + result.column),
                () -> assertEquals("HIT", result.outcome,
                        "Erro: esperava-se outcome 'HIT', mas obteve-se " + result.outcome),
                () -> assertEquals("caravela", result.shipType,
                        "Erro: esperava-se shipType 'caravela', mas obteve-se " + result.shipType)
        );
    }

    @Test
    @DisplayName("ShotResponse(): Deve instanciar e permitir atribuir results, shipsRemaining, gameStatus e winner")
    void ShotResponse() {
        Dtos.ShotResult result = new Dtos.ShotResult();
        result.row = "D";
        result.column = 10;
        result.outcome = "SUNK";
        result.shipType = "galeao";

        Dtos.ShotResponse res = new Dtos.ShotResponse();
        res.results = List.of(result);
        res.shipsRemaining = 8;
        res.gameStatus = "ONGOING";
        res.winner = null;

        assertAll("Validação de ShotResponse",
                () -> assertEquals(1, res.results.size(),
                        "Erro: esperava-se 1 resultado na lista, mas obteve-se " + res.results.size()),
                () -> assertEquals(8, res.shipsRemaining,
                        "Erro: esperava-se shipsRemaining 8, mas obteve-se " + res.shipsRemaining),
                () -> assertEquals("ONGOING", res.gameStatus,
                        "Erro: esperava-se gameStatus 'ONGOING', mas obteve-se " + res.gameStatus),
                () -> assertNull(res.winner,
                        "Erro: esperava-se winner null enquanto ONGOING, mas obteve-se " + res.winner)
        );
    }

    @Test
    @DisplayName("ErrorResponse(): Deve instanciar através do construtor parametrizado e guardar a mensagem de erro")
    void ErrorResponse() {
        Dtos.ErrorResponse err = new Dtos.ErrorResponse("Game not found: abc-123");

        assertEquals("Game not found: abc-123", err.error,
                "Erro: esperava-se 'Game not found: abc-123', mas obteve-se " + err.error);
    }
}